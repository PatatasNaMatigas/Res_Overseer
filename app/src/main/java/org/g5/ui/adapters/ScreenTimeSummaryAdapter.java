package org.g5.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.g5.core.Data;
import org.g5.overseer.R;
import org.g5.ui.callbacks.ScreenTimeSummaryDiffCallback;
import org.g5.ui.models.ScreenTimeSummaryModel;
import org.g5.util.Time;

import java.util.List;

public class ScreenTimeSummaryAdapter extends ListAdapter<ScreenTimeSummaryModel, ScreenTimeSummaryAdapter.ViewHolder> {

    public ScreenTimeSummaryAdapter() {
        super(new ScreenTimeSummaryDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.screen_time_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScreenTimeSummaryModel checkupHistoryModel = getItem(position);

        if (checkupHistoryModel.getVisible()) {
            holder.view.setVisibility(View.VISIBLE);
        } else {
            holder.view.setVisibility(View.GONE);
            return;
        }

        long screenTime = checkupHistoryModel.getScreenTime();

        holder.date.setText(Time.formatToDate(checkupHistoryModel.getDate()));
        holder.screenTime.setText(Time.formatTime(Time.convertSecondsToArray(screenTime)));

        if (screenTime < Time.hourToSecond(4)) {
            holder.icon.setImageResource(R.drawable.plain_wow);
            holder.view.setBackgroundResource(R.drawable.rounded_corner_variant_5);
        } else if (screenTime < Time.hourToSecond(6)) {
            holder.icon.setImageResource(R.drawable.plain_happy);
            holder.view.setBackgroundResource(R.drawable.rounded_corner_variant_2);
        } else if (screenTime < Time.hourToSecond(8)) {
            holder.icon.setImageResource(R.drawable.plain_tired);
            holder.view.setBackgroundResource(R.drawable.rounded_corner_variant_4);
        } else if (screenTime > Time.hourToSecond(8)) {
            holder.icon.setImageResource(R.drawable.plain_angry);
            holder.view.setBackgroundResource(R.drawable.rounded_corner_variant_3);
        }

        Log.d("ScreenTimeSummaryAdapter--", screenTime + " " + Time.hourToSecond(2));
    }

    public void updateList(List<ScreenTimeSummaryModel> newList) {
        submitList(newList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView screenTime;
        ImageView icon;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            screenTime = itemView.findViewById(R.id.screen_time);
            icon = itemView.findViewById(R.id.mood_icon);
            view = itemView.findViewById(R.id.screen_time_entry);
        }
    }
}
