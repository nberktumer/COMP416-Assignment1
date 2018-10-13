package follower.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

abstract class Client {
	private Socket socket;
	private BufferedReader inputStream;
	private PrintWriter outputStream;

	private String serverAddress;
	private int serverPort;

	/**
	 *
	 * @param address
	 *            IP address of the server
	 * @param port
	 *            port number of the server
	 */
	protected Client(String address, int port) {
		serverAddress = address;
		serverPort = port;
	}

	/**
	 * Establishes a socket connection to the server that is identified by the
	 * serverAddress and the serverPort
	 */
	public void connect() {
		try {
			socket = new Socket(serverAddress, serverPort);
			/*
			 * Read and write buffers on the socket
			 */
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintWriter(socket.getOutputStream());

			System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Client Error: no server has been found on " + serverAddress + ":" + serverPort);
		}
	}

	/**
	 * sends the message String to the server and retrieves the answer
	 * 
	 * @param message
	 *            input message string to the server
	 * @return the received server answer
	 */
	public String send(String message) {
		String response = new String();
		try {
			/*
			 * Sends the message to the server via PrintWriter
			 */
			outputStream.println(message);
			outputStream.flush();
			/*
			 * Reads a line from the server via Buffer Reader
			 */
			response = inputStream.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Client: Socket read error while sending a message.");
		}
		return response;
	}

	/**
	 * Disconnects the socket and closes the buffers
	 */
	public void disconnect() {
		try {
			inputStream.close();
			outputStream.close();
			socket.close();

			System.out.println("Client: Connection Closed");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Client: An error occurred while closing the connection.");
		}
	}
	
	/**
	 * Returns the input stream
	 * 
	 * @return inputStream
	 */
	public BufferedReader getInputStream() {
		return inputStream;
	}

	/**
	 * Returns the output stream
	 * 
	 * @return outputStream
	 */
	public PrintWriter getOutputStream() {
		return outputStream;
	}
	
	/**
	 * Returns the socket
	 * 
	 * @return socket
	 */
	public Socket getSocket() {
		return socket;
	}
}
