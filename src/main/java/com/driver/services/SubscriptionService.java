package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.driver.model.SubscriptionType.*;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){

        //Save The subscription Object into the Db and return the total Amount that user has to pay
        Subscription subscription=new Subscription();
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setStartSubscriptionDate(new Date());

        int totalAmount;
        int numberOfScreens=subscription.getNoOfScreensSubscribed();
        if(subscriptionEntryDto.getSubscriptionType()==BASIC) totalAmount=500+(200*numberOfScreens);
        else if(subscriptionEntryDto.getSubscriptionType()==PRO) totalAmount=800+(250*numberOfScreens);
        else totalAmount=1000+(350*numberOfScreens);

        subscription.setTotalAmountPaid(totalAmount);
        User user=userRepository.findById(subscriptionEntryDto.getUserId()).get();
        subscription.setUser(user);
        subscriptionRepository.save(subscription);
        user.setSubscription(subscription);
        userRepository.save(user);
        return totalAmount;
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository
        User user=userRepository.findById(userId).get();
        Subscription subscription=user.getSubscription();
        if(subscription.getSubscriptionType().equals(ELITE)) throw new Exception("Already the best Subscription");
        int previousAmount=subscription.getTotalAmountPaid();
        int updationAmount=0;
        if(subscription.getSubscriptionType().equals(BASIC)) {
            subscription.setSubscriptionType(PRO);
            updationAmount=800+(250*subscription.getNoOfScreensSubscribed());
            subscription.setTotalAmountPaid(updationAmount);
            subscriptionRepository.save(subscription);
        } else {
            subscription.setSubscriptionType(ELITE);
            updationAmount=1000+(350*subscription.getNoOfScreensSubscribed());
            subscription.setTotalAmountPaid(updationAmount);
            subscriptionRepository.save(subscription);
        }
        return updationAmount-previousAmount;
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb
        int totalRevenue=0;
        for(Subscription subscription:subscriptionRepository.findAll()) {
            totalRevenue+=subscription.getTotalAmountPaid();
        }
        return totalRevenue;
    }
}
