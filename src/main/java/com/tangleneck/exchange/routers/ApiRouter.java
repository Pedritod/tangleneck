package com.tangleneck.exchange.routers;

import com.tangleneck.exchange.handlers.ApiHandler;
import com.tangleneck.exchange.handlers.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
@RequiredArgsConstructor
public class ApiRouter {

    private static final String API_PATH = "/tangleneck";
    private static final String LOCATION_PATH = "/v1";
    private static final String REGISTER_PATH = "/register";
    private static final String CONFIRMATION_PATH = "/confirmation";
    private static final String LOGIN_PATH = "/login";

    @Autowired
    private ApiHandler apiHandler;

    @Autowired
    private ErrorHandler errorHandler;

    public RouterFunction<?> doRoute() {
        return
                nest(path(API_PATH),
                        nest(accept(APPLICATION_JSON),
                                route(POST(LOCATION_PATH + REGISTER_PATH), apiHandler::register)
                                        .andOther(route(GET(LOCATION_PATH + CONFIRMATION_PATH), apiHandler::confirmation))
                                        .andOther(route(POST(LOCATION_PATH + LOGIN_PATH), apiHandler::login))
                        ).andOther(route(RequestPredicates.all(), errorHandler::notFound))
                );
    }
}
