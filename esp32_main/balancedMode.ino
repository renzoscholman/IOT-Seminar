
void readSensorForBalancedMode() {      // read the sensor for 10 secs at 100Hz and fill up the buffer.. HR is calculated by transmitting core and transmitted

  TickType_t xLastWakeTime;
  xLastWakeTime = xTaskGetTickCount();

  int i = 0; 
  while (true) {
    //      Serial.print(millis() - startTime);
    //      Serial.println(" ms");
    //      startTime = millis();
    uint16_t sensorValue = analogRead(sensorPin);   //sensor value has 12 bits
    //    Serial.println(sensorValue, HEX);
    //    Serial.println(sensorValue);
    //      Serial.print("LO-");
    //      Serial.println(digitalRead(LO_MinPin));
    //      Serial.print("LO+");
    //      Serial.println(digitalRead(LO_PlusPin));
    //      Serial.print("-");
    arrForHR[i] = sensorValue;

    i++;

    if (i == 1000) {
      memcpy(buffArrForHR, arrForHR, 2000);       // because arrForHR is uint16_t
      ecgBufferReady = true;
      Serial.println("ecg Buffer is ready");
      for (int j = 0; j < 1000-WINDOW; j++) {   // slide the window forward
        arrForHR[j] = arrForHR[j+WINDOW]  ;
      } 
      i = 1000 - WINDOW;
    }
    /* 100 ms is converted into ticks and the period starts LastWakeTime.
       LastWakeTIme is updated automatically after each delayUntil call*/
    if (mode != balanced) {
      break;
    }
    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS(10) );
  }
}
