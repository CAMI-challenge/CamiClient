package cami.download;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class SwiftDownloadTest {
    // private static String CONTAINER_URL = "https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_TEST";
    private static String TEST_URL_FILE = "src/test/resources/test_url_file";


    // Replace New File with new TemporaryFolder().getRoot() for genuine temp path

    // @Rule
    public String tempFolder = new File("/tmp/test").getAbsolutePath();

    boolean foo = new File("/tmp/test").mkdirs();

    @Test
    public void canDownloadFile() throws Exception {
        String testFilePath = Paths.get(tempFolder, "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(TEST_URL_FILE, testFilePath, ".", 1);
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

    @Test
    public void canDownloadSpecificFile() throws Exception {
        String testFilePath = Paths.get(tempFolder, "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(TEST_URL_FILE, testFilePath, "log", 1);
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

}
