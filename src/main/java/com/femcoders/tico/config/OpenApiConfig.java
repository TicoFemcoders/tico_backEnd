package com.femcoders.tico.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Pega el JWT obtenido en POST /login")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .path("/login", buildLoginPath());
  }

  private PathItem buildLoginPath() {
    Schema<?> loginBody = new Schema<>()
        .type("object")
        .addProperty("email", new Schema<>().type("string").example("empleado@cohispania.com"))
        .addProperty("password", new Schema<>().type("string").example("tu-password"));

    Schema<?> authResponseBody = new Schema<>()
        .type("object")
        .addProperty("id", new Schema<>().type("integer"))
        .addProperty("name", new Schema<>().type("string"))
        .addProperty("email", new Schema<>().type("string"))
        .addProperty("roles", new Schema<>().type("array")
            .items(new Schema<>().type("string").example("ROLE_EMPLOYEE")));

    Operation post = new Operation()
        .tags(List.of("auth"))
        .summary("Login — obtener JWT")
        .description(
            "Devuelve el JWT en el **header Authorization** de la respuesta. Copia ese valor y úsalo en el botón Authorize.")
        .requestBody(new RequestBody()
            .required(true)
            .content(new Content().addMediaType("application/json",
                new MediaType().schema(loginBody))))
        .responses(new ApiResponses()
            .addApiResponse("200", new ApiResponse()
                .description("Login correcto — JWT en header Authorization")
                .content(new Content().addMediaType("application/json",
                    new MediaType().schema(authResponseBody))))
            .addApiResponse("401", new ApiResponse()
                .description("Credenciales incorrectas")));

    post.setSecurity(List.of());

    return new PathItem().post(post);
  }
}

