package org.g5.ui.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.g5.ui.models.WeeklyAppModel;

public class WeeklyAppDiffCallback extends DiffUtil.ItemCallback<WeeklyAppModel> {

    @Override
    public boolean areItemsTheSame(@NonNull WeeklyAppModel oldItem, @NonNull WeeklyAppModel newItem) {
        return oldItem.getName().equals(newItem.getName());
    }

    @Override
    public boolean areContentsTheSame(@NonNull WeeklyAppModel oldItem, @NonNull WeeklyAppModel newItem) {
        return oldItem.equals(newItem);
    }
}