package com.opennews.domain;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
public class TraceMessage<T, D> extends IdObject<Long> implements Headers {

  protected D data;
  private long timestamp;
  private T source;
  private String message;
  private Map<String, Object> headers;
  private final Object lock = new Object();

  public TraceMessage() {}

  public TraceMessage(long id) {
    super(id);
  }

  public TraceMessage(T source) {
    this.source = source;
  }

  public TraceMessage(long id, T source) {
    super(id);
    this.source = source;
  }

  public Timestamp getTime() {
    return new Timestamp(timestamp);
  }

  public void setTime(Timestamp time) {
    this.timestamp = time.getTime();
  }

  public T getSource() {
    return source;
  }

  public void setSource(T source) {
    this.source = source;
  }

  @Override
  protected void write(StringBuilder builder) {
    super.write(builder);
    append(builder, "time", timestamp > 0, buf -> buf.append(timestamp));
    append(builder, "source", source);
  }

  @Override
  protected void writeAfter(StringBuilder builder) {
    super.writeAfter(builder);
    append(builder, "message", message != null, buf -> buf.append(jsonString(message)));
    if (headers != null && !headers.isEmpty()) {
      headers.forEach(
          (s, o) -> append(builder, s, Objects.nonNull(o), buf -> buf.append(jsonValue(o))));
    }
  }

  private String jsonString(String value) {
    StringWriter output = new StringWriter();
    try {
      JSONValue.writeJSONString(value, output);
    } catch (IOException ignore) {
      //
    }
    return output.toString();
  }

  private String jsonValue(Object value) {
    StringWriter output = new StringWriter();
    try {
      JSONValue.writeJSONString(value, output);
    } catch (IOException ignore) {
    }
    return output.toString();
  }

  @Override
  public void addHeader(String key, Object value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    synchronized (lock) {
      headers.put(key, value);
    }
  }

  @Override
  public Object removeHeader(String key) {
    if (headers != null) {
      synchronized (lock) {
        return headers.remove(key);
      }
    }
    return null;
  }

  public boolean hasHeader(String key) {
    return headers != null && headers.containsKey(key);
  }

  @Override
  public <T> T getHeader(String key, Class<T> type, Supplier<T> defaultValue) {
    if (headers != null) {
      return type.cast(headers.get(key));
    }
    return defaultValue != null ? defaultValue.get() : null;
  }
}
