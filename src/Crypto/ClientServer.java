package Crypto;

import javax.swing.*;

//Class to precise who is connected : Client or Server
public class ClientServer {

    public static void main(String [] args){

        Object[] selectioValues = { "Client","Server"};
        String initialSection = "Client";

        Object selection = JOptionPane.showInputDialog(null, "Login as : ", "MyChatApp", JOptionPane.QUESTION_MESSAGE, null, selectioValues, initialSection);
        if(selection.equals("Server")){
            String[] arguments = new String[] {};

            new MultiThreadChatServerSync().main(arguments);
        }else {
            if (selection.equals("Client")) {
                String key = JOptionPane.showInputDialog("Enter Secret");
                String IPServer = JOptionPane.showInputDialog("Enter the Server ip address");
                if (IPServer == null) {
                    IPServer = "127.0.0.1";
                }
                String[] arguments = new String[]{IPServer, key};

                new ChatClient().main(arguments);
            }
        }
    }
}
