package ASSIGNMENT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TriviaQuestionLoader {
    private List<TriviaQuestion> questions = new ArrayList<>();

    public TriviaQuestionLoader(String filePath) throws IOException {
        loadQuestions(filePath);
    }

    private void loadQuestions(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get(filePath).toFile()))) {
            String line;
            TriviaQuestion question = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Question:")) {
                    question = new TriviaQuestion();
                    question.setQuestion(line.substring(9).trim());
                } else if (line.matches("^[A-D]:.*")) {
                    if (question != null) {
                        question.addOption(line.trim());
                    }
                } else if (line.startsWith("Correct: ")) {
                    if (question != null) {
                        question.setCorrectAnswer(line.substring(9).trim());
                        questions.add(question);
                        question = null;
                    }
                }
            }
        }
    }

    public TriviaQuestion getQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            return questions.get(index);
        }
        return null;
    }

    public int getTotalQuestions() {
        return questions.size();
    }
}
