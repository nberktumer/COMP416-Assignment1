package master.socket;

import master.model.MasterValues;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private Class<? extends ServerThread> serverThread;

    /**
     * Initiates a server socket on the input port, listens to the line, on
     * receiving an incoming connection creates and starts a ServerThread on the
     * client
     *
     * @param port
     */
    public Server(Class<? extends ServerThread> serverThreadClass, int port) {
        try {
            this.serverThread = serverThreadClass;
            this.serverSocket = new ServerSocket(port);
            System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server class.Constructor exception on opening a server socket");
        }
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            listenConnections();
        }
    }

    /**
     * Listens to the line and starts a connection on receiving a request from the
     * client The connection is started and initiated as a ServerThread object
     */
    private void listenConnections() {
        try {
            Socket socket = serverSocket.accept();

            System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());

            Constructor<?> constructor = this.serverThread.getConstructor(Socket.class);
            Thread object = (Thread) constructor.newInstance(new Object[]{socket});

            if (serverThread.getName().equals(DataServerThread.class.getName())) {
                MasterValues.addDataThread(socket.getInetAddress(), (DataServerThread) object);
            } else if (serverThread.getName().equals(CommandServerThread.class.getName())) {
                MasterValues.addCommandThread(socket.getInetAddress(), (CommandServerThread) object);
            }
            object.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }

}