package cami.download;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class SwiftDownloadTest {

    private static String CONTAINER_URL = "https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_TEST";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canDownloadFile() throws Exception {
        String testFilePath = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(CONTAINER_URL,  testFilePath, "." ,1);
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

    @Test
    public void canDownloadSpecificFile() throws Exception {
        String testFilePath = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(CONTAINER_URL,  testFilePath, "log" ,1);
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

    @Test
    public void canListBucket() throws Exception {
        SwiftDownload download = new SwiftDownload();
        assertTrue(download.list(CONTAINER_URL).contains("tmux-client-14010.log"));
    }
}
