package com.tangleneck.exchange.services;

import com.tangleneck.exchange.data.Customer;
import com.tangleneck.exchange.model.RegistrationRequest;
import com.tangleneck.exchange.repository.CustomerRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public Mono<Void> register(final RegistrationRequest registrationRequestMono) {
        Customer customer = buildCustomer(registrationRequestMono);
        Mono<Void> response = repository.save(customer).then()
                .onErrorResume(throwable -> Mono.error(new Exception()));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customer.getEmail());
        message.setSubject("Tangleneck - Email confirmation");
        message.setText("Hello," +
                "Thanks for registering to Tangleneck. To activate your account, click on the following link:" +
                "PEPE");
        emailSender.send(message);
        //TODO Email - spring integration
        return response;
    }

    private Customer buildCustomer(RegistrationRequest registrationRequestMono) {
        Customer customer = new Customer();
        customer.setEmail(registrationRequestMono.getEmail());
        customer.setPassword(DigestUtils.sha256(registrationRequestMono.getPassword()).toString());
        customer.setRegistrationDate(new Date());
        return customer;
    }
}
