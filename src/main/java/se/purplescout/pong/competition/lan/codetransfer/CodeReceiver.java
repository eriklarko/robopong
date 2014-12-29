package se.purplescout.pong.competition.lan.codetransfer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.competition.headless.NewPaddleListener;
import se.purplescout.pong.competition.compiler.DynaCompTest;
import se.purplescout.pong.game.Paddle;

public class CodeReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CodeReceiver.class);
    private static final int PORT = 12345;

    private boolean run = true;
    private Thread receiverThread;
    private final NewPaddleListener newPaddleListener;

    public CodeReceiver(NewPaddleListener newPaddleListener) {
        this.newPaddleListener = newPaddleListener;
    }

    public void startServer() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(PORT);

        receiverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    LOG.info("Accepting connections");
                    while (run) {

                        Socket client = serverSocket.accept();
                        new Thread(new Client(client)).start();
                    }
                } catch (Exception e) {
                    LOG.error("Code Receiver caught exception. Exiting", e);
                }
            }
        });
        receiverThread.start();
    }

    public void close() {
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }


    public class Client implements Runnable {
        Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            PrintWriter printWriter = null;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                StringBuilder code = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("--END--")) {
                        break;
                    }
                    //System.out.println("GOT LINE: " + line);

                    code.append(line);
                    code.append("\n");
                }

                Class<Paddle> p = (Class<Paddle>) DynaCompTest.compile(code.toString(), printWriter);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                if (p != null) {
                    LOG.info("INCOMING NEW OBJECT: " + p);
                    outputStreamWriter.write("ALL OK!");

                    if (newPaddleListener != null) {
                        newPaddleListener.newPaddle(p, code.toString());
                    }
                }
                outputStreamWriter.flush();
                outputStreamWriter.close();

                reader.close();
                socket.close();
            } catch (Exception ex) {
                //Logger.getLogger(RoboPongServer.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace(printWriter);
                printWriter.flush();
                printWriter.close();
                LOG.warn("Unable to compile and add paddle", ex);
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    LOG.warn("Unable to close socket", ex);
                }
            }
        }
    }
}
