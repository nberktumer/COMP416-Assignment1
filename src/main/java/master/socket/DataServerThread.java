package master.socket;

import config.Constants;
import master.model.MasterValues;
import util.FileUtils;

import java.io.*;
import java.net.Socket;

public class DataServerThread extends ServerThread {
    /**
     * Creates a data server thread on the input socket
     *
     * @param socket input socket to create a thread on
     */
    public DataServerThread(Socket socket) {
        super(socket);
    }

    /**
     * DataServerThread listens for new files requests and uploads/downloads them to/from the follower
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

                //System.out.println("Downloading " + fileName);

                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(FileUtils.getDebugDriveDirectory(), fileName)));

                for (int i = 0; i < fileLength; i++)
                    bufferedOutputStream.write(inputFileStream.read());

                bufferedOutputStream.close();

                //System.out.println("Download completed for " + fileName);

                File receivedFile = new File(FileUtils.getDebugDriveDirectory(), fileName);
                if (receivedFile.exists() && FileUtils.MD5checksum(receivedFile).equals(checksum)) {
                    getOutputStream().println(Constants.SUCCESS);
                    //System.out.println("Consistency check for " + fileName + " passed");
                } else {
                    getOutputStream().println(Constants.ERROR);
                    //System.out.println("Retransmit request for file " + fileName);
                    receivedFile.delete();
                }
                getOutputStream().flush();
                commandServerThread.fileName = null;
                commandServerThread.fileCheckSum = null;
                commandServerThread.fileLength = 0L;
            }
        } catch (IOException | NullPointerException | InterruptedException e) {
            System.err.println("DataServerThread: " + getName() + " has been disconnected.");
        }
    }
}
