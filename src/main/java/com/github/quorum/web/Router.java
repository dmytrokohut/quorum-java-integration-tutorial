package com.github.quorum.web;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@RequiredArgsConstructor
@Component
public class Router {

    private final RequestHandler requestHandler;

    private final SmartContractReader smartContractReader;

    @Bean
    public RouterFunction<ServerResponse> composedRouter() {
        return route(POST("/").and(contentType(MediaType.APPLICATION_JSON)), this.requestHandler::deployContract)
                .andRoute(PUT("/").and(contentType(MediaType.APPLICATION_JSON)), this.requestHandler::updateUser)
                .andRoute(GET("/").and(contentType(MediaType.ALL)), this.requestHandler::getUser)
                .andRoute(POST("/user").and(contentType(MediaType.APPLICATION_JSON)), this.smartContractReader::readUser);
    }
}
