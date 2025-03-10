package org.apache.hop.testing.params.provider;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

class HopFileArgumentsProvider extends AnnotationBasedArgumentsProvider<HopFilter> {
  //  private final InputStreamProvider inputStreamProvider;
  private Charset charset;
  private int numLinesToSkip;
  private CsvParser csvParser;

  public HopFileArgumentsProvider() {
    System.out.println(33);
  }

  @Override
  protected Stream<? extends Arguments> provideArguments(
      ExtensionContext context, HopFilter annotation) {
    return Stream.empty();
  }

  private static class HopFileParserIterator implements Iterator<Arguments> {
    private final CsvParser csvParser;
    private final HopFilter hopFileSource;
    //    private final boolean useHeadersInDisplayName;
    //    private final Set<String> nullValues;
    private Arguments nextArguments;
    private String[] headers;

    HopFileParserIterator(CsvParser csvParser, HopFilter hopFileSource) {
      this.csvParser = csvParser;
      this.hopFileSource = hopFileSource;
      //      this.useHeadersInDisplayName = hopFileSource.useHeadersInDisplayName();
      //      this.nullValues = CollectionUtils.toSet(hopFileSource.nullValues());
      this.advance();
    }

    public boolean hasNext() {
      return this.nextArguments != null;
    }

    public Arguments next() {
      Arguments result = this.nextArguments;
      this.advance();
      return result;
    }

    private void advance() {
      //      try {
      //        String[] csvRecord = this.csvParser.parseNext();
      //        if (csvRecord != null) {
      //          if (this.useHeadersInDisplayName && this.headers == null) {
      //            this.headers = CsvArgumentsProvider.getHeaders(this.csvParser);
      //          }
      //
      //          this.nextArguments =
      //              HopArgumentsProvider.processCsvRecord(
      //                  csvRecord, this.nullValues, this.useHeadersInDisplayName, this.headers);
      //        } else {
      //          this.nextArguments = null;
      //        }
      //      } catch (Throwable throwable) {
      //        HopArgumentsProvider.handleCsvException(throwable, this.hopFileSource);
      //      }
    }
  }

  interface InputStreamProvider {
    InputStream openClasspathResource(Class<?> baseClass, String path);

    InputStream openFile(String path);

    default Source classpathResource(String path) {
      return (context) -> this.openClasspathResource(context.getRequiredTestClass(), path);
    }

    default Source file(String path) {
      return (context) -> this.openFile(path);
    }
  }

  private static class DefaultInputStreamProvider
      implements HopFileArgumentsProvider.InputStreamProvider {
    private static final HopFileArgumentsProvider.DefaultInputStreamProvider INSTANCE =
        new HopFileArgumentsProvider.DefaultInputStreamProvider();

    private DefaultInputStreamProvider() {}

    public InputStream openClasspathResource(Class<?> baseClass, String path) {
      Preconditions.notBlank(
          path, () -> "Classpath resource [" + path + "] must not be null or blank");
      InputStream inputStream = baseClass.getResourceAsStream(path);
      return (InputStream)
          Preconditions.notNull(
              inputStream, () -> "Classpath resource [" + path + "] does not exist");
    }

    public InputStream openFile(String path) {
      Preconditions.notBlank(path, () -> "File [" + path + "] must not be null or blank");

      try {
        return Files.newInputStream(Paths.get(path));
      } catch (IOException e) {
        throw new JUnitException("File [" + path + "] could not be read", e);
      }
    }
  }

  @FunctionalInterface
  private interface Source {
    InputStream open(ExtensionContext context);
  }
}
