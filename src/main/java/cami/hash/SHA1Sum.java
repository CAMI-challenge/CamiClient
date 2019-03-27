package cami.hash;

import cami.CLI;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Sum implements IHashAlgorithm {
    public String getFingerprint(String path) {
	return getFingerprint(path, false);
    }

    public String getFingerprint(String path, boolean hideVersion) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
            try (FileInputStream fis = new FileInputStream(path)) {
                byte[] dataBytes = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, bytesRead);
                }
                byte[] digestBytes = md.digest();
                // convert the byte to hex format
                String bytesString = javax.xml.bind.DatatypeConverter.printHexBinary(digestBytes);

		if (!hideVersion) {
			bytesString = bytesString + CLI.VERSION.replaceAll("\\.", "");
		}
	
                return bytesString;
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
