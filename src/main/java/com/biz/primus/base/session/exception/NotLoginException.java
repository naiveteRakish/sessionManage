package com.biz.primus.base.session.exception;

import com.biz.primus.common.exception.BizSilentException;
import com.biz.primus.common.exception.ExceptionType;

public class NotLoginException extends BizSilentException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotLoginException(ExceptionType type, Object... args) {
		super(type, args);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(ExceptionType type, String message) {
		super(type, message);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(ExceptionType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(int exceptionCode, String message, Throwable cause) {
		super(exceptionCode, message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(int exceptionCode, String message) {
		super(exceptionCode, message);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotLoginException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
