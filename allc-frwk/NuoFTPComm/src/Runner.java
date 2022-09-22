import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * 
 */

/**
 * @author gustavo
 *
 */
public class Runner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Properties props = new Properties();
			FileInputStream is = new FileInputStream("NuoFTPComm.prop");
			props.load(is);
			if("-ts".equals(args[0])){
				Socket client = new Socket(args[1], Integer.valueOf(args[2]));
				System.out.println("Socket conectado");
				Thread.sleep(2000);
				client.close();
				System.out.println("Socket desconectado");
			} else if("-e".equals(args[0])){
				String user = "user=" + Encrypt.encriptar(args[1], "NuoFTP");
				String pass = "pass=" + Encrypt.encriptar(args[2], "NuoFTP");
				FileOutputStream fos = new FileOutputStream("NuoFTPComm.prop", false);
				String data = user + "\n" + pass;
				fos.write(data.getBytes(), 0, data.length());
				fos.close();
				System.out.println("User/pass encriptados");
			} else if("-ftp".equals(args[0]) || "-ftp4690".equals(args[0]) || "-ftpunix".equals(args[0])
					|| "-sftp".equals(args[0]) || "-sftp4690".equals(args[0]) || "-sftpunix".equals(args[0])){
				FTPProcess ftpProcess = new FTPProcess();
				ftpProcess.server = args[1];
				ftpProcess.port = 21;
				if("-ftp4690".equals(args[0]) || "-sftp4690".equals(args[0]))
					ftpProcess.ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_OS2);
				if("-ftpunix".equals(args[0]) || "-sftpunix".equals(args[0]))
					ftpProcess.ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
				if("-sftp".equals(args[0]) || "-sftp4690".equals(args[0]) || "-sftpunix".equals(args[0]))
					ftpProcess.port = 22;
				ftpProcess.user = Encrypt.desencriptar(props.getProperty("user"), "NuoFTP");
				ftpProcess.password = Encrypt.desencriptar(props.getProperty("pass"), "NuoFTP");
				ftpProcess.open();
				if("-l".equals(args[2])){
					ftpProcess.listFiles(args[3]);
//					ftpProcess.listFiles("F:/");
				} else if("-u".equals(args[2])){
					ftpProcess.sendFile(args[3], args[4]);
	//				ftpProcess.sendFile("/Users/gustavo/Documents/pepito", "F:/ALLC/pepito");
				} else if("-d".equals(args[2])){
					ftpProcess.downloadFile(args[3], args[4]);
	//				ftpProcess.downloadFile("F:/ALLC/pepito", "/Users/gustavo/Documents/pepito2");
				}
				ftpProcess.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
