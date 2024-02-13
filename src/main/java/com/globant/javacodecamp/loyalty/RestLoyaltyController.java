package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.model.Reward;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("loyalty")
public class RestLoyaltyController {

  private final LoyaltyPointsService loyaltyPointsService;

  public RestLoyaltyController(LoyaltyPointsService loyaltyPointsService) {
    this.loyaltyPointsService = loyaltyPointsService;
  }

  @PostMapping
  public List<Reward> addNewPointsAndRedeem(@RequestBody AddPointsRequest addPointsRequest) {
    var pointsByCustomer = Map.of(addPointsRequest.getCustomerId(), addPointsRequest.getPoints());
    return loyaltyPointsService.process(pointsByCustomer);
  }
}
