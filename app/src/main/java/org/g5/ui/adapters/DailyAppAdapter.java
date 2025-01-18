package org.g5.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import org.g5.overseer.R;
import org.g5.ui.model.DailyAppModel;

import java.util.List;

public class DailyAppAdapter extends RecyclerView.Adapter<DailyAppAdapter.ViewHolder> {

    private final List<DailyAppModel> appEntries;

    public DailyAppAdapter(List<DailyAppModel> appEntries) {
        this.appEntries = appEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_app_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyAppModel appEntry = appEntries.get(position);
        holder.icon.setImageDrawable(appEntry.getIcon());
        holder.name.setText(appEntry.getName());
        holder.timeSpent.setText(appEntry.getTimeSpent());
        holder.timeRecordedRange.setText(appEntry.getTimeRecordedRange());

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

    @Override
    public int getItemCount() {
        return appEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final TextView timeSpent;
        private final TextView timeRecordedRange;
        private final View viewType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            timeSpent = itemView.findViewById(R.id.time_spent);
            timeRecordedRange = itemView.findViewById(R.id.time_recorded_range);
            viewType = itemView.findViewById(R.id.app_entry);
        }
    }
}
