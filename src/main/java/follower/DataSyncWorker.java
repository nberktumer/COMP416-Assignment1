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
        List<String> masterChecksumList = new ArrayList<>(Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|")));
        if(masterChecksumList.size() == 1 && masterChecksumList.get(0).equals(""))
            masterChecksumList.clear();

        for (String checksum : masterChecksumList) {
            System.out.println(checksum + ", asdasd");
            if (!checksumMap.values().contains(checksum)) {
                // TODO: Download file
                ClientManager.getInstance().getDataClient().requestFile(checksum);
                File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()));
                if (file != null) {
                    checksumMap.put(file.getAbsolutePath(), checksum);
                }
            }
        }

        List<String> removeList = new ArrayList<>();
        for (String checksum : checksumMap.values()) {
            if (!masterChecksumList.contains(checksum)) {
                // TODO: Delete file
                File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()));
                if (file != null) {
                    file.delete();
                    removeList.add(file.getAbsolutePath());
                }
            }
        }

        for(String path: removeList) {
            checksumMap.remove(path);
        }

        File[] localFileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

        for (File file : localFileList) {
            // New File
            if (!checksumMap.containsKey(file.getAbsolutePath())) {
                checksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                //TODO: Upload new file
                uploadFile(file);

            } else if (checksumMap.containsKey(file.getAbsolutePath()) && !checksumMap.get(file.getAbsolutePath()).equals(FileUtils.MD5checksum(file))) {
                checksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                //TODO: Update file
                uploadFile(file);
            }
        }

        List<String> deletedFiles = new ArrayList<>();
        for (String path : checksumMap.keySet()) {
            File file = new File(path);
            String checksum = checksumMap.get(path);

            if (!file.exists()) {
                deletedFiles.add(file.getAbsolutePath());
                //TODO: Delete file from master
                ClientManager.getInstance().getCommandClient().send(Constants.DELETE_FILE + "|" + checksum);
            }
        }

        for (String path : deletedFiles) {
            checksumMap.remove(path);
        }


/*************************/
/*

List<String> fileDeleteExceptionList = new ArrayList<>();
        // Compare the current and previous checksum lists
        for (String checksum : prevChecksumList) {
            if (!masterChecksumList.contains(checksum))
                fileDeleteExceptionList.add(checksum);
        }

        for (String checksum : masterChecksumList) {
            File file = FileUtils.getFileWithChecksum(checksum, localFileList);
        }

        for (File file : localFileList) {
            String fileChecksum = FileUtils.MD5checksum(file);

            // Upload new file to the master
            if (!masterChecksumList.contains(fileChecksum)) {
                uploadFile(file);
            }
        }

        masterChecksumList = new ArrayList<>(Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|")));
        List<String> tempMasterChecksumList = new ArrayList<>(masterChecksumList);
        for (File file : localFileList) {
            tempMasterChecksumList.remove(FileUtils.MD5checksum(file));
        }


        for (String checksum : tempMasterChecksumList) {
            ClientManager.getInstance().getDataClient().requestFile(checksum);
        }


        prevChecksumList.clear();
        prevChecksumList.addAll(masterChecksumList);

/*




        // Get checksum list from master
        List<String> tempChecksumList = Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|"));
        List<String> checksumList = new ArrayList<>(tempChecksumList);

        List<String> fileDeleteExceptionList = new ArrayList<>();

        // Compare the current and previous checksum lists
        for(String checksum: prevChecksumList) {
            if(!checksumList.contains(checksum))
                fileDeleteExceptionList.add(checksum);
        }

        // Check deleted files
        for (String filePath : checksumMap.keySet()) {
            File file = new File(filePath);
            if (!file.exists() && !fileDeleteExceptionList.contains(checksumMap.get(filePath))) {

                // Let the master know that "file" is deleted
                ClientManager.getInstance().getCommandClient().send(Constants.DELETE_FILE + "|" + checksumMap.get(filePath));

                System.out.println(file.getName() + " is deleted.");
            }
        }

        File[] fileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

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
        prevChecksumList.addAll(tempChecksumList);*/
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