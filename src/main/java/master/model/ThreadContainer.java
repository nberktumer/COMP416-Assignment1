package master.model;

import master.socket.CommandServerThread;
import master.socket.DataServerThread;

import java.net.InetAddress;

public class ThreadContainer {

    private DataServerThread dataServerThread;
    private CommandServerThread commandServerThread;

    public DataServerThread getDataServerThread() {
        return dataServerThread;
    }

    public void setDataServerThread(DataServerThread dataServerThread) {
        this.dataServerThread = dataServerThread;
    }

    public CommandServerThread getCommandServerThread() {
        return commandServerThread;
    }

    public void setCommandServerThread(CommandServerThread commandServerThread) {
        this.commandServerThread = commandServerThread;
    }
}
