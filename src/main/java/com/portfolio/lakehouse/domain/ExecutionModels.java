package com.portfolio.lakehouse.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ExecutionModels {
    private ExecutionModels() { }
    public record ExecutionRequest(
            @NotBlank @Size(max=60) String pipelineKey,
            @NotBlank @Pattern(regexp="[a-zA-Z0-9._@-]{3,80}") String requestedBy,
            @NotEmpty Map<@Pattern(regexp="[a-z][a-zA-Z0-9_]{0,39}") String,@Size(max=120) String> parameters) { }
    public record Pipeline(String key,long databricksJobId,String owner,Set<String> allowedParameters,
                           BigDecimal baseCost,BigDecimal approvalThreshold,int slaMinutes) { }
    public enum ExecutionStatus { APPROVAL_REQUIRED,SUBMITTING,RUNNING,SUCCEEDED,QUALITY_FAILED,CANCELLED,FAILED }
    public record Execution(UUID id,String pipelineKey,String requestedBy,Map<String,String> parameters,
                            BigDecimal estimatedCost,ExecutionStatus status,Long databricksRunId,
                            String resultState,String errorMessage,Instant createdAt,Instant updatedAt) { }
    public record QualityResult(UUID executionId,boolean passed,int checksPassed,int checksFailed,String detail) { }
    public record Dashboard(long total,long running,long succeeded,long qualityFailed,long approvalRequired,
                            BigDecimal estimatedCost,Instant generatedAt) { }
}
