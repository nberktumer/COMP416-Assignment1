package master.socket;

import config.Constants;
import master.model.MasterValues;
import util.FileUtils;

import java.io.*;
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
                while (commandServerThread.fileName == null || commandServerThread.fileCheckSum == null || commandServerThread.fileLength == 0L) {
                    Thread.sleep(100);
                    if (!commandServerThread.isAlive())
                        this.interrupt();
                }

                String fileName = commandServerThread.fileName;
                String checksum = commandServerThread.fileCheckSum;
                long fileLength = commandServerThread.fileLength;

                System.out.println("DataServerThread: " + fileName);
                System.out.println("DataServerThread: " + checksum);
                System.out.println("DataServerThread: " + fileLength);

                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(FileUtils.getDebugDriveDirectory(), fileName)));

                for (int i = 0; i < fileLength; i++)
                    bufferedOutputStream.write(inputFileStream.read());

                bufferedOutputStream.close();

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
                commandServerThread.fileLength = 0L;
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
