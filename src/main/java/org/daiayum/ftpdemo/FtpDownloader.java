package org.daiayum.ftpdemo;

import java.io.*;
import java.nio.file.Files;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpDownloader {
	static Logger logger = LoggerFactory.getLogger(FtpDownloader.class);

	public static void main(String[] args) throws IOException {
		// create a instance of FtpConnector
		FtpConnector ftpConnector = new FtpConnector();

		// get ftp client object.
		FTPClient ftpClient = ftpConnector.connect();

		// list all the files which will be downloaded.
		FTPFile[] ftpFiles = ftpClient.listFiles();

		// set downloading dir where all the files will be
		// stored on local directory.
//		String downloading_dir = "/Downloads/FtpDownloaded/";

		for (FTPFile file : ftpFiles) {
			System.out.println(file.getName());
			/*
			 * File fileObj = new File(downloading_dir + file.getName());
			 * Files.createFile(fileObj.toPath()); try (OutputStream outputStream = new
			 * BufferedOutputStream(new FileOutputStream(fileObj))) {
			 * 
			 * // ftpclient.retrieveFile will get the file // from Ftp server and write it
			 * in // outputStream. boolean isFileRetrieve =
			 * ftpClient.retrieveFile(file.getName(), outputStream);
			 * logger.info("{} file is downloaded : {}", file.getName(), isFileRetrieve); }
			 */
		}
	}
}