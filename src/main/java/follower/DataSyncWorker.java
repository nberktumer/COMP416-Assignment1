package follower;

import config.Constants;
import util.FileUtils;

import java.io.File;
import java.util.*;

public class DataSyncWorker extends Thread {

    private Map<String, String> checksumMap;
    private List<String> prevChecksumList;

    public DataSyncWorker() {
        checksumMap = new HashMap<>();
        prevChecksumList = new ArrayList<>();
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            FileUtils.createFolder(FileUtils.getDriveDirectory());
            checkFiles();

            try {
                Thread.sleep(Constants.FILE_CHECK_TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkFiles() {
        // Get checksum list from master
        List<String> tempChecksumList = Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|"));
        List<String> checksumList = new ArrayList<>(tempChecksumList);

        List<String> fileDeleteExceptionList = new ArrayList<>();

        // Compare the current and previous checksum lists
        for(String checksum: prevChecksumList) {
            if(!checksumList.contains(checksum))
                fileDeleteExceptionList.add(checksum);
        }

        File[] fileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

        // Check deleted files
        for (String filePath : checksumMap.keySet()) {
            File file = new File(filePath);
            if (!file.exists() && !fileDeleteExceptionList.contains(checksumMap.get(filePath))) {

                // Let the master know that "file" is deleted
                ClientManager.getInstance().getCommandClient().send(Constants.DELETE_FILE + "|" + checksumMap.get(filePath));

                System.out.println(file.getName() + " is deleted.");
            }
        }

        // Refresh the file list in the drive directory
        fileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

        // Check new files and fileList content changes
        for (File file : fileList) {
            String fileChecksum = FileUtils.MD5checksum(file);
            if (!checksumMap.containsKey(file.getAbsolutePath())) {
                checksumMap.put(file.getAbsolutePath(), fileChecksum);

                System.out.println("DataSyncWorker: new file found. " + file.getName() + " will be sent to the master.");

                // Send the new file to the master
                uploadFile(file);

            } else if (checksumMap.containsKey(file.getAbsolutePath())
                    && !checksumMap.get(file.getAbsolutePath()).equals(fileChecksum)) {
                checksumMap.put(file.getAbsolutePath(), fileChecksum);

                System.out.println("DataSyncWorker: " + file.getName() + " is changed. It will be sent to the master.");

                // Send the updated file to the master
                uploadFile(file);

            }
        }

        // Get checksum list from master
        List<String> masterChecksumList = new ArrayList<>(Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|")));

        // Compare the files with the master
        for (File file : fileList) {
            String fileChecksum = FileUtils.MD5checksum(file);

            // Either the file does not exist or it is updated
            // Delete local file and later request the remaining new files
            if (!masterChecksumList.contains(fileChecksum)) {
                file.delete();
            } else {
                masterChecksumList.remove(fileChecksum);
            }
        }

        // Request the new files on the master
        for(String fileChecksum: checksumList) {
            ClientManager.getInstance().getDataClient().requestFile(fileChecksum);
        }

        prevChecksumList.clear();
        prevChecksumList.addAll(tempChecksumList);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("DataSyncWorker: thread is interrupted!!!");
        ClientManager.getInstance().disconnectAll();
    }

    private void uploadFile(File file) {
        int counter = 0;
        while (file.exists() && !ClientManager.getInstance().getDataClient().transfer(file) && counter < Constants.NUM_TRIALS) {
            // Wait for "Constants.RETRY_INTERVAL" ms and retry to upload the file
            try {
                Thread.sleep(Constants.RETRY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                counter++;
            }
        }
    }
}