
float calcHR (uint16_t rawDataArr[]) {
  int order = 3;
    int threshold = 7268416;
//  int threshold = 2000000;
  int filteredPeaksArrLen = 1000 - 2 * order;
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

  for (int i = (0 + order); i < (1000 - order); i++) {
    diff = srcArr[i - order] + srcArr[i + order] - 2 * srcArr[i];
    if (diff < 0) {
      if ((diff * diff) > threshold) {
        destArr[i] = 1;
      }
    } else {
      destArr[i] = 0;
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

void removeOutliers(float arr[], int arrLen, float c ) {
  //calculate mean
  float meanVal = 0.0;
  float std = 0.0;
  for (int i = 0; i < arrLen; i++) {
    meanVal = meanVal + arr[i];
  }
  meanVal = meanVal / arrLen;

  // calculated standard deviation
  for (int i = 0; i < arrLen; i++) {
    std = std + pow((meanVal - arr[i]), 2);
  }
  std = std / arrLen;
  std = sqrt(std);

  for (int i = 0; i < arrLen; i++) {
    if (arr[i] > (meanVal + c * std)) {
      arr[i] = -1;
    }
  }
  return;
}
