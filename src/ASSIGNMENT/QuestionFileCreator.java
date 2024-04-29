package ASSIGNMENT;

import java.io.File;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;

public class QuestionFileCreator {

    public static void main(String[] args) {
        // Create 20 question files
        createQuestionFile("question1.txt", "What is the capital of France?", "A. Paris", "B. Rome", "C. London", "D. Berlin", "A");
        createQuestionFile("question2.txt", "What is the largest ocean in the world?", "A. Pacific Ocean", "B. Atlantic Ocean", "C. Indian Ocean", "D. Arctic Ocean", "A");
        createQuestionFile("question3.txt", "Who developed the theory of relativity?", "A. Isaac Newton", "B. Albert Einstein", "C. Galileo Galilei", "D. Stephen Hawking", "B");
        createQuestionFile("question4.txt", "What is the chemical symbol for water?", "A. Wo", "B. H2O", "C. Co", "D. NaCl", "B");
        createQuestionFile("question5.txt", "Which planet is known as the Red Planet?", "A. Venus", "B. Mars", "C. Jupiter", "D. Saturn", "B");
        createQuestionFile("question6.txt", "What is the largest mammal in the world?", "A. Elephant", "B. Blue Whale", "C. Giraffe", "D. Hippopotamus", "B");
        createQuestionFile("question7.txt", "Who painted the Mona Lisa?", "A. Leonardo da Vinci", "B. Pablo Picasso", "C. Vincent van Gogh", "D. Michelangelo", "A");
        createQuestionFile("question8.txt", "What is the chemical symbol for gold?", "A. Go", "B. Ag", "C. Au", "D. Pt", "C");
        createQuestionFile("question9.txt", "What is the longest river in the world?", "A. Nile", "B. Amazon", "C. Yangtze", "D. Mississippi", "A");
        createQuestionFile("question10.txt", "Who wrote the play 'Hamlet'?", "A. William Shakespeare", "B. Charles Dickens", "C. Jane Austen", "D. Mark Twain", "A");
        createQuestionFile("question11.txt", "What is the chemical symbol for oxygen?", "A. O", "B. Ox", "C. Co", "D. O2", "A");
        createQuestionFile("question12.txt", "What is the hardest natural substance on Earth?", "A. Gold", "B. Diamond", "C. Iron", "D. Titanium", "B");
        createQuestionFile("question13.txt", "Who wrote 'The Great Gatsby'?", "A. F. Scott Fitzgerald", "B. Ernest Hemingway", "C. J. D. Salinger", "D. William Faulkner", "A");
        createQuestionFile("question14.txt", "What is the chemical symbol for carbon?", "A. C", "B. Co", "C. Ca", "D. Cr", "A");
        createQuestionFile("question15.txt", "What is the largest organ in the human body?", "A. Liver", "B. Heart", "C. Skin", "D. Brain", "C");
        createQuestionFile("question16.txt", "Who discovered penicillin?", "A. Alexander Fleming", "B. Louis Pasteur", "C. Marie Curie", "D. Dmitri Mendeleev", "A");
        createQuestionFile("question17.txt", "What is the chemical symbol for sodium?", "A. Sn", "B. So", "C. Na", "D. Sa", "C");
        createQuestionFile("question18.txt", "What is the smallest bone in the human body?", "A. Femur", "B. Tibia", "C. Stapes", "D. Humerus", "C");
        createQuestionFile("question19.txt", "Who painted 'Starry Night'?", "A. Vincent van Gogh", "B. Leonardo da Vinci", "C. Claude Monet", "D. Pablo Picasso", "A");
        createQuestionFile("question20.txt", "What is the chemical symbol for silver?", "A. Sv", "B. Si", "C. Ag", "D. Au", "C");
    }

    private static void createQuestionFile(String fileName, String question, String... optionsAndAnswer) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Question: " + question + "\n");
            for (String option : optionsAndAnswer) {
                writer.write(option + "\n");
            }
            writer.write("Correct: " + optionsAndAnswer[optionsAndAnswer.length - 1] + "\n");
            System.out.println("File created successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }
}
