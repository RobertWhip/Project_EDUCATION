package com.example.mechanic_pc.uzhgorodschools.service.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.mechanic_pc.uzhgorodschools.service.singleton.SingletonService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            startService(context);
        }
    }

    private void startService(Context context) {
        SingletonService.execute(context);
    }
}
