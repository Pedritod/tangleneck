package com.tangleneck.exchange.services;

import com.tangleneck.exchange.data.Customer;
import com.tangleneck.exchange.model.request.RegistrationRequest;
import com.tangleneck.exchange.model.response.LoginResponse;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private EmailSender emailSender;

    @Value("${jwt.key}")
    private String jwtPrivateKey;

    @Override
    public Mono<Void> register(final RegistrationRequest registrationRequestMono) {
        Customer customer = new Customer();
        customer.setEmail(registrationRequestMono.getEmail());
        customer.setPassword(Arrays.toString(DigestUtils.sha256(registrationRequestMono.getPassword())));
        customer.setRegistrationDate(new Date());
        return reactiveMongoTemplate.findById(registrationRequestMono.getEmail(), Customer.class)
                .flatMap(foundCustomer -> foundCustomer == null ? reactiveMongoTemplate.save(customer)
                        .flatMap(c -> emailSender.send(c.getId(), c.getEmail())) : Mono.error(new Exception("User exists")));
    }

    @Override
    public Mono<Void> confirmation(final String id) {
        return reactiveMongoTemplate.findById(id, Customer.class)
                .doOnNext(c -> c.setConfirmationDate(new Date()))
                .doOnNext(reactiveMongoTemplate::save).then();
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
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS512, jwtPrivateKey)
                .compact();
    }
}
