package com.tudelft.iots.ecg.classes;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

public class ToastView {
    public static void showToast(Context context, int resourceId, int toastDurationInMilliSeconds) {
        // Set the toast and duration
        final Toast mToastToShow = Toast.makeText(context, resourceId, Toast.LENGTH_LONG);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                mToastToShow.show();
            }
            public void onFinish() {
                mToastToShow.cancel();
            }
        };

        // Show the toast and starts the countdown
        mToastToShow.show();
        toastCountDown.start();
    }
}
