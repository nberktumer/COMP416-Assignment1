package master.socket;

import config.Constants;
import master.model.MasterValues;
import util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataServerThread extends ServerThread {
    /**
     * Creates a server thread on the input socket
     *
     * @param socket input socket to create a thread on
     */
    public DataServerThread(Socket socket) {
        super(socket);
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from
     * the client
     */
    @Override
    public void run() {
        super.run();

        try {
            while (MasterValues.getThreadContainerMap().get(getSocket().getInetAddress()).getCommandServerThread() == null) {
                Thread.sleep(100);
            }

            CommandServerThread commandServerThread = MasterValues.getThreadContainerMap().get(getSocket().getInetAddress()).getCommandServerThread();

            while (true) {
                while (commandServerThread.fileName == null || commandServerThread.fileCheckSum == null) {
                    Thread.sleep(100);
                    if (!commandServerThread.isAlive())
                        this.interrupt();
                }

                String fileName = commandServerThread.fileName;
                String checksum = commandServerThread.fileCheckSum;

                System.out.println("DataServerThread: " + fileName);
                System.out.println("DataServerThread: " + checksum);
                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                FileOutputStream f = new FileOutputStream(new File(FileUtils.getDebugDriveDirectory(), fileName));

                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                int current;
                while (inputFileStream.available() > 0) {
                    current = inputFileStream.read(buffer);
                    f.write(buffer, 0, current);
                }
                f.close();

                System.out.println("DataServerThread: File transfer completed at " + System.currentTimeMillis());

                File receivedFile = new File(FileUtils.getDebugDriveDirectory(), fileName);
                System.out.println("DataServerThread: File Name: " + fileName + ", Checksum: " + FileUtils.MD5checksum(receivedFile) + ", Received Checksum: " + checksum);
                if (receivedFile.exists() && FileUtils.MD5checksum(receivedFile).equals(checksum)) {
                    getOutputStream().println(Constants.SUCCESS);
                    System.out.println("Successfully received the file");
                } else {
                    getOutputStream().println(Constants.ERROR);
                    System.out.println("Checksum does not match.");
                    receivedFile.delete();
                }
                getOutputStream().flush();
                commandServerThread.fileName = null;
                commandServerThread.fileCheckSum = null;
            }

        } catch (IOException e) {
            String line = this.getName();
            System.err.println("DataServerThread: Run. IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException | InterruptedException e) {
            String line = this.getName();
            System.err.println("DataServerThread: Run.Client " + line + " Closed");
        }
    }
}
