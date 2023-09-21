package org.daiayum.ftpdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPService {
	Logger logger = LoggerFactory.getLogger(getClass());

	public void testConnection() throws IOException{
		logger.info("Starting connection...123");

		String server = "ftp.support.bonitasoft.com";
        int port = 21;
        String user = "migration_csc";
        String pass = "UW@K>DMDJ$Y#l])%yb32";

      //FTPSClient ftpClient = new FTPSClient("TLS", true);
        FTPSClient ftpClient = new FTPSClient("TLS");
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftpClient.setDefaultTimeout(5*60*1000); // 4 minutes
      //ftpClient.setDataTimeout(240000);
        ftpClient.setDataTimeout(Duration.ofMillis(5*60*1000));
        ftpClient.setBufferSize(1024*1024);
        ftpClient.setConnectTimeout(5*60*1000);
      //ftpClient.execPBSZ(0);
      //ftpClient.execPROT("P");

		ftpClient.enterLocalPassiveMode();
	    ftpClient.connect(server, port);
        ftpClient.enterLocalPassiveMode();
        ftpClient.login(user, pass);
        logger.info("Logged in: status {}"+ftpClient.getStatus());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        logger.info("**************..."+ftpClient.list());
        try {
            logger.info("&&&&&&&&&&&&&&&&&&&&Reading files...");
            FTPFile[] files = ftpClient.listFiles("/27745");
        	logger.info("*********************[GET] Files found on server: '" + files.length + "'");
            logger.info("%%%%%%%%%%%%%%%%%%Read files: {}", files != null ? files.length : 0);
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (FTPFile file : files) {
            	logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@Reading files...");
                String details = file.getName();
                if (file.isDirectory()) {
                    details = "[" + details + "]";
                }
                details += "\t\t" + file.getSize();
                details += "\t\t" + dateFormatter.format(file.getTimestamp().getTime());
                logger.info(details);
            }	
        }catch (Exception e){
        	logger.error("Error reading files. Error: {}", e.getMessage());
        }finally {
            ftpClient.logout();
            ftpClient.disconnect();        	
        }

 

 

	}
}