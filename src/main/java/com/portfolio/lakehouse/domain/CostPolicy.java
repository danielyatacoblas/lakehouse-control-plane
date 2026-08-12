package com.portfolio.lakehouse.domain;

import com.portfolio.lakehouse.domain.ExecutionModels.Pipeline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CostPolicy {
    public BigDecimal estimate(Pipeline pipeline,Map<String,String> parameters){
        var fullRefresh=Boolean.parseBoolean(parameters.getOrDefault("full_refresh","false"));
        var multiplier=fullRefresh?new BigDecimal("2.50"):BigDecimal.ONE;
        return pipeline.baseCost().multiply(multiplier).setScale(2,RoundingMode.HALF_UP);
    }
}
