package com.tangleneck.exchange.handlers;

import com.tangleneck.exchange.model.request.RegistrationRequest;
import com.tangleneck.exchange.model.response.LoginResponse;
import com.tangleneck.exchange.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ApiHandler {

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private AuthorizationService authorizationService;

    public Mono<ServerResponse> register(final ServerRequest serverRequest) {
        Mono<RegistrationRequest> registrationRequestMono = serverRequest.bodyToMono(RegistrationRequest.class);
        RegistrationRequest registrationRequest = registrationRequestMono.block();
        return ServerResponse.ok().build(this.authorizationService.register(registrationRequest));
    }

    public Mono<ServerResponse> confirmation(ServerRequest serverRequest) {
        Optional<String> id = serverRequest.queryParam("code");
        return ServerResponse.ok().build(authorizationService.confirmation(id.get()));
    }

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        Mono<RegistrationRequest> registrationRequestMono = serverRequest.bodyToMono(RegistrationRequest.class);
        RegistrationRequest registrationRequest = registrationRequestMono.block();
        return ServerResponse.ok().body(authorizationService.login(registrationRequest), LoginResponse.class);
    }
}