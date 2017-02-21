package com.call.detect;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhoneState extends IntentService {

    private static final String stateStr = "com.call.detect.extra.PARAM1";
    private static final String number = "com.call.detect.extra.PARAM2";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static String callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;
    private static String duration;
    private SharedPreferences mSharedPreferences;


    public PhoneState() {
        super("PhoneState");
    }

    public static void startPhoneStateService(Context context, String stateStr1, String number1) {
        Intent intent = new Intent(context, PhoneState.class);
        intent.putExtra(stateStr, stateStr1);
        intent.putExtra(number, number1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();
            final String stateStr1 = intent.getStringExtra(stateStr);
            final String number1 = intent.getStringExtra(number);
            mSharedPreferences = context.getSharedPreferences("callTime",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            int state = 0;
            //save the phone state
            if (stateStr1 != null) {
                if (stateStr1.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr1.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr1.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
            }
            onCallStateChanged(state, number1,editor);
        }
    }

    private void onCallStateChanged(int state, String number,SharedPreferences.Editor editor) {
    // do nothing if is same state
        if (lastState == state) {
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                //incoming call received
                isIncoming = true;
                saveCallStartTime(editor);
                savedNumber = number;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->Off hook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    //outgoing call started
                    isIncoming = false;
                    saveCallStartTime(editor);
                } else {
                    //incoming call answered
                    isIncoming = true;
                    saveCallStartTime(editor);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                String smsContent;
                String date1 = "a";
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //a miss call
                    smsContent = "miss call from " + savedNumber;
                    Log.d("call", smsContent + ". Date is " + date1);
                } else if (isIncoming) {
                    //incoming call end
                    compareTime();
                    smsContent = "received call from " + savedNumber + duration;
                    Log.d("call", smsContent + ". Date is " + date1);
                } else {
                    //outgoing call end
                    compareTime();
                    //get the call out number from shared preferences
                    savedNumber = mSharedPreferences.getString("callOutNumber","null");
                    smsContent = "call to " + savedNumber + duration;
                    Log.d("call", smsContent + ". Date is " + date1);
                }
                break;
        }
        lastState = state;
    }
    private  void compareTime() {
        // to get the call start time and create the call end time and compare it
        // save to duration 
        callStartTime = mSharedPreferences.getString("callStartTime","00,00:00:00");
        String callEndTime = DateFormat.format("dd,HH:mm:ss", new Date()).toString();

        try {
            Date callStartTime1 = new SimpleDateFormat("dd,HH:mm:ss", Locale.getDefault()).parse(callStartTime);
            Date callEndTime1 = new SimpleDateFormat("dd,HH:mm:ss", Locale.getDefault()).parse(callEndTime);
            long callingTime = callEndTime1.getTime() - callStartTime1.getTime();
            long hour = (callingTime/(60*60*1000));
            long min = ((callingTime/(60*1000))-hour*60);
            long second = (callingTime/1000-hour*60*60-min*60);
            if (hour != 0) {
                duration = ".Time: " + hour + "h " + min + "m " + second + "s.";
            } else if (min !=0) {
                duration = ".Time: " + min + "m " + second + "s.";
            } else {
                duration = ".Time: " + second + "s.";
            }
            Log.d("phoneState", duration);
        } catch (ParseException e) {
            Log.d("phoneState","parseExceptionHappened");
        }
    }
    private void saveCallStartTime(SharedPreferences.Editor editor) {
        callStartTime = DateFormat.format("dd,HH:mm:ss", new Date()).toString();
        editor.putString("callStartTime",callStartTime);
        editor.apply();
    }
}
