package ru.sber.transport.audit.fakeApp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({Controller.class, ControllerWithMapping.class, InterfacedControllerImpl.class, AuditedAction.class})
@OpenAPIDefinition(info = @Info(title = "Test app"))
public class DoNotStartThis {

    public static void main(String... args) {
        SpringApplication.run(DoNotStartThis.class, args);
    }

}
