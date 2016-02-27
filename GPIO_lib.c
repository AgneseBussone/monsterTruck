#include "GPIO_lib.h"

peripheral gpio;

int GPIO_map(){
    if((gpio.memoryFILE=open("/dev/mem",O_RDWR|O_SYNC)) < 0){
        printf("ERROR: memory file cannot be open\n");
        return -1;
    }

    gpio.map=mmap(
                NULL,                       //let the kernel choose where to place the mapping
                BLOCK_SIZE,                 //the mapping is initialized with 4*1024 size
                PROT_READ|PROT_WRITE,       //protection of the pages(read and write permission on registers)
                MAP_SHARED,                 //map shared with other processes
                gpio.memoryFILE,              //file to map
                GPIO_BASE                //physical address of the map
                );

    if(gpio.map == MAP_FAILED){
        printf("ERROR: memory file cannot be mapped\n");
        return -1;
    }

    gpio.addr=(volatile unsigned int*)gpio.map;

    return 0;
}

void GPIO_unmap()
{
    munmap(gpio.map, BLOCK_SIZE);
    close(gpio.memoryFILE);
}

void GPIO_setInput(int pin)
{
    *(gpio.addr+(pin/10)) &= ~(7<<((pin%10)*3));
}

void GPIO_setOutput(int pin)
{
    GPIO_setInput(pin);
    *(gpio.addr+(pin/10))|=1<<((pin%10)*3);
}

void GPIO_high(int pin)
{
    *(gpio.addr+7)=GPIO_BIT_SET(pin);
}

void GPIO_low(int pin)
{
    *(gpio.addr+10)=GPIO_BIT_SET(pin);
}


