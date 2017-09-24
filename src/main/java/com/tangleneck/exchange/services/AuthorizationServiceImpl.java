package com.tangleneck.exchange.services;

import com.tangleneck.exchange.data.Customer;
import com.tangleneck.exchange.model.request.RegistrationRequest;
import com.tangleneck.exchange.model.response.LoginResponse;
import com.tangleneck.exchange.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Void> register(final RegistrationRequest registrationRequestMono) {
        Customer customer = buildCustomer(registrationRequestMono);
        return reactiveMongoTemplate.save(customer).flatMap(c -> sendEmail(c.getEmail(), c.getId()));
    }

    @Override
    public Mono<Void> confirmation(final String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = Update
                .update("confirmationDate", new Date());
        return reactiveMongoTemplate.findAndModify(query, update, Customer.class).then();

        /*
        return repository.findById(id)
                .doOnNext(c -> c.setConfirmationDate(new Date()))
                .doOnNext(repository::save).then();
        */
    }

    @Override
    public Mono<LoginResponse> login(RegistrationRequest registrationRequest) {

        String email = registrationRequest.getEmail();
        String hashedPassword = Arrays.toString(DigestUtils.sha256(registrationRequest.getPassword()));
        Query query = new Query(Criteria.where("email").is(email)
                .and("password").is(hashedPassword));

        return reactiveMongoTemplate.findOne(query, Customer.class)
                .transform(this::buildResponseForLogin);

    }

    private Mono<LoginResponse> buildResponseForLogin(Mono<Customer> customerResponse) {
        return customerResponse.flatMap(c -> {
            if (c != null && c.getConfirmationDate() != null) {
                String jwt = generateToken(c.getEmail());
                return Mono.just(new LoginResponse(jwt, new Date()));
            }
            return Mono.error(new Exception("No user found"));
        });
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                //.setExpiration(new Date(System.currentTimeMillis() + ))
                .signWith(SignatureAlgorithm.HS512, "Pepito de los palotes")
                .compact();
    }

    private Customer buildCustomer(RegistrationRequest registrationRequestMono) {
        Customer customer = new Customer();
        customer.setEmail(registrationRequestMono.getEmail());
        customer.setPassword(Arrays.toString(DigestUtils.sha256(registrationRequestMono.getPassword())));
        customer.setRegistrationDate(new Date());
        return customer;
    }

    private Mono<Void> sendEmail(String email, String id) {
        String uri = "http://localhost:8080/tangleneck/v1/confirmation?code=" + id;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Tangleneck - Email confirmation");
        message.setText("Hello, Thanks for registering to Tangleneck. \n" +
                "To activate your account, click on the following link: \n" + uri);
        emailSender.send(message);
        return Mono.empty();
    }
}
