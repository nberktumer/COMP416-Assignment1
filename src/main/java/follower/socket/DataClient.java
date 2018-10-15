package follower.socket;

import config.Constants;
import follower.ClientManager;
import util.FileUtils;

import java.io.*;

public class DataClient extends Client {

    public DataClient(String address, int port) {
        super(address, port);
    }

    /**
     * Downloads the file with the given checksum from the master
     *
     * @param fileChecksum Checksum of the file that will be downloaded
     * @return Downloaded file
     */
    public File requestFile(String fileChecksum) {
        try {
            String[] fileInfoArr = ClientManager.getInstance().getCommandClient().send(Constants.REQUEST_FILE_INFO + "|" + fileChecksum).split("\\|");
            String fileName = fileInfoArr[0];
            long fileLength = Long.parseLong(fileInfoArr[1]);
            if (fileName.equals(Constants.ERROR))
                return null;

            System.out.println("Downloading " + fileName);

            boolean isReceived = false;
            do {
                ClientManager.getInstance().getCommandClient().getOutputStream().println(Constants.REQUEST_FILE + "|" + fileChecksum);
                ClientManager.getInstance().getCommandClient().getOutputStream().flush();

                final BufferedInputStream inputFileStream = new BufferedInputStream(getSocket().getInputStream());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(FileUtils.getDriveDirectory(), fileName)));

                for (int i = 0; i < fileLength; i++)
                    bufferedOutputStream.write(inputFileStream.read());

                bufferedOutputStream.close();

                System.out.println("Download completed for " + fileName);

                File receivedFile = new File(FileUtils.getDriveDirectory(), fileName);
                if (receivedFile.exists() && FileUtils.MD5checksum(receivedFile).equals(fileChecksum)) {
                    System.out.println("Consistency check for " + fileName + " passed");
                    isReceived = true;
                } else {
                    System.out.println("Retransmit request for file " + fileName);
                    Thread.sleep(Constants.RETRY_INTERVAL);
                    receivedFile.delete();
                }
            } while (!isReceived);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Uploads the given file to the server
     *
     * @param file File to be uploaded
     * @return Result of the upload process
     */
    public boolean transfer(File file) {
        try {
            String result;
            do {
                System.out.println("Uploading " + file.getName());

                ClientManager.getInstance().getCommandClient().send(Constants.NEW_FILE + "|" + file.getName() + "|" + FileUtils.MD5checksum(file) + "|" + file.length());

                final DataOutputStream outputFileStream = new DataOutputStream(new BufferedOutputStream(getSocket().getOutputStream()));
                final BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));

                int data;
                while ((data = inputFileStream.read()) != -1)
                    outputFileStream.write(data);

                inputFileStream.close();
                outputFileStream.flush();

                result = getInputStream().readLine();

                System.out.println("Upload completed for " + file.getName());
            } while (!result.equals(Constants.SUCCESS));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while uploading " + file.getName());
            return false;
        }
    }

}
