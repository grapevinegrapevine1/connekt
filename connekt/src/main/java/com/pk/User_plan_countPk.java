package com.pk;

import java.io.Serializable;
import lombok.Data;

@Data
public class User_plan_countPk implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public int id;
	public int user_id;
	public int plan_id;
	public long start_date;
	public long end_date;
}