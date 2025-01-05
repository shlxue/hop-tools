package org.apache.hop.testing.params.provider;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(HopFileArgumentsProvider.class)
public @interface HopFileSource {

  String[] values() default {"*.hpl", "*.hwf"};

  String[] names() default {};

  String[] projects() default {};

  String[] environment() default {};

  boolean root() default false;
}
