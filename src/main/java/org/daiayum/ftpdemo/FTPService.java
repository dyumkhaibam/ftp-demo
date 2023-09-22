package org.daiayum.ftpdemo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FTPService {

	@Autowired
	private Environment env;

	Logger logger = LoggerFactory.getLogger(getClass());

	public void testConnection() throws IOException {
		logger.info("Starting connection...12345");

		FTPSClient ftpClient = new FTPSClient("TLS", false);
		// FTPSClient ftpClient = new FTPSClient("TLS");
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		ftpClient.setDefaultTimeout(5 * 60 * 1000); // 4 minutes
		ftpClient.setDataTimeout(Duration.ofMillis(5 * 60 * 1000));
		ftpClient.setBufferSize(1024 * 1024);
		ftpClient.setConnectTimeout(5 * 60 * 1000);

		String host = env.getProperty("ftp.host");
		String userName = env.getProperty("ftp.username");
		String password = env.getProperty("ftp.password");

		ftpClient.enterLocalPassiveMode();
		logger.info("Connecting to hoost {}", host);
		ftpClient.connect(host, 21);

		ftpClient.execPBSZ(0);
		ftpClient.execPROT("P");
		ftpClient.enterLocalPassiveMode();
		ftpClient.login(userName, password);
		logger.info("Logged in status {}", ftpClient.getStatus());
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		try {
			logger.info("&&&&&&&&&&&&&&&&&&&&Reading files...");
			FTPFile[] files = ftpClient.listFiles("27747");
			logger.info("%%%%%%%%%%%%%%%%%%Read files: {}", files != null ? files.length : 0);

			if (files != null && files.length > 0) {

				DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				for (FTPFile file : files) {
					String details = file.getName();
					if (file.isDirectory()) {
						details = "[" + details + "]";
					}
					details += "\t\t" + file.getSize();
					details += "\t\t" + dateFormatter.format(file.getTimestamp().getTime());
					logger.info("Read file: {}", details);
				}
			}

		} catch (Exception e) {
			logger.error("Error reading files. Error: {}", e.getMessage());
		} finally {
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}

	@PostConstruct
	public void readFiles() {

		FTPSClient ftpClient = getFTPSClient(env.getProperty("ftp.host"), 21, env.getProperty("ftp.username"),
				env.getProperty("ftp.password"));
		String[] caseNumbers = new String[] { "27747", "axsdrf", "27741" };
		List<String> ignoredFiles = new ArrayList<>(Arrays.asList(".htaccess", ".htpasswd", ".directory"));
		for (String caseNumber : caseNumbers) {
			try {
				FTPFile[] files = ftpClient.listFiles(caseNumber);
				for (FTPFile file : files) {
					if (!file.isDirectory()) {
						if (!ignoredFiles.contains(file.getName())) {
							logger.info("Reading file. Case Number {}. File Name: {}.", caseNumber, file.getName());
							File tempFile = File.createTempFile(file.getName(), "");
							OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
							InputStream inputStream = ftpClient.retrieveFileStream(caseNumber + "/" + file.getName());
							byte[] bytesArray = new byte[4096];
							int bytesRead = -1;
							while ((bytesRead = inputStream.read(bytesArray)) != -1) {
								outputStream.write(bytesArray, 0, bytesRead);
							}

							boolean success = ftpClient.completePendingCommand();
							if (success) {
								logger.info("Reading file completed. Case Number {}. File Name: {}.", caseNumber,
										file.getName());
							}
							outputStream.close();
							inputStream.close();
						}
					}
				}

			} catch (IOException e) {
				logger.warn("Error reading file for case number " + caseNumber + ". Error: " + e.getMessage());
			}
		}

		try {
			if (ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public FTPSClient getFTPSClient(String server, int port, String userName, String password) {
		FTPSClient ftpClient = new FTPSClient("TLS", false);
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		ftpClient.setDefaultTimeout(5 * 60 * 1000); // 5 minutes
		ftpClient.setDataTimeout(Duration.ofMillis(5 * 60 * 1000L));
		ftpClient.setBufferSize(1024 * 1024);
		ftpClient.setConnectTimeout(5 * 60 * 1000);
		ftpClient.enterLocalPassiveMode();
		logger.info("Connecting to FTP server {}", server);
		try {
			ftpClient.connect(server, port);
			ftpClient.execPBSZ(0);
			ftpClient.execPROT("P");
			ftpClient.enterLocalPassiveMode();
			ftpClient.login(userName, password);
			logger.info("Login status: {}", ftpClient.getStatus());
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (Exception e) {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			throw new RuntimeException("Error connecting to FTP server. Error: {}" + e.getMessage());
		}

		return ftpClient;
	}
}