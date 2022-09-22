import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


/**
 * 
 */

/**
 * @author gustavo
 *
 */
public class FTPProcess {

    public String server;
    public int port;
    public String user;
    public String password;
    public FTPClientConfig ftpClientConfig;
    private FTPClient ftp;

    void open() throws IOException {
        ftp = new FTPClient();

        //ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(user, password);
        if(ftpClientConfig != null)
        	ftp.configure(ftpClientConfig);

        ftp.enterLocalPassiveMode();

    }
	
    void sendFile(String file, String path) throws IOException {
        ftp.storeFile(path, new FileInputStream(file));
    }
    
    void downloadFile(String source, String destination) throws IOException {
        FileOutputStream out = new FileOutputStream(destination);
        ftp.retrieveFile(source, out);
    }
    
    void listFiles(String dir) throws IOException {
    	FTPFile[] files = ftp.listDirectories(dir);
    	for(int i = 0; i < files.length;i++){
    		System.out.println(files[i].getName());
    	}
    	files = ftp.listFiles(dir);
    	for(int i = 0; i < files.length;i++){
    		System.out.println(files[i].getName());
    	}
    }
    
    void close() throws IOException {
        ftp.disconnect();
    }
    
}
