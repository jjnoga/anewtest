package ASSIGNMENT;

import java.io.File;
import java.io.IOException;

public class CreateQuestionFile {
    public static void main(String[] args) {
        String directoryPath = "/Users/abhignanreddy/Desktop/CN/project2/ASSIGNMENT/questions/";
        String fileName = "question.txt";

        File file = new File(directoryPath + fileName);

        try {
            if (file.createNewFile()) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }
}
