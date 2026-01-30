package net.profitwarning.api.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-exception-handler")
@SuppressWarnings("null")
class ExceptionTestController {

    @PostMapping("/validation")
    void triggerValidation(@RequestBody @Valid TestExceptionRequest request) {
    }

    @PostMapping("/manual-validation")
    void triggerManualValidation() throws MethodArgumentNotValidException, NoSuchMethodException {
        java.lang.reflect.Method method = ExceptionTestController.class.getDeclaredMethod("triggerValidation", TestExceptionRequest.class);
        org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, 0);
        throw new MethodArgumentNotValidException(parameter, new org.springframework.validation.BeanPropertyBindingResult(new TestExceptionRequest(""), "test"));
    }

    @GetMapping("/exception")
    void triggerException() {
        throw new RuntimeException("Unexpected error");
    }

    record TestExceptionRequest(
            @NotBlank(message = "Name is required")
            String name
    ) {}
}
