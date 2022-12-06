package com.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.model.User;
import com.properties.Const;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SubscriptionCollection;
import com.util.CommonUtil;
import com.util.StripeUtil;

@Controller
public class UserPayErrorController {

	@Autowired private StripeUtil stripeUtil;
	
	/**
	 * 画面表示
	 */
	@RequestMapping("/user_pay_error")
	public String disp_error(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return disp(true, model, req, ses);
	}
	@RequestMapping("/user_pay_setting")
	public String disp_setting(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return disp(false, model, req, ses);
	}
	private String disp(boolean isErr, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);

		// サブスク一覧
		SubscriptionCollection subscriptions = stripeUtil.getSubscriptionCollection(user);
		// 支払情報
		PaymentMethodCollection paymentMethods = stripeUtil.getPaymentMethods(user.getStripe_customer_id());
		
		// エラーメッセージ一覧
		List<String> err_msg = stripeUtil.getErrPaymentIntents(subscriptions);
		// 支払い情報が存在しない場合はエラーメッセージ追加
		if(paymentMethods.getData().size() == 0) err_msg.add(0, "決済情報が設定せれていません。");
		
		// リクエスト
		req.setAttribute("is_err", isErr);
		
		// エラーが存在する場合
		if(0 < err_msg.size()) {
			
			// 決済エラー可否
			req.setAttribute("msg_list", err_msg);
			//遷移
			return Const.PAGE_USER_PAY_ERROR;
			
		// エラーが存在しない場合
		}else {
			// 画面遷移
			return isErr ? Const.REDIRECT_HEADER + Const.PAGE_USER_TOP : Const.PAGE_USER_PAY_ERROR;
		}
	}
	
	/**
	 * クレジットカード再登録
	 */
	@RequestMapping("/checkout_pay_error")
	public String checkout(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);
		// カード再登録
		return stripeUtil.resetCard(user, req);
	}
}
