package Crypto;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.Random;

/**
 *
 * @author SlashMSU
 */

// For every client's connection we call this class
public class clientThread extends Thread{
    private String[] all_users = new String[10];
    private int i = 0;
    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private int maxClientsCount;
    private String key = null;

    public clientThread(Socket clientSocket, clientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;

    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;

        try {
      /*
       * Create input and output streams for this client.
       */
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;

            BigInteger  p,q,N,e,phi,d;
            Random r;
            int bitlength = 1024;

            r = new Random();
            p = BigInteger.probablePrime(bitlength, r);
            q = BigInteger.probablePrime(bitlength, r);
            N = p.multiply(q);
            phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            e = BigInteger.probablePrime(bitlength/2, r);

            while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0 ) {
                e.add(BigInteger.ONE);
            }

            sendToClient(os,e);
            confirmFromClient(is,"E received");
            sendToClient(os,N);
            confirmFromClient(is,"N received");
            sendToClient(os,phi);
            confirmFromClient(is,"phi received");

            d = new BigInteger(is.readLine());
            System.out.println(d);
            byte[] encryptedKeyInBytes = new byte[1024];

            int numberOfBytesReceivedKey = is.read(encryptedKeyInBytes);

            byte[] decrypted = decrypt(Arrays.copyOfRange(encryptedKeyInBytes, 0, numberOfBytesReceivedKey),d,N);
            System.out.println("Decrypted String in Bytes: " +  bytesToString(decrypted));

            System.out.println("Decrypted String: " + new String(decrypted));

            this.key = new String(decrypted);

            while (true) {
                os.println("Enter your name.");
                name = is.readLine().trim();
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    os.println("The name should not contain '@' character.");
                }
            }

            all_users[i] = name;
            for(int j=0; j<=i; j++)
                os.println(all_users[j]);
            i++;
            os.println(i);
      /* Welcome the new the client. */
            os.println("Welcome " + name
                    + " to our chat room.\nTo leave enter /quit in a new line.");
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        String text = "*** A new user " + name + " entered the chat room !!! ***";
                        byte[] enc = AES.encrypt(text.getBytes(), key.getBytes());
                        threads[i].os.write(enc);
                        threads[i].os.flush();
                    }
                }
            }

            System.out.println("-------------------------\n");
      /* Start the conversation. */

            byte[] encryptedTextInBytes = new byte[1024];
            while (true) {

                int numberOfBytesReceived = is.read(encryptedTextInBytes);

                //создать exception при разных ключах
                byte[] dec = AES.decrypt(Arrays.copyOfRange(encryptedTextInBytes, 0, numberOfBytesReceived), key.getBytes());

                String line = new String(dec);
                System.out.println("Text decripted text!!!!!: " + line);

                System.out.println(line.length());
                if (line.startsWith("/quit")) {
                    break;
                }
        
          /* The message is public, broadcast it to all other clients. */
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null) {
                        synchronized (threads[i]) {
                            threads[i].os.write(AES.encrypt(("<" + name + "> ").getBytes(), key.getBytes()));

                            threads[i].os.flush();
                            threads[i].os.write(Arrays.copyOfRange(encryptedTextInBytes, 0, numberOfBytesReceived));
                            threads[i].os.flush();
                        }
                    }
                }
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this
                        && threads[i].clientName != null) {
                    threads[i].os.println("*** The user " + name
                            + " is leaving the chat room !!! ***");
                    i--;
                }
            }
            os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }

    public void sendToClient(PrintStream os, BigInteger bigInteger){
        os.println(bigInteger);
        os.flush();
        System.out.println(bigInteger);
    }

    public void confirmFromClient(DataInputStream is,String text) throws IOException {
        while (true){
            if(is.readLine().equals(text))
            {
                System.out.println(text);
                break;
            }
        }
    }

    // Decrypt message
    public byte[] decrypt(byte[] message,BigInteger d, BigInteger N) {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }

    private static String bytesToString(byte[] encrypted) {
        String test = "";
        for (byte b : encrypted) {
            test += Byte.toString(b);
        }
        return test;
    }

}
