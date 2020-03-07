package com.example.hitrivia.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hitrivia.R;
import com.example.hitrivia.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    public final String MY_PREFS_NAME="PrefFile";
    private Switch rememberUser;
    private TextView userEmail;
    private String userEmailStr;
    private TextView userPass;
    private String userPassStr;
    private FirebaseAuth mAuth;
    private User currentUser;

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private LinearLayout loginButtonHolder;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.buttonSend);
        registerButton = findViewById(R.id.buttonRegister);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        loginButtonHolder = findViewById(R.id.loginButtonHolder);
        userEmail = findViewById(R.id.editTextEmail);
        userPass = findViewById(R.id.editTextPassword);
        rememberUser = findViewById(R.id.switchRememberUser);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        // If user picked remember me, use saved credentials to login
        boolean autoLogin = prefs.getBoolean("Remember", false);
        if (autoLogin) {
            ((TextView) findViewById(R.id.editTextEmail)).setText(prefs.getString("Email", "missing email"));
            ((TextView) findViewById(R.id.editTextPassword)).setText(prefs.getString("Password", "missing password"));
            findViewById(R.id.buttonSend).performClick();
        }
    }

    public void loginPress(View view) {
        if (userEmail.getText().toString().isEmpty() || userPass.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Fill All Fields.", Toast.LENGTH_SHORT).show();
        } else {
            userEmailStr = userEmail.getText().toString();
            userPassStr = userPass.getText().toString();
            // Check user credentials and show loading bar
            new loginTask().execute();
        }
    }

    public void registerPress(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void loginComplete() {
        Intent intent = new Intent(this, MenuActivity.class);

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("Remember", rememberUser.isChecked());
        if (rememberUser.isChecked()) {
            editor.putString("Email", userEmail.getText().toString());
            editor.putString("Password", userPass.getText().toString());
        }
        editor.putString("Name", currentUser.getFirstName());
        editor.putInt("HighScore", currentUser.getHighScore());
        editor.apply();
        // Finish task and remove progress bar
        finishTask();
        startActivity(intent);
    }

    private void disableInput() {
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
        progressBarHolder.setEnabled(false);
        userEmail.setEnabled(false);
        userPass.setEnabled(false);
        rememberUser.setEnabled(false);
    }

    private void enableInput() {
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
        progressBarHolder.setEnabled(true);
        userEmail.setEnabled(true);
        userPass.setEnabled(true);
        rememberUser.setEnabled(true);
    }

    // The loginTask onPostExecute
    private void finishTask() {
        outAnimation = new AlphaAnimation(0.2f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
        loginButtonHolder.setVisibility(View.VISIBLE);
        enableInput();
    }

    // Async task to show loading bar while user credentials are checked in the database
    private class loginTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            disableInput();
            inAnimation = new AlphaAnimation(0f, 0.2f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            loginButtonHolder.setVisibility(View.GONE);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Sign in to firebase
            mAuth.signInWithEmailAndPassword(userEmailStr, userPassStr)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Pull user name from firebase
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("Users/" + user.getUid());

                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        currentUser = dataSnapshot.getValue(User.class);
                                        loginComplete();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Failed to read value
                                    }
                                });
                            } else {
                                // Finish task and remove progress bar
                                finishTask();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "Authentication failed. Wrong username or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return null;
        }
    }
}


