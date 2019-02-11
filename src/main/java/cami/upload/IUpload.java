package cami.upload;

import java.io.IOException;

public interface IUpload {
	void upload(String sourcePath, String linkFilePath) throws IOException;
}
