package org.apache.hop.testing.junit;

import org.eclipse.swt.events.ShellEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractSpecProvider<T, M, E>
    implements Spec<T, M, E>, InvocationInterceptor, AutoCloseable {

  protected final Logger logger = LoggerFactory.getLogger(AbstractSpecProvider.class);

  private final Class<M> modeClass;
  private final Set<Spec<E, M, E>> additions;
  private final Function<E, Spec<E, M, E>[]> lazyLoader;
  private final Queue<Throwable> errors;
  private final List<Map.Entry<Spec, Throwable>> unknownErrors;
  private int position;
  protected T target;
  protected M mode;
  protected E dispatcher;
  private CountDownLatch specLatch;

  protected AbstractSpecProvider(Class<M> modeClass, Function<E, Spec<E, M, E>[]> lazyLoader) {
    this.modeClass = modeClass;
    this.additions = new LinkedHashSet<>();
    this.lazyLoader = lazyLoader;
    this.errors = new LinkedList<>();
    this.unknownErrors = new LinkedList<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public final void interceptTestTemplateMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> riContext,
      ExtensionContext context)
      throws Throwable {
    position++;
    M mode =
        Preconditions.notNull(
            StatusUtil.get(context, StoreKey.HOP_JUNIT_SPEC, modeClass),
            "HOP spec mode must not be null");
    this.target =
        Preconditions.notNull((T) riContext.getArguments().toArray()[0], "Param must not be null");
    if (!skip(mode)) {
      logger.trace("Call origin test method: {}", riContext.getExecutable().getName());
      invocation.proceed();
      return;
    }
    invocation.skip();
    this.mode = mode;
    this.dispatcher =
        Preconditions.notNull(getDispatcher(target), "Parent target must not be null");
    try {
      additions.addAll(Arrays.asList(lazyLoader.apply(dispatcher)));
    } catch (Throwable e) {
      logger.error("Init spec failed", e);
    }
    specLatch = new CountDownLatch(additions.size());
    try (this) {
      this.invoke(target, mode, dispatcher);
      if (!specLatch.await(10, TimeUnit.MINUTES)) {
        Assertions.fail("Timed out waiting for spec: " + additions.size());
      }
    } catch (Throwable t) {
      Assertions.fail("Exec spec " + getClass().getSimpleName(), t);
    } finally {
      try {
        Assertions.assertTrue(errors.isEmpty(), () -> printErrors(errors));
      } finally {
        errors.clear();
        try {
          Assertions.assertTrue(unknownErrors.isEmpty(), dumpErrors(unknownErrors));
        } finally {
          onPostTest();
          unknownErrors.clear();
        }
        onPostTest();
      }
    }
    //    Assertions.assertTrue(isDisposed(dispatcher), "Must be disposed target: " +
    // target.getClass());
  }

  @Override
  public void close() {}

  protected void onPostTest() {}

  protected void onShellAction(ShellEvent event) {
    additions.forEach(tsSpec -> execSpec(target, mode, dispatcher, tsSpec));
  }

  protected void dispose() {}

  @Override
  public void invoke(T target, M mode, E dispatcher) {
    logger.trace("Apply {} spec on {}", to(getClass()), to(target.getClass()));
  }

  protected abstract E to(T target);

  protected void execSpec(T target, M mode, E dispatcher, Spec<E, M, E> spec) {
    try {
      E subTarget = to(target);
      spec.invoke(subTarget, mode, dispatcher);
    } catch (Throwable throwable) {
      if (throwable instanceof AssertionError) {
        errors.add(throwable);
      } else {
        unknownErrors.add(new SimpleEntry<>(spec, throwable));
      }
    } finally {
      specLatch.countDown();
      if (spec instanceof AutoCloseable closeable) {
        try {
          closeable.close();
        } catch (Throwable ignored) {
        }
      }
    }
  }

  protected boolean skip(M mode) {
    return true;
  }

  protected final String to(Class<?> clazz) {
    return clazz.getSimpleName();
  }

  protected abstract E getDispatcher(T target);

  protected abstract boolean isDisposed(E dispatcher);

  private String printErrors(Collection<Throwable> errors) {
    StringBuilder builder = new StringBuilder(1024).append("\n");
    builder.append(errors.stream().map(this::toError).collect(Collectors.joining("\n")));
    builder.append("\n");
    return builder.toString();
  }

  private String toError(Throwable throwable) {
    if (throwable instanceof AssertionError) {
      return throwable.getMessage();
    }
    return throwable.toString();
  }

  private String dumpErrors(List<Map.Entry<Spec, Throwable>> errors) {
    StringBuilder builder = new StringBuilder(1024).append("\n\n");
    builder.append(String.format("%d unknown errors:\n", errors.size()));
    for (Map.Entry<Spec, Throwable> error : errors) {
      builder.append("Space: ").append(error.getKey().getClass().getSimpleName()).append("\n");
      builder.append(ExceptionUtils.readStackTrace(error.getValue()));
    }
    return builder.toString();
  }
}
