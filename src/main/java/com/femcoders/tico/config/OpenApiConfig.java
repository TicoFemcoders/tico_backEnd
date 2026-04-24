package com.femcoders.tico.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;

@Configuration
@Profile("dev")
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
        .addProperty("email", new Schema<>().type("string").example("usuario@empresa.com"))
        .addProperty("password", new Schema<>().type("string").example("tu_contraseña"));

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
                .description("Login exitoso — JWT en header Authorization")
                .headers(Map.of("Authorization", new Header()
                    .description("Bearer <token>")
                    .schema(new StringSchema()))))
            .addApiResponse("401", new ApiResponse()
                .description("Credenciales incorrectas")));

    post.setSecurity(List.of());

    return new PathItem().post(post);
  }
}
