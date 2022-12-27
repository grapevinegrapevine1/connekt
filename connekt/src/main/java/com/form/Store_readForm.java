package com.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Store_readForm {

	@NotBlank
	@Size(max=100)
	private String first_name;
	@NotBlank
	@Size(max=100)
	private String last_name;
	@NotBlank
	@Size(max=30)
	private String tel;
	private Date birth;
	@NotBlank
	private String birth_str;
	private boolean is_plan_count;
	
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = removeSpace(first_name);
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = removeSpace(last_name);
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel.replaceAll("[^0-9]", "");;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public String getBirth_str() {
		return birth_str;
	}
	public void setBirth_str(String birth_str) throws ParseException {
		this.birth_str = birth_str;
		this.birth = new SimpleDateFormat("yyyy-MM-dd").parse(birth_str);
	}
	public boolean getIs_plan_count() {
		return is_plan_count;
	}
	public void setIs_plan_count(boolean is_plan_count) {
		this.is_plan_count = is_plan_count;
	}
	/**
	 * 空白削除
	 */
	private static String removeSpace(String str) {
		return str.replaceAll("\\s+","");
	}
	
}
