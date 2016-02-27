#ifndef __PERIPHERAL_H
#define __PERIPHERAL_H

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>

#define BCM2835_BASE 0x20000000
#define BLOCK_SIZE 4*1024

typedef struct peripheral {
    int memoryFILE;
    void *map;
    volatile unsigned int *addr;
}peripheral;

#endif
