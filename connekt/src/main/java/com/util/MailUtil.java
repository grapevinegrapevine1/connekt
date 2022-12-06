package com.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.properties.Const;

@Service
public class MailUtil {

	// メールクラス
	@Autowired private JavaMailSender javaMailSender;
	
	/**
	 * ユーザー認証メール
	 */
	public void sendUserCertMail(String toEmail, String url) {
			
			// メールクラス
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		// 送信元
		mailMessage.setFrom(Const.MAIL_FROM);
		// 送信対象
		mailMessage.setTo(Const.TEST_EMAIL);
		// 件名
		mailMessage.setSubject("baken - ユーザー認証");
		// 本文
		mailMessage.setText("下記のURLからユーザー認証を行ってください。\nユーザー認証後、ログイン画面からご登録いただいたメールアドレスとパスワードでログインすることができます。\n\nユーザー認証用URL:" + url);
		
		// メール送信
		javaMailSender.send(mailMessage);
	}

	/**
	 * パスワード忘れ時メール
	 */
	public void sendForgetMail(String toEmail, String pw) {
		
		// メールクラス
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		// 送信元
		mailMessage.setFrom(Const.MAIL_FROM);
		// 送信対象
		mailMessage.setTo(Const.TEST_EMAIL);
		// 件名
		mailMessage.setSubject("baken - パスワード再設定");
		// 本文
		mailMessage.setText("パスワードが再設定されました。下記パスワードでログインすることができます。\nまた、ログイン後は設定画面からパスワードの変更を行ってください。\n\nパスワード:" + pw);
		
		// メール送信
		javaMailSender.send(mailMessage);
	}
	
	/**
	 * 店舗設定画面アクセス認証キー通知
	 */
	public void sendStoreSettingCertKey(String toEmail, String cert_key) {
			
			// メールクラス
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		// 送信元
		mailMessage.setFrom(Const.MAIL_FROM);
		// 送信対象
		mailMessage.setTo(Const.TEST_EMAIL);
		// 件名
		mailMessage.setSubject("baken - 店舗設定へのアクセス認証キー");
		// 本文
		mailMessage.setText("店舗設定画面へのアクセス認証キー通知です。\n\n下記認証キーを認証画面へ入力し、店舗設定画面へアクセスして下さい。\n\n認証キー：" + cert_key);
		
		// メール送信
		javaMailSender.send(mailMessage);
	}
	
	/**
	 * プラン削除
	 */
	public void sendNews_deletePlan(String toEmail, String storeNm, String planNm, String endDateStr, boolean isPlan) {
		
		String nm = isPlan ? "プラン" : "オプション";
		
			// メールクラス
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		// 送信元
		mailMessage.setFrom(Const.MAIL_FROM);
		// 送信対象
		mailMessage.setTo(Const.TEST_EMAIL);
		// 件名
		mailMessage.setSubject("baken - 契約中の" + nm + "が店舗から削除されました");
		// 本文
		mailMessage.setText("契約中の下記" + nm + "が削除されました。\n\n店舗名：" + storeNm + "\n契約" + nm + "名:" + planNm + "\n\nなお、この" + nm + "は次の更新日まで利用することができ、次の更新日で自動的に解約されます。\n解約日は"+endDateStr+"です。\n\n詳細は店舗一覧の「契約情報」または、店舗画面の「契約中のプラン詳細」でも確認することができます。");
		
		// メール送信
		javaMailSender.send(mailMessage);
	}
	
	/**
	 * 店舗削除
	 */
	public void sendNews_deleteStore(String toEmail, String storeNm, String planNm, long refundAmount, boolean isPlan) {
		
		String nm = isPlan ? "プラン" : "オプション";
		
			// メールクラス
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		// 送信元
		mailMessage.setFrom(Const.MAIL_FROM);
		// 送信対象
		mailMessage.setTo(Const.TEST_EMAIL);
		// 件名
		mailMessage.setSubject("baken - 契約中の店舗「"+storeNm+"」が削除されました");
		// 本文
		mailMessage.setText("契約中の店舗「" + nm + "」が削除されました。\n契約契約中の" + nm + "「" + planNm + "」は利用規約(「契約料金 - (契約料金 / 利用上限 * 利用数)」)に基づき未利用分の"+refundAmount+"円が返金されます。");
		
		// メール送信
		javaMailSender.send(mailMessage);
	}
}
