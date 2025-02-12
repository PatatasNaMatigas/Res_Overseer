package org.g5.ui.models;

import android.content.Context;

import org.g5.core.Data;
import org.g5.core.ScreenTimeTracker;
import org.g5.ui.ScreenTime;
import org.g5.ui.quiz.QuizData;
import org.g5.util.Time;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

public class ScreenTimeSummaryModel {

    private int[] date;
    private long screenTime;
    private boolean visible;

    public ScreenTimeSummaryModel(Context context, LocalDate localDate, boolean visible) {
        this.visible = visible;
        if (visible) {
            date = Time.ldToDateArray_MDY(localDate);

            screenTime = ScreenTimeTracker.getTotalScreenTimeForDate(context, localDate);
        }
    }

    public int[] getDate() {
        return date;
    }

    public long getScreenTime() {
        return screenTime;
    }

    public boolean getVisible() {
        return visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenTimeSummaryModel that = (ScreenTimeSummaryModel) o;
        return date == that.date;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(date), screenTime);
    }
}
