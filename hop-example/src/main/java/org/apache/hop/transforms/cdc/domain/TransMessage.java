package org.apache.hop.transforms.cdc.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class TransMessage extends TraceMessage<String, String> {
  private static final String FIX_CODE_PREFIX = "NDF"; // not found key data

  private boolean success;
  private FinishType finishType = FinishType.NONE;
  private Comparable<?> key;

  public static TransMessage build(
      String source, boolean success, long time, String code, String message) {
    TransMessage tm = new TransMessage(source);
    tm.setSuccess(success);
    tm.setTime(new Timestamp(time));
    tm.setCode(code);
    tm.setMessage(message);
    return tm;
  }

  public TransMessage() {}

  public TransMessage(long id) {
    super(id);
  }

  public TransMessage(String source) {
    super(source);
  }

  public TransMessage(long id, String source) {
    super(id, source);
  }

  public String getCode() {
    return super.data;
  }

  public boolean isFailed() {
    return !success;
  }

  public boolean isSuccess() {
    return success || fixStatusIfNeed();
  }

  public void setCode(String code) {
    super.data = code;
    if (fixStatusIfNeed()) {
      success = true;
    }
  }

  private boolean fixStatusIfNeed() {
    return data != null && data.startsWith(FIX_CODE_PREFIX);
  }

  public Comparable<?> getKey() {
    return key;
  }

  public void setKey(Comparable<?> key) {
    this.key = key;
  }

  @Override
  protected void writeBefore(StringBuilder builder) {
    super.writeBefore(builder);
    append(
        builder,
        "finishType",
        finishType != null,
        buf -> buf.append('"').append(finishType.getValue()).append('"'));
    append(builder, "success", success);
    append(builder, "key", key, key != null);
  }

  @Override
  protected void write(StringBuilder builder) {
    super.write(builder);
    append(builder, "code", getCode());
  }
}
