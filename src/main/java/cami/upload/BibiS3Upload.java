package cami.upload;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.net.URI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class BibiS3Upload implements IUpload {

    public void upload(String sourcePath, String linkFilePath) throws IOException {
	String fingerprint = null;
	File fileToUpload = null;
	URI uploadUri = null;

        System.out.println("upload starting using linkfile");
        File linkFile = new File(linkFilePath);
        if (!linkFile.exists()) {
            throw new IOException("file:" + linkFilePath + " does not exist.");
        }

	Scanner linkFileScanner = new Scanner(new FileReader(linkFile));
	try {
	    // fingerprint = linkFileScanner.next();
            uploadUri = new URI(linkFileScanner.next());	
	} catch (Exception e) {
	    System.out.println("Malformed linkfile");
	    e.printStackTrace();
	} finally {
	    linkFileScanner.close();
	}

	try {
	    fileToUpload = new File(sourcePath);
        } catch (Exception e) {
            System.out.println("file:" + sourcePath + " does not exist.");
            e.printStackTrace();
        }

	System.out.println(sourcePath);
	System.out.println(uploadUri);
	// System.out.println(fingerprint);


        startUpload(fileToUpload, uploadUri, fingerprint);
    }

    public void startUpload(File fileToUpload, URI uploadLink, String fingerprint) {

          CloseableHttpClient httpclient = HttpClients.createDefault();
          try {
             HttpPut httpput = new HttpPut(uploadLink);

             FileBody bin = new FileBody(fileToUpload);
             StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
             // StringBody fingerprintBody = new StringBody(fingerprint, ContentType.TEXT_PLAIN);

             HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("bin", bin)
                .addPart("comment", comment)
                .build();
                // .addPart("fingerprint", fingerprintBody)

             httpput.setEntity(reqEntity);

             System.out.println("executing request " + httpput.getRequestLine());
             CloseableHttpResponse response = httpclient.execute(httpput);
             try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                     System.out.println("Response content length: " +    resEntity.getContentLength());
                }
              EntityUtils.consume(resEntity);
             } finally {
                 response.close();
             }
	  } catch(Exception e) {
             System.out.println("Error with upload");
             e.printStackTrace();
          } finally {
	    try {
               httpclient.close();
            } catch (Exception e) {
               System.out.println("Error closing upload");
               e.printStackTrace();	
	    }
          }
    }
}
