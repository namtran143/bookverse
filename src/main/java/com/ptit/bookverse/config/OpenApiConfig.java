package com.ptit.bookverse.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
    @Bean OpenAPI bookVerseOpenApi() {
        return new OpenAPI().info(new Info().title("BookVerse API").version("1.0.0").description("Electronic book management API"));
    }
}
