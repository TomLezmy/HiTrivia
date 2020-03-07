package com.example.hitrivia.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hitrivia.Classes.Question;
import com.example.hitrivia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public final String MY_PREFS_NAME="PrefFile";
    private final int LENGTH_BETWEEN_QUESTIONS = 6;
    private final int QUESTIONS_PER_LEVEL = 5;
    private final int NUMBER_OF_QUESTIONS = 20;

    private String correctAnswer = "";
    private ArrayList<ArrayList<String>> allQuestions;
    private ArrayList<String> currentQuestion;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int level = 0;
    private TextView questionText;
    private Button[] optionButtons;
    private Button exitButton;
    private TextView scoreText;
    private TextView levelText;
    private FirebaseAuth mAuth;
    private Animation blinkAnimation;
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Initailize blinking animation
        blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        // Set blinking speed
        blinkAnimation.setDuration(100);
        blinkAnimation.setStartOffset(20);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(Animation.INFINITE);

        questionText = findViewById(R.id.textViewQuest);
        optionButtons = new Button[4];
        optionButtons[0] = findViewById(R.id.btnAns1);
        optionButtons[1]= findViewById(R.id.btnAns2);
        optionButtons[2] = findViewById(R.id.btnAns3);
        optionButtons[3] = findViewById(R.id.btnAns4);
        exitButton = findViewById(R.id.btnExit);
        scoreText = findViewById(R.id.textViewScore);
        levelText = findViewById(R.id.textViewLevel);
        allQuestions = new ArrayList<>();
        currentQuestion = new ArrayList<>();
        progressBarHolder = findViewById(R.id.progressBarHolder);
        new getDBTask().execute();
    }

    private void nextQuestion(Boolean isNextLevel){
        if (score == NUMBER_OF_QUESTIONS){
            endGame(true);
            return;
        }
        score++;
        scoreText.setText("Score: " + score);
        if (isNextLevel) {
            level++;
            levelText.setText("Level: " + level);
        }
        getQuestion(isNextLevel);
    }

    private void getQuestion(Boolean isNextLevel){
        ArrayList<String> questions;

        if (isNextLevel){
            // Get the new level questions and mixes the order
            questions = allQuestions.get(score / QUESTIONS_PER_LEVEL);
            currentQuestion = randomizeQuestions(questions);
        }

        questionText.setText(currentQuestion.get(currentQuestionIndex));
        setQuestion(new String[]{currentQuestion.get(currentQuestionIndex + 1), currentQuestion.get(currentQuestionIndex + 2),currentQuestion.get(currentQuestionIndex + 3),currentQuestion.get(currentQuestionIndex + 4),currentQuestion.get(currentQuestionIndex + 5)});

        // Questions are stored as [Q,O1,O2,O3,O4,A....]
        currentQuestionIndex += LENGTH_BETWEEN_QUESTIONS;
        if (currentQuestionIndex >= QUESTIONS_PER_LEVEL * LENGTH_BETWEEN_QUESTIONS) {
            currentQuestionIndex = 0;
        }
    }

    private ArrayList<String> randomizeQuestions(ArrayList<String> questions){
        ArrayList<String> randomizedQuestions = new ArrayList<>();

        Random rand = new Random();
        int[] numbersToRand = new int[] {0,1,2,3,4};
        for (int i = numbersToRand.length - 1; i > 0; i--)
        {
            int index = rand.nextInt(i + 1);
            int temp = numbersToRand[index];
            numbersToRand[index] = numbersToRand[i];
            numbersToRand[i] = temp;
        }

        for (int i = 0; i < QUESTIONS_PER_LEVEL; i++) {
            int questionIndex = numbersToRand[i] * LENGTH_BETWEEN_QUESTIONS;
            for (int j = 0; j < LENGTH_BETWEEN_QUESTIONS; j++){
                // Puts question,options and answer
                randomizedQuestions.add(questions.get(questionIndex + j));
            }
        }

        return randomizedQuestions;
    }

    private void setQuestion(String[] question){
        // Randomize the answers
        Random rand = new Random();
        int[] numbersToRand = new int[] {0,1,2,3};
        for (int i = numbersToRand.length - 1; i > 0; i--)
        {
            int index = rand.nextInt(i + 1);
            int temp = numbersToRand[index];
            numbersToRand[index] = numbersToRand[i];
            numbersToRand[i] = temp;
        }

        for (int i = 0; i < 4; i++) {
            if (numbersToRand[i] + 1 == Integer.parseInt(question[4])){
                correctAnswer = Integer.toString(i + 1);
            }
            optionButtons[i].setText(question[numbersToRand[i]]);
        }

        // Debug answer
        // Toast.makeText(this, correctAnswer, Toast.LENGTH_SHORT).show();
    }

    private void endGame(Boolean winGame){
        String endMessage = "";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt("HighScore", 0);
        if (score > highScore){
            // Save new high score in pref file and update firebase
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putInt("HighScore", score);
            editor.apply();
            updateScore(score);
        }

        if (winGame){
            endMessage = "You Won! Final score: " + score;
        }
        else {
            endMessage = "Game ended, your score is: " + score;
        }

        Toast.makeText(this, endMessage, Toast.LENGTH_SHORT).show();
        backToMenu();
    }

    private void updateScore(int score){
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users/" + user.getUid() + "/highScore");
        myRef.setValue(score);
        backToMenu();
    }

    private void backToMenu(){
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void onClickAnswer(View view) {
        Button btn = (Button)view;
        String btnStr = btn.toString();
        int btnStrLen = btnStr.length();
        String btnNumber = Character.toString(btnStr.charAt(btnStrLen - 2));
        if (btnNumber.equals(correctAnswer))
        {
            new correctAnswerAnimationTask(btn).execute();
        }
        else
        {
            Button correctButton = optionButtons[Integer.parseInt(correctAnswer) - 1];
            new correctAnswerAnimationTask(btn, correctButton).execute();
        }
    }

    public void onClickExit(View view) {
        backToMenu();
    }

    private void disableInput(){
        for (Button btn : optionButtons){
            btn.setEnabled(false);
        }
        exitButton.setEnabled(false);
    }

    private void enableInput(){
        for (Button btn : optionButtons){
            btn.setEnabled(true);
        }
        exitButton.setEnabled(true);
    }

    // The getDBTask onPostExecute
    private void finishTask() {
        outAnimation = new AlphaAnimation(0.2f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
        questionText.setVisibility(View.VISIBLE);
        enableInput();
    }

    private class getDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            disableInput();
            inAnimation = new AlphaAnimation(0f, 0.2f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            questionText.setVisibility(View.GONE);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Questions");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Read each level and fill questions into allQuestions
                    for (DataSnapshot levelSnapShot : dataSnapshot.getChildren()){
                        ArrayList<String> currLevelQuestionsList = new ArrayList<>();
                        for (DataSnapshot questionSnapShot : levelSnapShot.getChildren()){
                            Question currQuestion = questionSnapShot.getValue(Question.class);
                            currQuestion.splitOptions();
                            currLevelQuestionsList.addAll(currQuestion.toList());
                        }
                        allQuestions.add(currLevelQuestionsList);
                    }
                    // Remove loading and start game
                    finishTask();
                    nextQuestion(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                }
            });
            return null;
        }
    }

    private class correctAnswerAnimationTask extends AsyncTask<Void, Void, Void> {

        private Boolean isCorrectAnswer = false;
        private Button wrongBtn = null;
        private Button correctBtn;

        public correctAnswerAnimationTask(Button wrongBtn, Button correctBtn) {
            this.wrongBtn = wrongBtn;
            this.correctBtn = correctBtn;
        }

        public correctAnswerAnimationTask(Button correctBtn) {
            this.correctBtn = correctBtn;
            isCorrectAnswer = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            disableInput();
            // Paint in green and start blinking animation
            correctBtn.setBackgroundColor(Color.parseColor("#009425"));
            correctBtn.startAnimation(blinkAnimation);
            if (!isCorrectAnswer) {
                // Paint in red
                wrongBtn.setBackgroundColor(Color.parseColor("#bd0e02"));
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            correctBtn.clearAnimation();
            enableInput();
            if (isCorrectAnswer) {
                correctBtn.setBackgroundResource(R.drawable.button2_clickable);
                // If last question of the current level
                if (score % QUESTIONS_PER_LEVEL == 0){
                    nextQuestion(true);
                }
                else{
                    nextQuestion(false);
                }
            }
            else {
                endGame(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Wait for 1 second while the animation is displayed
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            return null;
        }
    }
}
