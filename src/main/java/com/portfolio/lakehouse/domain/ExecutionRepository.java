package com.portfolio.lakehouse.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.lakehouse.domain.ExecutionModels.Dashboard;
import com.portfolio.lakehouse.domain.ExecutionModels.Execution;
import com.portfolio.lakehouse.domain.ExecutionModels.ExecutionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExecutionRepository {
    private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    public ExecutionRepository(JdbcTemplate jdbc,ObjectMapper mapper){this.jdbc=jdbc;this.mapper=mapper;}
    public Execution create(String pipelineKey,String requestedBy,Map<String,String> parameters,BigDecimal cost,ExecutionStatus status){
        var id=UUID.randomUUID();
        jdbc.update("INSERT INTO pipeline_execution(id,pipeline_key,requested_by,parameters,estimated_cost,status) VALUES (?,?,?,?::jsonb,?,?)",
                id,pipelineKey,requestedBy,json(parameters),cost,status.name());audit(id,requestedBy,"EXECUTION_REQUESTED",status.name());return get(id);
    }
    public Execution get(UUID id){return find(id).orElseThrow(()->new ExecutionNotFoundException(id));}
    public Optional<Execution> find(UUID id){return jdbc.query("SELECT id,pipeline_key,requested_by,parameters::text,estimated_cost,status,databricks_run_id,result_state,error_message,created_at,updated_at FROM pipeline_execution WHERE id=?",
            (rs,row)->new Execution((UUID)rs.getObject(1),rs.getString(2),rs.getString(3),map(rs.getString(4)),rs.getBigDecimal(5),ExecutionStatus.valueOf(rs.getString(6)),
                    rs.getObject(7,Long.class),rs.getString(8),rs.getString(9),rs.getTimestamp(10).toInstant(),rs.getTimestamp(11).toInstant()),id).stream().findFirst();}
    public List<Execution> pollable(){return jdbc.query("SELECT id,pipeline_key,requested_by,parameters::text,estimated_cost,status,databricks_run_id,result_state,error_message,created_at,updated_at FROM pipeline_execution WHERE status IN ('SUBMITTING','RUNNING') ORDER BY created_at LIMIT 50",
            (rs,row)->new Execution((UUID)rs.getObject(1),rs.getString(2),rs.getString(3),map(rs.getString(4)),rs.getBigDecimal(5),ExecutionStatus.valueOf(rs.getString(6)),
                    rs.getObject(7,Long.class),rs.getString(8),rs.getString(9),rs.getTimestamp(10).toInstant(),rs.getTimestamp(11).toInstant()));}
    public void approve(UUID id,String actor){jdbc.update("UPDATE pipeline_execution SET status='SUBMITTING',updated_at=now() WHERE id=? AND status='APPROVAL_REQUIRED'",id);audit(id,actor,"EXECUTION_APPROVED",null);}
    public void submitted(UUID id,long runId){jdbc.update("UPDATE pipeline_execution SET databricks_run_id=?,status='RUNNING',updated_at=now() WHERE id=?",runId,id);audit(id,"system","DATABRICKS_SUBMITTED",Long.toString(runId));}
    public void transition(UUID id,ExecutionStatus status,String result,String error){jdbc.update("UPDATE pipeline_execution SET status=?,result_state=?,error_message=?,updated_at=now() WHERE id=?",status.name(),result,error,id);audit(id,"system","STATUS_CHANGED",status.name());}
    public void quality(UUID id,boolean passed,int ok,int failed,String detail){jdbc.update("INSERT INTO quality_result(execution_id,passed,checks_passed,checks_failed,detail) VALUES (?,?,?,?,?)",id,passed,ok,failed,detail);if(!passed)jdbc.update("INSERT INTO quality_incident(id,execution_id,severity,status,detail) VALUES (?,?, 'HIGH','OPEN',?)",UUID.randomUUID(),id,detail);}
    public void audit(UUID id,String actor,String action,String detail){jdbc.update("INSERT INTO audit_event(id,execution_id,actor,action,detail) VALUES (?,?,?,?,?)",UUID.randomUUID(),id,actor,action,detail);}
    public Dashboard dashboard(){return jdbc.queryForObject("""
            SELECT COUNT(*),COUNT(*) FILTER(WHERE status='RUNNING'),COUNT(*) FILTER(WHERE status='SUCCEEDED'),
            COUNT(*) FILTER(WHERE status='QUALITY_FAILED'),COUNT(*) FILTER(WHERE status='APPROVAL_REQUIRED'),COALESCE(SUM(estimated_cost),0)
            FROM pipeline_execution
            """,(rs,row)->new Dashboard(rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getLong(5),rs.getBigDecimal(6),Instant.now()));}
    private String json(Object value){try{return mapper.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalArgumentException("Invalid parameters",e);}}
    private Map<String,String> map(String value){try{return mapper.readValue(value,new TypeReference<>(){});}catch(JsonProcessingException e){throw new IllegalStateException("Corrupt stored parameters",e);}}
    public static final class ExecutionNotFoundException extends RuntimeException {private static final long serialVersionUID=1L;public ExecutionNotFoundException(UUID id){super("Execution not found: "+id);}}
}
