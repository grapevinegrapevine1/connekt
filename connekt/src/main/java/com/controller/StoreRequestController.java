package com.controller;

import java.util.ArrayList;
import java.util.Date;
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

import com.base.User_count;
import com.base.User_relation;
import com.enm.ResultWithinDate;
import com.form.Store_readForm;
import com.form.Store_requestForm;
import com.model.Store;
import com.model.User;
import com.model.User_option;
import com.model.User_option_count;
import com.model.User_option_count_date;
import com.model.User_plan;
import com.model.User_plan_count;
import com.model.User_plan_count_date;
import com.properties.Const;
import com.repository.User_option_count_dateRepository;
import com.repository.User_plan_count_dateRepository;
import com.service.Non_app_idService;
import com.service.UserService;
import com.service.User_optionService;
import com.service.User_option_countService;
import com.service.User_option_count_dateService;
import com.service.User_planService;
import com.service.User_plan_countService;
import com.service.User_plan_count_dateService;
import com.stripe.Stripe;
import com.stripe.model.PaymentMethodCollection;
import com.util.CommonUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StoreRequestController")
public class StoreRequestController extends BaseStoreController{
	
	@Autowired private UserService userService;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private User_plan_countService user_plan_countService;
	@Autowired private User_option_countService user_option_countService;
	@Autowired private User_plan_count_dateRepository user_plan_count_dateRepository;
	@Autowired private User_option_count_dateRepository user_option_count_dateRepository;
	@Autowired private User_plan_count_dateService user_plan_count_dateService;
	@Autowired private User_option_count_dateService user_option_count_dateService;
	@Autowired private Non_app_idService non_app_idService;
	@Autowired private StripeUtil stripeUtil;
	
	// セッションキー
	private static final String REQUEST_USER_ID= "request_user_id";
	
/* 通常リクエスト */
	
	/**
	 * 画面表示 - 画面表示前のチェック
	 */
	@RequestMapping("/store_request")
	@Transactional
	public String checkDisp(@RequestParam(value="user_id",required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// ユーザー
		User user = userService.find(user_id);
		// プランチェック
		stripeUtil.checkUpdatePlans(user);
		// 遷移
		return Const.REDIRECT_HEADER + "/disp_store_request?user_id=" + user_id;
	}
	
	/**
	 * 画面表示
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/disp_store_request")
	public String disp(@RequestParam(value="user_id",required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Stripeキー
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		
		// ユーザー
		User user = userService.find(user_id);
		
		// ユーザーが存在しない場合
		if(user == null) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add("リクエストされた顧客情報は存在しませんでした。顧客IDをご確認ください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 読取画面へ遷移
			return new StoreReadController().disp(model, req, ses);

		// ユーザーが存在する場合
		}else {
			
			// 選択中のプラン
			User_plan user_plan = user_planService.contain(store.getId(), user_id);
			// 選択中のオプション
			List<User_option> user_options = user_optionService.containByUserId(store.getId(), user_id);
			
			// プランが存在しない場合
			if(user_plan == null && 0 == user_options.size()) {
				
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				// エラーメッセージ
				error_messages.add("この顧客はプランが登録されていません。まずはプランの契約を行った後、再度手続きを行ってください。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
	
				// 読取画面へ遷移
				return new StoreReadController().disp(model, req, ses);
				
			// プランが存在する場合
			}else {
				
			/* プランテキスト作成 */
				
				// プラン文章
				String planText = stripeUtil.getPlanText(user_plan, user_options);
				
				// フォーム設定
				Store_requestForm store_requestForm = new Store_requestForm();
				store_requestForm.setUser_id(user_id);
				
				// モデル
				model.addAttribute("user_name", user.getName());
				model.addAttribute("user_plan", user_plan);
				model.addAttribute("user_options", user_options);
				model.addAttribute("store_requestForm", store_requestForm);
				// リクエスト
				req.setAttribute("contract_text", "以下利用プランのご確認をお願いします\n" + planText);
				// セッション
				ses.setAttribute(REQUEST_USER_ID, user_id);
				
				// 遷移
				return Const.PAGE_STORE_REQUEST;
			}
		}
	}
	
