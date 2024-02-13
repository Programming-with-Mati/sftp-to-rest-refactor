package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.model.Reward;

import java.util.List;

public class SftpLoyaltyPointsController {

  private final LoyaltyPointsService loyaltyPointsService;
  private final SftpCustomerTransactionFileReader customerTransactionFileReader;

  public SftpLoyaltyPointsController(LoyaltyPointsService loyaltyPointsService, SftpCustomerTransactionFileReader customerTransactionFileReader) {
    this.loyaltyPointsService = loyaltyPointsService;
    this.customerTransactionFileReader = customerTransactionFileReader;
  }

  public List<Reward> process() {
    var pointsPerUserInFile = customerTransactionFileReader.readCustomerTransactionsFile();
    return loyaltyPointsService.process(pointsPerUserInFile);
  }
}
