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

    public void downloadAll(String url, String destination, String regex, int threads) {
        List<String> files = getFiles(url);
        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        try {
            forkJoinPool.submit(() -> {
                files.stream().parallel().filter(file -> !file.endsWith("/")).filter(file -> matchesRegex(regex, file)).forEach(file -> download(url + "/" + file, Paths.get(destination, file).toString()));
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean matchesRegex(String regex, String text) {
        return Pattern.compile(regex).matcher(text).find();
    }

    private List<String> getFiles(String url) {
        List<String> fileList = new ArrayList<>();
        List<String> finalList = new ArrayList<>();
        boolean start = true;
        String lastElement = "";
        while (!fileList.isEmpty() || start) {
            start = false;
            URL website = null;
            try {
                website = new URL(url + lastElement);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try (InputStream in = website.openStream()) {
                fileList = new BufferedReader(new InputStreamReader(in,
                        StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
                if (fileList != null && !fileList.isEmpty()) {
                    finalList.addAll(fileList);
                    lastElement = "/?marker=" + fileList.get(fileList.size()-1);
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

    public void download(String url, String destination) {
        URL website = null;
        System.out.println(String.join(" ", "Downloading", url, "to", destination));
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try (InputStream in = website.openStream()) {
            File destinationFile = new File(destination);
            destinationFile.getParentFile().mkdirs();
            Files.copy(in, Paths.get(destination),
                    new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}