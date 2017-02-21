package com.call.detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;


public class autoStart extends BroadcastReceiver {
    public autoStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        
        
// check is it the correct intent action, outgoing call or phone state
        if (intent.getAction() != null) {
            String intentAction = intent.getAction();
            Log.d("tag1autostart1", intent.getAction() + ".");
            if (intentAction.equals("android.intent.action.NEW_OUTGOING_CALL")) {
                // if the intent action is outgoing call, then extract the info from the intent, show in log
                try { 
                     String add = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"); 
                     OutgoingCall.startOutgoingCallService(context, add); 
                 }catch (Exception e) { 
                     Log.d("autostart","outgoing call intent exception occur"); 
                 } 
            } else if (intentAction.equals("android.intent.action.PHONE_STATE")) { 
                 try { 
                     String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE); 
                     String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER); 
                     PhoneState.startPhoneStateService(context,stateStr,number); 
                 } catch (Exception e) { 
                     Log.e("tag1Phone Receive Error", " " + e); 
                 } 
             } 
        }
    }
}

