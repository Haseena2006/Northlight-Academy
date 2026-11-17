package com.northlight.academy.exception;
import com.northlight.academy.exception.ApiExceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  public record ErrorResponse(int status, String message, Map<String,String> errors, Instant timestamp) {}
  private ResponseEntity<ErrorResponse> res(HttpStatus s, String m, Map<String,String> errs){ return ResponseEntity.status(s).body(new ErrorResponse(s.value(), m, errs, Instant.now())); }
  @ExceptionHandler(NotFoundException.class) ResponseEntity<ErrorResponse> nf(NotFoundException e){ return res(HttpStatus.NOT_FOUND, e.getMessage(), null); }
  @ExceptionHandler(ConflictException.class) ResponseEntity<ErrorResponse> cf(ConflictException e){ return res(HttpStatus.CONFLICT, e.getMessage(), null); }
  @ExceptionHandler(ForbiddenException.class) ResponseEntity<ErrorResponse> fb(ForbiddenException e){ return res(HttpStatus.FORBIDDEN, e.getMessage(), null); }
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<ErrorResponse> ad(AccessDeniedException e){ return res(HttpStatus.FORBIDDEN, "Access denied", null); }
  @ExceptionHandler(BadRequestException.class) ResponseEntity<ErrorResponse> br(BadRequestException e){ return res(HttpStatus.BAD_REQUEST, e.getMessage(), null); }
  @ExceptionHandler(UnauthorizedException.class) ResponseEntity<ErrorResponse> un(UnauthorizedException e){ return res(HttpStatus.UNAUTHORIZED, e.getMessage(), null); }
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> valid(MethodArgumentNotValidException e){
    Map<String,String> m = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors().forEach(f -> m.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return res(HttpStatus.BAD_REQUEST, "Validation failed", m);
  }
  @ExceptionHandler(ConstraintViolationException.class) ResponseEntity<ErrorResponse> cv(ConstraintViolationException e){ return res(HttpStatus.BAD_REQUEST, "Validation failed", null); }
  @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) ResponseEntity<ErrorResponse> unreadable(Exception e){ return res(HttpStatus.BAD_REQUEST, "Malformed or invalid request", null); }
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorResponse> di(DataIntegrityViolationException e){ log.warn("Data integrity violation"); return res(HttpStatus.CONFLICT, "The request conflicts with existing data", null); }
  @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<ErrorResponse> big(MaxUploadSizeExceededException e){ return res(HttpStatus.PAYLOAD_TOO_LARGE, "File is too large (max 2 MB)", null); }
  @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<ErrorResponse> nr(NoResourceFoundException e){ return res(HttpStatus.NOT_FOUND, "Resource not found", null); }
  @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> other(Exception e){ log.error("Unhandled error", e); return res(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null); }
}
