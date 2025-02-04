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
import org.g5.ui.models.WeeklyAppModel;

import java.util.List;

public class WeeklyAppAdapter extends RecyclerView.Adapter<WeeklyAppAdapter.ViewHolder> {

    private final List<WeeklyAppModel> appEntries;

    public WeeklyAppAdapter(List<WeeklyAppModel> appEntries) {
        this.appEntries = appEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_app_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeeklyAppModel appEntry = appEntries.get(position);

        for (int i = 0; i < 3; i++) {
            holder.icon[i].setImageDrawable(appEntry.getIcon()[i]);
            holder.timeSpent[i].setText(appEntry.getTimeSpent()[i]);
        }
        holder.date.setText(appEntry.getDate());
        holder.month.setText(appEntry.getMonth());
        holder.day.setText(appEntry.getDay());

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
        private final ImageView[] icon;
        private final TextView[] timeSpent;
        private final TextView date;
        private final TextView month;
        private final TextView day;
        private final View viewType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = new ImageView[]{
                    itemView.findViewById(R.id.app_icon_1),
                    itemView.findViewById(R.id.app_icon_2),
                    itemView.findViewById(R.id.app_icon_3),
            };
            timeSpent = new TextView[] {
                    itemView.findViewById(R.id.time_spent_1),
                    itemView.findViewById(R.id.time_spent_2),
                    itemView.findViewById(R.id.time_spent_3),
            };
            date = itemView.findViewById(R.id.date);
            month = itemView.findViewById(R.id.month);
            day = itemView.findViewById(R.id.day);
            viewType = itemView.findViewById(R.id.app_entry);
        }
    }
}
