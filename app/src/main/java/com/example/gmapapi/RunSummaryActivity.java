package com.example.gmapapi;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RunSummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_summary);
        // Get data passed from MainActivity
        float totalDistance = getIntent().getFloatExtra("totalDistance", 0f);
        long totalTime = getIntent().getLongExtra("totalTime", 0L);
        float caloriesBurned = getIntent().getFloatExtra("caloriesBurned", 0f);
        int stepsTaken = getIntent().getIntExtra("stepsTaken", 0);

        // Display the results
        TextView distanceView = findViewById(R.id.distance_text);
        TextView timeView = findViewById(R.id.time_text);
        TextView caloriesView = findViewById(R.id.calories_text);
        TextView stepsView = findViewById(R.id.steps_text);

        distanceView.setText(totalDistance + " meters");
        timeView.setText(totalTime + " seconds");
        caloriesView.setText(caloriesBurned + " kcal");
        stepsView.setText(stepsTaken+" steps");
    }
}
