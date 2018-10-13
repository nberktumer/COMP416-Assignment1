package follower;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Follower {
	

	public Follower() {
		/*
		 * Scanner scanner = new Scanner(System.in);
		 * 
		 * boolean isValidIpAddress = false; boolean isValidPort = false;
		 * 
		 * String ipAddress = ""; int port = -1;
		 * 
		 * while (!isValidIpAddress) {
		 * System.out.println("Please enter the ip address:"); ipAddress =
		 * scanner.nextLine();
		 * 
		 * try { InetAddress.getByName(ipAddress); isValidIpAddress = true; } catch
		 * (UnknownHostException e) { System.err.println("Invalid ip address."); } }
		 * 
		 * while (!isValidPort) { System.out.println("Please enter the port number:");
		 * port = scanner.nextInt();
		 * 
		 * if (port >= 0 && port <= 65535) isValidPort = true; else System.err.
		 * println("Invalid port number. Port number must be between 0 and 65535.");
		 * 
		 * }
		 */

		ClientManager.getInstance().connectToMaster("127.0.0.1", 8888, 8889);
		startDataSyncWorker();		
	}
	
	/**
	 * Starts the DataSyncWorker which checks the file changes
	 */
	private void startDataSyncWorker() {
		new DataSyncWorker().start();
	}
}
