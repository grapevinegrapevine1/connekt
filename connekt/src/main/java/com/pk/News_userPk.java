package com.pk;

import java.io.Serializable;
import lombok.Data;

@Data
public class News_userPk implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int user_id;
	public int news_id;
}