package org.daiayum.ftpdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;

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
	
	@PostConstruct
	public void testConnection() throws IOException{
		logger.info("Starting connection...123");

		//FTPSClient ftpClient = new FTPSClient("TLS", true);
        FTPSClient ftpClient = new FTPSClient("TLS");
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftpClient.setDefaultTimeout(5*60*1000); // 4 minutes
        ftpClient.setDataTimeout(Duration.ofMillis(5*60*1000));
        ftpClient.setBufferSize(1024*1024);
        ftpClient.setConnectTimeout(5*60*1000);
      //ftpClient.execPBSZ(0);
      //ftpClient.execPROT("P");

        String host = env.getProperty("ftp.host");
        String userName = env.getProperty("ftp.username");
        String password = env.getProperty("ftp.password");
        
		ftpClient.enterLocalPassiveMode();
		logger.info("Connecting to hoost {}", host);
		ftpClient.connect(host, 21);
        ftpClient.enterLocalPassiveMode();
        ftpClient.login(userName, password);
        logger.info("Logged in: status {}"+ftpClient.getStatus());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        try {
            logger.info("&&&&&&&&&&&&&&&&&&&&Reading files...");
            FTPFile[] files = ftpClient.listFiles();
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
            	
        }catch (Exception e){
        	logger.error("Error reading files. Error: {}", e.getMessage());
        }finally {
            ftpClient.logout();
            ftpClient.disconnect();        	
        }
	}
}