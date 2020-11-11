void sendHRwithDelay() {
  TickType_t xLastWakeTime;
  xLastWakeTime = xTaskGetTickCount();

  for (int i = 0; i < 1000; i++) {

    uint16_t sensorValue = analogRead(sensorPin);   //sensor value has 12 bits
    arrForHR[i] = sensorValue;
    if (i == 999) {
      memcpy(buffArrForHR, arrForHR, 2000);       // because arrForHR is uint16_t
      ecgBufferReady = true;
      Serial.println("ecg Buffer is ready");
    }
    if (mode != lowPower) {
      break;
    }
    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS(10) );   // sample at 10 ms
  }

  for (long  i = 0; i < TIME_TO_SLEEP; i++ ) {
    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS(1000) );   // check every second if mode changed while sleeping
    if (mode != lowPower) {
      break;
    }
  }
  return;
}
