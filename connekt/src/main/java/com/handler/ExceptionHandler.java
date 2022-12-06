package com.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;

import com.properties.Const;
import com.stripe.exception.ApiConnectionException;

@ControllerAdvice
public class ExceptionHandler {
	
	/**
	 * Stripeネットワークエラー
	 */
	@org.springframework.web.bind.annotation.ExceptionHandler(ApiConnectionException.class)
	public String handleOriginalWebException(ApiConnectionException exception) {
		return Const.REDIRECT_HEADER + Const.PAGE_STRIPE_CONNECTION_ERROR;
	}
}
