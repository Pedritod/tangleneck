package com.tangleneck.exchange.services;

import com.tangleneck.exchange.model.request.RegistrationRequest;
import com.tangleneck.exchange.model.response.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<Void> register(RegistrationRequest registrationRequest);

    Mono<Void> confirmation(String id);

    Mono<LoginResponse> login(RegistrationRequest registrationRequest);
}
