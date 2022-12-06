package com.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.properties.Const;
import jp.fintan.keel.spring.web.token.transaction.InvalidTransactionTokenException;

public class BaseUserController {
	
	@ExceptionHandler(InvalidTransactionTokenException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public String invalidTransactionTokenExceptionHandler(InvalidTransactionTokenException e) {
		return Const.PAGE_USER_TRANS_ERROR;
	}
}
