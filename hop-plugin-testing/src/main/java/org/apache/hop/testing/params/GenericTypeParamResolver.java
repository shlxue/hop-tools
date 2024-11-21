package org.apache.hop.testing.params;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class GenericTypeParamResolver<T, I> implements ParameterResolver {
  protected final Logger logger = LoggerFactory.getLogger(GenericTypeParamResolver.class);

  protected final Type paramType;
  private final Class<I> interfaceClass;
  private final boolean forceMatch;
  protected ExtensionContext context;

  @SuppressWarnings("unchecked")
  protected GenericTypeParamResolver(boolean forceMatch) {
    this.forceMatch = forceMatch;
    ParameterizedType superclass = findTypeBasedParameterResolverSuperclass(this.getClass());
    Preconditions.notNull(
        superclass, "Failed to discover parameter type supported by %s" + getClass().getName());
    this.paramType = superclass.getActualTypeArguments()[0];
    this.interfaceClass = (Class<I>) superclass.getActualTypeArguments()[1];
    logger.trace(
        "Create param resolver for {} & {}", paramType.getTypeName(), interfaceClass.getName());
  }

  @Override
  public final boolean supportsParameter(ParameterContext param, ExtensionContext context)
      throws ParameterResolutionException {
    if (paramType instanceof Class<?> supportedClazz
        && param.getParameter().getParameterizedType() instanceof Class<?> paramClazz
        && supportedClazz.isAssignableFrom(paramClazz)) {
      logger.trace("Checking {} param", getClass().getSimpleName());
      return matchParamType(paramClazz, interfaceClass) && matchInterface(paramClazz, forceMatch);
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public final T resolveParameter(ParameterContext parameterContext, ExtensionContext context)
      throws ParameterResolutionException {
    Preconditions.notNull(parameterContext, "Parameter must not be null");
    this.context = context;
    Class<T> type = (Class<T>) parameterContext.getParameter().getType();
    List<Constructor<?>> constructors =
        Arrays.stream(type.getDeclaredConstructors())
            .filter(c -> Modifier.isPublic(c.getModifiers()))
            .toList();
    Preconditions.condition(
        !constructors.isEmpty(), () -> "Not found any public constructor on " + type.getName());
    List<Throwable> errors = new ArrayList<>();
    for (Constructor<?> item : constructors) {
      if (!matchConstructor(item)) {
        logger.debug("Ignore constructor {}", toSignature(item));
        continue;
      }
      try {
        return creator((Constructor<T>) item, context);
      } catch (InvocationTargetException e) {
        errors.add(e.getTargetException());
      } catch (Throwable e) {
        errors.add(e);
      }
    }
    String value = constructors.stream().map(this::toSignature).collect(Collectors.joining("\n  "));
    if (!errors.isEmpty()) {
      Assertions.fail(
          "Error by constructor " + toSignature(constructors.get(0)) + ":\n" + value,
          errors.get(0));
    }
    throw new ParameterResolutionException(
        String.format("Not found match constructor by %s(%d):\n%s", type.getSimpleName(), value));
  }

  protected boolean excludeSuperClass(Class<?> clazz) {
    return false;
  }

  protected boolean includeSuperClass(Class<?> clazz) {
    return true;
  }

  protected boolean matchParamType(Class<?> supportedClazz, Class<?> supportedInterface) {
    return supportedInterface.isAssignableFrom(supportedClazz);
  }

  protected boolean matchInterface(Class<?> type, boolean forceMatch) {
    boolean matched = false;
    while (!Object.class.equals(type)) {
      List<Class<?>> list = Arrays.asList(type.getInterfaces());
      if (list.stream().anyMatch(this::excludeSuperClass)) {
        return false;
      }
      if (list.stream().anyMatch(this::includeSuperClass)) {
        matched = true;
        break;
      }
      type = type.getSuperclass();
    }
    return matched || !forceMatch;
  }

  protected final boolean sameParams(Constructor<?> constructor, Class<?>... paramClasses) {
    return Arrays.equals(constructor.getParameterTypes(), paramClasses);
  }

  protected abstract boolean matchConstructor(Constructor<?> constructor);

  protected abstract T creator(Constructor<T> constructor, ExtensionContext context)
      throws ReflectiveOperationException;

  private ParameterizedType findTypeBasedParameterResolverSuperclass(Class<?> clazz) {
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null
        && superclass != Object.class
        && clazz.getGenericSuperclass() instanceof ParameterizedType genericSuperclass) {
      if (genericSuperclass.getRawType() instanceof Class<?> rawType) {
        if (Modifier.isAbstract(rawType.getModifiers()) && rawType.getTypeParameters().length > 0) {
          return genericSuperclass;
        }
      }
      return this.findTypeBasedParameterResolverSuperclass(superclass);
    }
    return null;
  }

  private String toSignature(Constructor<?> method) {
    return String.format(
        "%s(%s)",
        method.getDeclaringClass().getSimpleName(), paramTypes(method.getParameterTypes()));
  }

  private String paramTypes(Class<?>[] types) {
    return Stream.of(types).map(Class::getSimpleName).collect(Collectors.joining(", "));
  }
}
