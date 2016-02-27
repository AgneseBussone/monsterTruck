/**********************************************************
 CUSTOM INCLUDES
 **********************************************************/

#include "globalLib.h"
#include "utils.h"

/********************************************************
 DEFINES
 ********************************************************/

#define DEBUG  				//enables printf's for debugging purposes

/*******************************************************
 GLOBAL VARIABLES
 *******************************************************/

int udpSocket;
unsigned int currentID;
char currentGear;
char currentSteering;
volatile pthread_t th;
pthread_t proximityThread;
volatile int isBreaking;

/*******************************************************
 MAIN FUNCTION
 *******************************************************/

int main(void)
{
    struct sockaddr_in si_other;
    int slen=sizeof(si_other);
    char buf[BUFLEN], extraReceived[BUFLEN];
    unsigned int idReceived = 0;
    short int cmdReceived = 0;
    
    currentID = 0;
    udpSocket = -1;
    currentGear = GEAR_P;
    currentSteering = NO_STEERING;
    
    //listen on CTRL-C signal and close the socket if opened
    signal(SIGINT, closeHandler);
    
    /******************************************************
     INIT FUNCTIONS
     ******************************************************/
    if(initSocket() == -1) {
#ifdef DEBUG
        printf("Error while initializing the socket\n");
#endif
        exit(-1);
    }
    
    if (GPIO_map() == -1) {
#ifdef DEBUG
        printf("Error while initializing the GPIO ports\n");
#endif
        exit(-1);
    }
    
    if (pwm_sw_map() == -1) {
#ifdef DEBUG
        printf("Error while initializing the PWM/GPIO ports\n");
#endif
        exit(-1);
    }
    
    setupPins();
    
    /******************************************************
     MAIN LOOP
     ******************************************************/
    while(1){
       	if (recvfrom(udpSocket, buf, BUFLEN, 0, (struct sockaddr *)&si_other, (socklen_t *)&slen) == -1) {
#ifdef DEBUG
       	    printf("error while receiving data\n");
#endif
        }
        /***************************************************************
         //MESSAGE STRUCTURE:
         //ID: unique identifier for messages (unsigned int)
         //COMMAND: 0       accelerator
                    1       brake
                    2       lights
                    3       PRND
                    4       steering
                    5       turn OFF car
         //EXTRA: informations about the command
                    0 P     accelerator pressed
                      R     accelerator released
         
                    1 P     brake pressed
                      R     brake released
         
                    2 A     lights on
                      S     lights off
         
                    3 P     park mode
                      R     retro mode
                      D     drive mode
         
                    4 L	    left steering
                      R     right steering
                      S     no steering
         
                    5       turn off
         
         each field is separated by a " " and the message is read by an sscanf
         //see globalLib.h to read the keywords for each function
         ****************************************************************/
#ifdef DEBUG
        printf("Received packet from %s:%d\nData: %s\n\n", inet_ntoa(si_other.sin_addr), ntohs(si_other.sin_port), buf);
#endif
        
        sscanf(buf,"%u %hd %s", &idReceived, &cmdReceived, extraReceived);
#ifdef DEBUG
        printf("Current = %u, Received= %u Command = %hd Extra = %s\n", currentID, idReceived, cmdReceived, extraReceived);
#endif
        if(checkId(idReceived)) {
            switch(cmdReceived) {
                case ACCELERATOR: {
#ifdef DEBUG
                    printf("Accelerator: %s\n",extraReceived);
#endif
                    if(extraReceived[0] == PRESS_ACCELERATOR) {
                        if(currentGear == GEAR_D){
                            th=pwm_sw_setDutyCycle(FORWARD_PIN, 100);
                        } else if(currentGear==GEAR_R) {
                            th=pwm_sw_setDutyCycle(BACKWARD_PIN, 100);
                        }
                        usleep(100);
                    } else if (extraReceived[0] == RELEASE_ACCELERATOR) {
                        if(currentGear == GEAR_D) {
                            th = pwm_sw_setDutyCycle(FORWARD_PIN,MIN_SPEED);
                        } else if(currentGear == GEAR_R) {
                            th = pwm_sw_setDutyCycle(BACKWARD_PIN, MIN_SPEED);
                        }
                        usleep(100);
                    }
                    //GPIO_low(STOP_PIN);
                }
                    break;
                    
                case BRAKE: {
#ifdef DEBUG
                    printf("Brake: %s\n",extraReceived);
#endif
                    if(extraReceived[0] == PRESS_BRAKE) {
                        setBrake();
                        GPIO_high(STOP_PIN);
                        
                        if(currentGear == GEAR_D) {
                            th = pwm_sw_setDutyCycle(FORWARD_PIN,0);
                        } else if(currentGear == GEAR_R) {
                            th = pwm_sw_setDutyCycle(BACKWARD_PIN,0);
                        }
                        usleep(100);
                        
                    } else if (extraReceived[0] == RELEASE_BRAKE) {
                        resetBrake();
                        GPIO_low(STOP_PIN);
                        
                        if(currentGear == GEAR_D){
                            th = pwm_sw_setDutyCycle(FORWARD_PIN,MIN_SPEED);
                        } else if(currentGear == GEAR_R) {
                            th = pwm_sw_setDutyCycle(BACKWARD_PIN,MIN_SPEED);
                        }
                        usleep(100);
                    }
                }
                    break;
                    
                case LIGHTS:{
#ifdef DEBUG
                    printf("Lights: %s\n",extraReceived);
#endif
                    if(extraReceived[0] == LIGHTS_ON){
                        //turn the lights ON
                        GPIO_high(LIGHTS_PIN);
                        usleep(100);
                    } else if (extraReceived[0] == LIGHTS_OFF){
                        //turn the lights OFF
                        GPIO_low(LIGHTS_PIN);
                        usleep(100);
                    } else printf("Lights unknown\n");
                    GPIO_low(STOP_PIN);
                }
                    break;
                    
                case GEAR: {
#ifdef DEBUG
                    printf("PRND: %s\n",extraReceived);
#endif
                    if(extraReceived[0] == GEAR_P) {
                        closeProximity();
                        pthread_join(proximityThread,NULL);
                        //the car will stay stopped
                        if(currentGear == GEAR_D){
                            th = pwm_sw_setDutyCycle(FORWARD_PIN, 0);
                        }else if(currentGear == GEAR_R) {
                            th = pwm_sw_setDutyCycle(BACKWARD_PIN, 0);
                        }
                        currentGear = GEAR_P;
                        GPIO_high(STOP_PIN);
                    } else if (extraReceived[0] == GEAR_R) {
                        closeProximity();
                        pthread_join(proximityThread,NULL);
                        //the car is on Retro mode and goes to MIN_SPEED% the speed
                        th = pwm_sw_setDutyCycle(FORWARD_PIN, 0);
                        usleep(100);
                        th = pwm_sw_setDutyCycle(BACKWARD_PIN, MIN_SPEED);
                        currentGear = GEAR_R;
                        GPIO_low(STOP_PIN);
                    } else if (extraReceived[0] == GEAR_D) {
                        //activate the proximity sensor
                        pthread_create(&proximityThread, NULL, &proximityThreadFunc, NULL);
                        //the car is on drive mode and goes to MIN_SPEED% the speed
                        th = pwm_sw_setDutyCycle(BACKWARD_PIN, 0);
                        usleep(100);
                        th = pwm_sw_setDutyCycle(FORWARD_PIN,MIN_SPEED);
                        currentGear = GEAR_D;
                        GPIO_low(STOP_PIN);
                    }
                }
                    break;
                    
                case STEERING: {
#ifdef DEBUG
                    printf("STEERING: %s\n",extraReceived);
#endif
                    if(extraReceived[0] == STEERING_LEFT) {
                        GPIO_low(RIGHT_PIN);
                        usleep(1000);
                        GPIO_high(LEFT_PIN);
                        currentSteering = STEERING_LEFT;
                    } else if(extraReceived[0] == STEERING_RIGHT) {
                        GPIO_low(LEFT_PIN);
                        usleep(1000);
                        GPIO_high(RIGHT_PIN);
                        currentSteering = STEERING_RIGHT;
                    } else if(extraReceived[0] == NO_STEERING) {
                        GPIO_low(LEFT_PIN);
                        usleep(1000);
                        GPIO_low(RIGHT_PIN);
                        currentSteering = NO_STEERING;
                    }
                } 
                    break;
                    
                case TURN_OFF:
#ifdef DEBUG
                    printf("TURN OFF\n");
#endif
                    system("poweroff");
                    break;
                    
                default: {
#ifdef DEBUG
                    printf("Unrecognized Command\n");
#endif
                }
            }
        }
    }
    return 0;
}
