package com.example.hitrivia.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.hitrivia.R;


public class MenuActivity extends AppCompatActivity {
    public final String MY_PREFS_NAME="PrefFile";
    private TextView textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Get user name and greet
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString("Name", "");
        textField = findViewById(R.id.textViewGreet);
        textField.setText("Hello " + name);
    }

    public void logOut(View view) {
        // Remove user credentials and return to main screen
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.putBoolean("Remember",false);
        editor.apply();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void highScore(View view) {
        Intent intent = new Intent(this, HighScoreActivity.class);
        startActivity(intent);
    }

    public void play(View view) {
        startGame();
    }

    public void startGame(){
        Intent intent = new Intent(this,GameActivity.class);
        startActivity(intent);
    }
}
