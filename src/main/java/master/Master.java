package master;

import googledrive.DriveAPI;
import master.socket.CommandServerThread;
import master.socket.DataServerThread;
import master.socket.Server;


public class Master {
	public Master() {
		/*Scanner scanner = new Scanner(System.in);

		boolean isValidIpAddress = false;
		boolean isValidPort = false;

		String ipAddress = "";
		int port = -1;

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

		while (!isValidPort) {
			System.out.println("Please enter the port number:");
			port = scanner.nextInt();

			if (port >= 0 && port <= 65535)
				isValidPort = true;
			else
				System.err.println("Invalid port number. Port number must be between 0 and 65535.");

		}*/
		
		Server commandServer = new Server(CommandServerThread.class, 8888);
		Server dataServer = new Server(DataServerThread.class, 8889);
		
		commandServer.start();
		dataServer.start();

	}
	
	/**
	 * Establishes a drive connection to the user's account.
	 * 
	 * @param 
	 */
	public void establishDriveConnection(){
		
	}
	
	/**
	 * Terminates the drive connection created.
	 * 
	 * @param 
	 */
	public void terminateDriveConnection(){
		
	}
}
