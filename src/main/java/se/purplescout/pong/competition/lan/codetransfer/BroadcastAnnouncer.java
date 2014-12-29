package se.purplescout.pong.competition.lan.codetransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class BroadcastAnnouncer implements AutoCloseable {

    private Timer sendTimer;

    public BroadcastAnnouncer() {
    }

    public void start() {
        if (sendTimer != null) {
            close();
        }

        sendTimer = new Timer("UDP Broadcast thread");
        sendTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                broadcast();

            }
        }, 500, 500);

        System.out.println("Making broadcast announcements...");
    }

    private void broadcast() {
        StringBuilder log = new StringBuilder();

        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData;

//		   //Try the 255.255.255.255 first
//		   try {
//		     DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
//		     c.send(sendPacket);
//		     log.append("Broadcast packet sent to: 255.255.255.255 (Default interface)");
//		   } catch (Exception e) {
//		   }

            log.append("Broadcast packet sent to:");

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    //InetAddress broadcast = interfaceAddress.getBroadcast();
                    InetAddress broadcast = InetAddress.getByName("255.255.255.255");
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        sendData = ("ROBOTPONG SERVER ON THIS MACHINE: " + interfaceAddress.getAddress().getHostAddress()).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    log.append(" {" + broadcast.getHostAddress() + " on if: " + networkInterface.getDisplayName() + "}");
                }
            }

            //System.out.println(log);

            //Close the port!
            c.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        sendTimer.cancel();
    }

    public static void main(String[] args) {
        new BroadcastAnnouncer();
    }
}
