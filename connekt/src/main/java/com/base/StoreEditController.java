package com.base;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.model.Option;
import com.model.Plan;
import com.model.Store;
import com.model.User_option;
import com.model.User_plan;
import com.properties.Const;
import com.service.OptionService;
import com.service.PlanService;
import com.service.User_optionService;
import com.service.User_planService;
import com.stripe.exception.StripeException;
import com.util.CommonUtil;
import com.util.MailUtil;
import com.util.NewsUtil;
import com.util.StripeUtil;

public class StoreEditController {
	
	private PlanService planService;
	private OptionService optionService;
	private User_planService user_planService;
	private User_optionService user_optionService;
	private StripeUtil stripeUtil;
	private MailUtil mailUtil;
	private NewsUtil newsUtil;
	
	// リクエスト、セッションプランID
	private String REQ_PLAN_ID;
	private String REQ_CLASS;

	public StoreEditController(String req_class, StripeUtil stripeUtil, MailUtil mailUtil, NewsUtil newsUtil,PlanService planService, User_planService user_planService) {
		constructorSetting(req_class, stripeUtil, mailUtil, newsUtil);
		this.planService = planService;
		this.user_planService = user_planService;
	}
	public StoreEditController(String req_class, StripeUtil stripeUtil, MailUtil mailUtil, NewsUtil newsUtil, OptionService optionService, User_optionService user_optionService) {
		constructorSetting(req_class, stripeUtil, mailUtil, newsUtil);
		this.optionService = optionService;
		this.user_optionService = user_optionService;
	}
	
	private void constructorSetting(String req_class, StripeUtil stripeUtil, MailUtil mailUtil, NewsUtil newsUtil) {
		this.REQ_PLAN_ID = req_class + "_id";
		this.REQ_CLASS = req_class;
		this.stripeUtil = stripeUtil;
		this.mailUtil = mailUtil;
		this.newsUtil = newsUtil;
	}
	
	// 表示
	public String disp(Model model, HttpServletRequest req, HttpSession ses, boolean isPlan) throws Exception {
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		// プラン
		BaseModel baseModel;
		
		// プランIDが指定されている場合
		if(req.getParameter(REQ_PLAN_ID) != null) {
			
			// プランID
			int baseModel_id = Integer.parseInt(req.getParameter(REQ_PLAN_ID));
			// プラン情報取得
			baseModel = isPlan ? planService.find(baseModel_id) : optionService.find(baseModel_id);
			// プラン追加時フラグ
			baseModel.setIs_create(false);
			
		// プランIDが指定されていない場合
		}else {
			
			// プラン情報生成
			baseModel = new BaseModel();
			// 店舗ID
			baseModel.setStore_id(store.getId());
			// インターバル初期値
			baseModel.setPlan_interval(1);
			// 利用上限初期値
			baseModel.setCount(2);
			// 削除可否
			baseModel.setIs_delete(0);
			// プラン追加時フラグ
			baseModel.setIs_create(true);
		}
		
		// セッション設定
		ses.setAttribute(REQ_PLAN_ID, baseModel.getId());
		// モデル
		model.addAttribute(REQ_CLASS, baseModel);
		
		// 遷移
		return fowardEditPage(isPlan);
	}
	
	// 更新
	public String update(BaseModel baseModel, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses, boolean isPlan) throws Exception {

		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// エラーメッセージ
		List<String> error_messages = getBaseModelValidMsg(req, ses, baseModel);
		
		// エラーメッセージが存在する場合
		if(0 < error_messages.size()) {
			
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
		// 正常である場合
		}else {
			
			// 既存プラン
			BaseModel _baseModel = isPlan ? planService.find(baseModel.getId()) : optionService.find(baseModel.getId());
			
			// プランが存在しない場合
			if(_baseModel == null) {
				
				// Stripe商品作成
				String stripePlan_id = stripeUtil.createPlan(baseModel.getPrice(), baseModel.getPlan_interval(), baseModel.getStore_id());
				// 商品ID設定
				baseModel.setStripe_plan_id(stripePlan_id);
				
			// プランが存在する場合
			}else {
				
				// 更新不可項目を上書き
				baseModel.setPrice(_baseModel.getPrice());
				baseModel.setCount(_baseModel.getCount());
				baseModel.setPlan_interval(_baseModel.getPlan_interval());
				baseModel.setStripe_plan_id(_baseModel.getStripe_plan_id());
				
				/* 商品更新
				String stripeBaseModel_id = stripeUtil.updateBaseModel(_baseModel.getStripe_plan_id(), baseModel.getPrice(), baseModel.getPlan_interval());
				// 設定
				baseModel.setStripe_plan_id(stripeBaseModel_id);*/
			}
			
			// 保存
			if(isPlan) planService.save((Plan)baseModel);
			else optionService.save((Option)baseModel);
			
			// メッセージ
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("保存しました。");
			
			// リクエスト
			req.setAttribute(Const.MSG_INFO, info_messages);
			// セッション設定
			ses.setAttribute(REQ_PLAN_ID, baseModel.getId());
		}
		
		// 遷移
		return fowardEditPage(isPlan);
	}
	
