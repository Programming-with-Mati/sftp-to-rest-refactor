package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.repositories.CustomerPointsRepository;
import com.globant.javacodecamp.loyalty.repositories.RewardRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

  public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/shop";

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CustomerPointsRepository customerPointsRepository() {
    return new CustomerPointsRepository(CONNECTION_STRING);
  }

  @Bean
  public RewardRepository rewardRepository() {
    return new RewardRepository(CONNECTION_STRING);
  }
}
