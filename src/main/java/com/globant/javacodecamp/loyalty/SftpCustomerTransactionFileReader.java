package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.exceptions.LoyaltyPointsServiceException;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SftpCustomerTransactionFileReader {

  private final String username;
  private final String password;
  private final int port;
  private final String host;

  public SftpCustomerTransactionFileReader(String username, String password, int port, String host) {
    this.username = username;
    this.password = password;
    this.port = port;
    this.host = host;
  }

  public Map<Long, Integer> readCustomerTransactionsFile() {
    String remoteFilePath = "/upload/customer-transactions/customers.csv";
    Session session = null;
    ChannelSftp channelSftp = null;
    try {
      JSch jsch = new JSch();
      session = jsch.getSession(username, host, port);
      session.setPassword(password);
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();

      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();

      Map<Long, Integer> pointsPerUser = new HashMap<>();
      var reader = new BufferedReader(new InputStreamReader(channelSftp.get(remoteFilePath)));
      String line = reader.readLine();
      while (line != null) {
        var csvTransaction = line.split(",");
        var customerId = Long.parseLong(csvTransaction[0]);
        var points = 0;
        if (pointsPerUser.containsKey(customerId)) {
          points = pointsPerUser.get(customerId);
        }
        points += Integer.parseInt(csvTransaction[1]);

        pointsPerUser.put(customerId, points);
        line = reader.readLine();
      }

      return pointsPerUser;

    } catch (JSchException | SftpException | IOException e) {
      throw new LoyaltyPointsServiceException(e);
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }
}
