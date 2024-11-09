package com.rental.tool.config;//package com.rental.tool.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.servers.Server;
//import org.springdoc.core.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
//@Configuration
//public class OpenAPIConfig {
//
//    //Customizes the OpenAPI specification with API title, version, and server information.
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info().title("Tool Rental Service API")
//                        .version("1.0.0")
//                        .description("API for renting tools"))
//                .servers(List.of(new Server().url("http://localhost:8080/api").description("Local development server")));
//    }
//
//    //Groups  API endpoints (in this case, /api/rentals/**) for documentation purposes.
//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("public")
//                .pathsToMatch("/api/rentals/**")
//                .build();
//    }
//}
