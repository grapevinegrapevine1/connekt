//package com.controller;
//
//import java.io.IOException;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.base.User_relation;
//import com.properties.Const;
//import com.stripe.Stripe;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Invoice;
//import com.stripe.model.InvoiceCollection;
//import com.stripe.model.checkout.Session;
//import com.stripe.param.SubscriptionSearchParams;
//import com.stripe.param.checkout.SessionCreateParams;
//import com.util.CommonUtil;
//import com.util.StripeUtil;
//import com.stripe.model.Plan;
//import com.stripe.model.Subscription;
//import com.stripe.model.SubscriptionSearchResult;
///**
// * Srtipe リクエスト
// * https://stripe.com/docs/api/plans/create
// */
//@Controller
//public class RequestController {
//	
//	@Autowired StripeUtil stripeUtil;
//	
//	@GetMapping("/new_user_create")
//	public String new_user_create(HttpServletRequest req, HttpServletResponse res) throws StripeException, IOException {
//
//		// 顧客作成
//		String customerId = stripeUtil.createCustomer("request_test_1@test.com","テストユーザー");
//		
//		return stripeUtil.createCard_user(req, customerId, 1);
//	}
//
//	@GetMapping("/set_subscription")
//	public String setSubscription(HttpServletRequest req, HttpServletResponse res) throws Exception {
//		
//		stripeUtil.setSubscription(0,75,"cus_Lr84fy8thfXL20", null, null);
//		
//		return "request";
//	}
//	
//
//	public String disp(HttpServletRequest req, HttpServletResponse res) throws StripeException {
//		
//		// チェックアウトセッション
//		String sessionId = req.getParameter("session_id");
//		// checkoutメソッドのsuccess url実行時である場合
//		if(sessionId != null) {
//			Session session = Session.retrieve(sessionId);
//			// サブスク
//			String subscriptionId = session.getSubscription();
//			Subscription subscription = Subscription.retrieve(subscriptionId);
//			// 商品プラン
//			String planId = req.getParameter("plan_id");
//			// metadata
//			Map<String, Object> metadata = new HashMap<>();
//			metadata.put("plan_id", planId);
//			metadata.put("session_id", sessionId);
//			metadata.put("subscription_id", subscriptionId);
//			//metadata.put("store_id", store_id); Planテーブルに紐づいているため不要
//			//metadata.put("user_id", sessionUserId); Planテーブルに紐づいているため不要
//			Map<String, Object> params = new HashMap<>();
//			params.put("metadata", metadata);
//			// サブスク更新
//			subscription.update(params);
//		}
//		return "request";
//	}
//	
//	/**
//	 * https://stripe.com/docs/billing/quickstart
//	 * @param req
//	 * @param res
//	 * @return
//	 * @throws StripeException
//	 * @throws IOException
//	 */
//	@RequestMapping("/checkout")
//	public String checkout(HttpServletRequest req, HttpServletResponse res) throws StripeException, IOException {
//
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//		
//		String planId = "plan_LV7MySEbmebQpq";
//		Plan plan = Plan.retrieve(planId);
//		
//		SessionCreateParams params = SessionCreateParams.builder()
//				.addLineItem(
//					SessionCreateParams.LineItem
//					.builder()
//					.setPrice(plan.getId())
//					.setQuantity(1L)
//					.build())
//						.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
//						.setSuccessUrl("http://localhost:8081/?session_id={CHECKOUT_SESSION_ID}&plan_id="+planId)
//						.setCancelUrl("http://localhost:8081/")
//						.build();
//		
//		Session session = Session.create(params);
//		
//		res.sendRedirect(session.getUrl());
//		return "request";
//	}
//	
//	@RequestMapping(path = "/delete", method = { RequestMethod.POST })
//	public String deletePlan() throws StripeException {
//		
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//		
//		Plan plan = Plan.retrieve("plan_LV7MySEbmebQpq");
//		
//		//Plan deletedPlan = plan.delete();
//		
//		SubscriptionSearchParams params =
//		  SubscriptionSearchParams
//			.builder()
//			.setQuery("metadata['plan_id']:'plan_LV7MySEbmebQpq'")
//			.build();
//
//		SubscriptionSearchResult result = Subscription.search(params);
//		for(Subscription subscription : result.getData()) {
//			
//		}
//		
//		/*
//		// プランに紐づく、ユーザープラン情報リスト
//		List<User_relation> user_relations = isPlan ? convertUserRelations(user_planService.containByStoreId(store.getId())) : 
//													  convertUserRelations(user_optionService.containByStoreId(store.getId()));
//		// プランに紐づく、サブスクアイテムリスト
//		List<Subscription> subscriptions = new ArrayList<Subscription>();
//		
//		// ユーザープラン情報リスト数分
//		for(User_relation user_relation : user_relations) {
//			// サブスクアイテム追加
//			Subscription subscription = stripeUtil.getSubscription(user_relation.getStripe_subscription_id());
//			subscriptions.add(subscription);
//		}
//		*/
//		return "request";
//	}
//	// ユーザープラン情報リストの変換
//	private <T> List<User_relation> convertUserRelations(List<T> userRelationTarget){
//		return userRelationTarget.stream().map(b->(User_relation)b).collect(Collectors.toList());
//	}
//	@RequestMapping(path = "/cancel", method = { RequestMethod.POST })
//	public String cancelSubscription() throws StripeException {
//		
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//		
//		Subscription subscription = Subscription .retrieve("sub_1Ko6WsBbtACSed8BXpgNra4p");
//		
//		Map<String, Object> params = new HashMap<>();
//		params.put("cancel_at", (new Date().getTime() + 86400000) / 1000);
//		params.put("proration_behavior", "create_prorations");
//		//params.put("cancel_at_period_end", true);
//
//		subscription.update(params);
//		
//		return "request";
//	}
//	@RequestMapping(path = "/create_", method = { RequestMethod.POST })
//	public String create() throws StripeException {
//		
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//		
//		Map<String, Object> params = new HashMap<>();
//		params.put("amount", 100);
//		params.put("currency", "jpy");
//		params.put("interval", "day");
//		params.put("interval_count", 1);
//		params.put("product", "prod_LTtfV7KreNyWLe");
//		Plan plan = Plan.create(params);
//		
//		return "request";
//	}
//	
//	/**
//	 * https://stripe.com/docs/api/invoices/list
//	 * @return
//	 * @throws StripeException 
//	 * @throws ParseException 
//	 */
//	@RequestMapping(path = "/select", method = { RequestMethod.GET })
//	public String select() throws StripeException, ParseException {
//
//		String startDate = "2021/07/01";
//		String endDate = "2022/07/30";
//
//		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
//		Date date = sdFormat.parse(startDate);
//		Date date_end = sdFormat.parse(endDate);
//		
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//		
//		Map<String, Integer> createdparams = new HashMap<>();
////		createdparams.put("gte", CommonUtil.convertStipeTimestamp(date));
////		createdparams.put("lte", CommonUtil.convertStipeTimestamp(date_end));
//
//		Map<String, Object> params = new HashMap<>();
//		//params.put("created", createdparams);
//		
//		InvoiceCollection paymentIntents = Invoice.list(params);
//		
//		for(Invoice pay:paymentIntents.getData()) {
//			
//			// プランを提供する店舗のID
//			Subscription subscription = Subscription.retrieve(pay.getSubscription());
//			String storeId = subscription.getMetadata().get("store_id");
//			// 店舗の銀行口座や、名義人名など取得
//			Date paidAt = new Date(pay.getCreated() * 1000);
//			// 個別の請求情報(氏名、プラン名、金額、baken手数料)を請求書用に取得
//			String email = pay.getCustomerEmail();
//			String name = pay.getCustomerName();
//			// 請求料金
//			String stripe_plan_id = subscription.getItems().getData().get(0).getPlan().getId();
//			long amount = Plan.retrieve(stripe_plan_id).getAmount();
//			if(1000 < amount) {
//				int a = 1;
//			}
//		}
//		
//		return "request";
//	}
//}
