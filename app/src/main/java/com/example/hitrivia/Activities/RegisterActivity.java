package com.example.hitrivia.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hitrivia.R;
import com.example.hitrivia.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    public final String MY_PREFS_NAME = "PrefFile";
    private FirebaseAuth mAuth;

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private LinearLayout registerButtonHolder;

    private TextView userName;
    private TextView userLastName;
    private TextView userEmail;
    private TextView userPassword;
    private TextView userId;


    private User newUser;
    private String userEmailStr;
    private String userPasswordStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        progressBarHolder = findViewById(R.id.progressBarHolder);
        registerButtonHolder = findViewById(R.id.registerButtonHolder);
    }

    public void registerPress(View view) {
        userName = findViewById(R.id.editTextName);
        userLastName = findViewById(R.id.editTextLastName);
        userEmail = findViewById(R.id.editTextEmail);
        userPassword = findViewById(R.id.editTextRegisterPassword);
        userId = findViewById(R.id.editTextRegisterId);

        if (userName.getText().toString().isEmpty() || userLastName.getText().toString().isEmpty() || userId.getText().toString().isEmpty() || userEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Fill All Fields.", Toast.LENGTH_SHORT).show();
        } else {
            newUser = new User(userName.getText().toString(), userLastName.getText().toString(), userId.getText().toString(), userEmail.getText().toString());
            userEmailStr = newUser.getEmail();
            userPasswordStr = userPassword.getText().toString();
            new registerTask().execute();
        }
    }

    private void registerComplete(String name) {
        Intent intent = new Intent(this, MenuActivity.class);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Name", name);
        editor.putInt("HighScore", 0);
        editor.apply();
        // Finish task and remove progress bar
        finishTask();
        startActivity(intent);
    }

    private void disableInput() {
        userName.setEnabled(false);
        userLastName.setEnabled(false);
        userEmail.setEnabled(false);
        userPassword.setEnabled(false);
        userId.setEnabled(false);
    }

    private void enableInput() {
        userName.setEnabled(true);
        userLastName.setEnabled(true);
        userEmail.setEnabled(true);
        userPassword.setEnabled(true);
        userId.setEnabled(true);
    }

    // The registerTask onPostExecute
    private void finishTask() {
        enableInput();
        outAnimation = new AlphaAnimation(0.2f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
        registerButtonHolder.setVisibility(View.VISIBLE);
    }

    private class registerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            disableInput();
            inAnimation = new AlphaAnimation(0f, 0.2f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            registerButtonHolder.setVisibility(View.GONE);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAuth.createUserWithEmailAndPassword(userEmailStr, userPasswordStr)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                String uid = user.getUid();
                                // Enter user info to database
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("Users").child(uid);

                                myRef.setValue(newUser);
                                registerComplete(newUser.getFirstName());
                            } else {
                                // Finish task and remove progress bar
                                finishTask();

                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "Authentication failed. Wrong email or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return null;
        }
    }
}
