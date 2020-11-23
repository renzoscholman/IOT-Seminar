
void TaskReadSensor(void *pvParameters) {
  (void) pvParameters;

  /* The xLastWakeTime variable needs to be initialized with the current tick
    count.  Note that this is the only time we access this variable.  From this
    point on xLastWakeTime is managed automatically by the vTaskDelayUntil()
    API function. */

  for ( ;; ) {

    if (mode == highPower) {
      readSensorForHighPowerMode();

    } else if ( mode == balanced) {
      readSensorForBalancedMode();

    } else {
      sendHRwithDelay();
    }
  }



}
