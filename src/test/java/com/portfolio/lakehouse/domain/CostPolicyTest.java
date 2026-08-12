package com.portfolio.lakehouse.domain;
import com.portfolio.lakehouse.domain.ExecutionModels.Pipeline;import org.junit.jupiter.api.Test;import java.math.BigDecimal;import java.util.Map;import java.util.Set;import static org.assertj.core.api.Assertions.assertThat;
class CostPolicyTest {private final CostPolicy policy=new CostPolicy();private final Pipeline pipeline=new Pipeline("sales",1,"data",Set.of("full_refresh"),new BigDecimal("40"),new BigDecimal("75"),30);
 @Test void estimatesIncrementalCost(){assertThat(policy.estimate(pipeline,Map.of("full_refresh","false"))).isEqualByComparingTo("40.00");}
 @Test void appliesFullRefreshMultiplier(){assertThat(policy.estimate(pipeline,Map.of("full_refresh","true"))).isEqualByComparingTo("100.00");}}
