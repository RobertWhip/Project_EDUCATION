package com.example.mechanic_pc.uzhgorodschools.service.singleton;

import android.content.Context;
import android.content.Intent;

import com.example.mechanic_pc.uzhgorodschools.service.NotificationService;

public class SingletonService {
    private volatile static boolean started = false;

    public static void execute(Context context) {
        if (!started) {
            synchronized (SingletonService.class) {
                if (!started) {
                    context.startService(new Intent(context, NotificationService.class));
                    started = true;
                }
            }
        }
    }

    private SingletonService() {
    }
}
