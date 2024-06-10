package vsu.camundaOptimizer.mapper;


import vsu.camundaOptimizer.dto.HistoricActivityDto;
import vsu.camundaOptimizer.entity.HistoricActivity;

import java.util.List;
import java.util.stream.Collectors;

public class HistoricActivityMapper {
    public static HistoricActivityDto toDto(HistoricActivity activity) {
        return new HistoricActivityDto(
                activity.getActivityId(),
                activity.getDurationInMillis()
        );
    }

    public static List<HistoricActivityDto> toDtoList(List<HistoricActivity> activities) {
        return activities.stream().map(HistoricActivityMapper::toDto).collect(Collectors.toList());
    }
}

