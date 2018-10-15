package master.model;

import master.socket.CommandServerThread;
import master.socket.DataServerThread;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MasterValues {

    private MasterValues() {
    }

    private static Map<InetAddress, ThreadContainer> threadContainerMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Returns the threadContainerMap
     *
     * @return threadContainerMap
     */
    public static Map<InetAddress, ThreadContainer> getThreadContainerMap() {
        return threadContainerMap;
    }

    /**
     * Inserts a new DataServerThread to the given IP address
     *
     * @param inetAddress      IP address
     * @param dataServerThread DataServerThread to be inserted
     */
    public static synchronized void addDataThread(InetAddress inetAddress, DataServerThread dataServerThread) {
        if (threadContainerMap.containsKey(inetAddress)) {
            threadContainerMap.get(inetAddress).setDataServerThread(dataServerThread);
        } else {
            ThreadContainer threadContainer = new ThreadContainer();
            threadContainer.setDataServerThread(dataServerThread);
            threadContainerMap.put(inetAddress, threadContainer);
        }
    }

    /**
     * Inserts a new CommandServerThread to the given IP address
     *
     * @param inetAddress         IP address
     * @param commandServerThread CommandServerThread to be inserted
     */
    public static synchronized void addCommandThread(InetAddress inetAddress, CommandServerThread commandServerThread) {
        if (threadContainerMap.containsKey(inetAddress)) {
            threadContainerMap.get(inetAddress).setCommandServerThread(commandServerThread);
        } else {
            ThreadContainer threadContainer = new ThreadContainer();
            threadContainer.setCommandServerThread(commandServerThread);
            threadContainerMap.put(inetAddress, threadContainer);
        }
    }
}
