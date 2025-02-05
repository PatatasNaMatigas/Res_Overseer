package org.g5.ui.models;

import android.graphics.drawable.Drawable;

import java.util.Objects;

public class DailyAppModel {

    private final DailyAppModel previous;
    private final String name;
    private final String timeSpent;
    private final Drawable icon;
    private final int bg;

    public DailyAppModel(DailyAppModel lastView, String name, String timeSpent, Drawable icon) {
        this.name = name;
        this.timeSpent = timeSpent;
        this.icon = icon;
        previous = lastView;

        bg = (lastView != null)
                ? (lastView.getBg() < 2)
                ? lastView.getBg() + 1
                : 0
                : 0;
    }

    public String getName() {
        return name;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getBg() {
        return bg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyAppModel that = (DailyAppModel) o;
        return bg == that.bg &&
                Objects.equals(previous, that.previous) &&
                Objects.equals(name, that.name) &&
                Objects.equals(timeSpent, that.timeSpent) &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, name, timeSpent, icon, bg);
    }
}
