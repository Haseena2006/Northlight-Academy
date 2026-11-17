package com.northlight.academy.exception;
public final class ApiExceptions {
  private ApiExceptions(){}
  public static class NotFoundException extends RuntimeException { public NotFoundException(String m){super(m);} }
  public static class ConflictException extends RuntimeException { public ConflictException(String m){super(m);} }
  public static class ForbiddenException extends RuntimeException { public ForbiddenException(String m){super(m);} }
  public static class BadRequestException extends RuntimeException { public BadRequestException(String m){super(m);} }
  public static class UnauthorizedException extends RuntimeException { public UnauthorizedException(String m){super(m);} }
}
