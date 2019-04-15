package cami.download;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;
import cami.hash.SHA1Sum;

import static org.junit.Assert.assertTrue;

public class SwiftDownloadTest {
    private static String CONTAINER_URL = "https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_TEST";
    private static String TEST_URL_FILE = "src/test/resources/test_url_file";


    // Replace New File with new TemporaryFolder().getRoot() for genuine temp path

    // @Rule
    public String fileTempFolder = new File("/tmp/test").getAbsolutePath();

    @Rule
    public TemporaryFolder urlTempFolder = new TemporaryFolder();

    boolean foo = new File("/tmp/test").mkdirs();

/*
    @Test
    public void fileCanDownloadFile() throws Exception {
        String testFilePath = Paths.get(fileTempFolder, "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(TEST_URL_FILE, testFilePath, ".", 1, false, false, new SHA1Sum());
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }
*/

    @Test
    public void urlCanDownloadFile() throws Exception {
        String testFilePath = Paths.get(urlTempFolder.getRoot().getAbsolutePath(), "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(CONTAINER_URL, testFilePath, ".", 1, false, false, new SHA1Sum());
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

/*
    @Test
    public void fileCanDownloadSpecificFile() throws Exception {
        String testFilePath = Paths.get(fileTempFolder, "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(TEST_URL_FILE, testFilePath, "log", 1, false, false, new SHA1Sum());
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }
*/

    @Test
    public void urlCanDownloadSpecificFile() throws Exception {
        String testFilePath = Paths.get(urlTempFolder.getRoot().getAbsolutePath(), "test").toString();
        SwiftDownload download = new SwiftDownload();
        download.downloadAll(CONTAINER_URL, testFilePath, "log", 1, false, false, new SHA1Sum());
        File file = new File(testFilePath);
        assertTrue(file.exists());
    }

}
