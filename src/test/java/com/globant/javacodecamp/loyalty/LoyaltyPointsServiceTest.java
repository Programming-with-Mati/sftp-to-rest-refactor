package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.model.CustomerPoints;
import com.globant.javacodecamp.loyalty.repositories.CustomerPointsRepository;
import com.globant.javacodecamp.loyalty.repositories.RewardRepository;
import com.globant.javacodecamp.loyalty.utils.SftpUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class LoyaltyPointsServiceTest {

  @Container
  private MySQLContainer<?> mysql = getMySQLContainer();
  @Container
  private GenericContainer<?> sftp = createSftpServer();

  @Test
  void testWhenCustomerDoesntHavePoints() throws Exception {
    sftp.start();
    var customerPointsRepository = new CustomerPointsRepository(mysql.getJdbcUrl());
    var rewardRepository = new RewardRepository(mysql.getJdbcUrl());
    var username = "test";
    var password = "pass";
    var port = sftp.getMappedPort(22);
    var host = sftp.getHost();
    var fileReader = new SftpCustomerTransactionFileReader(username, password, port, host);
    var loyaltyPointsService = new LoyaltyPointsService(
            customerPointsRepository,
            rewardRepository
    );
    var fileContent = """
            2,10""";

    SftpUtils.uploadFileToSftp(username, password, port, host, fileContent, "/upload/customer-transactions/customers.csv");

    var pointsByUser = fileReader.readCustomerTransactionsFile();
    var rewards = loyaltyPointsService.process(pointsByUser);

    System.out.println(rewards);

    var points = customerPointsRepository.findAll();

    System.out.println(points);
    assertEquals(1, points.size());
    assertEquals(10, points.get(0).points());
  }

  @Test
  void testWhenCustomerHasPointsButNotEnoughTimePassed() throws Exception {
    sftp.start();
    var customerPointsRepository = new CustomerPointsRepository(mysql.getJdbcUrl());
    var rewardRepository = new RewardRepository(mysql.getJdbcUrl());
    var username = "test";
    var password = "pass";
    var port = sftp.getMappedPort(22);
    var host = sftp.getHost();
    var fileReader = new SftpCustomerTransactionFileReader(username, password, port, host);
    var loyaltyPointsService = new LoyaltyPointsService(
            customerPointsRepository,
            rewardRepository
    );

    var fileContent = """
            2,10""";

    SftpUtils.uploadFileToSftp(username, password, port, host, fileContent, "/upload/customer-transactions/customers.csv");
    customerPointsRepository.save(new CustomerPoints(0, 140, LocalDate.now().minusMonths(1), 2));

    var pointsByUser = fileReader.readCustomerTransactionsFile();
    var rewards = loyaltyPointsService.process(pointsByUser);

    System.out.println(rewards);
    assertEquals(0,rewards.size());

    var points = customerPointsRepository.findAll();

    System.out.println(points);
    assertEquals(1, points.size());
    assertEquals(150, points.get(0).points());
  }

  @Test
  void testWhenCustomerHasPointsEnoughTimePassed() throws Exception {
    sftp.start();
    var customerPointsRepository = new CustomerPointsRepository(mysql.getJdbcUrl());
    var rewardRepository = new RewardRepository(mysql.getJdbcUrl());
    var username = "test";
    var password = "pass";
    var port = sftp.getMappedPort(22);
    var host = sftp.getHost();
    var fileReader = new SftpCustomerTransactionFileReader(username, password, port, host);
    var loyaltyPointsService = new LoyaltyPointsService(
            customerPointsRepository,
            rewardRepository
    );

    var fileContent = """
            2,10""";

    SftpUtils.uploadFileToSftp(username, password, port, host, fileContent, "/upload/customer-transactions/customers.csv");
    customerPointsRepository.save(new CustomerPoints(0, 140, LocalDate.now().minusMonths(2), 2));

    var pointsByUser = fileReader.readCustomerTransactionsFile();
    var rewards = loyaltyPointsService.process(pointsByUser);

    System.out.println(rewards);
    assertEquals(1,rewards.size());

    var points = customerPointsRepository.findAll();

    System.out.println(points);
    assertEquals(1, points.size());
    assertEquals(0, points.get(0).points());
  }

  @NotNull
  private static MySQLContainer<?> getMySQLContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.32"))
            .withDatabaseName("shop")
            .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/init.sql")
            .withUsername("root");
  }
  private static GenericContainer<?> createSftpServer() {
    return new GenericContainer<>("atmoz/sftp:alpine-3.7")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("testcontainers/", 0777),
                    "/home/test/upload/customer-transactions"
            )
            .withExposedPorts(22)
            .withCommand("test:pass:::upload");
  }
}
