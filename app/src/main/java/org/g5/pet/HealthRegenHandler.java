package org.g5.pet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class HealthRegenHandler extends BroadcastReceiver {

    private LocalDateTime offTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
            offTime = LocalDateTime.now();
            Log.d("HEALTH-REGEN", offTime.toString());
        } else if (Objects.equals(intent.getAction(), Intent.ACTION_USER_PRESENT)) {
            Pet.startHealthRegen(Duration.between(offTime, LocalDateTime.now()).getSeconds());
        }
    }
}
