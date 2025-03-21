package mapper;


import java.util.Date;

public class RowLogs {

  private Long id;
  private Long tableId;
  private String op;
  private Date opTime;
  private String keyVal;
  private Date scanTime;
  private Long sourceId;
  private String logFile;
  private Long logPosition;
  private Date pushTime;
  private Long pushBatch;
  private Long pushOffset;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTableId() {
    return tableId;
  }

  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public Date getOpTime() {
    return opTime;
  }

  public void setOpTime(Date opTime) {
    this.opTime = opTime;
  }

  public String getKeyVal() {
    return keyVal;
  }

  public void setKeyVal(String keyVal) {
    this.keyVal = keyVal;
  }

  public Date getScanTime() {
    return scanTime;
  }

  public void setScanTime(Date scanTime) {
    this.scanTime = scanTime;
  }

  public Long getSourceId() {
    return sourceId;
  }

  public void setSourceId(Long sourceId) {
    this.sourceId = sourceId;
  }

  public String getLogFile() {
    return logFile;
  }

  public void setLogFile(String logFile) {
    this.logFile = logFile;
  }

  public Long getLogPosition() {
    return logPosition;
  }

  public void setLogPosition(Long logPosition) {
    this.logPosition = logPosition;
  }

  public Date getPushTime() {
    return pushTime;
  }

  public void setPushTime(Date pushTime) {
    this.pushTime = pushTime;
  }

  public Long getPushBatch() {
    return pushBatch;
  }

  public void setPushBatch(Long pushBatch) {
    this.pushBatch = pushBatch;
  }

  public Long getPushOffset() {
    return pushOffset;
  }

  public void setPushOffset(Long pushOffset) {
    this.pushOffset = pushOffset;
  }
}
