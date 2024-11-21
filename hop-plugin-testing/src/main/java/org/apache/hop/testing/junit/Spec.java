package org.apache.hop.testing.junit;

@FunctionalInterface
public interface Spec<T, M, E> {
  void invoke(T target, M mode, E dispatcher);
}
