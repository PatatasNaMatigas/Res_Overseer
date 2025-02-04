package org.g5.ui.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.g5.ui.models.DailyAppModel;

public class DailyAppDiffCallback extends DiffUtil.ItemCallback<DailyAppModel> {
    @Override
    public boolean areItemsTheSame(@NonNull DailyAppModel oldItem, @NonNull DailyAppModel newItem) {
        return oldItem.getName().equals(newItem.getName());
    }

    @Override
    public boolean areContentsTheSame(@NonNull DailyAppModel oldItem, @NonNull DailyAppModel newItem) {
        return oldItem.equals(newItem);
    }
}