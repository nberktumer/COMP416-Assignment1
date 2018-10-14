package follower;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Follower {
    private String ipAddress = "";
    private int commandPort = -1;
    private int dataPort = -1;

    public Follower() {
        getMasterInformationFromUser();

        ClientManager.getInstance().connectToMaster(ipAddress, commandPort, dataPort);
        startDataSyncWorker();
    }

    /**
     * Gets the master ip address and port numbers from the user using scanner
     */
    private void getMasterInformationFromUser() {
        Scanner scanner = new Scanner(System.in);

        boolean isValidIpAddress = false;
        boolean isValidCommandPort = false;
        boolean isValidDataPort = false;

        while (!isValidIpAddress) {
            System.out.println("Please enter the ip address:");
            ipAddress = scanner.nextLine();

            try {
                InetAddress.getByName(ipAddress);
                isValidIpAddress = true;
            } catch (UnknownHostException e) {
                System.err.println("Invalid ip address.");
            }
        }

        while (!isValidCommandPort) {
            System.out.println("Please enter the command port number:");
            try {
                commandPort = scanner.nextInt();

                if (commandPort >= 0 && commandPort <= 65535)
                    isValidCommandPort = true;
                else
                    System.err.println("Invalid port number. Port number must be between 0 and 65535.");
            } catch (InputMismatchException e) {
                System.err.println("Invalid port number. Port number must be between 0 and 65535.");
                scanner.next();
            }
        }

        while (!isValidDataPort) {
            System.out.println("Please enter the data port number:");
            try {
                dataPort = scanner.nextInt();

                if (dataPort >= 0 && dataPort <= 65535)
                    isValidDataPort = true;
                else
                    System.err.println("Invalid port number. Port number must be between 0 and 65535.");
            } catch (InputMismatchException e) {
                System.err.println("Invalid port number. Port number must be between 0 and 65535.");
                scanner.next();
            }
        }
    }

    /**
     * Starts the DataSyncWorker which checks the file changes
     */
    private void startDataSyncWorker() {
        new DataSyncWorker().start();
    }
}
