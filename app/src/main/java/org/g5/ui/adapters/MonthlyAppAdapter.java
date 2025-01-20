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
import org.g5.ui.model.MonthlyAppModel;

import java.util.List;

public class MonthlyAppAdapter extends RecyclerView.Adapter<MonthlyAppAdapter.MonthlyViewHolder> {
    private final List<MonthlyAppModel> data;

    public MonthlyAppAdapter(List<MonthlyAppModel> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MonthlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.monthly_app_entry, parent, false);
        return new MonthlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyViewHolder holder, int position) {
        MonthlyAppModel monthlyAppModel = data.get(position);
        holder.titleTextView.setText(monthlyAppModel.getMonth());
        for (int i = 0; i < 4; i++) {
            if (monthlyAppModel.getIcons()[i] == null)
                return;

            holder.icons[i].setImageDrawable(monthlyAppModel.getIcons()[i]);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MonthlyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView[] icons;

        public MonthlyViewHolder(@NonNull View itemView) {
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
