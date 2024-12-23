package org.g5.pet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import org.g5.overseer.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FloatingWindow {

    private String message;
    private int reaction;
    private static WindowManager windowManager;
    protected View floatingView;
    private WindowManager.LayoutParams params;
    private GestureDetector gestureDetector;

    public FloatingWindow message(String message) {
        this.message = message;
        return this;
    }

    public FloatingWindow react(int mode) {
        this.reaction = mode;
        return this;
    }

    public void start(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        floatingView = LayoutInflater.from(context).inflate(R.layout.floating_window, null, false);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 0;
        params.y = 100;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float SWIPE_THRESHOLD = 100;
                float SWIPE_VELOCITY_THRESHOLD = 100;

                float distanceY = e2.getY() - e1.getY();
                if (distanceY < -SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    onSwipeUp();
                    return true;
                }
                return false;
            }
        });

        floatingView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        windowManager.addView(floatingView, params);

        fadeIn(floatingView);
    }

    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
    }

    private void fadeOut(View view, Runnable onEnd) {
        view.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                        if (onEnd != null) {
                            onEnd.run();
                        }
                    }
                });
    }


    private void onSwipeUp() {
        if (floatingView != null) {
            fadeOut(floatingView, () -> {
                if (windowManager != null) {
                    windowManager.removeView(floatingView);
                    floatingView = null;
                }
            });
        }
    }
}
