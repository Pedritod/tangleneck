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
    private static final String ADDRESS_ARG = "/register";
    private static final String LOCATION_WITH_ADDRESS_PATH = LOCATION_PATH + ADDRESS_ARG;

    @Autowired
    private ApiHandler apiHandler;

    @Autowired
    private ErrorHandler errorHandler;

    public RouterFunction<?> doRoute() {
        return
                nest(path(API_PATH),
                    nest(accept(APPLICATION_JSON),
                        route(POST(LOCATION_WITH_ADDRESS_PATH), apiHandler::register)
                            .andOther(route(GET(LOCATION_WITH_ADDRESS_PATH), apiHandler::register))
                    ).andOther(route(RequestPredicates.all(), errorHandler::notFound))
                );
    }
}
