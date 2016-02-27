#ifndef __GPIO_LIB_H
#define __GPIO_LIB_H

#include "globalLib.h"
#include "peripheral.h"

#define GPIO_BASE                               BCM2835_BASE + 0x200000
#define GPIO_PIN_LEVEL                          *(gpio.addr + 13)
#define GPIO_PIN_DETECT_EVENT_STATUS            *(gpio.addr + 16)
#define GPIO_RISING_EDGE_ENABLE                 *(gpio.addr + 19)
#define GPIO_FALLING_EDGE_ENABLE                *(gpio.addr + 22)
#define GPIO_HIGH_DETECT_ENABLE                 *(gpio.addr + 25)
#define GPIO_LOW_DETECT ENABLE                  *(gpio.addr + 28)
#define GPIO_RISING_DETECT                      *(gpio.addr + 31)
#define GPIO_FALLING_DETECT                     *(gpio.addr + 34)

#define GPIO_BIT_SET(n)                         1 << n


extern peripheral gpio;

int GPIO_map();
void GPIO_unmap();

void GPIO_setInput(int pin);
void GPIO_setOutput(int pin);

void GPIO_high(int pin);
void GPIO_low(int pin);

#endif
