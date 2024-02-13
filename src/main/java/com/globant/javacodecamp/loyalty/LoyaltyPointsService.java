package com.globant.javacodecamp.loyalty;

import com.globant.javacodecamp.loyalty.model.CustomerPoints;
import com.globant.javacodecamp.loyalty.model.Reward;
import com.globant.javacodecamp.loyalty.repositories.CustomerPointsRepository;
import com.globant.javacodecamp.loyalty.repositories.RewardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LoyaltyPointsService {

  private final CustomerPointsRepository customerPointsRepository;
  private final RewardRepository rewardRepository;

  public LoyaltyPointsService(CustomerPointsRepository customerPointsRepository, RewardRepository rewardRepository) {
    this.customerPointsRepository = customerPointsRepository;
    this.rewardRepository = rewardRepository;
  }

  public List<Reward> process(Map<Long, Integer> pointsPerUser) {
    var customerPoints = addCustomerPoints(pointsPerUser);
    return redeemPoints(customerPoints);
  }

  public List<CustomerPoints> addCustomerPoints(Map<Long, Integer> pointsPerUser) {
    var customerIds = pointsPerUser.keySet();
    var customerPoints = customerPointsRepository.findAll(cp -> customerIds.contains(cp.customerId()) && cp.dateCreated().isAfter(LocalDate.now().minusMonths(2).minusDays(1)))
            .stream().collect(Collectors.toMap(CustomerPoints::customerId, Function.identity()));
    return pointsPerUser.entrySet()
            .stream()
            .map(entry -> addNewCustomerPoints(customerPoints, entry))
            .map(customerPointsRepository::saveOrUpdate)
            .toList();
  }

  public List<Reward> redeemPoints(List<CustomerPoints> customerPoints) {
    return customerPoints.stream()
            .map(this::redeemPoints)
            .map(this::save)
            .map(RewardWithCustomerPoints::reward)
            .filter(Objects::nonNull)
            .toList();
  }

  private RewardWithCustomerPoints save(RewardWithCustomerPoints rewardWithCustomerPoints) {
    Reward reward = null;
    CustomerPoints customerPoints = customerPointsRepository.saveOrUpdate(rewardWithCustomerPoints.customerPoints());;

    if (rewardWithCustomerPoints.reward() != null) {
      reward = rewardRepository.save(rewardWithCustomerPoints.reward());
    }

    return new RewardWithCustomerPoints(reward, customerPoints);
  }

  private RewardWithCustomerPoints redeemPoints(CustomerPoints cp) {
    Reward reward;
    var dateValid = LocalDate.now().plusMonths(2);
    if (cp.points() >= 150 && cp.points() < 300 && cp.dateCreated().isEqual(LocalDate.now().minusMonths(2))) {
      reward = new Reward(0, LocalDate.now(), cp.customerId(), 5, dateValid, false);
      return new RewardWithCustomerPoints(reward, cp.resetPoints());
    }

    if (cp.points() >= 300 && cp.points() < 600 && cp.dateCreated().isEqual(LocalDate.now().minusMonths(2))) {
      reward = new Reward(0, LocalDate.now(), cp.customerId(), 10, dateValid, false);
      return new RewardWithCustomerPoints(reward, cp.resetPoints());
    }

    if (cp.points() >= 600 && cp.points() < 1500 && cp.dateCreated().isEqual(LocalDate.now().minusMonths(2))) {
      reward = new Reward(0, LocalDate.now(), cp.customerId(), 15, dateValid, false);
      return new RewardWithCustomerPoints(reward, cp.resetPoints());
    }

    if (cp.points() >= 1500 && cp.dateCreated().isEqual(LocalDate.now().minusMonths(2))) {
      reward = new Reward(0, LocalDate.now(), cp.customerId(), 20, dateValid, false);
      return new RewardWithCustomerPoints(reward, cp.resetPoints());
    }

    return new RewardWithCustomerPoints(null, cp);
  }

  private CustomerPoints addNewCustomerPoints(Map<Long, CustomerPoints> customerPoints, Map.Entry<Long, Integer> entry) {
    return customerPoints.containsKey(entry.getKey()) ?
            customerPoints.get(entry.getKey()).add(entry.getValue()) :
            new CustomerPoints(0, entry.getValue(), LocalDate.now(), entry.getKey());
  }


  private record RewardWithCustomerPoints(Reward reward, CustomerPoints customerPoints) {
  }
}
