package util;

import config.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private FileUtils() {
    }

    /**
     * Returns the desktop directory path of the current user
     *
     * @return Desktop Directory Path
     */
    public static String getDesktopDirectory() {
        return System.getProperty("user.home") + "/Desktop";
    }

    public static String getDriveDirectory() {
        return getDesktopDirectory() + "/" + Constants.SYNC_FOLDER_NAME;
    }

    public static File[] getFileList(String path) {
        File dir = new File(path);
        return dir.listFiles();
    }

    /**
     * Creates a new folder with the given file path
     *
     * @param path Directory Path
     */
    public static void createFolder(String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdir();
    }

    public static File getFileWithChecksum(String checksum, File[] list) {
        for (File file : list)
            if (MD5checksum(file).equals(checksum))
                return file;
        return null;
    }

    public static List<String> getChecksumList(File[] list) {
        List<String> result = new ArrayList<>();
        for(File file: list) {
            result.add(MD5checksum(file));
        }
        return result;
    }

    public static String MD5checksum(File input) {
        try (InputStream inputStream = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] block = new byte[Constants.BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return StringUtils.byteArrayToString(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDebugDriveDirectory() {
        if (Constants.DEBUG_MODE)
            return getDesktopDirectory() + "/" + Constants.SYNC_FOLDER_NAME + "-debug";
        return getDriveDirectory();
    }
}
