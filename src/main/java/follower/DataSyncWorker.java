package follower;

import config.Constants;
import util.FileUtils;

import java.io.File;
import java.util.*;

public class DataSyncWorker extends Thread {

    private Map<String, String> checksumMap;

    public DataSyncWorker() {
        checksumMap = new HashMap<>();
    }

    /**
     * Constantly checks the file changes
     */
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

    /**
     * Checks the file changes and prints the appropriate messages
     *
     * @param masterChecksumList checksum list from the master
     */
    private void checkAndPrintFileDifferences(List<String> masterChecksumList) {
        boolean hasChange = false;
        Map<String, String> tempChecksumMap = new HashMap<>(checksumMap);

        List<String> downloadList = new ArrayList<>();
        for (String checksum : masterChecksumList) {
            if (!tempChecksumMap.values().contains(checksum) && FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory())) == null) {
                // Download file
                String[] fileInfoArr = ClientManager.getInstance().getCommandClient().send(Constants.REQUEST_FILE_INFO + "|" + checksum).split("\\|");
                String fileName = fileInfoArr[0];
                long fileSize = Long.parseLong(fileInfoArr[1]);

                if (!fileName.equals("null") && !hasChange) {
                    System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", the following files are going to be synchronized:");
                    hasChange = true;
                }
                if (!fileName.equals("null")) {
                    System.out.println("-" + fileName + " going to be downloaded from the master. Size: " + fileSize);
                    downloadList.add(fileName);
                }
            }
            File file;
            if((file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()))) != null)
                tempChecksumMap.put(file.getAbsolutePath(), checksum);
        }

        List<String> removeList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();
        for (String checksum : tempChecksumMap.values()) {
            if (!masterChecksumList.contains(checksum)) {
                // Delete file
                File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()));
                if (file != null && !downloadList.contains(file.getName())) {
                    if (!hasChange) {
                        System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", the following files are going to be synchronized:");
                        hasChange = true;
                    }
                    System.out.println("-" + file.getName() + " going to be deleted from the computer. Size: " + file.length());
                    removeList.add(file.getAbsolutePath());
                    deleteList.add(file.getName());
                }
            }
        }

        for (String path : removeList) {
            tempChecksumMap.remove(path);
        }

        File[] localFileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

        for (File file : localFileList) {
            if (!tempChecksumMap.containsKey(file.getAbsolutePath()) && !downloadList.contains(file.getName()) && !deleteList.contains(file.getName())) {
                tempChecksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                // Upload new file
                if (!hasChange) {
                    System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", the following files are going to be synchronized:");
                    hasChange = true;
                }
                System.out.println("-" + file.getName() + " going to be uploaded to the master. Size: " + file.length());

            } else if (tempChecksumMap.containsKey(file.getAbsolutePath()) && !tempChecksumMap.get(file.getAbsolutePath()).equals(FileUtils.MD5checksum(file)) && !downloadList.contains(file.getName()) && !deleteList.contains(file.getName())) {
                tempChecksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                // Update file
                if (!hasChange) {
                    System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", the following files are going to be synchronized:");
                    hasChange = true;
                }
                System.out.println("-" + file.getName() + " going to be uploaded to the master. Size: " + file.length());
            }
        }

        for (String path : tempChecksumMap.keySet()) {
            File file = new File(path);

            if (!file.exists()) {
                // Delete file from master
                if (!hasChange) {
                    System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", the following files are going to be synchronized:");
                    hasChange = true;
                }
                System.out.println("-" + file.getName() + " going to be deleted to the master. Size: " + file.length());
            }
        }

        if (!hasChange)
            System.out.println("Current time: " + new Date(System.currentTimeMillis()) + ", no update is needed. Already synced!");
    }

    /**
     * Checks the file changes and uploads/downloads the files
     *
     * @param masterChecksumList checksum list from the master
     */
    private void checkAndSyncFileDifferences(List<String> masterChecksumList) {
        for (String checksum : masterChecksumList) {
            if (!checksumMap.values().contains(checksum) && FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory())) == null) {
                // Download file
                ClientManager.getInstance().getDataClient().requestFile(checksum);
            }
            File file;
            if((file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()))) != null)
                checksumMap.put(file.getAbsolutePath(), checksum);
        }

        List<String> removeList = new ArrayList<>();
        for (String checksum : checksumMap.values()) {
            if (!masterChecksumList.contains(checksum)) {
                // Delete file
                File file = FileUtils.getFileWithChecksum(checksum, FileUtils.getFileList(FileUtils.getDriveDirectory()));
                if (file != null) {
                    file.delete();
                    removeList.add(file.getAbsolutePath());
                }
            }
        }

        for (String path : removeList) {
            checksumMap.remove(path);
        }

        File[] localFileList = FileUtils.getFileList(FileUtils.getDriveDirectory());

        for (File file : localFileList) {
            // New File
            if (!checksumMap.containsKey(file.getAbsolutePath())) {
                checksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                // Upload new file
                uploadFile(file);

            } else if (checksumMap.containsKey(file.getAbsolutePath()) && !checksumMap.get(file.getAbsolutePath()).equals(FileUtils.MD5checksum(file))) {
                checksumMap.put(file.getAbsolutePath(), FileUtils.MD5checksum(file));

                // Update file
                uploadFile(file);
            }
        }

        List<String> deletedFiles = new ArrayList<>();
        for (String path : checksumMap.keySet()) {
            File file = new File(path);
            String checksum = checksumMap.get(path);

            if (!file.exists()) {
                deletedFiles.add(file.getAbsolutePath());
                // Delete file from master
                ClientManager.getInstance().getCommandClient().send(Constants.DELETE_FILE + "|" + checksum);
            }
        }

        for (String path : deletedFiles) {
            checksumMap.remove(path);
        }
    }

    /**
     * Compares the files between the master and the follower and synchronizes the files
     */
    private void checkFiles() {
        List<String> masterChecksumList = new ArrayList<>(Arrays.asList(ClientManager.getInstance().getCommandClient().send(Constants.GET_FILE_LIST).split("\\|")));
        if (masterChecksumList.size() == 1 && masterChecksumList.get(0).equals(""))
            masterChecksumList.clear();

        checkAndPrintFileDifferences(masterChecksumList);
        checkAndSyncFileDifferences(masterChecksumList);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        ClientManager.getInstance().disconnectAll();
    }

    /**
     * Sends the given file to the master
     *
     * @param file file to upload
     */
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