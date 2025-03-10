package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TemplateContext implements TestTemplateInvocationContext {
  protected static final String FORMATTER = "[%s] %x. %s";
  protected static final Logger log = LoggerFactory.getLogger(TemplateContext.class);
  protected final String category;
  protected final String displayName;
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
    extensions.add(Preconditions.notNull(root, "root parameter resolver cannot be null"));
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
