package org.g5.employado;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.g5.core.Tracker;
import org.g5.pet.Pet;

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
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}