package geekbrainsChat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller implements Initializable {

    @FXML
    TextArea chatArea;
    @FXML
    TextField msgField;
    @FXML
    HBox upperPan;
    @FXML
    HBox bottomPan;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    ListView <String> clientList;

    final String IP = "localhost";
    final int PORT = 8189;
    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;

    private AtomicBoolean isAuthorised = new AtomicBoolean();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setAuthorised(false);
        try {
            clientSocket = new Socket(IP, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread timer = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isAuthorised.get()) {
                    try {
                        clientSocket.close();
                        System.out.println("Socket closed by timeout");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Socket closing failed");
                    }
                }
            }
        });
        timer.setDaemon(true);
        timer.start();
    }

    public void setAuthorised(boolean isAuthorised) {
        this.isAuthorised.set(isAuthorised);
        if(!isAuthorised){
            upperPan.setVisible(true);
            upperPan.setManaged(true);
            bottomPan.setVisible(false);
            bottomPan.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPan.setVisible(false);
            upperPan.setManaged(false);
            bottomPan.setVisible(true);
            bottomPan.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
        }
    }

    public void tryToAuth(){
            connect();
            try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            setAuthorised(false);

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                        try {
                            while(true){
                                String msg = in.readUTF();
                                if(msg.equals("/authOK")){
                                    setAuthorised(true);
                                    break;
                                } else{
                                    chatArea.appendText(msg);
                                }
                            }

                            while (true) {
                                String msg = in.readUTF();
                                if(msg.startsWith("/")){
                                    if(msg.startsWith("/clientList ")){
                                        String [] tokens = msg.split(" ");

                                        Platform.runLater(() -> {
                                            clientList.getItems().clear();
                                            for(int i = 1; i < tokens.length; i++) {
                                                clientList.getItems().add(tokens[i]);
                                            }
                                        });
                                    }
                                    if(msg.equals("/session finished")) break;
                                } else {chatArea.appendText(msg + "\n");}
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close();
                                System.out.println("Socket closed by client");
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("Socket closing failed");
                            }
                            setAuthorised(false);
                        }
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText()); //for private msg use "/m userName text" construction
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
