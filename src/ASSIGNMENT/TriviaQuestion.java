package ASSIGNMENT;

import java.util.ArrayList;
import java.util.List;

public class TriviaQuestion {
    private String question;
    private List<String> options = new ArrayList<>();
    private String correctAnswer;

    // Constructors
    public TriviaQuestion() {
    }

    public TriviaQuestion(String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options.addAll(options);
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return new ArrayList<>(options); // Return a copy to ensure immutability
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    // Setters
    public void setQuestion(String question) {
        this.question = question;
    }

    public void addOption(String option) {
        options.add(option);
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    // toString method
    @Override
    public String toString() {
        return "Question: " + question + "\nOptions: " + options + "\nCorrect Answer: " + correctAnswer;
    }
}
