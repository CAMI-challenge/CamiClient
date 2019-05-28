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
import org.apache.http.entity.FileEntity;

public class BibiS3Upload implements IUpload {

    public void upload(String sourcePath, String linkFilePath) throws IOException {
	String fingerprint = null;
	File fileToUpload = null;
	URI uploadUri = null;

        System.out.println("uploading " + sourcePath);
        File linkFile = new File(linkFilePath);
        if (!linkFile.exists()) {
            throw new IOException("file:" + linkFilePath + " does not exist.");
        }

	Scanner linkFileScanner = new Scanner(new FileReader(linkFile));
	try {
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

        startUpload(fileToUpload, uploadUri, fingerprint);
    }

    public void startUpload(File fileToUpload, URI uploadLink, String fingerprint) {

          CloseableHttpClient httpclient = HttpClients.createDefault();
          try {
             HttpPut httpput = new HttpPut(uploadLink);
	     httpput.addHeader("content-type", "application/x-www-form-urlencoded;charset=utf-8");

	     FileEntity entity = new FileEntity(fileToUpload);

             httpput.setEntity(entity);

             CloseableHttpResponse response = httpclient.execute(httpput);
	     int statusCode = 0;
             try {
                statusCode = response.getStatusLine().getStatusCode();

                HttpEntity resEntity = response.getEntity();
              EntityUtils.consume(resEntity);
             } finally {
		 if (statusCode == 201) {
			System.out.println("Upload successful");
		 } else {
			System.out.println("Upload failed with status code == " + statusCode);
		 }	
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
