package com.example.mechanic_pc.uzhgorodschools.service.boot;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.mechanic_pc.uzhgorodschools.service.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            start_service(context);
        }
    }

    private void start_service(Context context) {
        if(!isMyServiceRunning(NotificationService.class)) {
            context.startService(new Intent(context, NotificationService.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
