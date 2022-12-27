package com.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.base.User_count;
import com.base.User_relation;
import com.form.User_storeForm;
import com.model.Option;
import com.model.Plan;
import com.model.Store;
import com.model.User;
import com.model.User_option;
import com.model.User_plan;
import com.properties.Const;
import com.service.OptionService;
import com.service.PlanService;
import com.service.StoreService;
import com.service.UserService;
import com.service.User_optionService;
import com.service.User_option_countService;
import com.service.User_planService;
import com.service.User_plan_countService;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.util.CommonUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("UserStoreController")
public class UserStoreController extends BaseUserController{

	@Autowired private StoreRequestController storeRequestController;
	@Autowired private UserService userService;
	@Autowired private StoreService storeService;
	@Autowired private PlanService planService;
	@Autowired private OptionService optionService;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private User_plan_countService user_plan_countService;
	@Autowired private User_option_countService user_option_countService;
	@Autowired private StripeUtil stripeUtil;
	
	// セッションキー
	private static final String NON_APP_USER_ID = "non_app_user_id";
	
/* ユーザーリクエスト */
	
	/**
	 * 画面表示前のチェック
	 */
	@Transactional
	@RequestMapping("/search_store")
	public String checkDisp(@RequestParam(name="store_id", defaultValue="0") int store_id,
							Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// プランチェック
		stripeUtil.callChkUpdatePlan(ses);
		// 遷移
		return Const.REDIRECT_HEADER + "/dsip_search_store?store_id=" + store_id;
	}
	
	/**
	 * 画面表示 - 検索
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/dsip_search_store")
	public String disp(@RequestParam(name="store_id", defaultValue="0") int store_id,
						Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return disp(store_id, null, model, req, ses);
	}
	
	/**
	 * 画面表示前のチェック - 保存後
	 */
	@Transactional
	@RequestMapping("/search_store_checkouted")
	public String checkDisp_checkout(@RequestParam(name="store_id", defaultValue="0") int store_id,
									 @RequestParam(name="stop_type", defaultValue="0") int stop_type,
									 Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// プランチェック
		stripeUtil.callChkUpdatePlan(ses);
		// 遷移
		return Const.REDIRECT_HEADER + "/disp_search_store_checkouted?store_id=" + store_id + "&stop_type=" + stop_type;
	}
	/**
	 * 画面表示 - 保存後検索
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/disp_search_store_checkouted")
	public String disp_checkouted(@RequestParam(name="store_id", defaultValue="0") int store_id,
								  @RequestParam(name="stop_type", defaultValue="0") int stop_type,
								  Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// メッセージ
		List<String> info_messages = new ArrayList<String>();
		info_messages.add("保存しました。");
		// リクエスト
		req.setAttribute(Const.MSG_INFO, info_messages);
		
		return disp(store_id, null, model, req, ses);
	}
	
/* アプリ無しユーザーリクエスト */
	
	/**
	 * 画面表示 - 店舗からの検索(アプリ無しユーザー検索)
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/disp_search_store_nonAppUser")
	public String disp_checkouted(@RequestParam(name="user_id", required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// ユーザー
		User user = userService.find(user_id);
		// 画面表示
		return dispSearchStoreNonAppUser(user, false, model, req, ses);
	}	
	/**
	 * 画面表示 - 店舗からの保存後検索(アプリ無しユーザー検索)
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/disp_search_store_nonAppUser_checkouted")
	public String disp_updated(@RequestParam(name="user_id", required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// ユーザー
		User user = userService.find(user_id);
		// 画面表示
		return dispSearchStoreNonAppUser(user, true, model, req, ses);
	}
	
	/**
	 * 画面表示 - アプリ無しユーザー検索
	 */
	private String dispSearchStoreNonAppUser(User user, boolean isUpdated, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		
		// ユーザーが存在しない場合はエラー
		if(user == null) return Const.PAGE_404;
		// ユーザーが存在する場合は画面表示
		else {
			
			// 更新後である場合
			if(isUpdated) {
				// メッセージ
				List<String> info_messages = new ArrayList<String>();
				info_messages.add("保存しました。");
				// リクエスト
				req.setAttribute(Const.MSG_INFO, info_messages);
			}
			
			// 画面表示
			return disp(store.getId(), user, model, req, ses);
		}
	}
	
/* 表示処理 */
	
