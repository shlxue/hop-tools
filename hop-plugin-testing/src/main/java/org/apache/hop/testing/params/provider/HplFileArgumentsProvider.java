package org.apache.hop.testing.params.provider;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class HplFileArgumentsProvider extends AnnotationBasedArgumentsProvider<HplSource> {
  @Override
  protected Stream<? extends Arguments> provideArguments(
      ExtensionContext context, HplSource annotation) {
    return Stream.empty();
  }
}
