package follower.socket;

import config.Constants;
import follower.ClientManager;
import util.FileUtils;

import java.io.*;
import java.util.Collections;

public class DataClient extends Client {

    public DataClient(String address, int port) {
        super(address, port);
    }

    public File requestFile(String fileChecksum) {
        try {
            String[] fileInfoArr = ClientManager.getInstance().getCommandClient().send(Constants.REQUEST_FILE_INFO + "|" + fileChecksum).split("\\|");
            String fileName = fileInfoArr[0];
            long fileLength = Long.parseLong(fileInfoArr[1]);
            if(fileName.equals(Constants.ERROR))
                return null;

            System.out.println("DataClient: " + fileName + " will be downloaded from the master");

            boolean isReceived = false;
            do {
                ClientManager.getInstance().getCommandClient().getOutputStream().println(Constants.REQUEST_FILE + "|" + fileChecksum);
                ClientManager.getInstance().getCommandClient().getOutputStream().flush();

                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(FileUtils.getDriveDirectory(), fileName)));

                for(int i = 0; i < fileLength; i++)
                    bufferedOutputStream.write(inputFileStream.read());

                bufferedOutputStream.close();

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

                ClientManager.getInstance().getCommandClient().send(Constants.NEW_FILE + "|" + file.getName() + "|" + FileUtils.MD5checksum(file) + "|" + file.length());

                final DataOutputStream outputFileStream = new DataOutputStream(new BufferedOutputStream(getSocket().getOutputStream()));
                final BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));

                int data;
                while((data = inputFileStream.read()) != -1)
                    outputFileStream.write(data);

                inputFileStream.close();
                outputFileStream.flush();

                result = getInputStream().readLine();

                System.out.println("Client: File sent at " + System.currentTimeMillis());
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
