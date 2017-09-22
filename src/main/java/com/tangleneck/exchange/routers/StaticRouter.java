package com.tangleneck.exchange.routers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RouterFunctions.resources;

@Component
public class StaticRouter {

    private static final String ROUTE = "/**";
    private static final String PUBLIC = "public/";

    public RouterFunction<?> doRoute() {
        return resources(ROUTE, new ClassPathResource(PUBLIC));
    }
}
