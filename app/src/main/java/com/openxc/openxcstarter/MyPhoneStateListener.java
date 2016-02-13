package com.openxc.openxcstarter;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


public class MyPhoneStateListener extends android.telephony.PhoneStateListener {

    public static Boolean phoneRinging = false;
    private Context context;

    public MyPhoneStateListener(Context context) {
        this.context = context;
    }

    public void onCallStateChanged(int state, String incomingNumber) {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d("DEBUG", "IDLE");
                phoneRinging = false;
                audioManager.setSpeakerphoneOn(true);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d("DEBUG", "OFFHOOK");
                phoneRinging = false;
                audioManager.setSpeakerphoneOn(true);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d("DEBUG", "RINGING");
                phoneRinging = true;
                audioManager.setSpeakerphoneOn(true);

                break;
        }
    }

}

