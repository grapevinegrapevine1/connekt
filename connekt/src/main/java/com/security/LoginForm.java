package com.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class LoginForm extends User {

	private static final long serialVersionUID = 1L;

	public LoginForm(String email, String password, Collection<? extends GrantedAuthority> authorities) {
		super(email, password, authorities);
	}

}
