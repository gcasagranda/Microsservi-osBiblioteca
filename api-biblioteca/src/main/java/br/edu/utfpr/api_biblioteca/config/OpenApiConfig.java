package br.edu.utfpr.api_biblioteca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Biblioteca",
                version = "1.0",
                description = "Responsável pelo acesso e gestão do acervo de livros."
        )
)
public class OpenApiConfig {

}