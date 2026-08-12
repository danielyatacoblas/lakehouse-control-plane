package com.portfolio.lakehouse.api;

import com.portfolio.lakehouse.domain.ExecutionModels.ExecutionRequest;
import com.portfolio.lakehouse.domain.ExecutionRepository;
import com.portfolio.lakehouse.domain.ExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController @RequestMapping("/api/v1")
public class ExecutionController {
    private final ExecutionService service;private final ExecutionRepository repository;
    public ExecutionController(ExecutionService service,ExecutionRepository repository){this.service=service;this.repository=repository;}
    @PostMapping("/executions") @ResponseStatus(HttpStatus.ACCEPTED) Object request(@Valid @RequestBody ExecutionRequest request){return service.request(request);}
    @GetMapping("/executions/{id}") Object get(@PathVariable UUID id){return service.get(id);}
    @PostMapping("/executions/{id}/approve") Object approve(@PathVariable UUID id,@RequestHeader(value="X-Role",defaultValue="")String role,@RequestHeader(value="X-Actor",defaultValue="unknown")String actor){require(role,"DATA_APPROVER");return service.approve(id,actor);}
    @PostMapping("/executions/{id}/cancel") Object cancel(@PathVariable UUID id,@RequestHeader(value="X-Role",defaultValue="")String role,@RequestHeader(value="X-Actor",defaultValue="unknown")String actor){require(role,"DATA_OPERATOR");return service.cancel(id,actor);}
    @GetMapping("/dashboard") Object dashboard(){return repository.dashboard();}
    private static void require(String actual,String expected){if(!expected.equals(actual))throw new ForbiddenException("Role "+expected+" is required");}
    static final class ForbiddenException extends RuntimeException {private static final long serialVersionUID=1L;ForbiddenException(String message){super(message);}}
}
