package com.ptit.bookverse.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
