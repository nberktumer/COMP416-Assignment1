package master.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

abstract class ServerThread extends Thread {
	private BufferedReader inputStream;
	private PrintWriter outputStream;
	private Socket socket;

	/**
	 * Creates a server thread on the input socket
	 *
	 * @param socket
	 *            input socket to create a thread on
	 */
	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Abstract server thread. Initializes the inputStream and outputStream
	 */
	public void run() {
		try {
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Server Thread: IO error in server thread.");
		}
	}
	
	/**
	 * Returns the input stream
	 * 
	 * @return BufferedReader
	 */
	public BufferedReader getInputStream() {
		return inputStream;
	}

	/**
	 * Returns the output stream
	 * 
	 * @return PrintWriter
	 */
	public PrintWriter getOutputStream() {
		return outputStream;
	}
	
	/**
	 * Returns the socket
	 * 
	 * @return Socket
	 */
	public Socket getSocket() {
		return socket;
	}
}
