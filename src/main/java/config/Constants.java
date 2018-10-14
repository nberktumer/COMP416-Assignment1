package config;

public class Constants {
    public static final boolean DEBUG_MODE = true;

    public static final String SYNC_FOLDER_NAME = "comp416-drive";
    public static final int FILE_CHECK_TIME_INTERVAL = 10000;
    public static final int BUFFER_SIZE = 4096;
    public static final int RETRY_INTERVAL = 3000;
    public static final int NUM_TRIALS = 5;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static final String GET_FILE_LIST = "get_file_list";
    public static final String DELETE_FILE = "delete_file";
    public static final String NEW_FILE = "new_file";
    public static final String REQUEST_FILE_INFO = "request_file_info";
    public static final String REQUEST_FILE = "request_file";
}
