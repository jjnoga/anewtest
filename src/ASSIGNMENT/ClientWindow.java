package ASSIGNMENT;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.*;

public class ClientWindow implements ActionListener {
    private JButton pollButton;
    private JLabel clientIDLabel;
   
    private JButton submitButton;
    private JRadioButton options[];
    private ButtonGroup optionGroup;
    private JLabel questionLabel;
   
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel currentScoreLabel;
    private JLabel clientIDLable;
    private TimerTask clock;
    private Timer timer;
    private Client client; // Change made here
    public boolean canAnswer = false;

    private JFrame window;

    private static SecureRandom random = new SecureRandom();

    public ClientWindow(Client client) { // Change made here
        this.client = client; // Change made here
        window = new JFrame("Trivia");
        questionLabel = new JLabel("Q1. This is a sample question"); // represents the question
        window.add(questionLabel);
        questionLabel.setBounds(10, 5, 450, 100);

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int index = 0; index < options.length; index++) {
            options[index] = new JRadioButton("Option " + (index + 1)); // represents an option
            // if a radio button is clicked, the event would be thrown to this class to
            // handle
            options[index].addActionListener(this);
            options[index].setBounds(10, 110 + (index * 20), 350, 20);
            window.add(options[index]);
            optionGroup.add(options[index]);
        }


        timerLabel = new JLabel("TIMER"); // represents the countdown shown on the window
        timerLabel.setBounds(250, 250, 100, 20);
        window.add(timerLabel);

        clientIDLabel = new JLabel("Client ID:"); // represents the score
        clientIDLabel.setBounds(270, 20, 100, 20);
        window.add(clientIDLabel);

        clientIDLable = new JLabel("1"); // represents the score
        clientIDLable.setBounds(335, 20, 100, 20);
        window.add(clientIDLable);

        scoreLabel = new JLabel("SCORE: "); // represents the score
        scoreLabel.setBounds(50, 250, 80, 20);
        window.add(scoreLabel);

        currentScoreLabel = new JLabel("0");
        currentScoreLabel.setBounds(100, 250, 25, 20);
        window.add(currentScoreLabel);

        pollButton = new JButton("Poll"); // button that use clicks/ like a buzzer
        pollButton.setBounds(10, 300, 100, 20);
        pollButton.addActionListener(this); // calls actionPerformed of this class
        window.add(pollButton);

        submitButton = new JButton("Submit"); // button to submit their answer
        submitButton.setBounds(200, 300, 100, 20);
        submitButton.addActionListener(this); // calls actionPerformed of this class
        window.add(submitButton);

        window.setSize(450, 400);
        window.setBounds(50, 50, 450, 400);
        window.setLayout(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
    }

    // this method is called when you check/uncheck any radio button
    // this method is called when you press either of the buttons- submit/poll
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Poll".equals(e.getActionCommand())) {
            client.sendBuzz(); // Change made here
            if(timer != null) {
                timer.cancel();
                startTimer(10);
                pollButton.setEnabled(false);
                submitButton.setEnabled(true);
                for (JRadioButton option : options) {
                    option.setEnabled(true);
                }
            }
        } else if ("Submit".equals(e.getActionCommand())) {
            String selectedOption = getSelectedOptionIndex();
            if (selectedOption != "Nothing Selected") {
                timer.cancel();
                timer = null;
                for (JRadioButton option : options) {
                    option.setEnabled(false);
                }
                System.out.println("picked the option " + selectedOption);
                client.sendAnswerFeedback(selectedOption); // Change made here
            } else {
               
            }
        }
    }

    public void updateClientId(String id) {
        clientIDLable.setText(id);
    }

    public void updateScore(String score, String status) { //, String correctOrWrong
        int currScore = Integer.parseInt(score);
        System.out.println("reached updateScore");
        if (status.equals("Correct")) {
            currentScoreLabel.setText("" + (currScore + 10));
        } else if (status.equals("Wrong")) {
            currentScoreLabel.setText("" + (currScore - 10));
        } else if (status.equals("Timer ran out")) {
            currentScoreLabel.setText("" + (currScore - 20));
        }
    }

    private String getSelectedOptionIndex() {
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                return String.valueOf(i + 1);
            }
        }
        return "Nothing Selected";
    }

    public void startTimer(int duration) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        clock = new TimerCode(duration);
        timer.schedule(clock, 0, 1000);
    }

    // this class is responsible for running the timer on the window
    private class TimerCode extends TimerTask {
        private int duration;

        public TimerCode(int duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                if (duration < 0) {
                    if (submitButton.isEnabled() && canAnswer) {
                        client.sendAnswerFeedback("Didn't answer"); // Change made here
                        updateScore(currentScoreLabel.getText(), "Timer ran out");
                    } else {
                        enablePoll(false);
                        client.sendAnswerFeedback("Don't know"); // Change made here
                        updateScore(currentScoreLabel.getText(), "Timer ran out");
                    }
                    timerLabel.setText("Timer expired");
                    pollButton.setEnabled(false);
                    this.cancel();
                    return;
                }
                if (duration < 6) {
                    timerLabel.setForeground(Color.red);
                } else {
                    timerLabel.setForeground(Color.black);
                }
                timerLabel.setText("Time: " + duration + "s");
                duration--;
            });
        }
    }

    public void updateQuestion(String text) {
        questionLabel.setText(text);
    }

    public void setOptions(String[] optionsText) {
        for (int i = 0; i < options.length && i < optionsText.length; i++) {
            options[i].setText(optionsText[i]);
            options[i].setVisible(true);
        }
    }

    public void disableOptions() {
        canAnswer = true;
        for (int i = 0; i < options.length; i++) {
            options[i].setEnabled(false);
        }
    }

    public void enableOptions(boolean check) {
        for (int i = 0; i < options.length; i++) {
            options[i].setEnabled(check);
        }
    }

    public void enableSubmit(boolean enable) {
        submitButton.setEnabled(enable);
    }

    public void enablePoll(boolean enable) {
        pollButton.setEnabled(enable);
    }

    public void finished(String message) {
        JOptionPane.showMessageDialog(window, message);
    }

    
}
