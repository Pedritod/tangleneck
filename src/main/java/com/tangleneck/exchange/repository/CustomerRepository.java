package com.tangleneck.exchange.repository;

import com.tangleneck.exchange.data.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {
    Mono<Customer> save(Customer name);
}
