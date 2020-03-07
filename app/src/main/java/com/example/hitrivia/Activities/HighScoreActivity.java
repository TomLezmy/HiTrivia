package com.example.hitrivia.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.example.hitrivia.R;
import com.example.hitrivia.Classes.SortByScore;
import com.example.hitrivia.Classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class HighScoreActivity extends AppCompatActivity {

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private ScrollView scrollView;

    private ArrayList<User> users;
    private User[] sortedUserArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        progressBarHolder = findViewById(R.id.progressBarHolder);
        scrollView = findViewById(R.id.scrollView);
        users = new ArrayList<>();

        // Get All users from database ordered by score
        new getUsersTask().execute();
    }

    private void fillTable(){
        // Creating the table rows and textviews to display
        TableLayout tableLayout = findViewById(R.id.highScoreTableLayout);
        tableLayout.removeAllViews();
        TableRow topTableRow = new TableRow  (this);
        topTableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        topTableRow.setBackgroundResource(R.drawable.table_header_boreder);
        tableLayout.addView(topTableRow);

        TextView topTextView = new TextView(this);
        topTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.4f));
        topTextView.setText("Rank");
        topTextView.setTextSize(30f);
        topTextView.setGravity(Gravity.CENTER);
        topTextView.setTypeface(null, Typeface.BOLD);
        topTextView.setBackgroundResource(R.drawable.table_header_boreder);
        topTableRow.addView(topTextView);

        topTextView = new TextView(this);
        topTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
        topTextView.setText("Name");
        topTextView.setTextSize(30f);
        topTextView.setGravity(Gravity.CENTER);
        topTextView.setTypeface(null, Typeface.BOLD);
        topTextView.setBackgroundResource(R.drawable.table_header_boreder);
        topTableRow.addView(topTextView);

        topTextView = new TextView(this);
        topTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
        topTextView.setText("Score");
        topTextView.setTextSize(30f);
        topTextView.setGravity(Gravity.CENTER);
        topTextView.setTypeface(null, Typeface.BOLD);
        topTextView.setBackgroundResource(R.drawable.table_header_boreder);
        topTableRow.addView(topTextView);

        // Adding users to table
        for (int i = 0; i < sortedUserArray.length; i++){
            TableRow tb = new TableRow  (this);
            tb.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            tb.setBackgroundResource(R.drawable.table_border);
            tableLayout.addView(tb);

            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.4f));
            tv.setText(Integer.toString(i + 1));
            tv.setTextSize(24f);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.table_border);
            tb.addView(tv);

            tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
            tv.setText(sortedUserArray[i].getFirstName());
            tv.setTextSize(24f);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.table_border);
            tb.addView(tv);

            tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
            tv.setText(Integer.toString(sortedUserArray[i].getHighScore()));
            tv.setTextSize(24f);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.table_border);
            tb.addView(tv);
        }

        // Adding return button
        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        btn.setBackgroundResource(R.drawable.button1_clickable);
        btn.setText("Back");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
            }
        });
        tableLayout.addView(btn);
    }

    // The getUsersTask onPostExecute
    private void finishTask() {
        outAnimation = new AlphaAnimation(0.2f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private class getUsersTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 0.2f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Users");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot allUsers: dataSnapshot.getChildren())
                    {
                        User user = allUsers.getValue(User.class);
                        users.add(user);
                    }
                    sortedUserArray = new User[users.size()];
                    sortedUserArray = users.toArray(sortedUserArray);
                    // Sort user array by high score
                    Arrays.sort(sortedUserArray,new SortByScore());
                    finishTask();
                    fillTable();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                }
            });
            return null;
        }
    }
}
