package org.g5.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.g5.overseer.R;
import org.g5.ui.callbacks.MonthlyAppDiffCallback;
import org.g5.ui.models.MonthlyAppModel;

import java.util.List;

public class MonthlyAppAdapter extends ListAdapter<MonthlyAppModel, MonthlyAppAdapter.ViewHolder> {

    public MonthlyAppAdapter() {
        super(new MonthlyAppDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.monthly_app_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthlyAppModel monthlyAppModel = getItem(position);
        holder.titleTextView.setText(monthlyAppModel.getMonth());

        // Only update icons that have changed
        for (int i = 0; i < 4; i++) {
            if (monthlyAppModel.getIcons()[i] != null) {
                holder.icons[i].setImageDrawable(monthlyAppModel.getIcons()[i]);
            }
        }
    }

    public void updateList(List<MonthlyAppModel> newList) {
        submitList(newList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView[] icons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.month);
            icons = new ImageView[] {
                    itemView.findViewById(R.id.app_1),
                    itemView.findViewById(R.id.app_2),
                    itemView.findViewById(R.id.app_3),
                    itemView.findViewById(R.id.app_4),
            };
        }
    }
}
