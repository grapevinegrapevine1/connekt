package com.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.News;
import com.model.News_user;
import com.service.NewsService;
import com.service.News_userService;

/**
 * ニューステーブル用汎用クラス
 */
@Service
public class NewsUtil {

	@Autowired private NewsService newsService;
	@Autowired private News_userService news_userService;
	
	/**
	 * プラン/オプション削除時、ニュース登録
	 */
	public int saveNew_removePlan(boolean isPlan, int store_id, String store_name, String plan_name) {
		String nm = isPlan ? "プラン" : "オプション";
		String msg = "店舗「"+store_name+"」の"+nm+"「"+plan_name+"」が店舗から削除されました。契約中である場合は次の更新日まで利用するこができ、次の更新日で自動的に解約されます。\n詳細は店舗一覧の「契約情報」または、店舗画面の「契約中のプラン詳細」でも確認することができます。";
		return saveNews(store_id, store_name, plan_name, msg);
	}
	/**
	 * 店舗削除時、ニュース登録
	 */
	public int saveNew_removeStore(boolean isPlan, int store_id, String store_name, String plan_name, long amount) {
		String nm = isPlan ? "プラン" : "オプション";
		String msg = "店舗「"+store_name+"」が削除されました。契約中の"+nm+"「"+plan_name+"」は利用規約(「契約料金 - (契約料金 / 利用上限 * 利用数)」)に基づき未利用分の"+amount+"円が返金されます。";
		return saveNews(store_id, store_name, plan_name, msg);
	}
	/**
	 * ニュース情報登録
	 */
	private int saveNews(int store_id, String store_name, String plan_name, String msg) {
		
		// ニュース登録
		News news = new News();
		news.setNews(msg);
		news.setStore_id(store_id);
		news.setCreated_date(new Date());
		news = newsService.save(news);
		return news.getId();
	}
	
	/**
	 * ニュースユーザー登録
	 */
	public void saveNews_user(int user_id, int news_id) {
		
		// ニュースユーザー登録
		News_user news_user = new News_user();
		news_user.setUser_id(user_id);
		news_user.setNews_id(news_id);
		news_userService.save(news_user);
	}
}
