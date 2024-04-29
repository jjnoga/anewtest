package ASSIGNMENT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPThread implements Runnable {
    private DatagramSocket socket;
    private final Queue<String> queue;

    public UDPThread(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String clientId = new String(packet.getData(), 0, packet.getLength()).trim();
                synchronized (queue) {
                    if (!queue.contains(clientId)) {
                        queue.add(clientId);
                        System.out.println("Client " + clientId + " added to queue. Queue size is " + queue.size());
                    }
                }
                System.out.println("Queue size is " + queue.size());
            } catch (IOException e) {
                System.out.println("UDP Thread Error: " + e.getMessage());
            }
        }
    }

    public synchronized String firstInLine() {
        return queue.peek();
    }

    public synchronized void removeClients() {
        queue.clear();
    }

    public synchronized boolean checkIfEmpty() {
        return queue.isEmpty();
    }

    public synchronized void removeID(int clientID) {
        String ID = String.valueOf(clientID);
        queue.remove(ID);
    }
}
