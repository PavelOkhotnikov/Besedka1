package com.company.server;

import com.company.server.Auth;
import com.company.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

class Main {
    private Vector<ClientHandler> clients;

    Main() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            Auth.connection();
            server = new ServerSocket(8189);
            System.out.println("Сервер работает");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключён");
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Auth.disconnect();
        }
    }

    boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientList();
    }

    void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();
    }

    void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {
                o.sendMsg(msg);
            }
        }
    }

    void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMsg("от " + from.getNick() + ": " + msg);
                from.sendMsg("кому " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("Клиент " + nickTo + " не найден в чате");
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientlist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }

        String out = sb.toString();

        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }
}
