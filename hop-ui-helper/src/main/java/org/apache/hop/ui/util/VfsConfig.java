package org.apache.hop.ui.util;

public final class VfsConfig {
  private final String title;
  private final String message;
  private final String basePath;

  public static Builder of(String title) {
    return new Builder(title);
  }

  private VfsConfig(String title, String message, String basePath) {
    this.title = title;
    this.message = message;
    this.basePath = basePath;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getBasePath() {
    return basePath;
  }

  public static class Builder {
    private final String title;
    private String message;
    private String basePath;

    private Builder(String title) {
      this.title = title;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public Builder basePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    public VfsConfig build() {
      return new VfsConfig(title, message, basePath);
    }
  }
}
