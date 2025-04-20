package com.example.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvQuestionNumber, tvTimer, tvQuestion;
    private Button[] optionButtons = new Button[4];

    private int quizCategory;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private CountDownTimer countDownTimer;
    private static final long QUESTION_TIME = 15000; // 15 seconds
    private final Handler handler = new Handler();

    private List<Question> questions;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeViews();
        setupQuiz();
    }

    private void initializeViews() {
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
    }

    private void setupQuiz() {
        quizCategory = getIntent().getIntExtra("quizCategory", 1);
        dbHelper = new DatabaseHelper(this);
        questions = dbHelper.getQuestionsByCategory(quizCategory);

        if (questions == null || questions.isEmpty()) {
            showError("No questions available for this category");
            return;
        }

        showQuestion();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToWelcome();
            }
        }, 2000);
    }

    private void showQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResult();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        tvQuestionNumber.setText(String.format("Question %d of %d", (currentQuestionIndex + 1), questions.size()));
        tvQuestion.setText(q.getQuestionText());

        // Reset button backgrounds and enable them
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(q.getOptions()[i]);
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackgroundResource(R.drawable.quiz_button_background);
        }

        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(QUESTION_TIME, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format("%ds", seconds));
                
                // Change timer color to red when less than 5 seconds remain
                if (seconds <= 5) {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.black));
                }
            }

            public void onFinish() {
                tvTimer.setText("0s");
                tvTimer.setTextColor(getResources().getColor(android.R.color.black));
                handleTimeUp();
            }
        }.start();
    }

    private void handleTimeUp() {
        disableOptions();
        Toast.makeText(this, "Time's up!", Toast.LENGTH_SHORT).show();
        
        // Show correct answer
        Question currentQuestion = questions.get(currentQuestionIndex);
        optionButtons[currentQuestion.getCorrectAnswerIndex()]
            .setBackgroundResource(R.drawable.button_correct);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextQuestion();
            }
        }, 1500);
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
    public void onClick(final View view) {
        if (!(view instanceof Button)) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int selectedIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (view.getId() == optionButtons[i].getId()) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1) return;

        Question currentQuestion = questions.get(currentQuestionIndex);
        final boolean isCorrect = selectedIndex == currentQuestion.getCorrectAnswerIndex();
        
        // Apply animation and visual feedback
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
        view.setBackgroundResource(isCorrect ? R.drawable.button_correct : R.drawable.button_wrong);
        
        // Update score and show correct answer if wrong
        if (isCorrect) {
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
            optionButtons[currentQuestion.getCorrectAnswerIndex()]
                .setBackgroundResource(R.drawable.button_correct);
        }

        disableOptions();

        // Delay next question to show the feedback
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextQuestion();
            }
        }, 1500);
    }

    private void showResult() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed");
        builder.setMessage(String.format("Your score: %d out of %d\nAccuracy: %.1f%%", 
            score, questions.size(), (score * 100.0f / questions.size())));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                navigateToWelcome();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(QuizActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
