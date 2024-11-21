package org.apache.hop.transforms.cdc;

import com.opennews.hop.jdbc.Tab;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.StringUtil;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class Relationship implements Cloneable {
  private Tab<IValueMeta> master;
  private Tab<IValueMeta> detail;
  private String joinSql;
  private String conditionSql;
  private String orderBy;
  private boolean ignoreDel;
  private String delStep;
  private int limit;

  Relationship(Tab<IValueMeta> master, Tab<IValueMeta> detail) {
    this.master = master;
    this.detail = detail;
  }

  @Override
  protected Relationship clone() {
    try {
      Relationship clone = (Relationship) super.clone();
      clone.detail = detail.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException(e);
    }
  }

  public Tab<IValueMeta> getDetail() {
    return detail;
  }

  public String getJoinSql() {
    return joinSql;
  }

  public void setJoinSql(String joinSql) {
    this.joinSql = joinSql;
  }

  public String getConditionSql() {
    return conditionSql;
  }

  public void setConditionSql(String conditionSql) {
    this.conditionSql = conditionSql;
  }

  public void setMaster(Tab<IValueMeta> master) {
    this.master = master;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public String getDelStep() {
    return delStep;
  }

  public void setDelStep(String delStep) {
    this.delStep = delStep;
  }

  public boolean isIgnoreDel() {
    return ignoreDel;
  }

  public void setIgnoreDel(boolean ignoreDel) {
    this.ignoreDel = ignoreDel;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  String getTestSqlText() {
    char cr = '\n';
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ").append(buildSql("t1", detail.getPkFields(), ", ")).append(cr);
    sb.append("FROM ").append(detail.getFullName()).append(" t1").append(cr);
    if (!StringUtil.isEmpty(conditionSql)) {
      sb.append("WHERE ").append(conditionSql);
    }
    return sb.toString();
  }

  String getQuerySqlText() {
    return buildSql(
        sb -> {
          sb.append("-- WHERE -- ").append(getKeyExpression()).append(cr);
          if (StringUtil.isEmpty(conditionSql)) {
            sb.append("-- WHERE <query expression...>");
          } else {
            sb.append("WHERE ").append(conditionSql.trim());
          }
        });
  }

  String getDesignSqlText() {
    return buildSql(sb -> sb.append("WHERE ").append(getKeyExpression()));
  }

  static char cr = '\n';

  private String buildSql(Consumer<StringBuilder> whereStatement) {
    String m = "m";
    String t1 = "t1";
    StringBuilder sb = new StringBuilder(1024);
    sb.append("SELECT ").append(buildSql(m, master.getPkFields(), ", ")).append(cr);

    sb.append("FROM ").append(master.getFullName()).append(" ").append(m).append(cr);
    sb.append("  JOIN ").append(detail.getFullName()).append(" ").append(t1);
    String sql = getJoinSql();
    if (StringUtil.isEmpty(sql)) {
      sql = "-- <join condition...>";
    }
    sb.append(" ON ").append(sql).append(cr);

    whereStatement.accept(sb);
    return sb.toString();
  }

  private String buildSql(String alias, Collection<IValueMeta> fields, String delimiter) {
    return fields.stream()
        .map(IValueMeta::getName)
        .map(s -> alias + "." + s)
        .collect(Collectors.joining(delimiter));
  }

  private String getKeyExpression() {
    if (detail.getPkFields().isEmpty()) {
      return "<key expression...>";
    }
    UnaryOperator<String> keyExpression = s -> String.format("t1.%s = ?", s);
    return detail.formatSql(detail.getPkFields(), keyExpression, true);
  }
}
