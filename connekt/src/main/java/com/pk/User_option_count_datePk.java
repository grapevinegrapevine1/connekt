package com.pk;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class User_option_count_datePk implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int user_option_count_id;
	public Date use_date;
}