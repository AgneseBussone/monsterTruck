/***************************************************
                CUSTOM INCLUDES
 ****************************************************/
#include "utils.h"

/***************************************************
                GLOBAL VARIABLES
****************************************************/
unsigned int currentID;
int udpSocket;
pthread_t th;
pthread_t proximityThread;
/***************************************************
                FUNCTION IMPLEMENTATION
****************************************************/

//Checks if the datagram is too old

int checkId(uint idReceived){
#ifdef DEBUG
    printf("current= %d received= %d\n",currentID,idReceived);
#endif
    
    if(idReceived < currentID)
        return 0;

    if(currentID == idReceived && currentID != 0)
        return 0;
    
    currentID=idReceived;
    
    return 1;
}
/////////////////////////////////////////////////////

//CTRL-C handling function

void closeHandler(){
    GPIO_low(STOP_PIN);
    exit_pwm();
    pthread_join(th,NULL);
    closeProximity();
    pthread_join(proximityThread,NULL);
    GPIO_low(FORWARD_PIN);
    GPIO_low(BACKWARD_PIN);    
    GPIO_unmap();
    pwm_sw_unmap();
    
    if(udpSocket >= 0){
        close(udpSocket);
    }
    exit(0);
}
/////////////////////////////////////////////////////

//Initialize the UDP socket

int initSocket(){
    struct sockaddr_in si_me;
    if ((udpSocket=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP))==-1){
        return -1;
    }
    
    memset((char *) &si_me, 0, sizeof(si_me));
    si_me.sin_family = AF_INET;
    si_me.sin_port = htons(PORT);
    si_me.sin_addr.s_addr = htonl(INADDR_ANY);
    if (bind(udpSocket, (struct sockaddr*)&si_me, sizeof(si_me))==-1){
        return -1;
    }
    return 0;
}

////////////////////////////////////////////////////
void setupPins(){
    GPIO_setOutput(STOP_PIN);     //Stop LED
    GPIO_high(STOP_PIN);
    GPIO_setOutput(LIGHTS_PIN);     //Lights LED
    GPIO_low(LIGHTS_PIN);
//    GPIO_setInput(HALL_PIN);      //Hall Sensor
    GPIO_setOutput(PROXIMITY_TRIGGER);
    GPIO_low(PROXIMITY_TRIGGER);
    GPIO_setInput(PROXIMITY_ECHO);
    pwm_sw_setOutput(BACKWARD_PIN);
    pwm_sw_setOutput(FORWARD_PIN);
    GPIO_setOutput(LEFT_PIN);
    GPIO_low(LEFT_PIN);
    GPIO_setOutput(RIGHT_PIN);
    GPIO_low(RIGHT_PIN);
}