	/**
	 * 画面表示情報生成
	 */
	private String disp(int store_id, User nonAppUser, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// 店舗情報
		Store store = storeService.find(store_id);
		// 店舗へ遷移
		if(store == null) return Const.REDIRECT_HEADER + "disp_user_list?is_error=true";
		
		// プラン一覧
		List<Plan> plans = planService.find_list(store.getId());
		// オプション一覧
		List<Option> options = optionService.find_list(store.getId());
		
		// モデル
		model.addAttribute("store", store);
		model.addAttribute("plans", plans);
		model.addAttribute("options", options);
		
		// 店舗からの遷移可否
		boolean from_store;
		// フォーム
		User_storeForm user_storeForm= new User_storeForm();
		// ユーザー
		User user;
		
		// 店舗手続き画面からの遷移でない場合
		if(nonAppUser == null) {

			// セッションユーザーチェック
			String sesErrFoward = CommonUtil.isSesUser(ses);
			if(sesErrFoward != null) return sesErrFoward;
			
			// セッションユーザー情報
			user = CommonUtil.getSessionUser(ses);
			// フォーム設定
			from_store = false;
			// セッション削除
			ses.removeAttribute(NON_APP_USER_ID);
			
		// 店舗手続き画面からの遷移である場合
		}else {
			
			// ユーザー情報設定
			user = nonAppUser;
			
			// フォーム設定
			from_store = true;
			// セッション設定
			ses.setAttribute(NON_APP_USER_ID, user.getId());
		}
		
		// 選択中のプラン
		User_plan user_plan = user_planService.contain(store_id, user.getId());
		// 削除済みプランである場合は項目に追加
		if(user_plan != null && user_plan.getPlan().getIs_delete() == Const.IS_DELETED) plans.add(0, user_plan.getPlan());
		
		// 選択中のオプション
		List<User_option> user_options = user_optionService.containByUserId(store_id, user.getId());
		// 削除済みプランである場合は項目に追加
		for(User_option user_option : user_options) if(user_option.getOption().getIs_delete() == Const.IS_DELETED) options.add(0, user_option.getOption());
		
		// プラン詳細
		String text = stripeUtil.getPlanText(user_plan, user_options);
		
		// オプションIDリストへ変換
		List<Integer>user_option_values = user_options.stream().map(uo->uo.getOption_id()).collect(Collectors.toList());
		// 停止リスト
		List<Integer>user_option_stops = user_options.stream().filter(uo->uo.getStripe_cancel_at_period_end()).map(uo->uo.getOption_id()).collect(Collectors.toList());
		
		// フォーム設定
		user_storeForm.setStore_id(store_id);
		user_storeForm.setUser_id(user.getId());
		user_storeForm.setUser_plan(user_plan);
		user_storeForm.setUser_options(user_option_values);
		user_storeForm.setUser_options_th(user_options);
		
		// モデル
		model.addAttribute("user_storeForm", user_storeForm);
		model.addAttribute("user_options", user_options);
		model.addAttribute("user_option_stops", user_option_stops);
		
		// セッション
		ses.setAttribute(Const.SESSION_STORE_ID, store_id);
		// リクエスト
		req.setAttribute("contract_text", text);
		req.setAttribute("from_store", from_store);
		
		// 遷移
		return Const.PAGE_USER_STORE;
	}
	
/* サブスク更新 */
	
	/**
	 * サブスク登録
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/checkout_user_plan")
	public String checkout(@ModelAttribute @Validated User_storeForm user_storeForm, BindingResult bindingResult,
							@RequestParam(name="from_store", required=true) boolean from_store,
							Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// 店舗からの遷移でない場合
		if(!from_store) {
			// セッションユーザーチェック
			String sesErrFoward = CommonUtil.isSesUser(ses);
			if(sesErrFoward != null) return sesErrFoward;
		}
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// セッション店舗ID
		int sesStore_id = (int) ses.getAttribute(Const.SESSION_STORE_ID);
		
		// セッション店舗IDと画面IDが異なる場合
		if(sesStore_id != user_storeForm.getStore_id()) {
			// エラーメッセージ
			CommonUtil.setMessage("別のウインドウで店舗の観覧または保存が行われたため、保存することができませんでした。再度対象の店舗へアクセスいただき保存を実行してください。",req);
			// 遷移
			return disp(sesStore_id, model, req, ses);
			
		// 正常である場合
		}else {
			
		/* チェック */
			
			// エラーチェック(セッションユーザーチェック)
			ChkValidResult chkValidResult = chkValidUpdate(user_storeForm.getStore_id(), user_storeForm.getUser_id(), from_store, model, req, ses);
			// エラーが存在する場合はエラー遷移
			if(chkValidResult.getErr_forward() != null) return chkValidResult.getErr_forward();
			
			// ユーザー
			User user = chkValidResult.getUser();
			
		/* プラン */
			
