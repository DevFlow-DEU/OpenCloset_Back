package DevFlow.OpenCloset_Back.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        Server prodServer = new Server();
        prodServer.setUrl("https://opencloset.jihongeek.com");
        prodServer.setDescription("운영 서버 (HTTPS)");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 테스트 서버 (HTTP)");

        return new OpenAPI()
                .info(new Info()
                        .title("OpenCloset API")
                        .description("OpenCloset 백엔드 API 문서")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(prodServer)
                .addServersItem(localServer);
    }
}
