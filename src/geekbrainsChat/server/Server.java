package geekbrainsChat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    private Vector<ClientHandler> clientsList;

    public Server(){
        clientsList = new Vector<>();
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            AuthService.dbConnect();
            serverSocket = new ServerSocket(8189);
            System.out.println("Server started, waiting for clients..");

            while (true) {
                socket = serverSocket.accept();
                System.out.println("Client connected..");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Server initialization error");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Socket closing error");
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Server closing error");
            }
            AuthService.dbDisconnect();
        }
    }

    public void broadcastMsg(String msg){
        for (ClientHandler o : clientsList) {
            o.sendMsg(msg + "\n");
        }
    }

    public void broadcastClientList(){
        StringBuilder strList = new StringBuilder();
        strList.append("/clientList ");
        for (ClientHandler o : clientsList) {
            strList.append(o.getUserName() + " ");
        }
        String clList = strList.toString();
        for (ClientHandler o : clientsList) {
            o.sendMsg(clList);
        }
    }

    public void sendPrivateMsg (String userSender, String userReciever, String msg) {
        for (ClientHandler o : clientsList) {
            if (o.getUserName().equals(userReciever) || o.getUserName().equals(userSender)){
                o.sendMsg(userSender + " private: " + msg + "\n");
            }
        }
    }

    public boolean isUserConnected(String userName){
        for (ClientHandler o: clientsList){
            if(o.userName.equals(userName)){
              return true;
            }
        } return false;
    }

    public void subscribe (ClientHandler client){
        clientsList.add(client);
        broadcastClientList();
    }

    public void unsubscribe (ClientHandler client){
        clientsList.remove(client);
        broadcastClientList();
    }

}
