package org.g5.ui.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.g5.ui.models.ScreenTimeSummaryModel;

import java.util.Arrays;

public class ScreenTimeSummaryDiffCallback extends DiffUtil.ItemCallback<ScreenTimeSummaryModel> {
    @Override
    public boolean areItemsTheSame(@NonNull ScreenTimeSummaryModel oldItem, @NonNull ScreenTimeSummaryModel newItem) {
        return Arrays.equals(oldItem.getDate(), newItem.getDate());
    }

    @Override
    public boolean areContentsTheSame(@NonNull ScreenTimeSummaryModel oldItem, @NonNull ScreenTimeSummaryModel newItem) {
        return oldItem.equals(newItem);
    }
}