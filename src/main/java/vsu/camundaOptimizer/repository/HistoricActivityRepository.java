package vsu.camundaOptimizer.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class HistoricActivityRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public HistoricActivityRepository(@Qualifier("clickhouseDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> getActivityAverageDuration(String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
        String sql = "SELECT act_id_ as activityId, AVG(duration_) as averageTime FROM act_hi_actinst WHERE proc_def_key_ = ? %s GROUP BY act_id_";
        return executeQueryWithFilters(sql, procDefKey, activityTypes, startTime, endTime);
    }

    public List<Map<String, Object>> getActivityCount(String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
        String sql = "SELECT act_id_ as activityId, COUNT(*) as count FROM act_hi_actinst WHERE proc_def_key_ = ? %s GROUP BY act_id_";
        return executeQueryWithFilters(sql, procDefKey, activityTypes, startTime, endTime);
    }

    public List<Map<String, Object>> getActivityIncidentCount(String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
        String sql = "SELECT activity_id_ as activityId, COUNT(*) as incidentCount FROM act_hi_incident WHERE proc_def_key_ = ? %s GROUP BY activity_id_";
        return executeQueryWithFilters(sql, procDefKey, activityTypes, startTime, endTime);
    }

    // Новый метод для получения среднего времени выполнения по процессу
    public List<Map<String, Object>> getAverageDurationForProcess(String processName) {
        String sql = "SELECT name_activity as activityName, COUNT(*) as count FROM activity WHERE name_proc = ? GROUP BY name_activity";
        return executeQueryForProcess(sql, processName);
    }

    private List<Map<String, Object>> executeQueryForProcess(String sql, String processName) {
        return jdbcTemplate.execute((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, processName);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                    return results;
                }
            }
        });
    }

    private List<Map<String, Object>> executeQueryWithFilters(String baseSql, String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
        String filterSql = generateFilters(activityTypes, startTime, endTime);
        String sql = String.format(baseSql, filterSql);

        List<Object> queryParams = buildQueryParams(procDefKey, activityTypes, startTime, endTime);

        return jdbcTemplate.execute((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < queryParams.size(); i++) {
                    ps.setObject(i + 1, queryParams.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                    return results;
                }
            }
        });
    }

    private String generateFilters(List<String> activityTypes, Date startTime, Date endTime) {
        List<String> filters = new ArrayList<>();

        if (activityTypes != null && !activityTypes.isEmpty()) {
            filters.add(generateActivityTypeFilter(activityTypes));
        }
        if (startTime != null) {
            filters.add("end_time_ >= ?");
        }
        if (endTime != null) {
            filters.add("end_time_ <= ?");
        }

        return filters.isEmpty() ? "" : " AND " + String.join(" AND ", filters);
    }

    private String generateActivityTypeFilter(List<String> activityTypes) {
        return activityTypes.stream()
                .map(type -> "UPPER(act_type_) LIKE ?")
                .collect(Collectors.joining(" OR "));
    }

    private List<Object> buildQueryParams(String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
        List<Object> params = new ArrayList<>();
        params.add(procDefKey);
        if (activityTypes != null && !activityTypes.isEmpty()) {
            params.addAll(activityTypes.stream().map(type -> "%" + type.toUpperCase() + "%").collect(Collectors.toList()));
        }
        if (startTime != null) {
            params.add(dateFormat.format(startTime));
        }
        if (endTime != null) {
            params.add(dateFormat.format(endTime));
        }
        return params;
    }
}
//    public List<HistoricActivityDto> findHistoricActivities(String procDefKey, List<String> activityTypes, Date startTime, Date endTime) {
//        StringBuilder sql = new StringBuilder("SELECT act_id_, duration_ FROM act_hi_actinst WHERE proc_def_key_ = ?");
//
//        addFilter(activityTypes, startTime, endTime, sql);
//
//        return jdbcTemplate.query(sql.toString(), new HistoricActivityMapper(), getParams(procDefKey, startTime, endTime).toArray());
//    }
//    private static final class HistoricActivityMapper implements RowMapper<HistoricActivityDto> {
//        @Override
//        public HistoricActivityDto mapRow(ResultSet rs, int rowNum) throws SQLException {
//            return new HistoricActivityDto(rs.getString("act_id_"), rs.getLong("duration_"));
//        }
//    }

