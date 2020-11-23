
float calcHR (uint16_t rawDataArr[]) {
  int order = 3;
  int threshold = 1852321;
  //  int threshold = 2000000;
  int filteredPeaksArrLen = ECG_DATA_ARR_LEN - 2 * order;
  int filteredPeaksArr[filteredPeaksArrLen] = {0};
  int noOfPeaks = 0;
  float* instHRarr;
  int instHRarrLen = 0;
  float medianHR = 0;
  float thresholdForOutlier = 1.5;

  filterPeaks(rawDataArr, order, threshold, filteredPeaksArr);

  for (int i = 0; i < filteredPeaksArrLen; i++) {
    if (filteredPeaksArr[i] == 1) {
      noOfPeaks++;
    }
  }

//  Serial.print("No of peaks ");
//  Serial.println(noOfPeaks);

  if (noOfPeaks == 0) {
    return 0;
  }

  instHRarrLen = noOfPeaks - 1;
  instHRarr = (float*) malloc (instHRarrLen * sizeof(int));

  calcInstHR(filteredPeaksArr, filteredPeaksArrLen, instHRarr);

  for (int i = 0; i < instHRarrLen; i++) {
    instHRarr[i] = (60 * 100) / instHRarr[i]; // in bpm
  }

  removeOutliers(instHRarr, instHRarrLen, thresholdForOutlier);   // outlier HR has value of -1

  medianHR = calcMedianHR(instHRarr, instHRarrLen);     // takes outlier HRs with -1 values into account

  free(instHRarr);

  return medianHR;
}


void filterPeaks(uint16_t srcArr[], int order, int threshold, int destArr[]) {
  int diff = 0;
  int window [6] = {0};
  int counter = 0;
  int argMaxInWindow = 0;

  for (int i = (0 + order); i < (ECG_DATA_ARR_LEN - order); i++) {
    diff = srcArr[i - order] + srcArr[i + order] - 2 * srcArr[i];
    if (diff < 0) {
      if ((diff * diff) > threshold) {
        destArr[i - order] = 1;

//        Serial.print("Unfiltered peak at ");
//        Serial.println(i - order);
      } else {
        destArr[i - order] = 0;
      }
    } else {
      destArr[i - order] = 0;
    }
  }
  // filter out the duplicate peaks
  int i = 0;
  while (i < (ECG_DATA_ARR_LEN - 2 * order)) {

    if (destArr[i] == 1) {
      counter = 0;
      window[counter] = i;
      argMaxInWindow = counter;
      counter++;
      for (int j = 1; j < 6; j++) {
        if ((i + j) < (ECG_DATA_ARR_LEN - 2 * order)) {
          if ((destArr[i + j] == 1)) {
            window[counter] = i + j;
            if (srcArr[window[counter] + order] > srcArr[window[argMaxInWindow] + order]) {
              argMaxInWindow = counter;
            }
            counter++;
          }
        } else {
          break;
        }
      }
      for (int j = 0 ; j < counter; j++) {
        if (j == argMaxInWindow) {
          destArr[i + j] = 1;
//          Serial.print("Filtered peak at ");
//          Serial.println(i + j);
        } else {
          destArr[i + j] = 0;
        }
      }
      i = i + counter;
    } else {
      i++;
    }
  }

  return ;
}

void calcInstHR (int srcArr[], int srcArrLen, float destArr[]) {
  int j = -1;
  int prevI = 0;
  for (int i = 0; i < srcArrLen; i++) {
    if (srcArr[i] == 1) {
      if (j >= 0) {
        destArr[j] = (float) i - prevI;
      }
      j++;
      prevI = i;
    }
  }
  return;
}

void removeOutliers(float arr[], int arrLen, float c ) {

  int counter = 0;
  float meanVal = 0.0;
  float std = 0.0;

  for (int i = 0; i < arrLen; i++) {
    if (arr[i] < 40 || arr[i] > 220) {
      arr[i] = -1;
      counter ++;
    }
  }

  //calculate mean
  for (int i = 0; i < arrLen; i++) {
    if (arr[i] != -1) {
      meanVal = meanVal + arr[i];
    }
  }
  meanVal = meanVal / (arrLen - counter);

  // calculated standard deviation
  for (int i = 0; i < arrLen; i++) {
    if (arr[i] != -1) {
      std = std + pow((meanVal - arr[i]), 2);
    }
  }
  std = std / (arrLen - counter);
  std = sqrt(std);

  for (int i = 0; i < arrLen; i++) {
    if (arr[i] > (meanVal + c * std)) {
      arr[i] = -1;
    }
  }
  return;
}

float calcMedianHR (float arr[], int arrLen) {
  int temp = 0;
  int nrOfOutliers = 0;

  for (int i = 0 ; i < arrLen ; i++) {
    if (arr[i] == -1) {
      nrOfOutliers++;
    }
    for (int j = 0 ; j < arrLen - 1 ; j++) {
      if (arr[j] > arr[j + 1]) {
        temp = arr[j];
        arr[j] = arr[j + 1];
        arr[j + 1] = temp;
      }
    }
  }
  return (arr[nrOfOutliers + (arrLen - nrOfOutliers) / 2]);
}
