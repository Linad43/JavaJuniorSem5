package org.example.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientManager implements Runnable {
    private final Socket socket;
    private String name;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " conected");
            sendClientMassage(name + " conected");
        } catch (IOException e) {
            closeEvery(socket, bufferedWriter, bufferedReader);
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " disconnected");
        sendClientMassage(name + " disconnected");
    }

    private void closeEvery(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClient();
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private void sendClientMassage(String massage) {
        String[] findClient;
        Pattern pattern = Pattern.compile("@");
        Matcher matcher = pattern.matcher(massage);
        boolean privat = false;
        if (matcher.find()) {
            privat = true;
            massage = massage.replaceAll("@", "");
        }
        findClient = massage.split(" ");
        if (privat) {
            for (ClientManager client : clients) {
                if (client.name.equals(findClient[1])) {
                    massage = massage.replaceAll(findClient[1], "(private)");
                    try {
                        if (!client.name.equals(name) && massage != null) {
                            client.bufferedWriter.write(massage);
                            client.bufferedWriter.newLine();
                            client.bufferedWriter.flush();
                        }
                    } catch (IOException e) {
                        closeEvery(socket, bufferedWriter, bufferedReader);
                    }
                    break;
                }
            }
        } else {
            for (ClientManager client : clients) {
                try {
                    if (!client.name.equals(name) && massage != null) {
                        client.bufferedWriter.write(massage);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEvery(socket, bufferedWriter, bufferedReader);
                }
            }
        }
    }

    /**
     * Чтение сообщений от клиентов
     */
    @Override
    public void run() {
        String massageFromClient;
        while (socket.isConnected()) {
            try {
                //Чтение данных
                massageFromClient = bufferedReader.readLine();
                sendClientMassage(massageFromClient);
            } catch (IOException e) {
                closeEvery(socket, bufferedWriter, bufferedReader);
                break;
            }

        }
    }
}