			// プラン情報
			Plan _plan = planService.find(user_storeForm.getUser_plan().getPlan_id());
			// 更新対象プラン情報
			Plan plan = null;
			// 変更前のプラン
			User_plan user_plan = user_planService.contain(sesStore_id, user.getId());
			
			// プランが存在する場合
			if(_plan != null) {
				
				// 更新実行フラグ
				boolean updatingPlan = false;
				// 変更前のプランが存在しない場合
				if(user_plan== null) {
					// プランの新規登録のためチェックアウト
					plan = _plan;
					updatingPlan = true;
				// プランが変更されている場合
				}else if(_plan.getId() != user_plan.getPlan_id()) {
					// 変更前のサブスクキャンセル
					stripeUtil.cancelPlanSubscription(user_plan);
					// プラン変更のためチェックアウト
					plan = _plan;
					updatingPlan = true;
				}
				
				// プランが既に削除されている場合
				if(updatingPlan && plan == null && 0 < user_storeForm.getUser_plan().getPlan_id()) {
					// エラーメッセージ
					CommonUtil.setMessage("選択されたプランは削除されているため保存することができませんでした。", req);
					// 遷移
					return disp(sesStore_id, model, req, ses);
				}
			// プランが存在しない場合
			}else {
				// プランが登録されている場合はサブスクキャンセル
				if(user_plan != null) stripeUtil.cancelPlanSubscription(user_plan);
			}
			
		/* オプション */
			
			// 選択オプションリスト
			List<Option> options = new ArrayList<Option>();
			
			// 選択オプションが存在する場合
			if(user_storeForm.getUser_options() != null) {

				// 変更前のオプション
				List<User_option> user_options = user_optionService.containByUserId(sesStore_id, user.getId());
				
				// 選択オプション数分
				for(int option_id : user_storeForm.getUser_options()) {

					// 変更前のオプション一覧に存在するか
					boolean exist = user_options.stream().anyMatch(uo->uo.getOption_id() == option_id);
					// 存在しない場合
					if(!exist) {
						// オプション情報
						Option option = optionService.find(option_id);
						// オプションが存在しない場合
						if(0 < option_id && option == null) {
							// エラーメッセージ
							CommonUtil.setMessage("選択されたオプションは削除されているため保存することができませんでした。",req);
							// 遷移
							return disp(sesStore_id, model, req, ses);
						}
						
						// リスト追加
						options.add(option);
					}
				}
				
				// 変更前のオプション一覧数分
				for(User_option user_option : user_options) {
					
					// 選択後のオプション一覧に存在するか
					boolean exist = user_storeForm.getUser_options().stream().anyMatch(_option_id->_option_id == user_option.getOption_id());
					// 存在しない場合
					if(!exist) {
						// サブスクキャンセル
						stripeUtil.cancelOptionSubscription(user_option);
					}
				}
			}
			
		/* データ更新 */
			
			// プランまたはオプションが選択されている(更新対象プランが存在する)場合
			if(plan != null || 0 < options.size() ) {
				
				// 支払情報
				PaymentMethodCollection paymentMethods = stripeUtil.getPaymentMethods(user.getStripe_customer_id());
				// 支払情報が存在しない場合
				if(0 == paymentMethods.getData().size()) {
					// エラーメッセージ
					CommonUtil.setMessage("決済情報が設定されていないため、契約を登録することができませんでした。トップ画面の決済情報設定から決済情報を設定してください。", req);
					// 遷移
					return disp(sesStore_id, model, req, ses);
					
				// 支払情報が存在する場合
				}else {
					// サブスク登録
					stripeUtil.setSubscription(sesStore_id, user.getId(), user.getStripe_customer_id(), plan, options, paymentMethods);
				}
			}
			
			// 遷移
			return updatedForward(from_store, user_storeForm.getStore_id(), user.getId(), -1);
		}
	}
	
