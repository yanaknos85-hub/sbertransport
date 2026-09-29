package ru.sber.transport.audit.fakeApp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Test interfaced controller")
public class InterfacedControllerImpl implements InterfacedController {

    public String test(String data) {
        return data;
    }

    public String patch(String data) {
        return data;
    }

    public String postRequest(String data) {
        return data;
    }

    public String getRequest(String data) {
        return data;
    }

}
