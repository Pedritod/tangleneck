package com.tangleneck.exchange.services;

import com.tangleneck.exchange.model.RegistrationRequest;
import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<Void> register(RegistrationRequest registrationRequest);
}
