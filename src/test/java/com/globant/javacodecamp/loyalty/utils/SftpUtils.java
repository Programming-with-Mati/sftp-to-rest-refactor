package com.globant.javacodecamp.loyalty.utils;

import com.jcraft.jsch.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class SftpUtils {

  public static void uploadFileToSftp(String username, String password, Integer port, String host, String fileContent, String filePathInServer) throws JSchException, SftpException {
    Session session;
    ChannelSftp channelSftp;
    JSch jsch = new JSch();
    session = jsch.getSession(username, host, port);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();

    channelSftp = (ChannelSftp) session.openChannel("sftp");
    channelSftp.connect();

    channelSftp.put(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)), filePathInServer,0777);
  }
}
