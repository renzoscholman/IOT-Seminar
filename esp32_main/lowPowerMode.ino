void sendHRwithDelay() {
  TickType_t xLastWakeTime;
  xLastWakeTime = xTaskGetTickCount();
  
  readSensorForBalancedMode();

  for (long  i = 0; i < TIME_TO_SLEEP; i++ ) {
    if (mode != lowPower) {
      break;
    }
    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS(1000) );   // check every second if mode changed while sleeping
  }
  return;
}
