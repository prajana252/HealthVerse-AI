package com.healthverse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Used for both "no such assessment" and "wrong access code", so attackers cannot tell them apart. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException() { super("Assessment not found or access code incorrect"); }
}
