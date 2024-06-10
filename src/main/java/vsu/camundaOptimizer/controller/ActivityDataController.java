package vsu.camundaOptimizer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vsu.camundaOptimizer.service.ActivityService;
import vsu.camundaOptimizer.service.HistoricActivityService;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class ActivityDataController {

    private final ActivityService activityService;
    private final HistoricActivityService historicActivityService;

    @Autowired
    public ActivityDataController(ActivityService activityService, HistoricActivityService historicActivityService) {
        this.activityService = activityService;
        this.historicActivityService = historicActivityService;
    }

    @GetMapping("/heatmap/{type}/{processDefinitionId}")
    public List<Map<String, Object>> getHeatmapData(@PathVariable String processDefinitionId, @PathVariable String type,
                                                    @RequestParam(required = false) List<String> activityTypes,
                                                    @RequestParam(required = false) Double threshold,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") Date startTime,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") Date endTime) {
        return switch (type) {
            case ("time") ->
                    historicActivityService.getHeatmapDataByAverageTime(processDefinitionId, activityTypes, startTime, endTime, threshold);
            case ("count") ->
                    historicActivityService.getHeatmapDataByCount(processDefinitionId, activityTypes, startTime, endTime);
            case ("error") ->
                    historicActivityService.getHeatmapDataByIncidentCount(processDefinitionId, activityTypes, startTime, endTime);
            default -> throw new IllegalArgumentException("Invalid type parameter: " + type);
        };
    }
    //Process_7
    @GetMapping("/activity/{processDefinitionId}")
    public List<Map<String, Object>> getActivityData(@PathVariable String processDefinitionId) {
        long startTime = System.nanoTime();

        List<Map<String, Object>> data = historicActivityService.getActivityAverageTime(processDefinitionId);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Time taken to fetch data: " + duration + " nanoseconds");

        return data;
    }

    @GetMapping(value = "/process/bpmn/{processDefinitionId}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getBpmnProcess(@PathVariable String processDefinitionId) {
        try {
            String bpmnModel = activityService.getBpmnModelByKey(processDefinitionId);
            return ResponseEntity.ok().body(bpmnModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Процесс не найден");
        }
    }
}

