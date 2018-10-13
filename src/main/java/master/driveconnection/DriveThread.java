package master.driveconnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DriveThread extends Thread {


    /**
     * Creates a drive thread and establishes a drive
     * connection (NOTE THAT IN CASE OF NO PRIOR DRIVE
     * AUTHENTICATIONS, STARTS A NEW ONE
     *
     * @param
     */
    public DriveThread() {

    }

    /**
     * Initalizes the connection
     */
    public void run() {
        try {
            //connect to the drive

        } catch (Exception e) {
            System.err.println("Drive Thread: Running error: "+e);
        }
    }


}


