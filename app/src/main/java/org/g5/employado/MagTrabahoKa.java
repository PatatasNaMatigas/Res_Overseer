package org.g5.employado;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.g5.core.Tracker;
import org.g5.overseer.R;
import org.g5.pet.Pet;
import org.g5.ui.Home;
import org.g5.util.NotificationBuilder;
import org.g5.util.Time;

public class MagTrabahoKa extends Worker {

    private Context context;

    public MagTrabahoKa(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Your background work here
        try {
            Tracker.startTracking(context);
            NotificationBuilder notificationBuilder = new NotificationBuilder();
            notificationBuilder.createNotificationChannel(context);
            notificationBuilder.getID();

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    new Intent(context, Home.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            notificationBuilder.showNotification(context,
                    new NotificationCompat.Builder(
                            context,
                            notificationBuilder.getID())
                            .setSmallIcon(R.drawable.normal_icon)
                            .setContentTitle(Pet.getName())
                            .setContentText("Your total screen time is " + Time.formatTime(Time.convertSecondsToArray(Pet.getScreenTime())))
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setSound(null)
            );
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}