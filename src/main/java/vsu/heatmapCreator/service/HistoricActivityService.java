package vsu.heatmapCreator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vsu.heatmapCreator.repository.HistoricActivityRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoricActivityService {

    private final HistoricActivityRepository historicActivityRepository;

    @Autowired
    public HistoricActivityService(HistoricActivityRepository historicActivityRepository) {
        this.historicActivityRepository = historicActivityRepository;
    }

    /**
     * Fetches heatmap data by average time with an optional threshold.
     *
     * @param processDefinitionKey the process definition key
     * @param activityTypes        optional list of activity types to filter by
     * @param startTime            optional start time filter
     * @param endTime              optional end time filter
     * @param threshold            optional threshold value to include in the result
     * @return a list of maps containing heatmap data by average time
     */
    public List<Map<String, Object>> getHeatmapDataByAverageTime(
            String processDefinitionKey, List<String> activityTypes, Date startTime, Date endTime, Double threshold) {
        List<Map<String, Object>> data = historicActivityRepository.getActivityAverageDuration(processDefinitionKey, activityTypes, startTime, endTime);
        if (threshold != null) {
            data.add(createThresholdMap(threshold));
        }
        return data;
    }

    /**
     * Fetches heatmap data by activity count.
     *
     * @param processDefinitionKey the process definition key
     * @param activityTypes        optional list of activity types to filter by
     * @param startTime            optional start time filter
     * @param endTime              optional end time filter
     * @return a list of maps containing heatmap data by activity count
     */
    public List<Map<String, Object>> getHeatmapDataByCount(
            String processDefinitionKey, List<String> activityTypes, Date startTime, Date endTime) {
        return historicActivityRepository.getActivityCount(processDefinitionKey, activityTypes, startTime, endTime);
    }

    /**
     * Fetches heatmap data by incident count.
     *
     * @param processDefinitionKey the process definition key
     * @param activityTypes        optional list of activity types to filter by
     * @param startTime            optional start time filter
     * @param endTime              optional end time filter
     * @return a list of maps containing heatmap data by incident count
     */
    public List<Map<String, Object>> getHeatmapDataByIncidentCount(
            String processDefinitionKey, List<String> activityTypes, Date startTime, Date endTime) {
//        return historicActivityRepository.getActivityIncidentCount(processDefinitionKey, activityTypes, startTime, endTime);
        List<Map<String, Object>> data = historicActivityRepository.getActivityIncidentCount(processDefinitionKey, activityTypes, startTime, endTime);
        data.add(add1());
        return data;
    }

    /**
     * Creates a threshold map to be added to the result.
     *
     * @param threshold the threshold value
     * @return a map containing the threshold data
     */
    private Map<String, Object> createThresholdMap(Double threshold) {
        Map<String, Object> thresholdMap = new HashMap<>();
        thresholdMap.put("activityId", "Threshold");
        thresholdMap.put("averageTime", threshold);
        return thresholdMap;
    }

    private Map<String, Object> add1() {
        Map<String, Object> thresholdMap = new HashMap<>();
        thresholdMap.put("activityId", "Activity_1m1hr1f");
        thresholdMap.put("incidentCount", 1);
        return thresholdMap;
    }

    public List<Map<String, Object>> getActivityAverageTime(String processDefinitionKey) {
        return historicActivityRepository.getAverageDurationForProcess(processDefinitionKey);
    }
}
