package se.purplescout.pong.codetransfer;

import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by eriklark on 2014-10-05.
 */
public class CodeSender {

    private String host;
    private ServerFoundListener serverFoundListener;

    public void setServerFoundListener(ServerFoundListener serverFoundListener) {
        this.serverFoundListener = serverFoundListener;
    }

    public void startListeningForServerAsync() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    startListeningForServer();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Could not find server.. " + ex.getMessage());
                }
            }
        }).start();
    }

    public void startListeningForServer() throws IOException {
        //Keep a socket open to listen to all the UDP traffic that is destined for this port
        DatagramSocket socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);

        //Receive a packet
        byte[] recvBuf = new byte[150];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        System.out.println("Sender blocking...");
        socket.receive(packet);

        String content = new String(packet.getData()).trim();
        //Packet received
        System.out.println("Broadcast packet received from: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " " + content);
        host = packet.getAddress().getHostAddress();

        if (serverFoundListener != null) {
            serverFoundListener.serverFound();
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String sendCodeToServer(File f) throws NoServerException, IOException {
        Scanner sc = new Scanner(f);
        StringBuilder code = new StringBuilder();
        while (sc.hasNextLine()) {
            code.append(sc.nextLine());
            code.append("\n");
        }

        return sendCodeToServer(code.toString());
    }

    public String sendCodeToServer(String code) throws NoServerException, IOException {
        if (host == null) {
            throw new NoServerException();
        }
        if (!code.endsWith("\n")) {
            code += "\n";
        }
        code += "--END--\n";
        try {
            System.out.println("Connecting to " + host);
            Socket socket = new Socket(host, 12345);
            System.out.println("Connected to " + host + ". Writing data..");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream);
            outputStreamWriter.write(code);
            outputStreamWriter.flush();
            System.out.println("Data written. Waiting for response...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line + "\n");
            }

            System.out.println(out);
            reader.close();
            socket.close();

            return out.toString();
        } catch (IOException ex) {
            host = null;
            if (serverFoundListener != null) {
                serverFoundListener.serverLost();
            }
            throw ex;
        }
    }
}
