package cami.download;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class SwiftDownload {

    public void downloadAll(String urlFile, String destination, String regex, int threads) {
      
        List<String> urls = new ArrayList<>();

	String tmpStem = null;

        try {
		BufferedReader reader = new BufferedReader(new FileReader(urlFile));

		// top line of urlfile is a non-downloadable stem,
		// so we can work out subdirectory paths
		tmpStem = reader.readLine();

		String inputUrl;
		while((inputUrl = reader.readLine()) != null) {
		    urls.add(inputUrl);
		}
        } catch (IOException e) {
            e.printStackTrace();
	}

	// hack to avoid changing ObjectStorageWrapper
	String[] stemComponents = tmpStem.split("/");
	int lastStemComponentLength = stemComponents[stemComponents.length-1].length()+1;
	tmpStem = tmpStem.substring(lastStemComponentLength);
	System.out.println(tmpStem);

	final String stem = tmpStem;
 
        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        try {
            forkJoinPool.submit(() ->
                    urls.stream().parallel()
                            .filter(url -> matchesRegex(regex, url, stem))
                            .forEach(url -> download(url, destination, stem))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean matchesRegex(String regex, String text, String stem) {

        String[] urlParts = text.split("[?]", 2);
        String bareUrl = urlParts[0];

	int pos = stem.length();
	String fileNamePrefix = bareUrl.substring(pos);

        return Pattern.compile(regex).matcher(fileNamePrefix).find();
    }


    public void download(String url, String destination, String stem) {

        String[] urlParts = url.split("[?]", 2);
        String bareUrl = urlParts[0];

        int responseCode = 0;

	int pos = stem.length();
	String fileNamePrefix = bareUrl.substring(pos);

	String fileNamePrefixDestination = Paths.get(destination, fileNamePrefix).toString();

	File destinationFile = new File(fileNamePrefixDestination);
	destinationFile.getParentFile().mkdirs();

        System.out.println(String.join(" ", "Downloading", url, "to", fileNamePrefixDestination));

        URL website;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        try (InputStream in = website.openStream()) {
            Files.copy(in, Paths.get(fileNamePrefixDestination), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        } catch (IOException e) {
       	    Matcher exMsgStatusCodeMatcher = Pattern.compile("^Server returned HTTP response code: (\\d+)").matcher(e.getMessage());
            if(exMsgStatusCodeMatcher.find()) {
                responseCode = Integer.parseInt(exMsgStatusCodeMatcher.group(1));
            } else if(e.getClass().getSimpleName().equals("FileNotFoundException")) {
                // 404 is a special case because it will throw a FileNotFoundException instead of having "404" in the message
                responseCode = 404;
            } else {
                // There can be other types of exceptions not handled here
                System.out.println("Exception (" + e.getClass().getSimpleName() + ") doesn't contain status code: " + e);
		System.exit(1);
            }

	    if (responseCode == 401) {
            	System.out.println("Link expired, aborting - please download link file again ((status code from parsing exception message: " + responseCode + ")");
		System.exit(2);
	    } else {
            	System.out.println("Status code from parsing exception message: " + responseCode);
		System.exit(3);
	    }
        }
    }
}
