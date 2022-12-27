package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.form.User_listForm;
import com.model.User;
import com.model.User_option;
import com.model.User_plan;
import com.properties.Const;
import com.service.StoreService;
import com.service.User_optionService;
import com.service.User_planService;
import com.util.CommonUtil;
import com.util.StripeUtil;

@Controller
public class UserListController {

	@Autowired StoreService storeService;
	@Autowired User_planService user_planService;
	@Autowired User_optionService user_optionService;
	@Autowired StripeUtil stripeUtil;
	
	/**
	 * 画面表示前のチェック
	 */
	@Transactional
	@RequestMapping("/user_list")
	public String checkDisp(@RequestParam(name="is_error", defaultValue="false") boolean is_error , Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// プランチェック
		stripeUtil.callChkUpdatePlan(ses);
		// 遷移
		return Const.REDIRECT_HEADER + "/disp_user_list?is_error=" + is_error;
	}
	
	/**
	 * 画面表示
	 */
	@RequestMapping("/disp_user_list")
	public String disp(@RequestParam(name="is_error", defaultValue="false") boolean is_error , Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User sesUser = CommonUtil.getSessionUser(ses);
		
		// 店舗一覧フォームリスト
		Map<Integer, User_listForm> user_listFormMap = new HashMap<Integer, User_listForm>();
		
		// 登録プラン一覧
		List<User_plan> user_plans = user_planService.find_list(sesUser.getId());
		// 登録プラン数分繰り返す
		for(User_plan user_plan : user_plans) {
			
			// ユーザーフォーム
			User_listForm user_listForm = new User_listForm();
			user_listForm.setStore_id(user_plan.getStore_id());
			user_listForm.setStore_name(user_plan.getPlan().getStore().getStore_name());
			user_listForm.setUser_plan(user_plan);
			// マップ追加
			user_listFormMap.put(user_plan.getStore_id(), user_listForm);
		}
		
		// オプションリスト
		List<User_option> user_options = user_optionService.find_list(sesUser.getId());
		// 登録オプション数分繰り返す
		for(User_option user_option : user_options) {
			
			// マップに存在しない場合
			if(!user_listFormMap.containsKey(user_option.getStore_id())){
				
				// 店舗一覧フォーム
				User_listForm user_listForm = new User_listForm();
				user_listForm.setStore_id(user_option.getStore_id());
				user_listForm.setStore_name(user_option.getOption().getStore().getStore_name());
				user_listForm.getUser_options().add(user_option);
				// マップ追加
				user_listFormMap.put(user_option.getStore_id(), user_listForm);
				
			// マップに存在する場合
			}else {
				
				// 店舗一覧フォーム
				User_listForm user_listForm = user_listFormMap.get(user_option.getStore_id());
				// オプション追加
				user_listForm.getUser_options().add(user_option);
			}
		}
		
		// 店舗一覧フォームリスト
		List<User_listForm> user_listForms = new ArrayList<User_listForm>(user_listFormMap.values());
		// リスト数分繰り返す
		for(User_listForm user_listForm : user_listForms) {
			// 契約詳細テキスト
			user_listForm.setContract_text(stripeUtil.getPlanText(user_listForm.getUser_plan(), user_listForm.getUser_options()));
		}
		
		// モデル
		model.addAttribute("user_listForms", user_listForms);
		
		// リクエスト
		if(is_error) {
			
			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			// エラーメッセージ
			error_messages.add("検索された店舗は存在しません。店舗IDをご確認ください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
		}
			
		// 遷移
		return Const.PAGE_USER_LIST;
	}
}
