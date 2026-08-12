package com.portfolio.lakehouse.domain;

import com.portfolio.lakehouse.domain.ExecutionModels.Pipeline;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Set;

@Repository
public class PipelineCatalog {
    private final JdbcTemplate jdbc;
    public PipelineCatalog(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Pipeline get(String key){
        return jdbc.query("SELECT pipeline_key,databricks_job_id,owner,allowed_parameters,base_cost,approval_threshold,sla_minutes FROM pipeline_catalog WHERE pipeline_key=? AND active=true",
                (rs,row)->new Pipeline(rs.getString(1),rs.getLong(2),rs.getString(3),
                        Set.copyOf(Arrays.asList((String[])rs.getArray(4).getArray())),rs.getBigDecimal(5),rs.getBigDecimal(6),rs.getInt(7)),key)
                .stream().findFirst().orElseThrow(()->new PipelineNotFoundException(key));
    }
    public static final class PipelineNotFoundException extends RuntimeException {
        private static final long serialVersionUID=1L;
        public PipelineNotFoundException(String key){super("Pipeline not found: "+key);}
    }
}
