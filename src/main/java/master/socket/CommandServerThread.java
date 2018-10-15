package master.socket;

import config.Constants;
import master.model.MasterValues;
import util.FileUtils;

import java.io.*;
import java.net.Socket;

public class CommandServerThread extends ServerThread {

    protected String fileName;
    protected String fileCheckSum;
    protected long fileLength;

    /**
     * Creates a command server thread on the input socket
     *
     * @param socket input socket to create a thread on
     */
    public CommandServerThread(Socket socket) {
        super(socket);
    }

    /**
     * CommandServerThread listens for the requests from the follower
     */
    @Override
    public void run() {
        super.run();

        try {
            String line;

            while ((line = getInputStream().readLine()) != null) {
                String[] commandArr = line.split("\\|");
                String command = commandArr[0];

                if (command.equals(Constants.NEW_FILE)) {
                    String fileName = commandArr[1];
                    String fileChecksum = commandArr[2];
                    long fileLength = Long.parseLong(commandArr[3]);

                    this.fileCheckSum = fileChecksum;
                    this.fileName = fileName;
                    this.fileLength = fileLength;

                    getOutputStream().println(Constants.SUCCESS);
                    getOutputStream().flush();

                    //System.out.println("CommandServer: File info received at " + System.currentTimeMillis());
                } else if (command.equals(Constants.DELETE_FILE)) {
                    String checksum = commandArr[1];

                    File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDebugDriveDirectory()));
                    if (file != null && file.exists()) {
                        file.delete();
                        getOutputStream().println(Constants.SUCCESS);
                    } else {
                        getOutputStream().println(Constants.ERROR);
                    }
                    getOutputStream().flush();
                } else if (command.equals(Constants.GET_FILE_LIST)) {
                    String out = String.join("|", FileUtils.getChecksumList(FileUtils.getFileList(FileUtils.getDebugDriveDirectory())));
                    getOutputStream().println(out);
                    getOutputStream().flush();
                } else if (command.equals(Constants.REQUEST_FILE_INFO)) {
                    String checksum = commandArr[1];

                    File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDebugDriveDirectory()));
                    if (file != null)
                        getOutputStream().println(file.getName() + "|" + file.length());
                    else
                        getOutputStream().println(Constants.ERROR + "|0");
                    getOutputStream().flush();
                } else if (command.equals(Constants.REQUEST_FILE)) {
                    String checksum = commandArr[1];
                    File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDebugDriveDirectory()));

                    if (file == null) {
                        // File does not exist
                        getOutputStream().println("null");
                        getOutputStream().flush();
                        continue;
                    }
                    //System.out.println(file.getName() + " will be sent. Checsum: " + checksum);

                    OutputStream dataThreadOutputStream = MasterValues.getThreadContainerMap().get(getSocket().getInetAddress()).getDataServerThread().getSocket().getOutputStream();
                    final DataOutputStream outputFileStream = new DataOutputStream(new BufferedOutputStream(dataThreadOutputStream));
                    final BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));

                    int data;
                    while ((data = inputFileStream.read()) != -1)
                        outputFileStream.write(data);

                    inputFileStream.close();
                    outputFileStream.flush();
                    //System.out.println("Server: File sent at " + System.currentTimeMillis());
                }
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("DataServerThread: " + getName() + " has been disconnected.");
        }
    }
}
