class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};


class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();

      int rxMode = 0;

      if (rxValue.length() > 0) {
        //        Serial.println("*********");
        //        Serial.print("Received Value: ");
        //        for (int i = 0; i < rxValue.length(); i++) {
        //          Serial.print((int)rxValue[i]);
        //        }
        rxMode = (int)rxValue[0] - 48;
        //        Serial.print("Received : ");
        //        Serial.println(rxMode);
        if (rxMode == 0) {
          mode = lowPower;
          Serial.println("Low power mode set.");

        } else if (rxMode == 1) {
          mode = balanced;
          Serial.println("Balanced mode set.");

        } else if (rxMode == 2) {
          mode = highPower;
          Serial.println("High power mode set.");

        }


        //        Serial.println();
        //        Serial.println("*********");
      }
    }

};



void TaskTransmit(void *pvParameters)  // This is a task.
{
  pinMode(LED_BUILTIN, OUTPUT);
  uint8_t transmitData[] = {0};
  long prevTime = 0;

  // Create the BLE Device
  BLEDevice::init("myESP32");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLEService *pHRService = pServer->createService(HR_SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY // |
                      //                      BLECharacteristic::PROPERTY_INDICATE
                    );

  pHRCharacteristic = pHRService->createCharacteristic(
                        HR_CHARACTERISTIC_UUID,
                        BLECharacteristic::PROPERTY_READ   |
                        BLECharacteristic::PROPERTY_WRITE  |
                        BLECharacteristic::PROPERTY_NOTIFY // |
                        //                        BLECharacteristic::PROPERTY_INDICATE
                      );

  // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
  // Create a BLE Descriptor
  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setCallbacks(new MyCallbacks());
  pHRCharacteristic->addDescriptor(new BLE2902());
  pHRCharacteristic->setCallbacks(new MyCallbacks());

  // Start the service
  pService->start();
  pHRService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->addServiceUUID(HR_SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  //  Serial.println("Waiting a client connection to notify...");
  //  while(!deviceConnected){delay(100);}
  //  Serial.println("Connected");



  (void) pvParameters;

  for (;;)
  {
    //    deviceConnected = true;     // only for HR calculation from saved sensorData
    //    ecgBufferReady = true;       // only for HR calculation from saved sensorData
    // notify changed value
    if (deviceConnected) {
      if (mode == highPower) {
        if (bufferReady) {
          prevTime = millis();
          bufferReady = false;
          Serial.println(" Buffer is read");
          pCharacteristic->setValue((uint8_t*)&buffArrForTX, 20); // 20 bytes
          digitalWrite(LED_BUILTIN, HIGH);
//          pCharacteristic->indicate();
          pCharacteristic->notify();
          //      Serial.println("indicated");
          //        delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
          vTaskDelay(pdMS_TO_TICKS(10));
          digitalWrite(LED_BUILTIN, LOW);

          Serial.print("Time taken for ecg data transmission ");
          Serial.println(millis() - prevTime);
        }
      }

      if (ecgBufferReady) {
        prevTime = millis();
        ecgBufferReady = false;
        Serial.println("ecg Buffer is read");

        heartRate = calcHR(buffArrForHR);
        //heartRate = calcHR(sensorData);         // only for HR calculation from saved sensorData
        currTime = millis();
        Serial.print("Timestamp : ") ;
        Serial.print(currTime);
        Serial.print(" ");
        Serial.print(currTime, HEX);
        Serial.print(" Heart Rate : ");
        Serial.print(heartRate);
        Serial.println("bpm");
        Serial.print("Time taken for HR calc ");
        Serial.println(millis() - prevTime);

        transmitData[4] = (uint8_t) heartRate;
        transmitData[3] = (uint8_t) currTime;
        transmitData[2] = (uint8_t) (currTime >> 8);
        transmitData[1] = (uint8_t) (currTime >> 16);
        transmitData[0] = (uint8_t) (currTime >> 24);
        pHRCharacteristic->setValue((uint8_t*)&transmitData, 5);
        digitalWrite(LED_BUILTIN, HIGH);
        //        pHRCharacteristic->indicate();
        pHRCharacteristic->notify();
        //      Serial.println("indicated");
        //        delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
        vTaskDelay(pdMS_TO_TICKS(10));
        digitalWrite(LED_BUILTIN, LOW);
        Serial.print("Time taken for HR calc and transmission ");
        Serial.println(millis() - prevTime);
      }
    }
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
      //        delay(500); // give the bluetooth stack the chance to get things ready
      vTaskDelay(pdMS_TO_TICKS(500));
      pServer->startAdvertising(); // restart advertising
      //      Serial.println("Restart advertising");
      oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
      // do stuff here on connecting
      oldDeviceConnected = deviceConnected;
    }
  }
}
