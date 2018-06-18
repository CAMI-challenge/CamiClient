package cami.hash;

import cami.CLI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Sum implements IHashAlgorithm {

	@Override
	public String getFingerprint(String path) {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

			try (FileInputStream fis = new FileInputStream(path)) {

				// FileInputStream fis = new FileInputStream(path);
				byte[] dataBytes = new byte[8 * 1024];

				int nread = 0;

				while ((nread = fis.read(dataBytes)) != -1) {
					md.update(dataBytes, 0, nread);
				}
				byte[] mdbytes = md.digest();
				// convert the byte to hex format
                String bytesString = javax.xml.bind.DatatypeConverter.printHexBinary(mdbytes);
                return bytesString + CLI.VERSION.replaceAll("\\.","");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
