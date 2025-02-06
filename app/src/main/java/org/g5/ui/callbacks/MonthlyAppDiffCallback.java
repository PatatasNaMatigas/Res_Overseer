package org.g5.ui.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.g5.ui.models.MonthlyAppModel;

import java.util.Objects;

public class MonthlyAppDiffCallback  extends DiffUtil.ItemCallback<MonthlyAppModel> {

    @Override
    public boolean areItemsTheSame(@NonNull MonthlyAppModel oldItem, @NonNull MonthlyAppModel newItem) {
        return oldItem.getMonth().equals(newItem.getMonth());
    }

    @Override
    public boolean areContentsTheSame(@NonNull MonthlyAppModel oldItem, @NonNull MonthlyAppModel newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public Object getChangePayload(@NonNull MonthlyAppModel oldItem, @NonNull MonthlyAppModel newItem) {
        if (oldItem.getMonth().equals(newItem.getMonth())) {
            // Check which icons have changed
            for (int i = 0; i < 4; i++) {
                if (!Objects.equals(oldItem.getIcons()[i], newItem.getIcons()[i])) {
                    return i; // Return position of changed icon
                }
            }
        }
        return null;
    }
}