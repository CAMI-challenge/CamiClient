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
import java.util.stream.Collectors;

public class SwiftDownload {

    public void downloadAll(String urlFile, String destination, String regex, int threads) {
      
        List<String> urls = new ArrayList<>();

        try {
		BufferedReader reader = new BufferedReader(new FileReader(urlFile));
		String inputUrl;
		while((inputUrl = reader.readLine()) != null) {
		    urls.add(inputUrl);
		}
        } catch (IOException e) {
            e.printStackTrace();
	}
 
        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        try {
            forkJoinPool.submit(() ->
                    urls.stream().parallel()
                            .filter(url -> matchesRegex(regex, url))
                            .forEach(url -> download(url, destination))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean matchesRegex(String regex, String text) {

        String[] urlParts = text.split("[?]", 2);
        String bareUrl = urlParts[0];
        String params = "";
        if (urlParts.length == 2) {
          params = "?" + urlParts[1];
        }

        return Pattern.compile(regex).matcher(bareUrl).find();
    }


    public void download(String url, String destination) {

        String[] urlParts = url.split("[?]", 2);
        String bareUrl = urlParts[0];
        String params = "";
        if (urlParts.length == 2) {
          params = "?" + urlParts[1];
        }

	String[] bareUrlParts = bareUrl.split("/");
	String file = Paths.get(destination, bareUrlParts[bareUrlParts.length - 1]).toString();

        System.out.println(String.join(" ", "Downloading", url, "to", file));
        URL website;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        try (InputStream in = website.openStream()) {
            File destinationFile = new File(file);
            destinationFile.getParentFile().mkdirs();
            Files.copy(in, Paths.get(file), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
