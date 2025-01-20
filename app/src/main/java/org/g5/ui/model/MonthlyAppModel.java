package org.g5.ui.model;

import android.graphics.drawable.Drawable;

public class MonthlyAppModel {

    private String month;
    private Drawable[] icons;

    public MonthlyAppModel(String month, Drawable[] icons) {
        this.month = month;
        this.icons = icons;
    }

    public String getMonth() {
        return month;
    }

    public Drawable[] getIcons() {
        return icons;
    }
}
