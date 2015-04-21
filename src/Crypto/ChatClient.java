package Crypto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;




// Class to manage Client chat Box.
public class ChatClient {

    /** Chat client access */
    static class ChatAccess extends Observable {
        private Socket socket;
        private OutputStream outputStream;
        private DataInputStream is = null;
        private  String key = null;

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        /** Create socket, and receiving thread */
        public ChatAccess(String server, int port) throws IOException {
            socket = new Socket(server, port);
            outputStream = socket.getOutputStream();
            is =  new DataInputStream(socket.getInputStream());
            
            Thread receivingThread = new Thread() {
                int numberOfBytesReceived = 0;
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line = null;
                        BigInteger d, e,N,phi;

                        e = receiveBiginteger(line, reader, "E received");
                         send("E received");
                        N = receiveBiginteger(line, reader, "N received");
                         send("N received");
                        phi = receiveBiginteger(line, reader, "phi received");
                        send("phi received");

                        d = e.modInverse(phi);
                        System.out.println(d);
                        send(d.toString());

                        String key = JOptionPane.showInputDialog("Enter session secret");

                        System.out.println("Encrypting String: " + md5(key));
                        System.out.println("String in Bytes: " + bytesToString(md5(key).getBytes()));

                        // encrypt
                        byte[] encrypted = encrypt(md5(key).getBytes(),e, N);
                        System.out.println("Encrypted String in Bytes: " + bytesToString(encrypted));

                        // decrypt
                        byte[] decrypted = decrypt(encrypted,d,N);
                        System.out.println("Decrypted String in Bytes: " +  bytesToString(decrypted));

                        System.out.println("Decrypted String: " + new String(decrypted));
                        sendByteArray(encrypted);
                        key = md5(key);
//                        byte[] encrypted = encrypt("1a25s8fe5dsg65ad".getBytes(), e, N);
//                        System.out.println("String in Bytes: " + bytesToString("1a25s8fe5dsg65ad".getBytes()));
//                        System.out.println(encrypted);
//
//                        byte[] decrypted =  decrypt(encrypted,d,N);
//                        System.out.println("Decrypted String in Bytes: " +  bytesToString(decrypted));
//                        sendByteArray(encrypted);

                        // Action for send user's nick name
                        String name = JOptionPane.showInputDialog("Enter your nick name");
                        send(name);

                        int temp=0;
                        while (true)
                          {
                              line = reader.readLine();
                              System.out.println("Line = " + temp);
                              if(temp >= 4)
                              {
                                  System.out.println("1");
                                  break;
                              }
                              else
                              {
                                  temp++;
                                  System.out.println("2");
                                  notifyObservers(line);
                              }
                          }
                        System.out.println("Start chating");
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                    
                    byte[] encryptedTextInBytesFromServer = new byte[1024];

                    while(true)
                           {
                            try {
                                int numberOfBytesReceived = is.read(encryptedTextInBytesFromServer);

                                System.out.println("number of bits: " + numberOfBytesReceived);
                                byte[] dec = AES.decrypt(Arrays.copyOfRange(encryptedTextInBytesFromServer, 0, numberOfBytesReceived), key.getBytes());
                                System.out.println("3");

                                String line = new String(dec);
                                notifyObservers(line);
                                System.out.println("Text decripted text now!!!!!: " + line);
                            } catch (IOException ex) {
                                Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                           }
                }
            };
            receivingThread.start();
        }


        public BigInteger receiveBiginteger(String line, BufferedReader reader, String text) throws IOException {
            while (true)
            {
                line = reader.readLine();
                System.out.println(text + line);
                if(!line.isEmpty())
                {
                    break;
                }

            }
            return new BigInteger(line);
        }
        private static final String CRLF = "\r\n"; // newline

        /** Send a line of text */
        public void send(String text) {
            try {
                outputStream.write((text + CRLF).getBytes());
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        private static String bytesToString(byte[] encrypted) {
            String test = "";
            for (byte b : encrypted) {
                test += Byte.toString(b);
            }
            return test;
        }

        /** Send a line of text */
        public void sendByteArray(byte[] key) throws IOException {
            outputStream.write(key);
            outputStream.flush();
        }

        // Decrypt message
        public byte[] decrypt(byte[] message, BigInteger d, BigInteger N) {
            return (new BigInteger(message)).modPow(d, N).toByteArray();

        }

        //Encrypt message
        public byte[] encrypt(byte[] message, BigInteger e, BigInteger N) {
            return (new BigInteger(message)).modPow(e, N).toByteArray();
        }


        /** Send a line of text */
        public void sendCrypto(String text) {
            try {
                    text+=CRLF;
                    System.out.println("Text clar: " + text);
                    byte[] enc = AES.encrypt(text.getBytes(), key.getBytes());
                    System.out.println("Text cripted text by AES: "+ new String(enc));
                    System.out.println("Encrypted text getBytes()" + enc);
                    
                outputStream.write(enc);
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        /** Close the socket */
        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    public static String md5(String input) {

        String md5 = null;

        if(null == input) return null;

        try {

            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());

            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }
        return md5;
    }


    /** Chat client UI */
    public static class ChatFrame extends JFrame implements Observer {

        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private ChatAccess chatAccess;

        public ChatFrame(ChatAccess chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGUI();
        }



        /** Builds the user interface */
        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            sendButton = new JButton("Send");
            box.add(inputTextField);
            box.add(sendButton);




            // Action for the inputTextField and the goButton
            ActionListener sendListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String str = inputTextField.getText();
                    if (str != null && str.trim().length() > 0)
                        chatAccess.sendCrypto(str);
                    inputTextField.selectAll();
                    inputTextField.requestFocus();
                    inputTextField.setText("");
                }
            };
            inputTextField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        /** Updates the UI depending on the Object argument */
        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                   
                   System.out.println("recieved text: " + finalArg);
                    textArea.append(finalArg.toString());
                    textArea.append("\n");
                }
            });
        }
        
    }

    public static void main(String[] args) {
        String server = args[0];

        int port = 2222;
        ChatAccess access = null;

        try {
            access = new ChatAccess(server, port);
            access.key = "1a25s8fe5dsg65ad";
        } catch (IOException ex) {
            System.out.println("Cannot connect to " + server + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
        
        JFrame frame = new ChatFrame(access);
////        frame.setTitle("My diploma chat - connected to " + server + ":" + port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}