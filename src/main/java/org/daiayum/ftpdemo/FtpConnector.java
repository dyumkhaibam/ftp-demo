package org.daiayum.ftpdemo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpConnector {

	Logger logger
		= LoggerFactory.getLogger(FtpConnector.class);

	public FTPClient connect() throws IOException
	{
		// Create an instance of FTPClient
		FTPSClient ftpClient = new FTPSClient(false);
		ftpClient.addProtocolCommandListener(new PrintCommandListener(System.out));
		try {
			// establish a connection with specific host and
			// port.
			ftpClient.connect("ftp.support.bonitasoft.com", 21);

			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				logger.info(
					"Operation failed. Server reply code: "
					+ replyCode);
				ftpClient.disconnect();
			}

			// login to ftp server with username and
			// password.
			boolean success
				= ftpClient.login("migration_csc", "UW@K>DMDJ$Y#l])%yb32");
			if (!success) {
				ftpClient.disconnect();
			}
			// assign file type according to the server.
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();

			// change specific directory of ftp server from
			// you want to download files.
			boolean changedRemoteDir
				= ftpClient.changeWorkingDirectory(
					"/");
			if (!changedRemoteDir) {
				logger.info("Remote directory not found.");
			}
		}
		catch (UnknownHostException E) {
			logger.info("No such ftp server");
		}
		catch (IOException e) {
			logger.info(e.getMessage());
		}
		return ftpClient;
	}
}
