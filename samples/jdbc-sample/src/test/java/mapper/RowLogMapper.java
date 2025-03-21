package mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RowLogMapper {
  void insertHistory(
      @Param("date") String date,
      @Param("idList") List<Long> idList,
      @Param("rowLogs") List<RowLogs> item);

}
