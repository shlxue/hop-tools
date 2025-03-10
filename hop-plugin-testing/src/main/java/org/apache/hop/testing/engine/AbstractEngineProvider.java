package org.apache.hop.testing.engine;

import org.apache.hop.core.Result;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.testing.junit.StatusUtil;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

abstract class AbstractEngineProvider<E> implements InvocationInterceptor {
  private final Class<E> type;
  protected E engine;
  protected Result result;
  protected ITransform transform;

  public AbstractEngineProvider(Class<E> type) {
    this.type = type;
  }

  @Override
  public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> methodContext, ExtensionContext context) throws Throwable {
    matchParams(methodContext.getArguments().toArray());
    if (engine == null) {
      this.engine = type.cast(StatusUtil.get(context, "", IPipelineEngine.class));
    }
    this.result = execute();
    InvocationInterceptor.super.interceptTestTemplateMethod(invocation, methodContext, context);
  }

  protected void matchParams(Object... args) {
    for (Object arg : args) {
      Class<?> clazz = arg.getClass();
      if (type.isAssignableFrom(clazz)) {
        this.engine = (E) arg;
      }
    }
  }

  protected abstract Result execute() throws HopException;
}
