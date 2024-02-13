package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.exceptions.LoyaltyPointsServiceException;
import com.globant.javacodecamp.loyalty.model.Reward;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class LoyaltyPointsService {

  private final String username;
  private final String password;
  private final int port;
  private final String host;
  private final String connectionString;

  public LoyaltyPointsService(String username, String password, int port, String host, String connectionString) {
    this.username = username;
    this.password = password;
    this.port = port;
    this.host = host;
    this.connectionString = connectionString;
  }

  public List<Reward> process() {
    List<Reward> rewards = new ArrayList<>();
    String remoteFilePath = "/upload/customer-transactions/customers.csv";
    Session session = null;
    ChannelSftp channelSftp = null;
    try (var connection = createConnection()) {
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

      var preparedStatement = connection.prepareStatement(
              """
                      SELECT id, points, customer_id, date_created
                      FROM customer_points
                      WHERE customer_id IN (%s)
                      AND date_created >= ?;""".formatted(getParameterPlaceholders(pointsPerUser))
      );
      var paramIndex = 1;
      for (Long customerId : pointsPerUser.keySet()) {
        preparedStatement.setLong(paramIndex, customerId);
        paramIndex++;
      }
      preparedStatement.setDate(paramIndex, Date.valueOf(LocalDate.now().minusMonths(2)));

      var resultSet = preparedStatement.executeQuery();
      var update = connection.prepareStatement("""
                      UPDATE customer_points
                      SET points = ?
                      WHERE customer_id =?;""");
      var insert = connection.prepareStatement("""
                        INSERT INTO reward(rate,date_created,date_valid,customer_id, redeemed)
                        VALUES (?, ?, ?, ?, ?)""");
      var insertNewPoints = connection.prepareStatement("""
                        INSERT INTO customer_points(points, customer_id, date_created)
                        VALUES (?, ?, ?)""");
      while (resultSet.next()) {
        var customerId = resultSet.getLong("customer_id");
        var points = pointsPerUser.remove(customerId);
        var newPoints = points + resultSet.getInt("points");
        var dateCreated = resultSet.getDate("date_created").toLocalDate();
        Reward reward = null;
        var rewardValidDate = LocalDate.now().plusMonths(2);

        if (newPoints >= 150 && newPoints < 300 && dateCreated.isEqual(LocalDate.now().minusMonths(2))) {
          reward = new Reward(0,LocalDate.now(), customerId, 5, rewardValidDate, false);
          newPoints = 0;
        } else if (newPoints >= 300 && newPoints < 600 && dateCreated.isEqual(LocalDate.now().minusMonths(2))) {
          reward = new Reward(0, LocalDate.now(), customerId, 10, rewardValidDate, false);
          newPoints = 0;
        } else if (newPoints >= 600 && newPoints < 1500 && dateCreated.isEqual(LocalDate.now().minusMonths(2))) {
          reward = new Reward(0, LocalDate.now(), customerId, 15, rewardValidDate, false);
          newPoints = 0;
        }

        update.setInt(1,newPoints);
        update.setLong(2, customerId);
        update.addBatch();
        if (reward != null) {
          rewards.add(reward);
          insert.setInt(1, reward.rate());
          insert.setDate(2,Date.valueOf(reward.dateCreated()));
          insert.setDate(3,Date.valueOf(reward.dateValid()));
          insert.setLong(4,reward.customerId());
          insert.setBoolean(5,reward.redeemed());
          insert.addBatch();
        }

      }
      for (Map.Entry<Long, Integer> userPoints : pointsPerUser.entrySet()) {
        //points, customer_id, date_created
        insertNewPoints.setInt(1, userPoints.getValue());
        insertNewPoints.setLong(2, userPoints.getKey());
        insertNewPoints.setDate(3, Date.valueOf(LocalDate.now()));
        insertNewPoints.addBatch();
      }
      update.executeBatch();
      insert.executeBatch();
      insertNewPoints.executeBatch();
      return rewards;

    } catch (JSchException | SftpException | IOException | SQLException e) {
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

  private String getParameterPlaceholders(Map<Long, Integer> pointsPerUser) {
    StringBuilder params = new StringBuilder();
    for (int i = 0; i < pointsPerUser.size(); i++) {
      if (i > 0) {
        params.append(", ");
      }
      params.append("?");
    }
    return params.toString();
  }

  private Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return DriverManager
            .getConnection(connectionString, "root", "test");
  }
}
