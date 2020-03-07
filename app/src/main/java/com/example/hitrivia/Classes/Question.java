package com.example.hitrivia.Classes;

import java.util.ArrayList;

public class Question {
    private String question;
    private String options;
    private int answer;
    private int numOfOptions;
    private String optionsArr[];

    // Empty constructor for firebase read
    public Question(){
    }

    public Question(String question, String options, int answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;

        String[] optionsSplit = options.split("/");
        numOfOptions = optionsSplit.length;
        optionsArr = new String[numOfOptions];
        for (int i = 0; i < numOfOptions; i++){
            optionsArr[i] = optionsSplit[i];
        }
    }

    public void splitOptions(){
        String[] optionsSplit = options.split("/");
        numOfOptions = optionsSplit.length;
        optionsArr = new String[numOfOptions];
        for (int i = 0; i < numOfOptions; i++){
            optionsArr[i] = optionsSplit[i];
        }
    }

    // Getters for firebase read
    public String getQuestion() {
        return question;
    }
    public String getOptions() {
        return options;
    }
    public int getAnswer() {
        return answer;
    }

    public int getNumOfOptions() {
        return numOfOptions;
    }

    public String[] getOptionsArr() {
        return optionsArr;
    }

    // Turns Question to List [Question,option1,option2..., Answer]
    public ArrayList<String> toList(){
        ArrayList<String> result = new ArrayList<>();
        result.add(question);
        for (int i = 0; i < numOfOptions; i++){
            result.add(optionsArr[i]);
        }
        result.add(Integer.toString(answer));

        return  result;
    }

}
