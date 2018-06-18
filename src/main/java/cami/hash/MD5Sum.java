package cami.hash;

import cami.CLI;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Sum implements IHashAlgorithm {
    public String getFingerprint(String path) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(path)) {
                byte[] dataBytes = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, bytesRead);
                }
                byte[] digestBytes = md.digest();
                // convert the byte to hex format
                String bytesString = javax.xml.bind.DatatypeConverter.printHexBinary(digestBytes);
                return bytesString + CLI.VERSION.replaceAll("\\.", "");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
