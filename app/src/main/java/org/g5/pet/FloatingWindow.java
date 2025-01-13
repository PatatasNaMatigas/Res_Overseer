package org.g5.pet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.g5.overseer.R;
import org.g5.ui.Permission;
import org.g5.util.AccessibilityUtils;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FloatingWindow {

    public static final int NORMAL = 0;
    public static final int DIZZY = 1;
    public static final int TIRED = 2;
    public static final int DYING = 3;
    public static final int HAPPY = 4;

    private Drawable normal;
    private Drawable dizzy;
    private Drawable tired;
    private Drawable dying;
    private Drawable happy;
    private Drawable reaction;

    private String message;
    private String name;
    private int mode;
    private static WindowManager windowManager;
    protected static View floatingView;
    private WindowManager.LayoutParams params;

    public FloatingWindow name(String name) {
        this.name = name;
        return this;
    }

    public FloatingWindow message(String message) {
        this.message = message;
        return this;
    }

    public FloatingWindow react(int mode) {
        this.mode = mode;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void start(AppCompatActivity appCompatActivity) {

        normal = ContextCompat.getDrawable(appCompatActivity, R.drawable.normal);
        dizzy = ContextCompat.getDrawable(appCompatActivity, R.drawable.dizzy);
        tired = ContextCompat.getDrawable(appCompatActivity, R.drawable.tired);
        dying = ContextCompat.getDrawable(appCompatActivity, R.drawable.dead);
        happy = ContextCompat.getDrawable(appCompatActivity, R.drawable.happy);

        switch (mode) {
            case NORMAL:
                reaction = normal;
                break;
            case DIZZY:
                reaction = dizzy;
                break;
            case TIRED:
                reaction = tired;
                break;
            case DYING:
                reaction = dying;
                break;
            case HAPPY:
                reaction = happy;
                break;
        }

        windowManager = (WindowManager) appCompatActivity.getSystemService(Context.WINDOW_SERVICE);

        floatingView = LayoutInflater.from(appCompatActivity).inflate(R.layout.floating_window, null, false);

        DisplayMetrics displayMetrics = appCompatActivity.getResources().getDisplayMetrics();

        params = new WindowManager.LayoutParams(
                (int) (displayMetrics.widthPixels * 0.9),
                (int) (displayMetrics.heightPixels * 0.3),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.END;
        params.x = 0;
        params.y = 100;

        // Set an OnTouchListener to detect clicks
        floatingView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onFloatingWindowClicked();
                return true;
            }
            return false;
        });

        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            appCompatActivity.startActivity(new Intent(appCompatActivity, Permission.class));
            windowManager.addView(floatingView, params);
        }

        updateName();
        updateMessage();
        updateReaction();
    }

    private void onFloatingWindowClicked() {
        removeFloatingWindow();
    }

    private void updateReaction() {
        ImageView icon = floatingView.findViewById(R.id.type);

        if (reaction != null && icon != null) {
            icon.setImageDrawable(reaction);
        }
    }

    private void updateName() {
        TextView textView = floatingView.findViewById(R.id.name);

        if (textView == null)
            return;

        textView.setText(name);

        String text = textView.getText().toString();

        Paint paint = textView.getPaint();
        float textWidth = paint.measureText(text);

        int dynamicMarginStart = (int) (textWidth / 1.8);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
        params.setMarginStart(dynamicMarginStart);
        textView.setLayoutParams(params);
    }

    private void updateMessage() {
        TextView textView = floatingView.findViewById(R.id.message);

        if (textView != null) {
            textView.setText(message);
        }
    }

    private static void removeFloatingWindow() {
        if (floatingView != null) {
            if (windowManager != null) {
                windowManager.removeView(floatingView);
                floatingView = null;
            }
        }
    }

    public static boolean permissionGranted(AppCompatActivity appCompatActivity) {
        WindowManager windowManager;
        View floatingView;
        try {
            windowManager = (WindowManager) appCompatActivity.getSystemService(Context.WINDOW_SERVICE);

            floatingView = LayoutInflater.from(appCompatActivity).inflate(R.layout.floating_window, null, false);

            DisplayMetrics displayMetrics = appCompatActivity.getResources().getDisplayMetrics();

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    (int) (displayMetrics.widthPixels * 0.9),
                    (int) (displayMetrics.heightPixels * 0.3),
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            : WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.END;
            params.x = 0;
            params.y = 100;

            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            return false;
        }
        windowManager.removeView(floatingView);
        return true;
    }
}
