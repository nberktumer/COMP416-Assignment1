package follower.socket;

import config.Constants;
import follower.ClientManager;
import util.FileUtils;

import java.io.*;

public class DataClient extends Client {

    public DataClient(String address, int port) {
        super(address, port);
    }

    public File requestFile(String fileChecksum) {
        try {
            String fileName = ClientManager.getInstance().getCommandClient().send(Constants.REQUEST_FILE_NAME + "|" + fileChecksum);

            System.out.println("DataClient: " + fileName + " will be downloaded from the master");

            boolean isReceived = false;
            do {
                ClientManager.getInstance().getCommandClient().getOutputStream().println(Constants.REQUEST_FILE + "|" + fileChecksum);
                ClientManager.getInstance().getCommandClient().getOutputStream().flush();
                getSocket().setSendBufferSize(1024000);
                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                FileOutputStream fileOutputStream = new FileOutputStream(new File(FileUtils.getDriveDirectory(), fileName));

                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                int current;
                while (inputFileStream.available() > 0) {
                    current = inputFileStream.read(buffer);
                    fileOutputStream.write(buffer, 0, current);
                }
                fileOutputStream.close();

                System.out.println("DataClient: "+fileName+" is downloaded at " + System.currentTimeMillis());

                File receivedFile = new File(FileUtils.getDriveDirectory(), fileName);

                if (receivedFile.exists() && FileUtils.MD5checksum(receivedFile).equals(fileChecksum)) {
                    System.out.println("Successfully received the file");
                    isReceived = true;
                } else {
                    System.out.println("Checksums do not match.");
                    Thread.sleep(Constants.RETRY_INTERVAL);
                    receivedFile.delete();
                }
            } while (!isReceived);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean transfer(File file) {
        try {
            String result;
            int counter = 0;
            do {
                if(counter > Constants.NUM_TRIALS)
                    throw new Exception();

                System.out.println(file.getName() + ", " + FileUtils.MD5checksum(file));

                ClientManager.getInstance().getCommandClient().send(Constants.NEW_FILE + "|" + file.getName() + "|" + FileUtils.MD5checksum(file));

                final BufferedOutputStream outputFileStream = new BufferedOutputStream(getSocket().getOutputStream());
                final BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));
                final byte[] buffer = new byte[Constants.BUFFER_SIZE];

                for (int read = inputFileStream.read(buffer); read >= 0; read = inputFileStream.read(buffer)) {
                    outputFileStream.write(buffer, 0, read);
                }

                outputFileStream.flush();
                System.out.println("Client: File sent at " + System.currentTimeMillis());

                result = getInputStream().readLine();

                inputFileStream.close();
                counter++;
            } while (!result.equals(Constants.SUCCESS));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("DataClient: An error occurred while uploading " + file.getName() + " file.");
            return false;
        }
    }

}
