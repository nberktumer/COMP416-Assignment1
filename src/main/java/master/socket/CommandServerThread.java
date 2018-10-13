package master.socket;

import config.Constants;
import master.model.MasterValues;
import util.FileUtils;

import java.io.*;
import java.net.Socket;

public class CommandServerThread extends ServerThread {

    protected String fileName;
    protected String fileCheckSum;

    /**
     * Creates a server thread on the input socket
     *
     * @param socket input socket to create a thread on
     */
    public CommandServerThread(Socket socket) {
        super(socket);
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from
     * the client1
     */
    @Override
    public void run() {
        super.run();

        try {
            String line;

            while ((line = getInputStream().readLine()) != null) {
                String[] commandArr = line.split("\\|");

                System.out.println(line);

                String command = commandArr[0];

                System.out.println(command);
                if (command.equals(Constants.NEW_FILE)) {
                    String fileName = commandArr[1];
                    String fileChecksum = commandArr[2];

                    System.out.println("fileName: " + fileName);
                    System.out.println("checksum: " + fileChecksum);
                    this.fileCheckSum = fileChecksum;
                    this.fileName = fileName;

                    getOutputStream().println(Constants.SUCCESS);
                    getOutputStream().flush();

                    System.out.println("CommandServer: File info received at " + System.currentTimeMillis());
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
                } else if (command.equals(Constants.REQUEST_FILE_NAME)) {
                    String checksum = commandArr[1];

                    File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDebugDriveDirectory()));
                    if (file != null)
                        getOutputStream().println(file.getName());
                    else
                        getOutputStream().println("null");
                    getOutputStream().flush();
                } else if (command.equals(Constants.REQUEST_FILE)) {
                    String checksum = commandArr[1];
                    File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDebugDriveDirectory()));

                    if (file == null) {
                        System.out.println("file does not exist!!!");
                        getOutputStream().println("null");
                        getOutputStream().flush();
                        continue;
                    }
                    System.out.println(file.getName() + " will be sent. Checsum: " + checksum);

                    OutputStream dataThreadOutputStream = MasterValues.getThreadContainerMap().get(getSocket().getInetAddress()).getDataServerThread().getSocket().getOutputStream();
                    final BufferedOutputStream outputFileStream = new BufferedOutputStream(dataThreadOutputStream);
                    final BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));
                    final byte[] buffer = new byte[Constants.BUFFER_SIZE];

                    for (int read = inputFileStream.read(buffer); read >= 0; read = inputFileStream.read(buffer)) {
                        outputFileStream.write(buffer, 0, read);
                    }

                    inputFileStream.close();
                    outputFileStream.flush();
                    System.out.println("Server: File sent at " + System.currentTimeMillis());
                }

				/*getOutputStream().println(line);
				getOutputStream().flush();
				System.out.println("Server Thread: Client " + getSocket().getRemoteSocketAddress() + " sent : " + line);*/


            }

        } catch (IOException e) {
            String line = this.getName();
            System.err.println("Server Thread: Run. IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException e) {
            e.printStackTrace();
            String line = this.getName();
            System.err.println("Server Thread:  " + line + " Closed");
        }
    }
}
