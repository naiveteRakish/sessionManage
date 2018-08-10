package com.biz.primus.base.session.exception;

import com.biz.primus.common.exception.BizSilentException;
import com.biz.primus.common.exception.ExceptionType;

public class InvalidCsrfTokenException extends BizSilentException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCsrfTokenException(ExceptionType type, Object... args) {
		super(type, args);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(ExceptionType type, String message) {
		super(type, message);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(ExceptionType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(int exceptionCode, String message, Throwable cause) {
		super(exceptionCode, message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(int exceptionCode, String message) {
		super(exceptionCode, message);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidCsrfTokenException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
