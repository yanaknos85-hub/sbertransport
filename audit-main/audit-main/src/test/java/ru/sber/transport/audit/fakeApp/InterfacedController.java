package ru.sber.transport.audit.fakeApp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/interfaced")
@Tag(name = "Test interfaced controller")
public interface InterfacedController {

    @PostMapping("/")
    String test(@RequestParam("data") String data);

    @PatchMapping("/")
    @Operation(description = "Interfaced patch test")
    String patch(@RequestParam("data") String data);

    @RequestMapping(method = RequestMethod.DELETE, value = "request")
    @Operation(description = "Interfaced patch request test")
    String postRequest(@RequestParam("data") String data);

    @RequestMapping(method = RequestMethod.GET, value = {"/first", "second"})
    @Operation(description = "Interfaced et request test")
    String getRequest(@RequestParam("data") String data);

}
