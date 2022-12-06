package com.pk;

import java.io.Serializable;
import lombok.Data;

@Data
public class User_option_countPk implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int user_id;
	public int option_id;
	public long start_date;
	public long end_date;
}