package com.call.detect;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

// to detect the number when call out, the number is saved and going to use in phone state service

public class OutgoingCall extends IntentService {
    private static final String EXTRA_PARAM1 = "android.intent.extra.PHONE_NUMBER";

    public OutgoingCall() {
        super("OutgoingCall");
    }


    public static void startOutgoingCallService(Context context, String add) {
        Intent intent = new Intent(context, OutgoingCall.class);
        intent.putExtra(EXTRA_PARAM1, add);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();
            // check the phone number
            final String add = intent.getStringExtra(EXTRA_PARAM1);
            if (add != null) {
                 saveCallOutNumber(context, add);
            }
        }
    }

    private void saveCallOutNumber (Context context, String number) {
        Log.d("outgoingCall",number);
        SharedPreferences mSharedPreferences = context.getSharedPreferences("callTime",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("callOutNumber",number);
        editor.apply();
    }

}