	/**
	 * プラン削除
	 */
	public String delete(BaseModel baseModel, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses, boolean isPlan) throws Exception {

		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// エラーメッセージ
		List<String> error_messages = getBaseModelValidMsg(req, ses, baseModel);
		
		// エラーメッセージが存在する場合
		if(0 < error_messages.size()) {
			
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 遷移
			return fowardEditPage(isPlan);
			
		// 正常である場合
		}else {

			// セッション店舗情報
			Store store = CommonUtil.getSessionStore(ses);
			// プラン
			BaseModel _baseModel = isPlan ? planService.find(baseModel.getId()) : optionService.find(baseModel.getId());
			
			// 商品削除(Planを削除しても次回更新までのSubscriptionは残るため、ユーザーは次回更新まで利用できる流れとなる)
			new StripeUtil().deletePlan(_baseModel.getStripe_plan_id());
			
			
			// プランである場合
			if(isPlan) {
				
				// 店舗のプラン契約一覧
				List<User_plan> user_plans = user_planService.containByStoreId(store.getId());
				// 全契約を次の更新日で停止
				for(User_plan user_plan : user_plans) {
					// プラン削除
					deletePlanAndNews(store, isPlan, user_plan, user_plan.getPlan().getName());
				}
				
				// プラン論理削除
				planService.delete(baseModel.getId());
				
			// オプションである場合
			} else {
				
				// 店舗のオプション契約一覧
				List<User_option> user_options = user_optionService.containByStoreId(store.getId());
				// 全契約を次の更新日で停止
				for(User_option user_option : user_options) {
					// オプション削除
					deletePlanAndNews(store, isPlan, user_option, user_option.getOption().getName());
				}
				// オプション論理削除
				optionService.delete(baseModel.getId());
			}
			
			// メッセージ
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("プランを削除しました。");
			
			// リクエスト
			req.setAttribute(Const.MSG_INFO, info_messages);
			// セッション設定
			ses.setAttribute(REQ_PLAN_ID, baseModel.getId());
			
			// 遷移
			return Const.REDIRECT_HEADER + Const.PAGE_STORE_PlAN;
		}
	}
	
	/**
	 * プラン削除・ニュース通知
	 */
	private void deletePlanAndNews(Store store, boolean isPlan, User_relation user_relation, String planNm) throws StripeException {

		// サブスク停止・終了日取得
		String endDate = stripeUtil.stopContinueSubscription(user_relation.getStripe_subscription_id());
		
		// ニュース登録
		int news_id = newsUtil.saveNew_removePlan(isPlan, store.getId(), store.getName(), planNm);
		// ニュースユーザー登録
		newsUtil.saveNews_user(user_relation.getUser_id(), news_id);
		
		// メール通知
		Runnable runnable = () -> {
			mailUtil.sendNews_deletePlan(user_relation.getUser().getEmail(),store.getName(), planNm, endDate, isPlan);
		};
		runnable.run();
	}
	
	/**
	 * 遷移先
	 */
	private static String fowardEditPage(boolean isPlan) {
		return isPlan ? Const.PAGE_STORE_PlAN_EDIT : Const.PAGE_STORE_OPTION_EDIT;
	}
	
	/**
	 *  更新対象の正常確認
	 */
	private List<String> getBaseModelValidMsg(HttpServletRequest req, HttpSession ses, BaseModel baseModel) {
		
		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		
		// 店舗
		Store store = CommonUtil.getSessionStore(ses);
		
		// 店舗IDが異なる場合
		if(store.getId() != baseModel.getStore_id()) {
			
			// エラーメッセージ
			error_messages.add("店舗IDが不正に変更されているため更新することができません。");
			
		// セッションのプランIDと異なる場合
		}else if(ses.getAttribute(REQ_PLAN_ID) == null || baseModel.getId() != (int)ses.getAttribute(REQ_PLAN_ID) ) {
			
			// エラーメッセージ
			error_messages.add("別のウインドウで新たにプラン編集が行われたため、この画面でプラン編集を行うことはできません。再度プラン一覧画面からプラン編集を行ってください。");
		}
		
		// 返却
		return error_messages;
	}
}
