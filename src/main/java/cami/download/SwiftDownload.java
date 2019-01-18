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

    public void downloadAll(String source, String destination, String regex, int threads) {
	if (source.substring(0,8).equals("https://") || source.substring(0,7).equals("http://")) {
		urlDownloadAll(source, destination, regex, threads);
	} else {
		fileDownloadAll(source, destination, regex, threads);
	}
    }

    public void urlDownloadAll(String url, String destination, String regex, int threads) {
        List<String> files = getFiles(url);
        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        try {
            forkJoinPool.submit(() ->
                    files.stream().parallel()
                            .filter(file -> !file.endsWith("/"))
                            .filter(file -> urlMatchesRegex(regex, file))
                            .forEach(file -> urlDownload(url + "/" + file, Paths.get(destination, file).toString()))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void fileDownloadAll(String urlFile, String destination, String regex, int threads) {
      
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
                            .filter(url -> fileMatchesRegex(regex, url, stem))
                            .forEach(url -> fileDownload(url, destination, stem))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean urlMatchesRegex(String regex, String text) {
        return Pattern.compile(regex).matcher(text).find();
    }

    public static boolean fileMatchesRegex(String regex, String text, String stem) {

        String[] urlParts = text.split("[?]", 2);
        String bareUrl = urlParts[0];

	int pos = stem.length();
	String fileNamePrefix = bareUrl.substring(pos);

        return Pattern.compile(regex).matcher(fileNamePrefix).find();
    }

    public void urlDownload(String url, String destination) {
        System.out.println(String.join(" ", "Downloading", url, "to", destination));
        URL website;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        try (InputStream in = website.openStream()) {
            File destinationFile = new File(destination);
            destinationFile.getParentFile().mkdirs();
            Files.copy(in, Paths.get(destination), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileDownload(String url, String destination, String stem) {

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

    private List<String> getFiles(String url) {
        List<String> fileList = new ArrayList<>();
        List<String> finalList = new ArrayList<>();
        boolean start = true;
        String lastElement = "";
        while (!fileList.isEmpty() || start) {
            start = false;
            URL website;
            try {
                website = new URL(url + lastElement);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                break;
            }
            try (InputStream in = website.openStream()) {
                fileList = new BufferedReader(new InputStreamReader(in,
                        StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
                if (fileList != null && !fileList.isEmpty()) {
                    finalList.addAll(fileList);
                    lastElement = "/?marker=" + fileList.get(fileList.size() - 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return finalList;
    }

    public String list(String url) {
        return String.join(System.getProperty("line.separator"), getFiles(url));
    }

}
