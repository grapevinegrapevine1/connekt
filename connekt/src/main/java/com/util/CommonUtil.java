package com.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Hex;
import org.springframework.validation.BindingResult;

import com.base.User_count;
import com.enm.ResultWithinDate;
import com.model.Store;
import com.model.User;
import com.properties.Const;

public class CommonUtil {

	/**
	 * セッションユーザー情報をリクエストに設定
	 */
	public static void setReqSessionUser(HttpServletRequest req, HttpSession ses) {
		// セッション情報設定
		req.setAttribute(Const.SESSION_USER, ses.getAttribute(Const.SESSION_USER));
		// 店舗セッション削除
		req.removeAttribute(Const.SESSION_STORE);
	}

	/**
	 * セッション店舗情報をリクエストに設定
	 */
	public static void setReqSessionStore(HttpServletRequest req, HttpSession ses) {
		// セッション情報設定
		req.setAttribute(Const.SESSION_STORE, ses.getAttribute(Const.SESSION_STORE));
		// 店舗セッション削除
		req.removeAttribute(Const.SESSION_USER);
	}

	/**
	 * セッションユーザー情報取得
	 */
	public static User getSessionUser(HttpSession ses) {
		return ses.getAttribute(Const.SESSION_USER) == null ? null : (User) ses.getAttribute(Const.SESSION_USER);
	}

	/**
	 * セッション店舗情報取得
	 */
	public static Store getSessionStore(HttpSession ses) {
		return ses.getAttribute(Const.SESSION_STORE) == null ? null : (Store) ses.getAttribute(Const.SESSION_STORE);
	}

	/**
	 * セッションユーザー情報設定
	 */
	public static void setSessionUser(HttpSession ses, User user) {
		ses.setAttribute(Const.SESSION_USER, user);
	}

	/**
	 * セッション店舗情報設定
	 */
	public static void setSessionStore(HttpSession ses, Store store) {
		ses.setAttribute(Const.SESSION_STORE, store);
	}

	/**
	 * セッションユーザー情報削除
	 */
	public static void removeSessionUser(HttpSession ses) {
		ses.removeAttribute(Const.SESSION_USER);
	}

	/**
	 * セッション店舗情報削除
	 */
	public static void removeSessionStore(HttpSession ses) {
		ses.removeAttribute(Const.SESSION_STORE);
	}

	/**
	 * セッション情報削除
	 */
	public static void removeSession(HttpSession ses) {
		removeSessionUser(ses);
		removeSessionStore(ses);
	}

	/**
	 * 文字列空チェック
	 */
	public static boolean isEmpty(String val) {
		return val == null || val.equals("");
	}

	/**
	 * タイムスタンプを日付文字列へ変換
	 */
	public static String formatTimestamp(long unix) {
		Date date = new Date(unix * 1000);
		
		date = convertUnixToTimestamp(date);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateFormat.format(date);
	}
	public static Date formatTimestampDate(long unix) {
		Date date = new Date(unix * 1000);
		
		date = convertTimestampToDate(date);
		
		return date;
	}
	public static long getTimestamp(long timestamp) {
		Date date = new Date(timestamp * 1000);
		return date.getTime();
	}
	
	public static long getUnixToTime(Date date) {
		
		return convertUnixToTimestamp(date).getTime() / 1000;
	}
	
	private static Date convertUnixToTimestamp(Date date) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		//cal.add(Calendar.HOUR, 24);
		return cal.getTime();
	}

	private static Date convertTimestampToDate(Date date) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		//cal.add(Calendar.HOUR, 24);
		return cal.getTime();
	}
	
	/**
	 * Validatoinチェック
	 */
	public static String isValid(BindingResult bindingResult, HttpServletRequest req) {

		// エラーメッセージ
		String errorMessage = null;

		// エラーが存在する場合
		if (bindingResult != null && bindingResult.hasErrors()) {
			// エラーメッセージ取得
			errorMessage = bindingResult.getAllErrors().stream().map(m -> m.getDefaultMessage())
					.collect(Collectors.joining("・"));
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add(errorMessage);
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
		}
		// 返却
		return errorMessage;
	}

	/**
	 * 暗号化
	 */
	public static String encrypt(String text) throws Exception {
		
		// 秘密鍵
		SecretKeySpec sksSpec = new SecretKeySpec(Const.ENCRYPT_KEY.getBytes(), "Blowfish");
		
		// 暗号クラス
		Cipher cipher = Cipher.getInstance("Blowfish");
		// 暗号・複合設定
		cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);
		// 暗号化
		byte[] encByte = cipher.doFinal(text.getBytes());
		
		// 16進数化し返却
		return new String(Hex.encodeHex(encByte));
	}
	/**
	 * 複合化
	 */
	public static String decrypt(String hexEncText) throws Exception {
		
		// 秘密鍵
		SecretKeySpec sksSpec = new SecretKeySpec(Const.ENCRYPT_KEY.getBytes(), "Blowfish");
		
		// 暗号・複合クラス
		Cipher cipher = Cipher.getInstance("Blowfish");
		// 複合設定
		cipher.init(Cipher.DECRYPT_MODE, sksSpec);
		
		// 16進数複合
		byte[] text = Hex.decodeHex(hexEncText.toCharArray());
		// 複合化
		byte[] decText = cipher.doFinal(text);
		
		// 返却
		return new String(decText);
	}
	
	/**
	 * URLヘッダー
	 */
	public static String getUrlHead(HttpServletRequest req) throws MalformedURLException {
		// URL
		URL url = new URL(req.getRequestURL().toString());
		return url.getProtocol() + "://" + url.getAuthority();
	}

	/**
	 * 利用期間内チェック
	 */
	public static ResultWithinDate chkWithinDate(long start_date, long end_date) {

		long sysDate = new Date().getTime();
		
		ResultWithinDate result;
		
		if(sysDate < (start_date * 1000)) result = ResultWithinDate.ERR_PRE;
		else if((end_date * 1000) < sysDate) result = ResultWithinDate.ERR_AFT;
		else result = ResultWithinDate.NOT_ERR;
		
		return result;
	}
	
	public static int convertStipeTimestamp(Date date) {
		return (int) (date != null ? date.getTime() / 1000 : 0);
	}
	
	/**
	 * プラン利用数取得
	 */
	public static int getUserCount(User_count user_count) {
		// カウント
		return user_count == null ? 0 : user_count.getCount();
	}
	
	/**
	 * 返品可否
	 */
	public static boolean isRefund(LocalDateTime created_date, int userCount) {

		// 返品可否
		LocalDateTime now = LocalDateTime.now(); 
		// 契約日 ＋ 2日
		LocalDateTime refundPeriod =created_date.plusDays(2);
		// 期限内可否(true:期限内)
		boolean inPeriod = refundPeriod.isAfter(now);
		// 返品可否設定(未利用 & 期限内)
		return userCount == 0 && inPeriod;
	}
	
	/**
	 * セッション情報が存在するかをチェック
	 */
	public static String isSesUser(HttpSession ses) {
		// セッションユーザーが存在しない場合はエラー画面遷移
		return CommonUtil.getSessionUser(ses) != null ? null : Const.REDIRECT_HEADER + Const.PAGE_SESSION_ERROR;
	}
	public static String isSesStore(HttpSession ses) {
		// セッションユーザーが存在しない場合はエラー画面遷移
		return CommonUtil.getSessionStore(ses) != null ? null : Const.REDIRECT_HEADER + Const.PAGE_SESSION_ERROR;
	}
}
