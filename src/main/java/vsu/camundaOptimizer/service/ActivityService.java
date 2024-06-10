package vsu.camundaOptimizer.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final RepositoryService repositoryService;

    public ActivityService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public String getBpmnModelByKey(String processDefinitionKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        if (processDefinition == null) {
            throw new RuntimeException("Process definition not found for key: " + processDefinitionKey);
        }
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());
        return Bpmn.convertToString(modelInstance);
    }
}