/* 氏名でリクエスト */
	
	/**
	 * 氏名でリクエストされた際の画面表示 - 画面表示前のチェック
	 */
	@Transactional
	@PostMapping("/store_request_user")
	public String checkReqNonAppUser(@ModelAttribute @Validated Store_readForm store_readForm, BindingResult bindingResult,Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		// ユーザー名
		String name = store_readForm.getFirst_name() + store_readForm.getLast_name();
		
		// ユーザー
		User user = userService.findNonAppUser(name, store_readForm.getTel(), store_readForm.getBirth());
		
		// ユーザーが存在しない場合
		if(user == null) {

			// ユーザー作成
			user = new User();
			user.setEmail("baken_non_app_user_" + non_app_idService.createId().getId() + "@baken.sakura.ne.jp");
			user.setName(name);
			user.setPassword(Const.NON_APP_USER_PASSWORD);
			user.setTel(store_readForm.getTel());
			user.setBirth(store_readForm.getBirth());
			
			// 顧客作成
			String customerId = stripeUtil.createCustomer(user.getEmail(),user.getName());
			user.setStripe_customer_id(customerId);
			
			// ユーザー保存
			user = userService.save(user);

			// カード登録画面へ遷移
			return stripeUtil.createCard_nonAppUser(req, customerId, getUserStoreUrl(user.getId()) );
			
		// ユーザーが存在する場合
		}else {
			
			// 顧客ID
			String stripe_customer_id = stripeUtil.getCustomer(user.getStripe_customer_id());
			// 支払情報
			PaymentMethodCollection paymentMethods = stripeUtil.getPaymentMethods(stripe_customer_id);
			
			// 支払情報が存在しない（クレカ有効期限エラーでカード削除されている）場合
			if(0 == paymentMethods.getData().size()) {
				
				// カード登録画面へ遷移
				return stripeUtil.createCard_nonAppUser(req, stripe_customer_id, getUserStoreUrl(user.getId()) );
				
			// 支払情報が存在する(正常である)場合
			}else {
				
				// ユーザープラン
				User_plan user_plan = user_planService.contain(store.getId(), user.getId());
				// ユーザーオプション
				List<User_option> user_options = user_optionService.containByUserId(store.getId(), user.getId());
				
				// プランチェック
				stripeUtil.checkUpdatePlans(user);
				
				// ユーザープラン・オプションが存在しない or プラン更新時である場合
				if((user_plan == null && 0 == user_options.size()) ||!store_readForm.getIs_plan_count()) {
					
					// プラン選択画面
					return Const.REDIRECT_HEADER + getUserStoreUrl(user.getId());
					
				// ユーザープランまたはオプションが存在する場合
				}else {
					
					// セッション設定
					ses.setAttribute(REQUEST_USER_ID, user.getId());
					// 遷移
					return Const.REDIRECT_HEADER + "/disp_store_request_user?user_id=" + user.getId();
				}
			}
		}
	}
	
	/**
	 * 氏名でリクエストされた際の画面表示
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@GetMapping("/disp_store_request_user")
	public String reqNonAppUser(@RequestParam(name="user_id", required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// 更新対象セッションユーザーID
		int ses_user_id = (int) ses.getAttribute(REQUEST_USER_ID);
		// セッションユーザーIDと対象ユーザーIDが異なる場合
		if(ses_user_id != user_id) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add("不正なユーザー情報のアクセスです。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 読取画面へ遷移
			return new StoreReadController().disp(model, req, ses);
			
		// 正常である場合
		}else {
			// 手続き画面
			return disp(user_id, model, req, ses);
		}
	}
	
/* 更新 */
	
	/**
	 * ユーザープラン選択画面URL取得
	 */
	private static String getUserStoreUrl(int user_id) {
		return "/disp_search_store_nonAppUser?user_id=" + user_id;
	}
	
	/**
	 * カウントの更新
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/request_procedure")
	public String updateCount(@ModelAttribute @Validated Store_requestForm store_requestForm, BindingResult bindingResult ,Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// 更新対象セッションユーザーID
		int user_id = (int) ses.getAttribute(REQUEST_USER_ID);
		
		// 更新対象ユーザーIDが異なる場合
		if(user_id != store_requestForm.getUser_id()) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("別のウインドウで他のユーザー手続きが行われた可能性があるため、この画面では手続きを行うことはできません。再度この画面からやりなおして下さい。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 遷移
			return Const.PAGE_STORE_READ;
			
		// プラン・オプションが選択されていない場合
		}else if((store_requestForm.getUser_plan_id() == null || "0".equals(store_requestForm.getUser_plan_id())) && 
				 (store_requestForm.getUser_option_id() == null || 0 == store_requestForm.getUser_option_id().size())) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("手続きを行うプランが何も選択されていません。プランを選択しお手続きを行ってください。(手続きを行う事で選択したプランの顧客の利用回数を更新します)");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 遷移
			return disp(user_id, model, req, ses);
			
		// 正常である場合
		}else {
			
			// プランが選択されている場合
			if(store_requestForm.getUser_plan_id() != null && !"0".equals(store_requestForm.getUser_plan_id())) {
				
				// 保存
				String errFoward = countSave(store_requestForm.getUser_plan_id(), user_id, model, req, ses, true);
				// エラーが存在する場合はエラー画面遷移
				if(errFoward != null) return errFoward;
			}
			
			// オプションにチェックが入っている場合
			if(store_requestForm.getUser_option_id() != null) {
				
				// オプション数分
				for(String user_option_id : store_requestForm.getUser_option_id()) {
					
					// 保存
					String errFoward = countSave(user_option_id, user_id, model, req, ses, false);
					// エラーが存在する場合はエラー画面遷移
					if(errFoward != null) return errFoward;
				}
			}
			
			// メッセージ
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("手続きが完了しました。");
			// リクエスト
			req.setAttribute(Const.MSG_INFO, info_messages);
			
			// 遷移
			return disp(user_id, model, req, ses);
		}
	}
	
	/**
	 * 利用数更新
	 */
	@SuppressWarnings("unused")
	private String countSave(String user_plan_id, int user_id, Model model, HttpServletRequest req, HttpSession ses, boolean isPlan) throws Exception {

		// プラン/オプションID・開始日・終了日
		String[] split = user_plan_id.split("-");
		int plan_id = Integer.parseInt(split[0]);
		long start_date = Long.parseLong(split[1]);
		long end_date = Long.parseLong(split[2]);
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		
		// ユーザープラン
		User_relation user_relation = isPlan
				? user_planService.containByPlanId(store.getId(), user_id, plan_id)
				: user_optionService.containByPlanId(store.getId(), user_id, plan_id);
		// プラン/オプションが存在しない場合
		if(user_relation == null) return errDisp("既に契約が解除されているため手続きを行うことができませんでした。", user_id, model, req, ses);
		
		// 決済エラー可否(true:エラー)
		boolean errUsrPay = stripeUtil.chkErrPaymentIntent(stripeUtil.getSubscription(user_relation.getStripe_subscription_id()));
		// 決済エラーである場合
		if(errUsrPay) {
			// 遷移
			return errDisp("決済に失敗している契約が含まれています。", user_id, model, req, ses);
			
		// 決済エラーでない場合
		}else{
			
			// 利用期間内チェック
			ResultWithinDate result = CommonUtil.chkWithinDate(start_date, end_date);
			// 開始日より前である場合
			if(result == ResultWithinDate.ERR_PRE) {
				// 遷移
				return errDisp("利用開始前の契約が含まれるため利用することができません。", user_id, model, req, ses);
				
			// 終了日より後ろである場合
			}else if(result == ResultWithinDate.ERR_AFT){
				// 遷移
				return errDisp("すでに利用期間を終了している契約が含まれているため利用することができません。", user_id, model, req, ses);
			}
		}
		
		// DBのカウント情報
		User_count user_count;
		// 利用情報取得
		if(isPlan) user_count = user_plan_countService.contain(user_id, plan_id, start_date, end_date);
		else user_count = user_option_countService.contain(user_id, plan_id, start_date, end_date);
		
		// 同日中の利用可否
		boolean usedToday = isPlan ? null != user_plan_count_dateRepository.checkUsedToday(user_count.getId())
									:null != user_option_count_dateRepository.checkUsedToday(user_count.getId());
		
		// 同日中の利用されている場合
		if(usedToday) {
			// 遷移
			return errDisp((isPlan ? ((User_plan_count)user_count).getPlan().getName(): ((User_option_count)user_count).getOption().getName()) + "は本日中に1度手続きされているため本日は利用することができません。", user_id, model, req, ses);
			
		//　正常である場合
		}else {
			
			// 既存カウント情報が存在する場合
			if(user_count != null) {
				
				// プラン/オプション利用上限数に達している場合
				if(user_count.getBaseCount() <= user_count.getCount()) {
					// 遷移
					return errDisp("利用数上限に達しているプランの利用手続きが実行されているため、利用手続きを行うことができませんでした。利用数上限に達していないプランのみ利用手続きを行うことができます。", user_id, model, req, ses);
					
				// 正常利用できる場合
				}else {
					
					// プラン保存時である場合
					if(isPlan) {
						// 利用日情報
						User_plan_count_date user_plan_count_date = new User_plan_count_date();
						user_plan_count_date.setUser_plan_count_id(user_count.getId());
						user_plan_count_date.setUse_date(new Date());
						// 保存
						user_plan_count_dateService.save(user_plan_count_date);
						
					// オプション保存時である場合
					}else {
						// 利用日情報
						User_option_count_date user_option_count_date = new User_option_count_date();
						user_option_count_date.setUser_option_count_id(user_count.getId());
						user_option_count_date.setUse_date(new Date());
						// 保存
						user_option_count_dateService.save(user_option_count_date);
					}
					
					// 返却
					return null;
				}
				
			// 既存カウント情報が見つからない場合
			}else {
				// 遷移
				return errDisp("不正にデータが更新されているため、手続きすることができませんでした。再度手続きをやり直してください。", user_id, model, req, ses);
			}
		}
	}
	
	/**
	 * エラー画面遷移
	 */
	private String errDisp(String msg, int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		error_messages.add(msg);
		// リクエスト
		req.setAttribute(Const.MSG_ERROR, error_messages);
		
		// 遷移
		return disp(user_id, model, req, ses);
	}
}
