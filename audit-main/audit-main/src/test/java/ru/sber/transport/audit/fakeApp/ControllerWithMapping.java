package ru.sber.transport.audit.fakeApp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.audit.fakeApp.exception.ForbiddenException;

@RequestMapping("/mapping")
@RestController
@Tag(name = "Test controller")
public class ControllerWithMapping {

    @GetMapping
    @Operation(description = "Get test")
    String test(@RequestParam("data") String data) {
        return data;
    }

    @PutMapping
    String put(@RequestParam("data") String data) {
        return data;
    }

    @DeleteMapping
    @Operation(description = "Delete test")
    String delete(@RequestParam("data") String data) {
        return data;
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.GET}, path = "request")
    @Operation(description = "Multiple operations test")
    String requestMapping(@RequestParam("data") String data) {
        throw new ForbiddenException();
    }

}
