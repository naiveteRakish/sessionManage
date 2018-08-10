package com.biz.primus.base.session.exception;

import com.biz.primus.common.exception.BizSilentException;
import com.biz.primus.common.exception.ExceptionType;

public class NotFindRequiredFieldException extends BizSilentException{

	public NotFindRequiredFieldException(ExceptionType type, Object... args) {
		super(type, args);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(ExceptionType type, String message) {
		super(type, message);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(ExceptionType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(int exceptionCode, String message, Throwable cause) {
		super(exceptionCode, message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(int exceptionCode, String message) {
		super(exceptionCode, message);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotFindRequiredFieldException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	

}
