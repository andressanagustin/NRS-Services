/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.sftp.client;

import java.io.FileInputStream;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class SFTPClient {

    final Logger LOGGER = Logger.getLogger(SFTPClient.class);

    // For FTP server
    private String hostName;
    private String hostPort;
    private String userName;
    private String passWord;
    private String destinationDir;
    private Boolean usePublic;

    // For sFTP server
    private ChannelSftp channelSftp = null;
    private Session session = null;
    private Channel channel = null;

    private int userGroupId = 0;

    public SFTPClient() {

    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }

    public int getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(int userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Boolean getUsePublic() {
        return usePublic;
    }

    public void setUsePublic(Boolean usePublic) {
        this.usePublic = usePublic;
    }

    private void initChannelSftp() {
        channelSftp = null;
        session = null;
        try {

            JSch jsch = new JSch();
            //
            jsch.setKnownHosts("/root/.ssh/known_hosts");

            session = jsch.getSession(userName, hostName, Integer.valueOf(hostPort));
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            // logger.info("get Session end");
            session.setPassword(passWord);

        } catch (JSchException ex) {
            LOGGER.error(ex);
        } catch (NumberFormatException ex) {
            LOGGER.error(ex);
        }
    }

    /*
     * Upload file to ftp server that has configuration on sysConfig.properties
     * filename: name of file that will be stored on ftp fis: input stream of
     * file that will be stored on ftp enableLog: enable log return value: URN
     */
    public boolean uploadFileToFTP(String filename, InputStream fis) {
        boolean result = false;
        initChannelSftp();
        try {
            // logger.info("session connect begin");
            if (!session.isConnected()) {
                session.connect();
            }
            // logger.info("session connect end");
            channel = session.openChannel("sftp");

            // logger.info("channel connect begin");
            channel.connect();
            // logger.info("channel connect end");
            channelSftp = (ChannelSftp) channel;
            LOGGER.info(channelSftp.getId() + " is conected " + channelSftp.getHome());
            try {
                LOGGER.info("destination" + destinationDir);
                channelSftp.cd(destinationDir);
                // logger.info("cd relative Dir");
            } catch (SftpException e) {
                LOGGER.error(e.getMessage(), e);
                try {
                    String[] complPath = destinationDir.split("/");
                    channelSftp.cd("/");
                    for (String folder : complPath) {
                        if (folder.length() > 0) {
                            try {
                                System.out.println("Current Dir : " + channelSftp.pwd());
                                channelSftp.cd(folder);
                            } catch (SftpException e2) {
                                channelSftp.mkdir(folder);
                                channelSftp.cd(folder);
                            }
                        }
                    }
                    //channelSftp.mkdir(destinationDir);
                    channelSftp.cd(destinationDir);
                } catch (SftpException ez) {
                    LOGGER.error(ez.getMessage(), ez);
                    channelSftp.exit();
                    channel.disconnect();
                    session.disconnect();
                    return false;
                }
            }

            channelSftp.put(fis, filename);
            LOGGER.info("Upload successful portfolio file name:" + filename);
            result = true;

            try {
                fis.close();
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        } catch (JSchException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (SftpException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return result;
    }

    public boolean uploadFileToFTP(String desFileName, String srcFilePath) {
        boolean result = false;
        try {
            InputStream fis = new FileInputStream(srcFilePath);
            result = uploadFileToFTP(desFileName, fis);
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex);
        }

        return result;
    }

    public boolean checkExist(String fileName) {
        boolean existed = false;

        initChannelSftp();
        try {
            if (!session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }

            List<ChannelSftp.LsEntry> ls = channelSftp.ls(destinationDir);
            if (ls != null) {
                // Iterate listing.
                LOGGER.info(fileName);
                for (LsEntry entry : ls) {
                    String file_name = entry.getFilename();
                    if (!entry.getAttrs().isDir()) {
                        if (fileName.toLowerCase().startsWith(file_name)) {
                            existed = true;
                            break;
                        }
                    }
                }
            }
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        } catch (JSchException ex) {
            LOGGER.error(ex.getMessage(), ex);
            existed = false;
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (SftpException ex) {
            LOGGER.error(ex.getMessage(), ex);
            existed = false;
            if (session.isConnected()) {
                session.disconnect();
            }
        }

        return existed;
    }

    public void deleteFile(String fileName) {

        initChannelSftp();
        try {
            if (!session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
            channelSftp.rm(fileName);
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();

        } catch (JSchException ex) {
            LOGGER.error(ex.getMessage(), ex);
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (SftpException ex) {
            LOGGER.error(ex.getMessage(), ex);
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public List<ChannelSftp.LsEntry> getListFiles() {
        initChannelSftp();
        try {
            if (!session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
            List<ChannelSftp.LsEntry> ls = channelSftp.ls(destinationDir);
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
            return ls;
        } catch (JSchException ex) {
            LOGGER.info(ex.getMessage());
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (SftpException ex) {
            LOGGER.info(ex.getMessage());
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        return null;
    }

    public InputStream getFile(String fileName) {
        initChannelSftp();
        try {
            if (!session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
            InputStream is = channelSftp.get(fileName);

            return is;
        } catch (JSchException ex) {
            LOGGER.info(ex.getMessage());
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (SftpException ex) {
            LOGGER.info(ex.getMessage());
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        return null;
    }

    public void closeConection() {
        if (session.isConnected()) {
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        }
    }
}
