package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.ui.Home;
import org.g5.ui.Permission;

public class Q3Done extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start);

        ((TextView) findViewById(R.id.question_text)).setText("It's always good to recognize and to keep your emotions in check! I'll check back in with you tomorrow, OK? Bye!");

        findViewById(R.id.start_button).setOnClickListener(v -> {
            QuizData.done();
            Intent intent = new Intent(this, Report.class);
            startActivity(intent);
            finish();
        });
    }
}