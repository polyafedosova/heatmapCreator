package vsu.heatmapCreator.dto;

public class HistoricActivityDto {
    private String activityId;
    private long activityData;

    public HistoricActivityDto(String activityId, long activityData) {
        this.activityId = activityId;
        this.activityData = activityData;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public long getDurationInMillis() {
        return activityData;
    }

    public void setDurationInMillis(long durationInMillis) {
        this.activityData = durationInMillis;
    }
}
