package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class QuizSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[] quizButtons = new Button[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_selection);

        for (int i = 0; i < 12; i++) {
            String buttonID = "btnQuiz" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            quizButtons[i] = findViewById(resID);
            quizButtons[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < 12; i++) {
            if (view.getId() == quizButtons[i].getId()) {
                Intent intent = new Intent(QuizSelectionActivity.this, QuizActivity.class);
                intent.putExtra("quizCategory", i + 1);
                startActivity(intent);
                break;
            }
        }
    }
}
