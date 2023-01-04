package com.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.model.News_user;
import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.service.News_userService;
import com.service.StoreService;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SubscriptionCollection;
import com.util.CommonUtil;
import com.util.StripeUtil;

@Controller
public class UserTopController {

	@Autowired private StripeUtil stripeUtil;
	@Autowired private StoreService storeService;
	@Autowired private News_userService news_userService;
	
	@RequestMapping("/user_top")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// リクエスト設定
		CommonUtil.setReqSessionUser(req, ses);
		
		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);
		// 支払情報
		PaymentMethodCollection paymentMethods = stripeUtil.getPaymentMethods(user.getStripe_customer_id());
		
		// サブスク一覧
		SubscriptionCollection subscriptions = stripeUtil.getSubscriptionCollection(user);
		
		// カード情報存在可否
		boolean existsCard;
		// クレジットカード情報が存在する場合
		if(existsCard = 0 < paymentMethods.getData().size()) {
			// 決済エラーのサブスクを再決済
			stripeUtil.reCheckoutSubscription(subscriptions,
											  paymentMethods.getData().get(0));
		}
		
		// 決済エラー可否(サブスク決済エラーが存在する or クレジットカード登録が存在しない場合)
		boolean errPaid = stripeUtil.chkErrPaymentIntent(subscriptions);
		req.setAttribute("is_pay_err", errPaid || !existsCard);
		
		// ニュース一覧
		List<News_user> news_users = news_userService.findByUserId(user.getId());
		req.setAttribute("news_users", news_users);
		
		// ログイン時セッション店舗ID設定
		setReqLoginStoreId(req, ses);
		
		//遷移
		return Const.PAGE_USER_TOP;
	}
	
	/**
	 * ログイン時セッション店舗ID設定
	 */
	private void setReqLoginStoreId(HttpServletRequest req, HttpSession ses) {
		
		// ログイン時セッション店舗ID
		Object login_store_id = ses.getAttribute(Const.LOGIN_STORE_ID);
		// ログイン時セッション店舗IDキー削除
		ses.removeAttribute(Const.LOGIN_STORE_ID);
		// リクエスト設定
		if(login_store_id != null && !login_store_id.toString().equals("")) {
			// 店舗
			Store loginStore = storeService.find(Integer.parseInt(login_store_id.toString()));
			// リクエスト設定
			req.setAttribute(Const.LOGIN_STORE_ID, login_store_id + "_" + loginStore.getStore_name());
		}
	}
}
