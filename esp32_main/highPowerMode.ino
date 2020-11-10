void readSensorForHighPowerMode() {

  TickType_t xLastWakeTime;
  xLastWakeTime = xTaskGetTickCount();

  int i = 0;     // 13 12-bit sensor values in 19.5 8-bit ints
  bool incrementBy2 = false;
  int seqNr = 0;

  int j = 0;
  uint16_t sensorValue = 0;

  //    long startTime = 0;
  while (true) {
    //      Serial.print(millis() - startTime);
    //      Serial.println(" ms");
    //      startTime = millis();
    sensorValue = analogRead(sensorPin);   //sensor value has 12 bits
    //    Serial.println(sensorValue);
    //    Serial.println(sensorValue, HEX);
    //    Serial.print("-");
    //    Serial.print("LO-");
    //    Serial.println(digitalRead(LO_MinPin));
    //    Serial.print("LO+");
    //    Serial.println(digitalRead(LO_PlusPin));
    if (incrementBy2 == false) {
      arrForTX[i] = (uint8_t) (sensorValue >> 4);   //put the first 8 bits in the array element. Typecast keeps the last 8 bits
      arrForTX[i + 1] = (uint8_t) ((sensorValue & 0x000F) << 4); //put the remaining 4 bits in the first half of next element
      i++;
      incrementBy2 = true;
    } else {
      arrForTX[i] = (uint8_t) (sensorValue >> 8) | arrForTX[i]; // put the first 4 bits in the second half of the element
      arrForTX[i + 1] = (uint8_t) sensorValue;  //put the last 8 bits in the next element
      i = i + 2;
      incrementBy2 = false;
    }

    if (i == 19) {  //13th sensor value occupies 19th and first half of 20th elements
      arrForTX[i] = (uint8_t)(arrForTX[i] | (uint8_t) seqNr);
      memcpy(buffArrForTX, arrForTX, 20);
      bufferReady = true;
      Serial.println(" Buffer is ready");
      if (seqNr == 15) {              // so it can only occupy the last remaining 4 bits
        seqNr = 0;
      } else {
        seqNr++;
      }

//      Serial.println();
//      for (int k = 0; k < 20; k++) {
//        Serial.print(arrForTX[k], HEX);
//        Serial.print("-");
//      }
//      Serial.println();

      i = 0;
      incrementBy2 = false;

      for (int k = 0; k < 20; k++) {    // clear the array
        arrForTX[k] = 0;
      }
    }
    arrForHR[j] = sensorValue;
    j++;
    if (j == 1000) {
      j = 0;
      memcpy(buffArrForHR, arrForHR, 2000);       // because arrForHR is uint16_t
      ecgBufferReady = true;
      Serial.println("ecg Buffer is ready");
//      Serial.println();
//      for (int k = 0; k < 1000; k++) {
//        Serial.print(arrForHR[k], HEX);
//        Serial.print("-");
//      }
//      Serial.println();
    }

    if (mode != highPower) {
      break;
    }
    /* 100 ms is converted into ticks and the period starts LastWakeTime.
       LastWakeTIme is updated automatically after each delayUntil call*/
    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS(10) );
  }

}
