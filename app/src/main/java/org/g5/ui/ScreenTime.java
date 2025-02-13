package org.g5.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.g5.core.Data;
import org.g5.overseer.R;
import org.g5.ui.adapters.ScreenTimeSummaryAdapter;
import org.g5.ui.models.ScreenTimeSummaryModel;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class ScreenTime extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_time_summary_page);

        findViewById(R.id.exit).setOnClickListener(v -> {
            startActivity(new Intent(this, Summary.class));
            finish();
        });

        initializeHistoryAdapter();
    }

    private void initializeHistoryAdapter() {
        RecyclerView historyRecyclerView = findViewById(R.id.data_recycler_view);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ScreenTimeSummaryAdapter historyAdapter = new ScreenTimeSummaryAdapter();
        historyRecyclerView.setAdapter(historyAdapter);
        historyAdapter.submitList(createHistoryModels());
    }

    private List<ScreenTimeSummaryModel> createHistoryModels() {
        LocalDate[] week = {
                LocalDate.of(2025, Month.FEBRUARY, 7),
                LocalDate.of(2025, Month.FEBRUARY, 8),
                LocalDate.of(2025, Month.FEBRUARY, 9),
                LocalDate.of(2025, Month.FEBRUARY, 10),
                LocalDate.of(2025, Month.FEBRUARY, 11),
        };
        List<ScreenTimeSummaryModel> checkupHistoryModels = new ArrayList<>();
        for (LocalDate day : week)
            checkupHistoryModels.add(new ScreenTimeSummaryModel(this, day, true));
        checkupHistoryModels.add(new ScreenTimeSummaryModel(this, null, false));
        return checkupHistoryModels;
    }
}
