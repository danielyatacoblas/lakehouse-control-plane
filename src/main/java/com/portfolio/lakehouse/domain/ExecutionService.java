package com.portfolio.lakehouse.domain;

import com.portfolio.lakehouse.databricks.DatabricksClient;
import com.portfolio.lakehouse.domain.ExecutionModels.Execution;
import com.portfolio.lakehouse.domain.ExecutionModels.ExecutionRequest;
import com.portfolio.lakehouse.domain.ExecutionModels.ExecutionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.UUID;

@Service
public class ExecutionService {
    private final PipelineCatalog catalog;private final CostPolicy costs;private final ExecutionRepository repository;private final DatabricksClient databricks;
    public ExecutionService(PipelineCatalog catalog,CostPolicy costs,ExecutionRepository repository,DatabricksClient databricks){this.catalog=catalog;this.costs=costs;this.repository=repository;this.databricks=databricks;}
    public Execution request(ExecutionRequest request){
        var pipeline=catalog.get(request.pipelineKey());var unknown=new LinkedHashSet<>(request.parameters().keySet());unknown.removeAll(pipeline.allowedParameters());if(!unknown.isEmpty())throw new IllegalArgumentException("Parameters not allowed: "+unknown);
        var cost=costs.estimate(pipeline,request.parameters());var status=cost.compareTo(pipeline.approvalThreshold())>0?ExecutionStatus.APPROVAL_REQUIRED:ExecutionStatus.SUBMITTING;
        var execution=repository.create(pipeline.key(),request.requestedBy(),request.parameters(),cost,status);if(status==ExecutionStatus.SUBMITTING)submit(execution);return repository.get(execution.id());
    }
    public Execution approve(UUID id,String actor){var execution=repository.get(id);if(execution.status()!=ExecutionStatus.APPROVAL_REQUIRED)throw new IllegalStateException("Execution does not require approval");repository.approve(id,actor);submit(repository.get(id));return repository.get(id);}
    public Execution cancel(UUID id,String actor){var execution=repository.get(id);if(execution.databricksRunId()!=null)databricks.cancel(execution.databricksRunId());repository.transition(id,ExecutionStatus.CANCELLED,"CANCELED",null);repository.audit(id,actor,"EXECUTION_CANCELLED",null);return repository.get(id);}
    public Execution get(UUID id){return repository.get(id);}
    private void submit(Execution execution){var pipeline=catalog.get(execution.pipelineKey());var runId=databricks.runNow(pipeline.databricksJobId(),execution.id(),execution.parameters());repository.submitted(execution.id(),runId);}
    @Scheduled(fixedDelayString="${lakehouse.poll-interval:1000}")
    public void poll(){for(var execution:repository.pollable())if(execution.databricksRunId()!=null)try{var run=databricks.state(execution.databricksRunId());var life=run.state().life_cycle_state();if("TERMINATED".equals(life)){var quality=run.quality();var passed="SUCCESS".equals(run.state().result_state())&&quality.passed();repository.quality(execution.id(),passed,quality.checks_passed(),quality.checks_failed(),quality.detail());repository.transition(execution.id(),passed?ExecutionStatus.SUCCEEDED:ExecutionStatus.QUALITY_FAILED,run.state().result_state(),passed?null:quality.detail());}}catch(RuntimeException e){repository.audit(execution.id(),"system","POLL_FAILED",e.getMessage());}}
}
