package com.portfolio.lakehouse.simulator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.lakehouse.databricks.DatabricksClient.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;

import java.time.Instant;import java.util.Map;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.atomic.AtomicLong;

@Profile("local-fake") @RestController @RequestMapping("/simulator/api/2.2/jobs")
public class DatabricksSimulator {
    private final AtomicLong ids=new AtomicLong(7000);private final Map<Long,FakeRun> runs=new ConcurrentHashMap<>();private final Map<String,Long> tokens=new ConcurrentHashMap<>();private final Map<String,Integer> throttles=new ConcurrentHashMap<>();
    @PostMapping("/run-now") ResponseEntity<?> run(@RequestBody SimRunNow request){
        if(Boolean.parseBoolean(request.jobParameters().getOrDefault("simulate_rate_limit","false"))&&throttles.merge(request.idempotencyToken(),1,Integer::sum)==1)return ResponseEntity.status(429).header("Retry-After","1").body(Map.of("error_code","REQUEST_LIMIT_EXCEEDED","message","demo throttle"));
        var id=tokens.computeIfAbsent(request.idempotencyToken(),key->{var next=ids.incrementAndGet();runs.put(next,new FakeRun(Instant.now(),request.jobParameters()));return next;});return ResponseEntity.ok(Map.of("run_id",id,"number_in_job",id));
    }
    @GetMapping("/runs/get") RunState state(@RequestParam("run_id")long id){var run=required(id);var elapsed=java.time.Duration.between(run.started(),Instant.now()).toMillis();var terminated=elapsed>1800;var qualityFails=Boolean.parseBoolean(run.parameters().getOrDefault("fail_quality","false"));return new RunState(id,new State(terminated?"TERMINATED":elapsed>500?"RUNNING":"PENDING",terminated?"SUCCESS":null,"local simulator"),new Quality(!qualityFails,qualityFails?2:3,qualityFails?1:0,qualityFails?"Null-rate threshold exceeded":"All quality checks passed"));}
    @PostMapping("/runs/cancel") void cancel(@RequestBody Map<String,Long> body){runs.remove(body.get("run_id"));}
    private FakeRun required(long id){var run=runs.get(id);if(run==null)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"run not found");return run;}
    record SimRunNow(@JsonProperty("job_id")long jobId,@JsonProperty("idempotency_token")String idempotencyToken,@JsonProperty("job_parameters")Map<String,String> jobParameters){}
    record FakeRun(Instant started,Map<String,String> parameters){}
}
