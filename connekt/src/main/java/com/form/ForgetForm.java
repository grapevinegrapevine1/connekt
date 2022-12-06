package com.form;

import javax.validation.constraints.NotBlank;

public class ForgetForm {

	// メールアドレス
	@NotBlank
	private String email;
	// 氏名・店舗名
	@NotBlank
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
