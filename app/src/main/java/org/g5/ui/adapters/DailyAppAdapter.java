package org.g5.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.g5.overseer.R;
import org.g5.ui.callbacks.DailyAppDiffCallback;
import org.g5.ui.models.DailyAppModel;

import java.util.Objects;

public class DailyAppAdapter extends ListAdapter<DailyAppModel, DailyAppAdapter.ViewHolder> {

    public DailyAppAdapter() {
        super(new DailyAppDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_app_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyAppModel appEntry = getItem(position);
        holder.icon.setImageDrawable(appEntry.getIcon());
        holder.name.setText(appEntry.getName());
        holder.timeSpent.setText(appEntry.getTimeSpent());

        int code = appEntry.getBg();
        if (code == 0)
            holder.viewType.setBackground(
                    AppCompatResources.getDrawable(
                            holder.viewType.getContext(),
                            R.drawable.rounded_corner_variant_2
                    )
            );
        else if (code == 1)
            holder.viewType.setBackground(
                    AppCompatResources.getDrawable(
                            holder.viewType.getContext(),
                            R.drawable.rounded_corner_variant_3
                    )
            );
        else
            holder.viewType.setBackground(
                    AppCompatResources.getDrawable(
                            holder.viewType.getContext(),
                            R.drawable.rounded_corner_variant_4
                    )
            );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final TextView timeSpent;
        private final View viewType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            timeSpent = itemView.findViewById(R.id.time_spent);
            viewType = itemView.findViewById(R.id.app_entry);
        }
    }
}