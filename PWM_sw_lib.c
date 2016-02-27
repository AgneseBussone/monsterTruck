#include "PWM_sw_lib.h"

peripheral pwm_sw;
pthread_t th=0;

/* This mutex is used for the goOut variable */
pthread_mutex_t mutexGoOut = (pthread_mutex_t)PTHREAD_MUTEX_INITIALIZER;;
static int goOut=0;

/*This mutex is used to protect the Duty Cycle variable DC */
pthread_mutex_t mutexDC = (pthread_mutex_t)PTHREAD_MUTEX_INITIALIZER;;
static int DC;

/*This mutex is used to protect the last used pin variable */
pthread_mutex_t mutexPin = (pthread_mutex_t)PTHREAD_MUTEX_INITIALIZER;;
static int pin;

static void setDC(int dutyCycle)
{
    pthread_mutex_lock(&mutexDC);
    DC = dutyCycle;
    pthread_mutex_unlock(&mutexDC);
}

int getDC(){
    int currentDC;
    pthread_mutex_lock(&mutexDC);
    currentDC = DC;
    pthread_mutex_unlock(&mutexDC);
    return currentDC;
}

static void setPin(int usedPin)
{
    pthread_mutex_lock(&mutexPin);
    pin = usedPin;
    pthread_mutex_unlock(&mutexPin);
}

static int getPin(void)
{
    int currentPin;
    pthread_mutex_lock(&mutexPin);
    currentPin = pin;
    pthread_mutex_unlock(&mutexPin);
    return currentPin;
}

int pwm_sw_map(){
    if((pwm_sw.memoryFILE=open("/dev/mem",O_RDWR|O_SYNC)) < 0){
        printf("ERROR: memory file cannot be open\n");
        return -1;
    }
    
    pwm_sw.map=mmap(
                    NULL,                       //let the kernel choose where to place the mapping
                    BLOCK_SIZE,                 //the mapping is initialized with 4*1024 size
                    PROT_READ|PROT_WRITE,       //protection of the pages(read and write permission on registers)
                    MAP_SHARED,                 //map shared with other processes
                    pwm_sw.memoryFILE,              //file to map
                    PWM_SW_BASE               //physical address of the map
                    );
    
    if(pwm_sw.map == MAP_FAILED){
        printf("ERROR: memory file cannot be mapped\n");
        return -1;
    }
    
    pwm_sw.addr=(volatile unsigned int*)pwm_sw.map;
    
    return 0;
    
}
void pwm_sw_unmap(){
    munmap(pwm_sw.map, BLOCK_SIZE);
    close(pwm_sw.memoryFILE);
}

void pwm_sw_setOutput(int pin){
    *(pwm_sw.addr+(pin/10)) &= ~(7<<((pin%10)*3));
    *(pwm_sw.addr+(pin/10))|=1<<((pin%10)*3);
}

void pwm_sw_high(int pin){
    *(pwm_sw.addr+7)=1<<pin;
}

void pwm_sw_low(int pin){
    *(pwm_sw.addr+10)=1<<pin;
}

void *mythread(void *pa){
    int currentDC;
    int currentPin = getPin();
    while(1){
        pthread_mutex_lock(&mutexGoOut);
        if (goOut) {
            pthread_mutex_unlock(&mutexGoOut);
            break;
        }
        pthread_mutex_unlock(&mutexGoOut);
        currentDC = getDC();
        currentPin = getPin();
        if(currentDC < STEP){
            GPIO_low(currentPin);
        } else {
            pwm_sw_high(currentPin);
            usleep(1000*(PERIOD * currentDC /100));
            pwm_sw_low(currentPin);
            usleep(1000*(PERIOD - (PERIOD * currentDC /100)));
        }
    }
    GPIO_low(currentPin);
    return NULL;
}

pthread_t pwm_sw_setDutyCycle(int usedPin,int dutyCycle){
    if(!th){
        if(dutyCycle > 0){
            setPin(usedPin);
            setDC(dutyCycle);
	    goOut = 0;
            pthread_create(&th,NULL,&mythread,NULL);
        }
    }else{
        setPin(usedPin);
        if(dutyCycle > 0)
            setDC(dutyCycle);
        else{
            exit_pwm();
	    pthread_join(th,NULL);
            th = 0;
            setDC(0);
        }
    }
    return th;
}

void exit_pwm(){
    pthread_mutex_lock(&mutexGoOut);
    goOut = 1;
    pthread_mutex_unlock(&mutexGoOut);
}