/* 次回更新の停止・再開 */
	
	/**
	 * 次回更新の停止
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/stop_user_relation")
	public String stopUserPlan(@RequestParam(name="relation_id", required=true) int relation_id,
								@RequestParam(name="store_id", required=true) int store_id,
								@RequestParam(name="user_id", required=true) int user_id,
								@RequestParam(name="is_plan", required=true) boolean isPlan,
								@RequestParam(name="is_stop", required=true) boolean is_stop,
								@RequestParam(name="from_store", required=true) boolean from_store, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// プランの次回更新再開
		return setStopOrRestartSubscription(isPlan, from_store, is_stop ? 0 : 1, relation_id, store_id, user_id, model, req, ses);
	}
	
	/**
	 * 返金
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/refund_user_relation")
	public String refundUserOption(@RequestParam(name="relation_id", required=true) int relation_id,
								@RequestParam(name="store_id", required=true) int store_id,
								@RequestParam(name="user_id", required=true) int user_id,
								@RequestParam(name="is_plan", required=true) boolean isPlan,
								@RequestParam(name="from_store", required=true) boolean from_store, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// 返金
		return setStopOrRestartSubscription(isPlan, from_store, Const.PLAN_UPDATE_TYPE_REFUND, relation_id, store_id, user_id, model, req, ses);
	}
	
	/**
	 * プランの停止/再開
	 * @param stop_type 0:停止 / 1:再開 / 2:返品
	 */
	private String setStopOrRestartSubscription(boolean isPlan,  boolean from_store, int stop_type, int relation_id, int store_id, int user_id,
												Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
	/* チェック */
		
		// エラーチェック(セッションユーザーチェック)
		ChkValidResult chkValidResult = chkValidUpdate(store_id, user_id, from_store, model, req, ses);
		// エラーが存在する場合はエラー遷移
		if(chkValidResult.getErr_forward() != null) return chkValidResult.getErr_forward();
		
		// ユーザー
		User user = chkValidResult.getUser();
		// 契約情報
		User_relation user_relation = isPlan ? user_planService.containByPlanId(store_id, user.getId(), relation_id) :
											   user_optionService.containByPlanId(store_id, user.getId(), relation_id);
		
		// 返品である場合
		if(stop_type == Const.PLAN_UPDATE_TYPE_REFUND) {
			
			// サブスク情報
			Subscription subscription = stripeUtil.getSubscription(user_relation.getStripe_subscription_id());
			// カウント情報
			User_count user_count;
			if(isPlan) user_count = user_plan_countService.contain(user_relation.getUser_id(), relation_id, subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
			else user_count = user_option_countService.contain(user_relation.getUser_id(),relation_id, subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
			
			// 返品不可である場合
			if(!CommonUtil.isRefund(user_relation.getCreated_date(), CommonUtil.getUserCount(user_count))) {
				
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				// エラーメッセージ
				error_messages.add("返品期限(契約後2日以内)を過ぎている、または1回以上利用された契約のため返品することはできません。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
				// ユーザー店舗画面 or プラン選択画面へ遷移
				return !from_store ? disp(store_id, model, req, ses) : storeRequestController.reqNonAppUser(user.getId(), model, req, ses);
			}
		}
		
	/* 更新 */
		
		// 次回更新の停止
		if(stop_type == Const.PLAN_UPDATE_TYPE_STOP) stripeUtil.stopContinueSubscription(user_relation.getStripe_subscription_id());
		// 次回更新の開始
		else if(stop_type == Const.PLAN_UPDATE_TYPE_RESTART) stripeUtil.restartSubscription(user_relation.getStripe_subscription_id());
		// 返品
		else stripeUtil.refundSubscription(user_relation, isPlan);

		// 店舗画面表示
		return updatedForward(from_store ,store_id, user.getId(), stop_type);
	}
	
/* 共通 */
	
	/**
	 * データ更新後の遷移
	 * @param stop_type 0:停止 / 1:再開 / 2:返品
	 */
	public static String updatedForward(boolean from_store, int store_id, int user_id, int stop_type) {
		
		// 遷移先
		String forward;
		// 店舗から遷移ではない場合
		if(!from_store) {
			// ユーザー店舗画面表示
			forward = Const.REDIRECT_HEADER + "/search_store_checkouted?store_id=" + store_id + "&stop_type=" + stop_type;
			
		// 店舗からの遷移である嗚位
		}else {
			// 通常更新時である場合、店舗手続き画面表示
			if(stop_type == -1) forward = Const.REDIRECT_HEADER + "/store_request?user_id=" + user_id;
			// プラン停止・再開・返金時である場合、ユーザー店舗画面表示
			else forward = Const.REDIRECT_HEADER + "/disp_search_store_nonAppUser_checkouted?user_id=" + user_id + "&stop_type=" + stop_type;
		}
		
		// 画面表示
		return forward;
	}
	
	/**
	 * データ更新時チェック結果
	 */
	private class ChkValidResult{
		private User user;
		private String err_forward;

		public ChkValidResult(User user) {
			this.user = user;
		}
		public ChkValidResult(String err_forward) {
			this.err_forward = err_forward;
		}
		
		public User getUser() {
			return user;
		}
		public String getErr_forward() {
			return err_forward;
		}
	}
	
	/**
	 * データ更新時エラーチェック
	 */
	private ChkValidResult chkValidUpdate(int fm_store_id, int fm_user_id, boolean from_store, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗ID
		int sesStore_id = (int) ses.getAttribute(Const.SESSION_STORE_ID);
		
		// セッション店舗IDと画面IDが異なる場合
		if(sesStore_id != fm_store_id) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add("別のウインドウで店舗の観覧または保存が行われたため、保存することができませんでした。再度対象の店舗へアクセスいただき保存を実行してください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// ユーザー店舗画面遷移
			return new ChkValidResult(disp(sesStore_id, model, req, ses));
			
		// 正常である場合
		}else {
			
			// セッションユーザー情報
			User user;
			// 店舗手続き画面からの遷移でない場合
			if(!from_store) {
				// セッションユーザー設定
				user = CommonUtil.getSessionUser(ses);
				
			// 店舗手続き画面からの遷移である場合
			}else {
				
				// セッションユーザーＩＤが存在しない場合
				if(ses.getAttribute(NON_APP_USER_ID) == null) {
					
					// エラーメッセージ
					List<String> error_messages = new ArrayList<String>();
					// エラーメッセージ
					error_messages.add("プランを登録しようとしているユーザー情報を確認することができませんでした。最初からやり直してください。");
					// リクエスト
					req.setAttribute(Const.MSG_ERROR, error_messages);
					// 読取画面遷移
					return new ChkValidResult(new StoreReadController().disp(model, req, ses));
					
				// セッションユーザーＩＤが存在する場合
				}else {
					
					// ユーザーＩＤ
					int user_id = (int) ses.getAttribute(NON_APP_USER_ID);
					// ユーザー情報設定
					user = userService.find(user_id);
					// セッション削除
					ses.removeAttribute(NON_APP_USER_ID);
				}
			}
			
			// 画面のユーｻﾞｰIDが異なる場合
			if(user.getId() != fm_user_id) {
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				// エラーメッセージ
				error_messages.add("別のウインドウで別ユーザーの保存が行われた可能性があるため、保存できませんでした。再度操作をおこなってください。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
				// ユーザー店舗画面 or 読取画面遷移
				return new ChkValidResult(!from_store ? disp(sesStore_id, model, req, ses) : new StoreReadController().disp(model, req, ses));
			}
			
			// エラーなし
			return new ChkValidResult(user);
		}
	}
	
/* 未使用 */
	
	/**
	 * 未使用のメソッド
	 * spring.datasource.auto-commit=false
	 * https://stackoverflow.com/questions/55343616/how-to-disable-autocommit-spring-boot
	 * transaction
	 * https://qiita.com/NagaokaKenichi/items/a279857cc2d22a35d0dd
	 */
	//@PostMapping("/save_user_plan")
//	public String update(@ModelAttribute User_storeForm user_storeForm, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
//		
//		// セッション店舗ID
//		int sesStore_id = (int) ses.getAttribute(Const.SESSION_STORE_ID);
//		
//		// セッション店舗IDと画面IDが異なる場合
//		if(sesStore_id != user_storeForm.getStore_id()) {
//			
//			// 店舗ID設定
//			user_storeForm.setStore_id(sesStore_id);
//			
//			// エラーメッセージ
//			//setDiffStoreIdErr(req);
//			
//		// 正常である場合
//		}else {
//			
//			// セッションユーザー情報
//			User sesUser = CommonUtil.getSessionUser(ses);
//			
//			// フォーム設定
//			user_storeForm.getUser_plan().setUser_id(sesUser.getId());
//			user_storeForm.getUser_plan().setStore_id(sesStore_id);
//			
//			//トランザクション
//			TransactionStatus transStat = transactionUtil.getTrans();
//			
//			try {
//				
//				// プラン削除
//				user_planService.deleteAll(sesStore_id, sesUser.getId());
//				// オプション削除
//				user_optionService.deleteAll(sesStore_id, sesUser.getId());
//				
//				// プランが選択されている場合
//				if(0 < user_storeForm.getUser_plan().getPlan_id()) {
//					
//					// プラン保存
//					user_planService.saveOne(user_storeForm.getUser_plan());
//					// オプション保存
//					user_optionService.saveAll(user_storeForm.getUser_options(sesStore_id,sesUser.getId()));
//				}
//				
//				// メッセージ
//				List<String> info_messages = new ArrayList<String>();
//				info_messages.add("保存しました。");
//				
//				// リクエスト
//				req.setAttribute(Const.MSG_INFO, info_messages);
//				
//				// コミット
//				transactionUtil.commit(transStat);
//				
//			}catch(Exception e) {
//				// ロールバック
//				transactionUtil.rollback(transStat);
//				throw e;
//			}
//		}
//		
//		// 遷移
//		return Const.PAGE_USER_STORE;
//	}
}
