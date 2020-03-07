package com.example.hitrivia.Classes;

import java.util.Comparator;

// Class used to sort all users by high score
public class SortByScore implements Comparator<User> {
    public int compare(User a, User b)
    {
        return b.getHighScore() - a.getHighScore();
    }
}
