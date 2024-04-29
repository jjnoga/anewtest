package ASSIGNMENT;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ClientThread implements Runnable {
    private static final Set<ClientThread> handlers = Collections.synchronizedSet(new HashSet<>());
    private static int currentQuestionIndex = 1;
    private static final int totalQuestions = 20;
    private static final Map<String, Integer> scores = new HashMap<>();
    private static final Set<String> respondedClients = new HashSet<>();
    private static boolean sentFinal = false;
    private static int answeringQuestion = 0;
    public static boolean answeringClientLeft = false;
    private final Socket clientSocket;
    private final UDPThread udpThread;
    private PrintWriter out;
    private DataOutputStream dos;
    private DataInputStream dis;
    private BufferedReader in;
    private int clientId;
    private static String correctAnswer = "";

    public ClientThread(Socket socket, int clientId, UDPThread udpThread) {
        this.clientSocket = socket;
        this.clientId = clientId;
        this.udpThread = udpThread;
        scores.putIfAbsent(String.valueOf(clientId), 0);
        try {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            sendClientId();
            synchronized (handlers) {
                handlers.add(this);
                sendCurrentQuestion();
            }

            String feedback;
            while ((feedback = dis.readUTF()) != null) {
                handleFeedback(feedback);
            }
        } catch (IOException e) {
            handleIOException(e);
        } finally {
            if (sentFinal) {
                killSwitch();
            }
        }
    }

    private void sendClientId() throws IOException {
        dos.writeUTF(String.valueOf(clientId));
        dos.flush();
    }

    private void handleFeedback(String feedback) throws IOException {
        if (correctAnswer.isEmpty()) {
            correctAnswer = findAnswer();
        }
        synchronized (handlers) {
            switch (feedback.trim()) {
                case "Buzz":
                    handleBuzz();
                    break;
                case "Didn't answer":
                    handleNoAnswer();
                    break;
                case "Don't know":
                    handleDontKnow();
                    break;
                default:
                    handleAnswer(feedback);
                    break;
            }
        }
    }

    private void handleBuzz() throws IOException {
        if (!udpThread.checkIfEmpty()) {
            System.out.println("not waiting");
            return;
        }
        System.out.println("waiting");
        while (udpThread.checkIfEmpty()) {
            // Wait until UDPThread is not empty
        }
        handleBuzzAction();
    }

    private void handleBuzzAction() throws IOException {
        String firstClientId = udpThread.firstInLine();
        if (String.valueOf(clientId).equals(firstClientId)) {
            System.out.println("sending ack to " + firstClientId);
            answeringQuestion = clientId;
            dos.writeUTF("ack");
            dos.flush();
        } else {
            System.out.println("sending nack to " + clientId);
            dos.writeUTF("nack");
            dos.flush();
        }
    }

    private void handleNoAnswer() throws IOException {
        System.out.println("Didn't answer so get penalized");
        updateScore(clientId, "Penalize");
        respondedClients.add(String.valueOf(clientId));
        if (currentQuestionIndex == totalQuestions && respondedClients.size() == handlers.size()) {
            sendFinishMessage();
            return;
        }
        udpThread.removeID(clientId);
        if (!udpThread.checkIfEmpty()) {
            giveSecondChanceToNextClient();
        } else {
            checkIfAllResponded();
        }
    }

    private void handleDontKnow() throws IOException {
        respondedClients.add(String.valueOf(clientId));
        if (currentQuestionIndex == totalQuestions && respondedClients.size() == handlers.size()) {
            sendFinishMessage();
            return;
        }
        System.out.println("didn't answer " + clientId);
        checkIfAllResponded();
    }

    private void handleAnswer(String feedback) throws IOException {
        if (correctAnswer.equals(feedback.trim())) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer(feedback);
        }
    }

    private void handleCorrectAnswer() throws IOException {
        System.out.println("This is the correct answer so moving on to the next one");
        updateScore(clientId, "Correct");
        if (currentQuestionIndex == totalQuestions) {
            handleEndGame();
        } else {
            handleNext();
        }
    }

    private void handleWrongAnswer(String feedback) throws IOException {
        System.out.println("WRONG ANSWER!!!!! so moving on to the next one");
        updateScore(clientId, "Wrong");
        respondedClients.add(String.valueOf(clientId));
        if (currentQuestionIndex == totalQuestions && respondedClients.size() == handlers.size()) {
            sendFinishMessage();
            return;
        }
        udpThread.removeID(clientId);
        if (!udpThread.checkIfEmpty()) {
            giveSecondChanceToNextClient();
        } else {
            checkIfAllResponded();
        }
    }

    private void giveSecondChanceToNextClient() throws IOException {
        String nextClientId = udpThread.firstInLine();
        if (nextClientId != null) {
            System.out.println("Giving a second chance to client: " + nextClientId);
            sendAckToSecondClient(nextClientId);
        } else {
            System.out.println("No more clients in line.");
            handleNext();
        }
    }

    private void sendAckToSecondClient(String clientId) throws IOException {
        ClientThread clientHandler = findClientHandlerById(clientId);
        answeringQuestion = Integer.parseInt(clientId);
        if (clientHandler != null) {
            clientHandler.dos.writeUTF("ack");
            clientHandler.dos.flush();
        } else {
            System.err.println("ClientHandler not found for ID: " + clientId);
        }
    }

    private ClientThread findClientHandlerById(String clientId) {
        synchronized (handlers) {
            for (ClientThread handler : handlers) {
                if (String.valueOf(handler.clientId).equals(clientId)) {
                    return handler;
                }
            }
        }
        return null;
    }

    private void handleEndGame() throws IOException {
        respondedClients.add(String.valueOf(clientId));
        answeringClientLeft = true;
        sendFinishMessage();
    }

    private void checkIfAllResponded() throws IOException {
        System.out.println("res: " + respondedClients.size());
        System.out.println("han: " + handlers.size());
        if (respondedClients.size() == handlers.size() && currentQuestionIndex != totalQuestions) {
            correctAnswer = "";
            respondedClients.clear();
            handleNext();
        }
    }

    private void updateScore(int clientId, String check) throws IOException {
        int currentScore = scores.getOrDefault(String.valueOf(clientId), 0);
        switch (check) {
            case "Correct":
                currentScore += 10;
                dos.writeUTF("Correct");
                System.out.println("currentScoreCorrect: " + currentScore);
                break;
            case "Wrong":
                currentScore -= 10;
                dos.writeUTF("Wrong");
                System.out.println("currentScoreWrong: " + currentScore);
                break;
            case "Penalize":
                currentScore -= 20;
                dos.writeUTF("Penalize");
                System.out.println("currentScorePenalize: " + currentScore);
                break;
        }
        System.out.println(currentScore);
        scores.put(String.valueOf(clientId), currentScore);
        dos.writeUTF("UPDATE");
        dos.writeUTF(String.valueOf(currentScore));
        dos.writeUTF(check.equals("Penalize") ? "Timer ran out" : check);
        dos.flush();
    }

    private void handleNext() throws IOException {
        synchronized (ClientThread.class) {
            correctAnswer = "";
            answeringQuestion = 0;
            respondedClients.clear();
            udpThread.removeClients();
            currentQuestionIndex++;
            for (ClientThread handler : handlers) {
                handler.sendCurrentQuestion();
            }
        }
    }

    private void sendFinishMessage() throws IOException {
        int highestScore = scores.values().stream().max(Integer::compare).orElse(0);
        synchronized (ClientThread.class) {
            System.out.println("Sending finish message");
            if (respondedClients.size() >= handlers.size() || answeringClientLeft) {
                Set<String> winners = scores.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(highestScore))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                for (ClientThread handler : handlers) {
                    DataOutputStream handlerDos = handler.dos;
                    handlerDos.writeUTF("FINISHED");
                    handlerDos.writeUTF(winners.contains(String.valueOf(handler.clientId))
                            ? "Game Finished, YOU WON!!"
                            : "Game Finished, YOU LOST!!");
                    handlerDos.flush();
                }
                printScoresInDescendingOrder();
                sentFinal = true;
            }
        }
    }

    private String findAnswer() throws IOException {
        String currentPath = new java.io.File(".").getCanonicalPath();
        String questionFilePath = currentPath + "/ASSIGNMENT 2/question" + currentQuestionIndex + ".txt";
        String answer = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(questionFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Correct: ")) {
                    answer = line.replace("Correct: ", "");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the question file: " + e.getMessage());
        }
        return answer;
    }

    private void sendCurrentQuestion() throws IOException {
        String currentPath = new java.io.File(".").getCanonicalPath();
        System.out.println("Current dir:" + currentPath);
        String questionFilePath = currentPath + "/ASSIGNMENT 2/question" + currentQuestionIndex + ".txt";
        byte[] fileContent = Files.readAllBytes(Paths.get(questionFilePath));
        dos.writeUTF("Next Question");
        dos.writeInt(fileContent.length);
        dos.write(fileContent);
        dos.flush();
    }

    private void handleIOException(IOException e) {
        Iterator<ClientThread> iterator = handlers.iterator();
        while (iterator.hasNext()) {
            ClientThread handler = iterator.next();
            if (handler.clientId == clientId) {
                udpThread.removeID(clientId);
                respondedClients.remove(String.valueOf(clientId));
                scores.remove(String.valueOf(clientId));
                iterator.remove();
                if (currentQuestionIndex == totalQuestions) {
                    try {
                        sendFinishMessage();
                    } catch (IOException e1) {
                        System.out.println("Error occurred in sending win message " + e1);
                    }
                }
                if (respondedClients.size() == handlers.size()) {
                    try {
                        handleNext();
                    } catch (IOException e1) {
                        System.out.println("Error occurred in next question " + e1);
                    }
                }
                break;
            }
        }
        if (answeringQuestion == clientId) {
            try {
                scores.remove(String.valueOf(clientId));
                if (!udpThread.checkIfEmpty()) {
                    udpThread.removeID(clientId);
                    giveSecondChanceToNextClient();
                } else {
                    if (currentQuestionIndex == totalQuestions) {
                        sendFinishMessage();
                    } else {
                        checkIfAllResponded();
                    }
                }
            } catch (IOException e1) {
                System.out.println("Error occurred in next question " + e1);
            }
        }
        System.err.println("Error in communication with client " + clientId + ": " + e.getMessage());
    }

    private void killSwitch() {
        try {
            terminate();
            handlers.clear();
            if (clientSocket != null) clientSocket.close();
            if (out != null) out.close();
            if (dos != null) dos.close();
            if (in != null) in.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error closing resources for client " + clientId + ": " + e.getMessage());
        }
    }

    private void terminate() throws IOException {
        synchronized (ClientThread.class) {
            for (ClientThread handler : handlers) {
                DataOutputStream handlerDos = handler.dos;
                handlerDos.writeUTF("TERMINATE");
            }
        }
    }

    public void printScoresInDescendingOrder() {
        scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out
                        .println("Client " + entry.getKey() + " scored " + entry.getValue() + " points"));
    }

	public void sendAnswerFeedback(String string) {
		// TODO Auto-generated method stub
		
	}

	public void sendBuzz() {
		// TODO Auto-generated method stub
		
	}
}
