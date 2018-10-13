package follower;

import follower.socket.CommandClient;
import follower.socket.DataClient;

public class ClientManager {
    private CommandClient commandClient;
    private DataClient dataClient;

    private ClientManager() {
    }

    private static class SingletonHelper {
        private static final ClientManager INSTANCE = new ClientManager();
    }

    /**
     * Returns the singleton ClientManager instance
     *
     * @return ClientManager instance
     */
    public static ClientManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Connects to the master server with the given IP address and ports
     *
     * @param ipAddress   IP address to connect to the master server
     * @param commandPort Port number for connecting to the CommandServer
     * @param dataPort    Port number for connecting to the DataServer
     */
    public void connectToMaster(String ipAddress, int commandPort, int dataPort) {
        commandClient = new CommandClient(ipAddress, commandPort);
        dataClient = new DataClient(ipAddress, dataPort);

        commandClient.connect();
        dataClient.connect();
    }

    /**
     * Returns the CommandClient instance
     *
     * @return commandClient CommandClient instance
     */
    public CommandClient getCommandClient() {
        return commandClient;
    }

    /**
     * Returns the DataClient instance
     *
     * @return dataClient DataClient instance
     */
    public DataClient getDataClient() {
        return dataClient;
    }

    public void disconnectAll() {
        commandClient.disconnect();
        dataClient.disconnect();
    }
}
