package com.tangleneck.exchange;

import com.tangleneck.exchange.routers.ApiRouter;
import com.tangleneck.exchange.routers.StaticRouter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;

@SpringBootApplication
@ComponentScan
@EnableWebFlux
@EnableAutoConfiguration
public class TangleneckApplication  {
	public static void main(String[] args) {
		SpringApplication.run(TangleneckApplication.class, args);
	}

	@Bean
	public RouterFunction<?> mainRouterFunction(ApiRouter apiRouter, StaticRouter staticRouter) {
		return apiRouter
				.doRoute()
				.andOther(staticRouter.doRoute());
	}
}
