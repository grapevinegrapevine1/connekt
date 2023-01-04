package com.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.form.ForgetForm;
import com.model.User;
import com.model.User_option;
import com.model.User_plan;
import com.properties.Const;
import com.security.LoginController;
import com.service.CertUtil;
import com.service.UserService;
import com.service.User_optionService;
import com.service.User_planService;
import com.stripe.exception.InvalidRequestException;
import com.util.CommonUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("UserSettingController")
public class UserSettingController extends BaseUserController{
	
	@Autowired private UserService user_service;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private StripeUtil stripeUtil;
	@Autowired private CertUtil certUtil;
	
/* ユーザー設定 */
	
	/**
	 * 設定画面表示
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/user_setting")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User sesUser = CommonUtil.getSessionUser(ses);
		
		// モデル
		model.addAttribute("user", sesUser);
		model.addAttribute("is_create", false);
		// 遷移
		return Const.PAGE_USER_SETTING;
	}
	
	/**
	 * 設定値保存
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/save_user")
	public String update(@ModelAttribute @Validated User user, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// セッションユーザー情報
		User sesUser = CommonUtil.getSessionUser(ses);
		
		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		
		// IDが変更されている場合
		if(sesUser.getId() != user.getId()) {
			
			// ID上書き
			user.setId(sesUser.getId());
			
			// エラーメッセージ
			error_messages.add("IDが不正に変更されているため更新することができません。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
		// メールアドレスが変更されている場合
		}else if(!sesUser.getEmail().equals(user.getEmail())) {
			
			// メールアドレス上書き
			user.setEmail(sesUser.getEmail());
			
			// エラーメッセージ
			error_messages.add("メールアドレスが不正に変更されているため更新することができません。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
		// 正常である場合
		}else {
			
			// パスワードが入力されていない場合は、既存のパスワードを設定
			if(CommonUtil.isEmpty(user.getPassword())) user.setPassword(sesUser.getPassword());
			// 既存ユーザーデータ取得
			User srhUser = user_service.contain(user.getEmail());
			
			// 更新
			user.setStripe_customer_id(srhUser.getStripe_customer_id());
			sesUser = user_service.save(user);
			// セッション上書き
			CommonUtil.setSessionUser(ses, sesUser);
			
			// メッセージ
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("保存しました。");
			
			// リクエスト
			req.setAttribute(Const.MSG_INFO, info_messages);
		}
		
		// モデル
		model.addAttribute("is_create", false);
		
		// 遷移
		return Const.PAGE_USER_SETTING;
	}
	
/* ユーザー作成---------------------------------------------------------------------------------------- */
	
	/**
	 * ユーザー作成画面表示
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/user_create")
	public String dispCreate(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// モデル
		model.addAttribute("user", new User());
		model.addAttribute("is_create", true);
		
		// 遷移
		return Const.PAGE_USER_SETTING;
	}

	/**
	 * Stripe カード登録
	 * https://qiita.com/hideokamoto/items/0ea4fe01cac8c8ad8669
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/create_user")
	public String create(@ModelAttribute @Validated User user, BindingResult bindingResult, Model model, HttpServletRequest req) throws Exception {

		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// 既存ユーザー
		List<User> users_temp = user_service.contains(user.getEmail());
		User srhUser;
		
		// 既存同一メールアドレスユーザーが複数存在する場合
		if(2 <= users_temp.size()) {
			// ユーザー削除
			for(User user_temp : users_temp) user_service.delete(user_temp.getId());
			// ユーザー設定
			srhUser = null;
		// 一意のユーザーである場合
		}else {
			// ユーザー設定
			srhUser = 0 < users_temp.size() ? users_temp.get(0) : null;
		}
		
		// ユーザーが存在しない or ユーザー登録前である(Customer作成時に何らかのエラー発生ユーザー) or カード登録前である(カード登録後遷移に失敗している)場合
		if(srhUser == null || srhUser.getStatus() == Const.USER_STATUS_DEAULT || srhUser.getStatus() == Const.USER_STATUS_SET_CARD) {
			
			// 既存ユーザーが存在する場合は削除
			if(srhUser != null) user_service.delete(srhUser.getId());
			
			// 顧客ID
			String customerId = null;
			// 顧客作成
			try {
				customerId = stripeUtil.createCustomer(user.getEmail(),user.getName());
			}catch(InvalidRequestException e) {
				// 現金決済種別が存在するエラーである場合
				if(stripeUtil.isDeleteCusWithCach(e)) {
					// エラーメッセージ
					List<String> error_messages = new ArrayList<String>();
					error_messages.add("入力されたメールアドレスは利用することができません。");
					// リクエスト
					req.setAttribute(Const.MSG_ERROR, error_messages);
					// モデル
					model.addAttribute("is_create", true);
					// 遷移
					return Const.PAGE_USER_SETTING;
				}
			}
			
			user.setStripe_customer_id(customerId);
			
		/* カード登録開始 */
			
