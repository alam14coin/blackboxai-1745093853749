package com.example.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvQuestionNumber, tvTimer, tvQuestion;
    private Button[] optionButtons = new Button[4];

    private int quizCategory;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private CountDownTimer countDownTimer;
    private static final long QUESTION_TIME = 15000; // 15 seconds

    private List<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestion = findViewById(R.id.tvQuestion);

        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        for (Button btn : optionButtons) {
            btn.setOnClickListener(this);
        }

        quizCategory = getIntent().getIntExtra("quizCategory", 1);

        loadQuestionsForCategory(quizCategory);
        showQuestion();
    }

    private void loadQuestionsForCategory(int category) {
        // For demo, create dummy questions. In real app, load from DB or API.
        questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            questions.add(new Question(
                    "Question " + i + " for category " + category + "?",
                    new String[]{"Option 1", "Option 2", "Option 3", "Option 4"},
                    0 // correct answer index
            ));
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResult();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        tvQuestionNumber.setText("Question number " + (currentQuestionIndex + 1));
        tvQuestion.setText(q.getQuestionText());

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(q.getOptions()[i]);
            optionButtons[i].setEnabled(true);
        }

        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(QUESTION_TIME, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText((millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                tvTimer.setText("0s");
                disableOptions();
                nextQuestion();
            }
        }.start();
    }

    private void disableOptions() {
        for (Button btn : optionButtons) {
            btn.setEnabled(false);
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showQuestion();
    }

    @Override
    public void onClick(View view) {
        countDownTimer.cancel();

        int selectedIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (view.getId() == optionButtons[i].getId()) {
                selectedIndex = i;
                break;
            }
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        if (selectedIndex == currentQuestion.getCorrectAnswerIndex()) {
            score++;
        }

        disableOptions();
        nextQuestion();
    }

    private void showResult() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed");
        builder.setMessage("Your score: " + score + " out of " + questions.size());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(QuizActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private static class Question {
        private String questionText;
        private String[] options;
        private int correctAnswerIndex;

        public Question(String questionText, String[] options, int correctAnswerIndex) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }
    }
}
