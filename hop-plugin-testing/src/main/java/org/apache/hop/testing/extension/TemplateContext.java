package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TemplateContext implements TestTemplateInvocationContext {
  private static final String FORMATTER = "[%s] %x. %s";
  private final String category;
  private final String displayName;
  protected final Set<Extension> extensions;

  TemplateContext(
      String category,
      String displayName,
      ExecutionCondition[] conditions,
      ParameterResolver root,
      InvocationInterceptor... interceptors) {
    this.category = category;
    this.displayName = displayName;
    this.extensions = new LinkedHashSet<>(conditions.length + interceptors.length + 1);
    extensions.addAll(Arrays.asList(conditions));
    extensions.add(root);
    extensions.addAll(Arrays.asList(interceptors));
  }

  @Override
  public String getDisplayName(int invocationIndex) {
    return String.format(FORMATTER, category, invocationIndex, displayName);
  }

  @Override
  public List<Extension> getAdditionalExtensions() {
    return extensions.stream().toList();
  }
}
