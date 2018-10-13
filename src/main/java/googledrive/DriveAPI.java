package googledrive;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DriveAPI {

    private static final String APPLICATION_NAME = "DriveCloud";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Global Drive API client.
     */
    private static Drive drive;

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static HashMap getFileMap() throws IOException {
        FileList files = drive.files().list()
                .setFields("nextPageToken, files(id, name, md5Checksum)")
                .execute();

        List<File> fileList = files.getFiles();
        if (fileList == null || fileList.isEmpty()) {
            System.out.println("No files found.");
            return null;
        } else {
            HashMap<String, String> fileMap = new HashMap<>();
            for (File file : fileList) {
                fileMap.put(file.getName(), file.getMd5Checksum());
            }
            return fileMap;
        }
    }

    public void updateFile(String fileName, java.io.File newFileContent, String newMimeType) throws IOException {
        FileList files = drive.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> fileList = files.getFiles();
        if (fileList == null || fileList.isEmpty()) {
            createFile(fileName, newFileContent, newMimeType);
        } else {
            String fileId = null;
            File fileToUpdate = null;
            for (File file : fileList) {
                if (file.getName().equals(fileName)) {
                    fileId = file.getId();
                    fileToUpdate = file;
                }
            }

            if (fileId == null) {
                createFile(fileName, newFileContent, newMimeType);
                return;
            }

            FileContent fileContent = new FileContent(newMimeType, newFileContent);
            drive.files().update(fileId, fileToUpdate, fileContent).execute();
        }
    }

    private void createFile(String fileName, java.io.File fileContent, String mimeType) throws IOException {
        File file = new File();
        file.setName(fileName);
        file.setMimeType(mimeType);
        File newFile = drive.files().create(file)
                .setFields("id")
                .execute();

        FileContent content = new FileContent(mimeType, fileContent);
        drive.files().update(newFile.getId(), newFile, content).execute();
    }

    public void deleteFile(String fileName) throws IOException {
        FileList files = drive.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> fileList = files.getFiles();
        if (fileList == null || fileList.isEmpty()) {
            System.out.println("No files to be deleted!");
        } else {
            String fileId = null;
            for (File file : fileList) {
                if (file.getName().equals(fileName)) {
                    fileId = file.getId();
                }
            }

            if (fileId == null) {
                System.out.println("No files to be deleted!");
                return;
            }

            drive.files().delete(fileId).execute();
        }
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

