package com.portfolio.lakehouse.api;

import com.portfolio.lakehouse.domain.ExecutionRepository.ExecutionNotFoundException;
import com.portfolio.lakehouse.domain.PipelineCatalog.PipelineNotFoundException;
import org.springframework.http.HttpStatus;import org.springframework.http.ProblemDetail;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.ExceptionHandler;import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({ExecutionNotFoundException.class,PipelineNotFoundException.class}) ProblemDetail notFound(RuntimeException e){return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());}
    @ExceptionHandler(ExecutionController.ForbiddenException.class) ProblemDetail forbidden(RuntimeException e){return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,e.getMessage());}
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class}) ProblemDetail conflict(RuntimeException e){return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.getMessage());}
    @ExceptionHandler(MethodArgumentNotValidException.class) ProblemDetail invalid(MethodArgumentNotValidException e){var p=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Invalid execution request");p.setProperty("errors",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList());return p;}
}
