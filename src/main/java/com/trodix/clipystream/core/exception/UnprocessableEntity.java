package com.trodix.clipystream.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntity extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnprocessableEntity(final String message) {
		super(message);
	}
}
