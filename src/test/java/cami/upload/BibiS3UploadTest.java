package cami.upload;

import cami.objectstoragewrapper.core.ICredentials;
import cami.objectstoragewrapper.aws.ResourceAction;
import cami.objectstoragewrapper.aws.S3CredentialsBuilder;
import cami.objectstoragewrapper.main.FileManagerFactory;
import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.conditions.StringCondition;
import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BibiS3UploadTest {
    private static final String FINGERPRINT = "FINGERPRINT";
    private static BibiS3Upload upload;
    private static String credentialsPath = "/home/belmann/.aws/credentials";
    private static String profile = "default";
    private static String bucketName = "cami-upload";
    private static Map<String, String> credentialsMap;
    private static IFileManager manager;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    //@BeforeClass
    public static void initCredentials() {
        S3CredentialsBuilder builder = new S3CredentialsBuilder(credentialsPath, profile);

        Condition[] conditions = new Condition[]{
                new StringCondition(StringCondition.StringComparisonType.StringLike, "s3:prefix",
                        "junit/camiClient/bibiS3UploadTest/testUpload/*")
        };

        builder.addResourceAction(new ResourceAction(bucketName, "", S3Actions.ListObjects, conditions));
        builder.addResourceAction(new ResourceAction(bucketName, "/junit/camiClient/bibiS3UploadTest/testUpload/*",
                S3Actions.AllS3Actions));
        ICredentials credentials = builder.getCredentials(129600, "bob");

        credentialsMap = new HashMap<>();
/*
        credentialsMap.put(BibiS3Upload.ACCESS_KEY, credentials.getAccessKey());
        credentialsMap.put(BibiS3Upload.SECRET_KEY, credentials.getSecretAccessKey());
        credentialsMap.put(BibiS3Upload.SESSION_TOKEN, credentials.getSessionToken());
        credentialsMap.put(BibiS3Upload.PATH, "s3://" + bucketName + "/junit/camiClient/bibiS3UploadTest/testUpload/");
        credentialsMap.put(BibiS3Upload.FINGERPRINT, FINGERPRINT);

        System.out.println(credentials.getAccessKey());
        System.out.println(credentials.getSecretAccessKey());
        System.out.println(credentials.getSessionToken());
*/
    }

    //@BeforeClass
    public static void initBibiUpload() {
        upload = new BibiS3Upload();
    }

    //@BeforeClass
    public static void createFileManager() {
        manager = FileManagerFactory.getAWSManager(credentialsPath, profile, bucketName);
    }

    //@Test
    public void testUpload() {
        final String path = "src/test/resources/binning.txt";

        exit.expectSystemExit();
        exit.checkAssertionAfterwards(() -> {
            List<IFile> list = manager.list("junit/camiClient/bibiS3UploadTest/testUpload/");
            assertEquals(list.size(), 1);
            manager.delete("junit/camiClient/bibiS3UploadTest/testUpload/binning.txt");
        });
/*
        upload.startUpload(credentialsMap.get(BibiS3Upload.ACCESS_KEY),
                credentialsMap.get(BibiS3Upload.SECRET_KEY),
                credentialsMap.get(BibiS3Upload.SESSION_TOKEN),
                credentialsMap.get(BibiS3Upload.FINGERPRINT),
                path,
                credentialsMap.get(BibiS3Upload.PATH));
*/
    }

    //@Test
    public void testFingerPrint() {
        String path = "src/test/resources/binning.txt";
        exit.expectSystemExit();
        exit.checkAssertionAfterwards(() -> {
            Map<String, String> metadataMap = manager.getObjectMetadata(
                    "junit/camiClient/bibiS3UploadTest/testUpload/binning.txt");
            // assertEquals(FINGERPRINT, metadataMap.get(BibiS3Upload.FINGERPRINT));
            manager.delete("junit/camiClient/bibiS3UploadTest/testUpload/binning.txt");
        });
/*
        upload.startUpload(credentialsMap.get(BibiS3Upload.ACCESS_KEY),
                credentialsMap.get(BibiS3Upload.SECRET_KEY),
                credentialsMap.get(BibiS3Upload.SESSION_TOKEN),
                credentialsMap.get(BibiS3Upload.FINGERPRINT),
                path,
                credentialsMap.get(BibiS3Upload.PATH));
*/
    }
}
