#ifndef __UTILS_H
#define __UTILS_H

/***************************************************
 INCLUDES
 ***************************************************/
#include "globalLib.h"
#include "GPIO_lib.h"
#include "PWM_sw_lib.h"
#include "PROXIMITY_lib.h"
/***************************************************
 PROTOTYPES
 ***************************************************/
int checkId(uint idReceived);
void closeHandler();
int initSocket();
void setupPins();

#endif
