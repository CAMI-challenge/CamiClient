package cami.upload;

import java.io.IOException;

public interface IUpload {

	public void upload(String sourcePath,
			String credentialsPath) throws IOException;
	
}