			// ステータス変更
			user.setStatus(Const.USER_STATUS_SET_CARD);
			// 保存
			user = user_service.save(user);
			
			// カード登録画面へ遷移
			return stripeUtil.createCard_user(req, customerId, user.getId());
			
		// 正常である場合
		}else {
			
			// 認証前ユーザーである場合
			if(srhUser.getStatus() == Const.USER_STATUS_CERT_START) {
				
				// 認証メール送信
				certUtil.resendCertMail(srhUser.getId(), srhUser.getEmail(), req, true);
				// 遷移
				return LoginController.dispLogin(model, req, true);
				
			// ユーザーが存在する場合
			}else {
				
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				error_messages.add("入力されたメールアドレスは、既に登録されているため新たに登録することができません。別のメールアドレスを登録してください。");
				
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				// モデル
				model.addAttribute("is_create", true);
				
				// 遷移
				return Const.PAGE_USER_SETTING;
			}
		}
	}
	
	/**
	 * カード登録後の認証開始
	 */
	@Transactional
	@GetMapping("/cert_start")
	public String certStart(@RequestParam(name="user_id", required=true) int user_id, Model model, HttpServletRequest req) throws Exception {

		// 既存ユーザー
		User user = user_service.find(user_id);
		
		// ユーザーが存在する & カード登録終了後である場合
		if(user != null && user.getStatus() == Const.USER_STATUS_SET_CARD) {
			
		/* 認証開始 */
			
			// ステータス更新
			user.setStatus(Const.USER_STATUS_CERT_START);
			// ユーザー保存
			user = user_service.save(user);
			
			// 認証メール送信
			certUtil.sendCertMail(user.getId(), user.getEmail(), req, true);
			
			// 遷移
			return LoginController.dispLogin(model, req, true);
			
		// 不正である場合
		}else{

			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("不正なアクセスです。");
			
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 遷移
			return LoginController.dispLogin(model, req, true);
		}
	}
	
/* 認証 */
	
	/**
	 * 再認証入力
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/user_recert_input")
	public String recertInput(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return certUtil.recertInput(true, model);
	}
	
	/**
	 * 認証再送信
	 */
	@TransactionTokenCheck
	@PostMapping("/user_recert")
	public String recert(@ModelAttribute @Validated ForgetForm forgetForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return certUtil.recert(forgetForm, bindingResult, model, req, true);
	}
	
	/**
	 * ユーザー認証
	 */
	@Transactional
	@GetMapping("/cert_user")
	public String cert(@RequestParam(value="cert",required=true) String cert, Model model, HttpServletRequest req) throws Exception {
		return certUtil.cert(cert, model, req, true);
	}
	
/* 削除 */
	
	/**
	 * ユーザー削除
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/delete_user")
	public String delete(@RequestParam(name="id", required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User sesUser = CommonUtil.getSessionUser(ses);
		// IDが異なる場合
		if(user_id != sesUser.getId()) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add("IDが不正に変更されているため更新することができません。");
			
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			// モデル
			model.addAttribute("user", sesUser);
			model.addAttribute("is_create", false);
			
			// 遷移
			return Const.PAGE_USER_SETTING;
			
		// 正常である場合
		}else {
			
			// 既存ユーザー
			User srhUser = user_service.find(sesUser.getId());
			
			// ユーザーが存在する場合
			if(srhUser != null) {
				
				// ユーザープラン
				List<User_plan> user_plans = user_planService.find_list(srhUser.getId());
				// 全ユーザープラン数分
				for(User_plan user_plan : user_plans) {
					// サブスクキャンセル(ユーザープラン関連データは請求書用に残す。ユーザープラン関連データは残っていてもSubscriptionが破棄されていれば更新されない)
					stripeUtil.cancelPlanSubscription(user_plan);
				}
				
				// ユーザーオプション
				List<User_option> user_options = user_optionService.find_list(srhUser.getId());
				// 全ユーザーオプションン数分
				for(User_option user_option : user_options) {
					// サブスクキャンセル
					stripeUtil.cancelOptionSubscription(user_option);
				}
			}
			
			// 削除ステータスに更新
			srhUser.setStatus(Const.USER_STATUS_DELETED);
			// メールアドレスを削除済みに更新(メールアドレスに入力できない文字を結合するためログイン不可となる)
			srhUser.setEmail(Const.USER_DELETED_EMAIL + srhUser.getEmail());
			// 保存
			user_service.save(srhUser);
			
			// メッセージ
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("ユーザーの削除が完了しました。");
			// リクエスト
			req.setAttribute(Const.MSG_INFO, info_messages);
			
			// ログイン画面へ遷移
			return LoginController.dispLogin(model, req, true);
		}
	}
}
