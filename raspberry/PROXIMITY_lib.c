#include "PROXIMITY_lib.h"
#include "GPIO_lib.h"
#include "globalLib.h"
#include "PWM_sw_lib.h"
#include <sys/time.h>

pthread_mutex_t mutexExit = PTHREAD_MUTEX_INITIALIZER;
static int exitThread=0;
pthread_mutex_t mutexBrake = PTHREAD_MUTEX_INITIALIZER;
static int isBreaking = 0;

void setBrake()
{
    pthread_mutex_lock(&mutexBrake);
    isBreaking = 1;
    pthread_mutex_unlock(&mutexBrake);
}

void resetBrake()
{
    pthread_mutex_lock(&mutexBrake);
    isBreaking = 0;
    pthread_mutex_unlock(&mutexBrake);
}

static int getBrakeStatus()
{
    int curBrake;
    pthread_mutex_lock(&mutexBrake);
    curBrake = isBreaking;
    pthread_mutex_unlock(&mutexBrake);
    return curBrake;
}

void *proximityThreadFunc(void *pa){
    unsigned long res;
    struct timeval start,stop;
    int cnt=0, obstacle = 0;
    int oldDC=0;
    int quit = 0;
    
    while(1) {
        if (pthread_mutex_trylock(&mutexExit) == 0) {
            quit = exitThread;
            pthread_mutex_unlock(&mutexExit);
        }
        
        if (quit) {
            break;
        } else if(!quit){// && currentGear == GEAR_D){
            GPIO_low(PROXIMITY_TRIGGER);
            usleep(20);
            GPIO_high(PROXIMITY_TRIGGER);
            usleep(20);
            GPIO_low(PROXIMITY_TRIGGER);
            cnt=0;
            while((GPIO_PIN_LEVEL & GPIO_BIT_SET(PROXIMITY_ECHO))==0);
            gettimeofday(&start,NULL);
            while((GPIO_PIN_LEVEL & GPIO_BIT_SET(PROXIMITY_ECHO))!=0);
            gettimeofday(&stop,NULL);
            res = (stop.tv_usec - start.tv_usec)/58;
            
            if(res > 250){
                continue;
            }
            if(res < 20){
                GPIO_high(STOP_PIN);
                if(!obstacle) {
                    oldDC = getDC();
                    pwm_sw_setDutyCycle(FORWARD_PIN,0);
                    usleep(1000);
                    pwm_sw_setDutyCycle(BACKWARD_PIN,100);
                    usleep(100000);
                    pwm_sw_setDutyCycle(BACKWARD_PIN,0);
                }
                pwm_sw_setDutyCycle(FORWARD_PIN,0);
                obstacle=1;
            }else if (obstacle) {// && !getBrakeStatus()) {
                obstacle=0;
                GPIO_low(STOP_PIN);
                pwm_sw_setDutyCycle(FORWARD_PIN,oldDC);
            }
            usleep(100000);
        }
    }
    pthread_exit(NULL);
    exitThread = 0;
    return NULL;
}

void closeProximity()
{
    pthread_mutex_lock(&mutexExit);
    exitThread=1;
    pthread_mutex_unlock(&mutexExit);
}
