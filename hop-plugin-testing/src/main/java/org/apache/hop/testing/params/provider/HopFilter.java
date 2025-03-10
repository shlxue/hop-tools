package org.apache.hop.testing.params.provider;

import org.apache.hop.core.IExtensionData;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(HopFileArgumentsProvider.class)
public @interface HopFilter {
  Class<? extends IExtensionData> value() default IExtensionData.class;

  String[] pluginId() default {};

  String[] project() default {};

  String[] environment() default {"dev"};

  String[] files() default {"\\d{4}.*.hpl", "\\d{4}.*.hwf"};
}
