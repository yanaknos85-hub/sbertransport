package org.springframework.validation.method;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;

public class DefaultMethodValidationResultWrapper {

    @Getter
    @Delegate(types = DefaultMethodValidationResult.class)
    private final DefaultMethodValidationResult delegatee;

    public DefaultMethodValidationResultWrapper(Object target, Method method, List<ParameterValidationResult> results) {
        Assert.notEmpty(results, "'results' is required and must not be empty");
        Assert.notNull(target, "'target' is required");
        Assert.notNull(method, "Method is required");
        this.delegatee = new DefaultMethodValidationResult(target, method, results, List.of());
    }

}
