 #ifndef __PWM_SW_LIB_H
#define __PWM_SW_LIB_H

#include "peripheral.h"
#include "GPIO_lib.h"
#include "globalLib.h"

#define PWM_SW_BASE BCM2835_BASE + 0x200000

#define PERIOD 10    // 10 ms of period

extern peripheral pwm_sw;

typedef struct parameters{
	int pin;
	int DC;
}parameters;

int pwm_sw_map();
void pwm_sw_unmap();

void pwm_sw_setOutput(int pin);

pthread_t pwm_sw_setDutyCycle(int usedPin,int dutyCycle);
void exit_pwm();

void pwm_sw_high(int pin);
void pwm_sw_low(int pin);

int getDC();
#endif

