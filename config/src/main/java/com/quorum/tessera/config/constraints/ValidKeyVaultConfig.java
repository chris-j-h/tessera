package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD, PARAMETER, ANNOTATION_TYPE, TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = KeyVaultConfigValidator.class)
@Documented
public @interface ValidKeyVaultConfig {
  String message() default "{ValidKeyVaultConfig.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
