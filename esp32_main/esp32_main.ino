#if CONFIG_FREERTOS_UNICORE
#define ARDUINO_RUNNING_CORE 0
#else
#define ARDUINO_RUNNING_CORE 1
#endif

#ifndef LED_BUILTIN
#define LED_BUILTIN 2
#endif 

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define HR_SERVICE_UUID     "e7f60890-f50f-4885-91c8-26edd498c631"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define HR_CHARACTERISTIC_UUID "de0020e7-e47a-46bf-b0bb-173bab5b11a6"
 
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
//BLEServer* pHRServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
BLECharacteristic* pHRCharacteristic = NULL;


const long TIME_TO_SLEEP = 60;   // in sec
const int ECG_DATA_ARR_LEN = 500;
const int WINDOW = 100;
bool deviceConnected = false;
bool oldDeviceConnected = false;
bool bufferReady = false;
bool ecgBufferReady = false;
uint8_t arrForTX[20] = {0};
uint8_t buffArrForTX[20] = {0};
uint16_t arrForHR[ECG_DATA_ARR_LEN] = {0};
uint16_t buffArrForHR[ECG_DATA_ARR_LEN] = {0};
float heartRate = 0;
const int sensorPin = 36;
int LO_MinPin = 25;
int LO_PlusPin = 26;
unsigned long currTime = 0;
int globalIdx = 0;

enum MODE {lowPower, balanced, highPower};
MODE mode = balanced;


// define tasks for FreeRTOS
void TaskTransmit( void *pvParameters );
void TaskReadSensor( void *pvParameters );


void setup() {

  Serial.begin(115200);
 

  // Set up two tasks to run independently.
  xTaskCreatePinnedToCore(
    TaskTransmit
    ,  "TaskTransmit"   // A name just for humans
    ,  102400  // This stack size can be checked & adjusted by reading the Stack Highwater
    ,  NULL // pvParameters - used by task scheduler
    ,  1  // Priority, with 3 (configMAX_PRIORITIES - 1) being the highest, and 0 being the lowest.
    ,  NULL
    ,  1); // core

  xTaskCreatePinnedToCore(
    TaskReadSensor
    ,  "TaskReadSensor"
    ,  1024  // Stack size
    ,  NULL
    ,  2  // Priority
    ,  NULL
    ,  0);  //core

  // Now the task scheduler, which takes over control of scheduling individual tasks, is automatically started.
}

void loop()
{
  // Empty. Things are done in Tasks.
  delay(10);
}

/*--------------------------------------------------*/
/*---------------------- Tasks ---------------------*/
/*--------------------------------------------------*/
