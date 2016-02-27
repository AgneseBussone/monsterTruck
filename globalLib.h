#ifndef __GLOBALLIB_H
#define __GLOBALLIB_H

/**********************************************************
 STANDARD INCLUDES
 **********************************************************/

#include <stdio.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <memory.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <signal.h>

/**********************************************************
 DEFINES
 **********************************************************/
#define PORT 9930
#define BUFLEN 10
#define STEP 10
#define MIN_SPEED 60 

/**********************************************************
 COMMAND DEFINITIONS
 **********************************************************/
#define ACCELERATOR 0
#define PRESS_ACCELERATOR 'P'
#define RELEASE_ACCELERATOR 'R'

#define BRAKE 1
#define PRESS_BRAKE 'P'
#define RELEASE_BRAKE 'R'

#define LIGHTS 2
#define LIGHTS_ON 'A'
#define LIGHTS_OFF 'S'

#define GEAR 3
#define GEAR_P 'P'
#define GEAR_R 'R'
#define GEAR_D 'D'

#define STEERING 4
#define STEERING_LEFT 'L'
#define STEERING_RIGHT 'R'
#define NO_STEERING 'S'

#define TURN_OFF 5

/**********************************************************
 PIN DEFINITIONS
 **********************************************************/
#define FORWARD_PIN 27
#define BACKWARD_PIN 22
#define LIGHTS_PIN 18
#define STOP_PIN 26
#define RIGHT_PIN 19
#define LEFT_PIN 13
#define PROXIMITY_TRIGGER 17
#define PROXIMITY_ECHO 24 
/**********************************************************
 EXTERN VARIABLES
 **********************************************************/

extern int udpSocket;
extern unsigned int currentID;
extern pthread_t proximityThread;
#endif
