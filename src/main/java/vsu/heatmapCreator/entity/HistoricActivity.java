package vsu.heatmapCreator.entity;

import java.util.Date;

public class HistoricActivity {
    private String procDefKey;
    private String procDefId;
    private String activityId;
    private String activityType;
    private Date startTime;
    private Date endTime;
    private long durationInMillis;

    public String getActivityId() {
        return activityId;
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }
}

