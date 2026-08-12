package com.portfolio.lakehouse.databricks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class DatabricksClient {
    private final RestClient client; private final int maxAttempts;
    public DatabricksClient(RestClient.Builder builder,@Value("${databricks.host}")String host,
                            @Value("${databricks.token:local-token}")String token,
                            @Value("${databricks.retry.max-attempts:3}")int maxAttempts){
        this.client=builder.baseUrl(host).defaultHeader("Authorization","Bearer "+token).build();this.maxAttempts=maxAttempts;
    }
    public long runNow(long jobId,UUID executionId,Map<String,String> parameters){
        var response=retry(()->client.post().uri("/api/2.2/jobs/run-now").body(new RunNow(jobId,executionId.toString(),parameters)).retrieve().body(RunNowResponse.class));
        if(response==null)throw new IllegalStateException("Databricks returned an empty run response");return response.runId();
    }
    public RunState state(long runId){var response=retry(()->client.get().uri(uri->uri.path("/api/2.2/jobs/runs/get").queryParam("run_id",runId).build()).retrieve().body(RunState.class));if(response==null)throw new IllegalStateException("Empty run state");return response;}
    public void cancel(long runId){retry(()->{client.post().uri("/api/2.2/jobs/runs/cancel").body(Map.of("run_id",runId)).retrieve().toBodilessEntity();return Boolean.TRUE;});}
    private <T>T retry(java.util.concurrent.Callable<T> action){
        RuntimeException last=null;
        for(int attempt=1;attempt<=maxAttempts;attempt++)try{return action.call();}catch(RestClientResponseException e){last=e;if(e.getStatusCode().value()!=429||attempt==maxAttempts)throw e;sleep(Duration.ofMillis(150L*attempt));}catch(Exception e){throw new IllegalStateException("Databricks call failed",e);}
        throw last==null?new IllegalStateException("Databricks retry failed"):last;
    }
    private static void sleep(Duration delay){try{Thread.sleep(delay.toMillis());}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Retry interrupted",e);}}
    public record RunNow(long job_id,String idempotency_token,Map<String,String> job_parameters){}
    public record RunNowResponse(long run_id){public long runId(){return run_id;}}
    public record RunState(long run_id,State state,Quality quality){}
    public record State(String life_cycle_state,String result_state,String state_message){}
    public record Quality(boolean passed,int checks_passed,int checks_failed,String detail){}
}
