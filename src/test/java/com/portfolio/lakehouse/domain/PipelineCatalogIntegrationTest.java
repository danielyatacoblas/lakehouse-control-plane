package com.portfolio.lakehouse.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.task.scheduling.enabled=false")
class PipelineCatalogIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired PipelineCatalog catalog;

    @Test
    void flywaySeedsSafePipelineCatalog() {
        var pipeline = catalog.get("sales-bronze-gold");
        assertThat(pipeline.databricksJobId()).isEqualTo(11223344L);
        assertThat(pipeline.allowedParameters()).contains("source_date", "full_refresh", "fail_quality");
        assertThat(pipeline.approvalThreshold()).isEqualByComparingTo("75.00");
    }
}
