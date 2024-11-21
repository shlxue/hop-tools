package org.apache.hop.testing.condition;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.STABLE;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnableOnX11Condition.class)
@API(status = STABLE, since = "0.3")
public @interface EnableOnX11 {}
