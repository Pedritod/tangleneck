package com.tangleneck.exchange.services;

import com.sendgrid.*;
import com.tangleneck.exchange.data.Customer;
import com.tangleneck.exchange.model.request.RegistrationRequest;
import com.tangleneck.exchange.model.response.LoginResponse;
import com.tangleneck.exchange.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Value("${sendgrid.key}")
    private String mySecretVar;

    @Override
    public Mono<Void> register(final RegistrationRequest registrationRequestMono) {
        Customer customer = buildCustomer(registrationRequestMono);
        return reactiveMongoTemplate.save(customer).flatMap(c -> sendEmail(c.getEmail(), c.getId()));
    }

    @Override
    public Mono<Void> confirmation(final String id) {
        return repository.findById(id)
                .doOnNext(c -> c.setConfirmationDate(new Date()))
                .doOnNext(repository::save).then();

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
        SendGrid sendgrid = new SendGrid(mySecretVar);
        Email from = new Email("tangleneck@iota.com");

        String uri = "https://dashboard.heroku.com/apps/lit-ravine-74924/tangleneck/v1/confirmation?code=" + id;
        String subject = "Tangleneck - Email confirmation";
        Email to = new Email("pedrocolomina@gmail.com");
        Content content = new Content("text/plain", "Hello, Thanks for registering to Tangleneck. \n" +
                "To activate your account, click on the following link: \n " + uri + ")");
        Mail mail = new Mail(from, subject, to, content);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        try {
            request.setBody(mail.build());
            sendgrid.api(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
