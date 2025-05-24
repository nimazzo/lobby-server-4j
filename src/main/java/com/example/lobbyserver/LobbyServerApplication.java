package com.example.lobbyserver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(title = "User API", version = "1.0"),
        security = @SecurityRequirement(name = "basicAuth") // Applies to all endpoints by default
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@SpringBootApplication
public class LobbyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LobbyServerApplication.class, args);
    }

}
