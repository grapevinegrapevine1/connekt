package com.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.pk.News_userPk;

@Entity
@Table(name = "news_user")
@IdClass(value=News_userPk.class)
public class News_user {

	@Id
	private int user_id;
	@Id
	private int news_id;
	
	// ニュース情報
	@OneToOne
	@JoinColumn(name="news_id", insertable = false, updatable = false)
	private News news;

	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getNews_id() {
		return news_id;
	}
	public void setNews_id(int news_id) {
		this.news_id = news_id;
	}
	public News getNews() {
		return news;
	}
	public void setNews(News news) {
		this.news = news;
	}
}
