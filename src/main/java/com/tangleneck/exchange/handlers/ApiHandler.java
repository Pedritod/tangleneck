package com.tangleneck.exchange.handlers;

import com.tangleneck.exchange.model.RegistrationRequest;
import com.tangleneck.exchange.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ApiHandler {

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private AuthorizationService authorizationService;

    public Mono<ServerResponse> register(final ServerRequest request) {
        Mono<RegistrationRequest> registrationRequestMono = request.bodyToMono(RegistrationRequest.class);
        RegistrationRequest registrationRequest = registrationRequestMono.block();
        return ServerResponse.ok().build(this.authorizationService.register(registrationRequest));
    }
}