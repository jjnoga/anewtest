package ASSIGNMENT;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static String usersIP = "";
    private Socket socket;
    private String clientId;
    private final String serverAddress;
    private final int serverPort;
    private ClientWindow clientWindow;
    private static final int UDP_PORT = 4445;
    private boolean reading = true;
    private DataInputStream dis;
    private DataOutputStream dos;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            this.clientWindow = new ClientWindow(this);
            String clientId = dis.readUTF();
            this.clientId = clientId;
            clientWindow.updateClientId(clientId); // Corrected method name
            listenForServerMessages(dis);
        } catch (IOException e) {
            System.err.println("Could not connect to the server or receive the file: " + e.getMessage());
        }
    }

    private void listenForServerMessages(DataInputStream dis) {
        new Thread(() -> {
            try {
                boolean isFinished = true;
                while (!socket.isClosed() && isFinished && reading) {
                    String response = dis.readUTF();
                    System.out.println("Response from server: " + response);
                    switch (response) {
                        case "ack":
                            handleAckResponse();
                            break;
                        case "nack":
                            handleNackResponse();
                            break;
                        case "Next Question":
                            handleNextQuestionResponse(dis);
                            break;
                        case "UPDATE":
                            handleUpdateResponse(dis);
                            break;
                        case "FINISHED":
                            handleFinishedResponse(dis);
                            break;
                        case "TERMINATE":
                            handleTerminateResponse();
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        }).start();
    }

    private void handleAckResponse() {
        clientWindow.enableOptions(true);
        clientWindow.enableSubmit(true);
        clientWindow.enablePoll(false);
        clientWindow.startTimer(10);
        System.out.println("I was first!");
    }

    private void handleNackResponse() {
        System.out.println("Not first.");
    }

    private void handleNextQuestionResponse(DataInputStream dis) throws IOException {
        System.out.println("Current Question is happening");
        int fileLength = dis.readInt();
        System.out.println("file length " + fileLength);
        clientWindow.startTimer(15);
        if (fileLength > 0) {
            byte[] content = new byte[fileLength];
            dis.readFully(content, 0, fileLength);
            String fileName = "clientQuestion.txt";
            saveToFile(fileName, content);
            displayQuestionFromFile(fileName);
            clientWindow.enableSubmit(false);
            clientWindow.enablePoll(true);
            clientWindow.disableOptions();
        }
    }

    private void handleUpdateResponse(DataInputStream dis) throws IOException {
        System.out.println("There is an update");
        String currScore = dis.readUTF();
        String correctOrWrong = dis.readUTF();
        clientWindow.enableOptions(false);
        clientWindow.enableSubmit(false);
        clientWindow.enablePoll(false);
        System.out.println("reached update handler");
        clientWindow.updateScore(currScore, correctOrWrong);
    }

    private void handleFinishedResponse(DataInputStream dis) throws IOException {
        System.out.println("Game is finished");
        String winningMessage = dis.readUTF();
        clientWindow.finished(winningMessage);
    }

    private void handleTerminateResponse() {
        boolean isFinished = false;
        System.out.println("Server has terminated the connection. Exiting...");
        closeConnection();
    }

    private void closeConnection() {
        try {
            if (dis != null)
                dis.close();
            if (dos != null)
                dos.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }

    private static void saveToFile(String fileName, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(content);
        }
    }

    private void displayQuestionFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder questionBuilder = new StringBuilder();
            ArrayList<String> options = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                // if (line.startsWith("Correct: ")) {
                //     break;
                // } else 
                if (!line.trim().isEmpty()) {
                    if (questionBuilder.length() == 0) {
                        questionBuilder.append(line);
                    } else {
                        options.add(line);
                    }
                }
            }
            this.clientWindow.updateQuestion(questionBuilder.toString());
            this.clientWindow.setOptions(options.toArray(new String[0]));

        } catch (IOException e) {
            System.err.println("Error reading the question file: " + e.getMessage());
        }
    }

    public void sendAnswerFeedback(String feedback) {
        System.out.println(feedback);
        System.out.println("Client id sending is " + clientId);
        if (dos != null) {
            try {
                dos.writeUTF(feedback);
                dos.flush();
            } catch (IOException e) {
                System.err.println("Error sending feedback: " + e.getMessage());
            }
        } else {
            System.out.println("DataOutputStream 'dos' is not initialized.");
        }
    }

    public void sendBuzz() {
        try (DatagramSocket socket = new DatagramSocket()) {
            System.out.println("Buzz has been sent");
            String message = String.valueOf(clientId);
            byte[] messageBytes = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(usersIP);
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, serverAddress, UDP_PORT);
            socket.send(packet);
            sendAnswerFeedback("Buzz");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the server's IP:");
        usersIP = scanner.nextLine();
        scanner.close();
        Client client = new Client(usersIP, 1234);
        client.connectToServer();
    }

    public void setScore(int newScore) {
        // TODO Auto-generated method stub
    }
}
