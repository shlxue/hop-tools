package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.stream.Stream;

abstract class BaseTestTemplateProvider implements TestTemplateInvocationContextProvider {
  protected final Logger log;
  protected final Class<?>[] requestTypes;
  protected final Class<?>[] optionalTypes;

  BaseTestTemplateProvider(Logger log, Class<?>[] requestTypes, Class<?>... optionalTypes) {
    this.log = log;
    this.requestTypes = requestTypes;
    this.optionalTypes = optionalTypes;
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    return (requestTypes.length == 0 || testMethod.getParameterCount() >= 1)
        && matchParameters(testMethod.getParameterTypes());
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    return Stream.empty();
  }

  protected boolean matchParameters(Class<?>... params) {
    return Stream.of(params).anyMatch(clazz -> hasSupportType(requestTypes, clazz))
        && Stream.of(params)
            .allMatch(
                clazz ->
                    hasSupportType(requestTypes, clazz) || hasSupportType(optionalTypes, clazz));
  }

  protected boolean hasSupportType(Class<?>[] types, Class<?> paramType) {
    for (Class<?> type : types) {
      if (type.isAssignableFrom(paramType)) {
        return true;
      }
    }
    return false;
  }
}
