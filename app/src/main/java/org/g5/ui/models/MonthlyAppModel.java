package org.g5.ui.models;

import android.graphics.drawable.Drawable;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyAppModel that = (MonthlyAppModel) o;
        return month.equals(that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, Arrays.hashCode(icons));
    }
}
