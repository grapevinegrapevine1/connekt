package com.util;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.base.BaseModel;
import com.base.User_count;
import com.base.User_relation;
import com.enm.ResultWithinDate;
import com.model.Option;
import com.model.User;
import com.model.User_option;
import com.model.User_option_count;
import com.model.User_plan;
import com.model.User_plan_count;
import com.properties.Const;
import com.service.OptionService;
import com.service.PlanService;
import com.service.StoreService;
import com.service.User_optionService;
import com.service.User_option_countService;
import com.service.User_planService;
import com.service.User_plan_countService;
import com.stripe.Stripe;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Plan;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.InvoiceSearchParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.RefundListParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionCreateParams.PaymentBehavior;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionRetrieveParams;
import com.stripe.param.SubscriptionSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Builder;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.PaymentMethodType;

@Service
public class StripeUtil {

	@Autowired private PlanService planService;
	@Autowired private StoreService storeService;
	@Autowired private OptionService optionService;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private User_plan_countService user_plan_countService;
	@Autowired private User_option_countService user_option_countService;
	
	private static final String INTERVAL_UNIT = Const.TEST_INTERVAL;
	private static final String CURRENCY = "jpy";
	private static final String STATUS_CANCELED = "canceled";
	
	/**
	 * 顧客作成
	 */
	public String createCustomer(String email, String name) throws StripeException {
		
		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// メールアドレスで顧客情報取得
		Map<String, Object> cus_options = new HashMap<>();
		cus_options.put("email", email);
		List<Customer> customers = Customer.list(cus_options).getData();
		// Userテーブルに存在せず、Stripeにだけ存在するメールアドレスである場合は削除(原因はCustomer登録後のUserテーブル登録前にネットワークエラーとなったパターン)
		for(Customer customer: customers) customer.delete();
		
		// パラメータ
		Map<String, Object> params = new HashMap<>();
		params.put("email", email);
		params.put("name", name);
		// 顧客作成
		Customer customer = Customer.create(params);
		// 返却
		return customer.getId();
	}
	
	/**
	 * Stripeリクエストエラー内容が「決済種別に現金決済が存在するため、ユーザー情報を削除することができません」であるかをチェック
	 */
	public boolean isDeleteCusWithCach(InvalidRequestException e) {
		return e.getMessage().matches(".*You cannot delete a customer with a cash balance.*");
	}
	
	/**
	 * 顧客ID取得
	 */
	public String getCustomer(String stripe_customer_id) throws StripeException {
		
		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		// 顧客情報
		Customer customer = Customer.retrieve(stripe_customer_id);
		return customer.getId();
	}

	
	/**
	 * ユーザー作成時カード登録
	 */
	public String createCard_user(HttpServletRequest req, String stripe_customer_id, int user_id) throws MalformedURLException, StripeException{
		
		// URL
		String sucsessUrl = "/cert_start?user_id=" + user_id, cancelUrl = "/user_create";
		// カード登録チェックアウト
		return createCardCheckout(req, stripe_customer_id, sucsessUrl, cancelUrl);
	}

	/**
	 * ユーザー作成時カード登録
	 */
	private String resetCard_user(HttpServletRequest req, String stripe_customer_id, int user_id) throws MalformedURLException, StripeException{
		
		// URL
		String url = "/user_top";
		// カード登録チェックアウト
		return createCardCheckout(req, stripe_customer_id, url, url);
	}
	
	/**
	 * アプリ無しユーザー作成時カード登録
	 */
	public String createCard_nonAppUser(HttpServletRequest req, String stripe_customer_id, String url) throws MalformedURLException, StripeException{
		
		// URL
		String sucsessUrl = url, cancelUrl = "/store_read";
		// カード登録チェックアウト
		return createCardCheckout(req, stripe_customer_id, sucsessUrl, cancelUrl);
	}
	/**
	 * カード登録チェックアウト
	 */
	private String createCardCheckout(HttpServletRequest req, String stripe_customer_id, String successUrl, String cancelUrl) throws StripeException, MalformedURLException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// 決済種別
		List<PaymentMethodType> paymentMthodType = new ArrayList<PaymentMethodType>();
		paymentMthodType.add(PaymentMethodType.CARD);

		// URL
		String url = CommonUtil.getUrlHead(req);
		// ビルダー
		Builder stBuilder = SessionCreateParams.builder();
		// チェックアウト設定
		SessionCreateParams sesParams = stBuilder
				.setMode(Mode.SETUP)
				.setCustomer(stripe_customer_id)
				.setSuccessUrl(url + successUrl)
				.setCancelUrl(url + cancelUrl)
				.addAllPaymentMethodType(paymentMthodType).build();
		
		// チェックアウトセッション作成
		Session stripeSes = Session.create(sesParams);
		
		// チェックアウトセッションURL
		return Const.REDIRECT_HEADER + stripeSes.getUrl();
	}
	
/* チェックアウト */
	
	/**
	 * サブスクのサーバーチェックアウト
	 * https://stripe.com/docs/billing/subscriptions/build-subscriptions?ui=elements
	 * @throws Exception 
	 */
	public void setSubscription(int store_id, int user_id, String stripe_customer_id, com.model.Plan planModel, List<Option> options, PaymentMethodCollection paymentMethods) throws Exception {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// プラン情報が存在する場合
		if(planModel != null) {
			
			// サブスク登録パラメータ
			SubscriptionCreateParams subCreateParams = getSubscriptionParam(stripe_customer_id, paymentMethods, planModel.getStripe_plan_id(), store_id, planModel.getId(), user_id);
			// サブスク設定
			Subscription subscription = Subscription.create(subCreateParams);
			// 利用数情報更新
			updatePlanCount(user_id, planModel.getStripe_plan_id(), subscription);
		}
		
		// オプション情報が存在する場合
		if(options != null) {
			
			// オプション情報数分
			for(Option option : options) {
				
				// サブスク登録パラメータ
				SubscriptionCreateParams subCreateParams = getSubscriptionParam(stripe_customer_id, paymentMethods, option.getStripe_plan_id(), store_id, option.getId(), user_id);
				// サブスク設定
				Subscription subscription = Subscription.create(subCreateParams);
				// 決済結果
				//subscription.getLatestInvoiceObject().getPaid()
				// 利用数情報更新
				updateOptionCount(user_id, option.getStripe_plan_id(), subscription);
			}
		}
	}
	
//	private void sendReceptUrl(Subscription subscription) throws StripeException {
//
//		// インボイス
//		Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
//		// チャージ
//		Charge charge = Charge.retrieve(invoice.getCharge());
//		// 領収書ページ
//		String pdUrl = charge.getReceiptUrl();
//		
//	}
	
	/**
	 * 顧客に紐づく支払い情報リスト取得
	 */
	public Iterable<Invoice> searchInvoicesByStore(int store_id, java.util.Date start_date, java.util.Date end_date) throws StripeException {
		return searchInvoice(store_id, -1, start_date, end_date);
	}
	public Iterable<Invoice> searchInvoiceByUser(int user_id, java.util.Date start_date, java.util.Date end_date) throws StripeException {
		return searchInvoice(-1, user_id, start_date, end_date);
	}
	private Iterable<Invoice> searchInvoice(int store_id, int user_id, java.util.Date start_date, java.util.Date end_date) throws StripeException {
		
		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
	/* Chargeから取得 */
		
//		// 対象期間
//		com.stripe.param.ChargeListParams.Created created = com.stripe.param.ChargeListParams.Created.builder()
//					.setGte(CommonUtil.getUnixToTime(start_date))
//					.setLte(CommonUtil.getUnixToTime(end_date))
//					.build();
//		// パラメータ設定
//		com.stripe.param.ChargeListParams.Builder builder = ChargeListParams.builder();
//		builder.setCreated(created)
//				.setLimit((long) Const.MAX_SALES_COUNT)
//				.addExpand("data.balance_transaction")
//				.addExpand("data.payment_intent")
//				.addExpand("data.invoice.subscription");
//		if(!CommonUtil.isEmpty(customer_id)) builder.setCustomer(customer_id);
//		ChargeListParams params = builder.build();
//		
//		// 支払い情報リスト取得
//		Iterable<Charge> charges = Charge.list(params).autoPagingIterable();
		
	/* Subscription取得 */
		
		// 店舗/ユーザーに紐づくSubscriptionを全て取得
		com.stripe.param.SubscriptionSearchParams.Builder builder= SubscriptionSearchParams.builder();
//		builder.setQuery("metadata['store_id']:'6'");
		if(0 < store_id) builder.setQuery("metadata['store_id']:'"+ store_id +"'");
		else builder.setQuery("metadata['user_id']:'"+ user_id +"'");
		
		// パラメータ生成
		SubscriptionSearchParams sbParams = builder.build();
		// Subscription取得
		Iterable<Subscription> _subscriptions = Subscription.search(sbParams).autoPagingIterable();
		Stream<Subscription> subscriptions = iterableToStream(_subscriptions);
		// 検索期間
		long st = CommonUtil.getUnixToTime(start_date), ed = CommonUtil.getUnixToTime(end_date);
		List<Invoice> invoices = new CopyOnWriteArrayList<Invoice>();
		// 対象インボイスを格納
		subscriptions.parallel().forEach(sb->addInvoice(invoices, sb, st, ed));
		// 作成順ソート
		invoices.stream().sorted(Comparator.comparing(Invoice::getCreated)).collect(Collectors.toList());
		
		// テスト出力
//		String a = "";
//		for(Invoice invoice : invoices){
//			a += invoice.getSubscriptionObject().getMetadata().get("store_id") + ":" +invoice.getId() + "\n";
//		}
//		System.out.println(a);
		
		// 返却
		return invoices;
	}
	
	private void addInvoice(List<Invoice> invoices, Subscription sb, long st, long ed){

		// パラメータ
		InvoiceSearchParams inParam = InvoiceSearchParams.builder()
				.setQuery("subscription:'"+sb.getId()+"' and created>="+st+" and created<="+ed)
				.setLimit((long) Const.MAX_SALES_COUNT)
				.addExpand("data.payment_intent")
				.addExpand("data.charge.balance_transaction")
				.build();
		// Invoice取得
		Stream<Invoice> stream = null;
		try {
			stream = iterableToStream(Invoice.search(inParam).autoPagingIterable());
		} catch (StripeException e) {
			e.printStackTrace();
		}
		
		// リスト追加(サブスク情報設定)
		invoices.addAll(stream.peek(i->i.setSubscriptionObject(sb)).collect(Collectors.toList()));
	}
	// IterableをStreamに変換する一般的なメソッド
	public static <T> Stream<T> iterableToStream(Iterable<T> iterable) {
		// Iterableからspliteratorを取得し、それをシーケンシャルストリームに変換します
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	/**
	 * 手数料の設定
	 */
	public void setBalanceTransaction(String id, Charge charge) throws StripeException {
		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		// 設定
		charge.setBalanceTransactionObject(BalanceTransaction.retrieve(id));
	}
	
	/**
	 * 返金情報リスト取得
	 */
	public Iterable<Refund> getRefunds(java.util.Date start_date, java.util.Date end_date) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// 対象期間
		com.stripe.param.RefundListParams.Created created = com.stripe.param.RefundListParams.Created.builder()
					.setGte(CommonUtil.getUnixToTime(start_date))
					.setLte(CommonUtil.getUnixToTime(end_date))
					.build();
		// パラメータ設定
		com.stripe.param.RefundListParams.Builder builder = RefundListParams.builder()
					.setLimit((long) Const.MAX_SALES_COUNT)
					.setCreated(created)
					.addExpand("data.payment_intent.invoice.subscription");
		RefundListParams params = builder.build();
		// 支払い情報リスト取得
		Iterable<Refund> refunds = Refund.list(params).autoPagingIterable();
		
		// 返却
		return refunds;
	}
	
	/**
	 * ユーザーの支払情報取得
	 */
	public PaymentMethodCollection getPaymentMethods(String stripe_customer_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// 支払情報パラメータ
		Map<String, Object> params = new HashMap<>();
		params.put("customer", stripe_customer_id);
		params.put("type", "card");
		
		// 支払情報
		PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
		// 返却
		return paymentMethods;
	}
	
	/**
	 * サブスク登録パラメータ取得
	 */
	private SubscriptionCreateParams getSubscriptionParam(String stripe_customer_id, PaymentMethodCollection paymentMethods, String stripe_plan_id, int store_id, int plan_id, int user_id) {
		
		// サブスク登録パラメータ
		SubscriptionCreateParams subCreateParams = SubscriptionCreateParams.builder()
				.setCustomer(stripe_customer_id)
				.addItem(SubscriptionCreateParams.Item.builder().setPrice(stripe_plan_id).build())
				.setPaymentBehavior(PaymentBehavior.ALLOW_INCOMPLETE)
				.setDefaultPaymentMethod(paymentMethods.getData().get(0).getId())
				.putMetadata("store_id", String.valueOf(store_id))
				.putMetadata("plan_id", String.valueOf(plan_id))
				.putMetadata("user_id", String.valueOf(user_id))
				.addAllExpand(Arrays.asList("latest_invoice.payment_intent")).build();
		
		// 返却
		return subCreateParams;
	}
	
	/**
	 * サブスク一覧取得
	 */
	public SubscriptionCollection getSubscriptionCollection(User user) throws StripeException {

		// パラメータ設定
		SubscriptionListParams params = SubscriptionListParams.builder().setCustomer(user.getStripe_customer_id())
																		.addExpand("data.latest_invoice.payment_intent").build();
		// サブスク一覧
		SubscriptionCollection subscriptions = Subscription.list(params);
		// 返却
		return subscriptions;
	}
	
	/**
	 * クレジットカード再登録
	 */
	public String resetCard(User user, HttpServletRequest req) throws StripeException, MalformedURLException {
		
		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;

		// 支払情報
		PaymentMethodCollection paymentMethods = getPaymentMethods(user.getStripe_customer_id());
		
		// クレジットカード情報が存在する場合
		if(0 < paymentMethods.getData().size()) {

			// 支払い情報
			PaymentMethod paymentMethod = paymentMethods.getData().get(0);
			// カード削除
			paymentMethod.detach();
		}
		
		// カード再登録
		return resetCard_user(req, user.getStripe_customer_id(), user.getId());
	}
	
	/**.collect(Collectors.joining());
	 * 再決済
	 */
	public void reCheckoutSubscription(SubscriptionCollection subscriptions, PaymentMethod paymentMethod) throws StripeException {
		
		// サブスク一覧数分
		for(Subscription subscription : subscriptions.getData()) {
			
			// 最後の請求情報
			Invoice invoice = subscription.getLatestInvoiceObject();
			
			// 請求時プロセス情報が発生している場合
			if(invoice.getPaymentIntent() != null) {
				
				// 請求時プロセス情報
				PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
				
				// 決済エラーが発生している場合
				if(paymentIntent.getLastPaymentError() != null) {
					
					// 非同期処理
					Runnable runner = () -> { 

						try {
							
							// 再決済パラメータ(決済カード情報設定)
							PaymentIntentConfirmParams paymentIntentConfirmParams = PaymentIntentConfirmParams.builder().setPaymentMethod(paymentMethod.getId()).build();
							// 再決済
							paymentIntent.confirm(paymentIntentConfirmParams);
							
						} catch (StripeException e) {}
					};
					runner.run();
				}
			}
		}
	}
	
	/**
	 * 決済エラーチェック
	 * @return true:エラー / false:エラーなし
	 */
	public boolean chkErrPaymentIntent(SubscriptionCollection subscriptions) throws StripeException {
		return 0 < getErrPaymentIntents(subscriptions, true).size();
	}
	/**
	 * 決済エラーメッセージリスト取得
	 * @return エラーメッセージリスト
	 */
	public List<String> getErrPaymentIntents(SubscriptionCollection subscriptions) throws StripeException {
		return getErrPaymentIntents(subscriptions, false);
	}
	/**
	 * サブスク一覧の決済エラーチェック
	 * @param anyCheck true:エラー可否調査のみ / false:全決済エラーのエラーメッセージ取得
	 * @return エラーメッセージリスト
	 */
	private List<String> getErrPaymentIntents(SubscriptionCollection subscriptions, boolean anyCheck) throws StripeException {

		// エラーメッセージリスト
		List<String> errList = new ArrayList<String>();
		
		// サブスク一覧数分
		for(Subscription subscription : subscriptions.getData()) {
			
			// 決済エラーチェック
			String errMsg = getErrPaymentIntent(subscription);
			// エラーメッセージが存在する場合
			if(errMsg != null) {
				
				// エラーメッセージ追加
				errList.add(errMsg);
				
				// エラー可否調査のみである場合は処理終了
				if(anyCheck) break;
			}
			
		}
		// エラーメッセージリスト返却
		return errList;
	}

	/**
	 * 決済エラーチェック
	 * @return true:エラー / false:エラーなし
	 */
	public boolean chkErrPaymentIntent(Subscription subscription) throws StripeException {
		return getErrPaymentIntent(subscription) != null;
	}
	/**
	 * 決済エラーチェック
	 * @return エラーメッセージ
	 */
	private String getErrPaymentIntent(Subscription subscription) throws StripeException {
		
		// エラーメッセージ
		String errMsg = null;
		// 最後の請求情報
		Invoice invoice = subscription.getLatestInvoiceObject();
		
		// 請求時プロセス情報が発生している場合
		if(invoice.getPaymentIntent() != null) {
			
			// 請求時プロセス情報
			PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
			
			// 決済エラーが発生している場合
			if(paymentIntent.getLastPaymentError() != null) {
				
				// 契約名
				String planNm;
				// ユーザープラン
				User_relation user_relation = user_planService.containBySubscriptionId(subscription.getId());
				// ユーザープランが存在する場合はプラン名設定
				if(user_relation != null) planNm = ((User_plan)user_relation).getPlan().getName();
				// 存在しない場合
				else {
					// ユーザーオプション
					user_relation = user_optionService.containBySubscriptionId(subscription.getId());
					// オプション名
					planNm = ((User_option)user_relation).getOption().getName();
				}
				
				// 店名
				String storeNm = storeService.find(user_relation.getStore_id()).getStore_name();
				// エラーメッセージ
				errMsg = "店舗:" + storeNm + " - 契約:" + planNm + "の決済エラーにより契約が完了していません。";
			}
		}
		
		// エラーメッセージ返却
		return errMsg;
	}
	
	/**
	 * 対象サブスク存在可否
	 */
	private boolean findSubscriptinUserRelation(SubscriptionCollection subscriptions, User_relation user_relation) {
		return subscriptions.getData().stream().anyMatch(s->s.getId() == user_relation.getStripe_subscription_id());
	}
	
	/**
	 * チェックアウト
	 */
//	public String createCheckout(HttpServletRequest req, int store_id, String stripe_customer_id,
//			com.model.Plan planModel, List<Option> options) throws StripeException, IOException {
//		return createCheckout(req, store_id, stripe_customer_id, planModel, options, 0);
//	}
//	public String createCheckout(HttpServletRequest req, int store_id, String stripe_customer_id,
//								com.model.Plan planModel, List<Option> options, int storeFromUserId) throws StripeException, IOException {
//
//		// STRIPE キー設定
//		Stripe.apiKey = Const.STRIPE_API_KEY;
//
//		/* ビルダー */
//		
//		// ビルダー
//		Builder stBuilder = SessionCreateParams.builder();
//		// プランが存在する場合は、ビルダーにプラン登録
//		if(planModel != null) addBuildItem(stBuilder, planModel.getStripe_plan_id());
//		
//		// オプションが存在する場合
//		if(options != null) {
//			// オプション数分
//			for (Option optionModel : options) {
//				// ビルダーにオプション登録
//				addBuildItem(stBuilder, optionModel.getStripe_plan_id());
//			}
//		}
//		
//		/* URL生成 */
//		
//		// 店舗からの遷移可否
//		boolean fromStore = 0 < storeFromUserId;
//		
//		// 遷移先
//		String forward = CommonUtil.getUrlHead(req) + (!fromStore ? "/search_store" : "/store_request");
//		// 成功URLパラメータ
//		String urlParam = !fromStore ? "?session_id={CHECKOUT_SESSION_ID}&store_id=" + store_id+ "&is_checkouted=true" : "?session_id={CHECKOUT_SESSION_ID}&user_id=" + storeFromUserId;
//		
//		/* チェックアウト */
//		
//		// チェックアウト設定
//		SessionCreateParams sesParams = stBuilder.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
//				.setCustomer(stripe_customer_id)
//				.setSuccessUrl(forward + urlParam)
//				.setCancelUrl(forward + "?store_id=" + store_id).build();
//		// チェックアウトセッション作成
//		Session stripeSes = Session.create(sesParams);
//
//		// チェックアウトセッションURL
//		return Const.REDIRECT_HEADER + stripeSes.getUrl();
//	}
//	
//	// チェックアウト対象商品追加
//	private void addBuildItem(Builder stBuilder, String stripePlanId) {
//		// 追加
//		stBuilder.addLineItem(SessionCreateParams.LineItem.builder().setPrice(stripePlanId).setQuantity(1L).build());
//	}

	// サブスク情報取得
	public Subscription getSubscription(String stripe_subscription_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		// 返却
		SubscriptionRetrieveParams params = SubscriptionRetrieveParams.builder().addExpand("latest_invoice.payment_intent").build();
		return Subscription.retrieve(stripe_subscription_id,params, null);
	}
	
/* キャンセル関連 */
	
	// サブスク返金
	public void refundSubscription(User_relation user_relation, boolean isPlan) throws StripeException {

		// 返金
		refundSubscription(user_relation.getStripe_subscription_id());
		// サブスクキャンセル
		cancelSubscription(user_relation.getStripe_subscription_id());
		// データ削除
		if(isPlan) user_planService.delete((User_plan)user_relation);
		else user_optionService.delete((User_option)user_relation);
	}
	private void refundSubscription(String stripe_subscription_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// サブスク
		Subscription subscription = getSubscription(stripe_subscription_id);
		// 支払い情報
		Invoice invoice = subscription.getLatestInvoiceObject();
		
		// 返金
		RefundCreateParams params = RefundCreateParams.builder()
									.setPaymentIntent(invoice.getPaymentIntent())
									//.setCharge(invoice.getCharge())
									.build();
		Refund.create(params);
	}
	
	// サブスクキャンセル
	public void cancelPlanSubscription(User_plan user_plan) throws StripeException {
		
		// サブスクキャンセル
		cancelSubscription(user_plan.getStripe_subscription_id());
		// データ削除
		user_planService.delete(user_plan);
	}
	public void cancelOptionSubscription(User_option user_option) throws StripeException {
		
		// サブスクキャンセル
		cancelSubscription(user_option.getStripe_subscription_id());
		// データ削除
		user_optionService.delete(user_option);
	}
	private void cancelSubscription(String stripe_subscription_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;

		// サブスク
		Subscription subscription = getSubscription(stripe_subscription_id);
		// サブスクをキャンセル
		if(!subscription.getStatus().equals(STATUS_CANCELED)) subscription.cancel();
	}
	
	// サブスク停止
	public String stopContinueSubscription(String stripe_subscription_id) throws StripeException {
		return stopRestartSubscroption(stripe_subscription_id, true);
	}
	// サブスク再開
	public void restartSubscription(String stripe_subscription_id) throws StripeException {
		stopRestartSubscroption(stripe_subscription_id, false);
	}
	
	/**
	 * サブスク停止・停止日取得
	 */
	private String stopRestartSubscroption(String stripe_subscription_id, boolean isStop) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// サブスクアイテム
		Subscription subscription = getSubscription(stripe_subscription_id);
		// サブスク停止
		Map<String, Object> params = new HashMap<>();
		params.put("cancel_at_period_end", isStop);
		subscription.update(params);
		
		// 終了日返却
		return CommonUtil.formatTimestamp(subscription.getCurrentPeriodEnd());
	}
	
/* 商品 */
	
	// 商品作成
	public String createPlan(int amount, int interval, int store_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;

		// 商品作成パラメータ
		Map<String, Object> params = new HashMap<>();
		params.put("amount", amount);
		params.put("currency", CURRENCY);
		params.put("interval", INTERVAL_UNIT);
		params.put("interval_count", interval);
		params.put("product", Const.STRIPE_PROC_ID);
		// 商品作成
		Plan plan = Plan.create(params);

		// メタデータ作成
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("store_id", store_id);
		// 更新パラメータ
		Map<String, Object> params_update = new HashMap<>();
		params_update.put("metadata", metadata);
		// 商品更新
		plan = plan.update(params_update);

		// 商品作成・返却
		return plan.getId();
	}
	
	// 商品更新
	public String updatePlan(String stripe_plan_id, int amount, int interval) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;

		// 商品
		Plan plan = Plan.retrieve(stripe_plan_id);

		// 更新パラメータ
		Map<String, Object> params_update = new HashMap<>();
		params_update.put("amount", amount);
		params_update.put("interval_count", interval);
		// 商品更新
		plan = plan.update(params_update);

		// 商品作成・返却
		return plan.getId();
	}
	
	// 商品更新
	public void deletePlan(String stripe_plan_id) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// 商品
		Plan plan = Plan.retrieve(stripe_plan_id);
		// 削除
		plan.delete();
	}
	
/* 最新化 */
	
	// ユーザープラン最新化
	public void callChkUpdatePlan(HttpSession ses) throws Exception {

		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);
		// プランチェック
		checkUpdatePlans(user);
	}
	/**
	 * プラン・オプション情報最新化
	 */
	@Transactional
	public void checkUpdatePlans(User user) throws Exception {
		
		// ユーザーが存在する場合
		if(user != null) {
			
			// STRIPE キー設定
			Stripe.apiKey = Const.STRIPE_API_KEY;
			// サブスク一覧
			SubscriptionCollection subscriptions = getSubscriptionCollection(user);
			
		/* 無効となったサブスクをユーザープランからも削除 */
			
			// 登録プラン一覧
			List<User_plan> user_plans = user_planService.find_list(user.getId());
			// 登録プラン一覧数分
			for(User_relation user_relation : user_plans) {
				
				// 対象サブスク存在可否
				boolean exist_plan = findSubscriptinUserRelation(subscriptions,user_relation);
				// 対象サブスクが存在しない場合(支払い期限切れなどにより)
				if(!exist_plan) {
					// ユーザープラン削除
					user_planService.delete((User_plan)user_relation);
				}
			}
			
			// オプションリスト
			List<User_option> user_options = user_optionService.find_list(user.getId());
			// 登録オプション一覧数分
			for(User_relation user_relation : user_options) {
				
				// 対象サブスク存在可否
				boolean exist_option = findSubscriptinUserRelation(subscriptions,user_relation);
				// 対象サブスクが存在しない場合(支払い期限切れなどにより)
				if(!exist_option) {
					// ユーザーオプション削除
					user_optionService.delete((User_option)user_relation);
				}
			}
			
		/* ユーザープラン登録 */
			
			// サブスク一覧数分
			for(Subscription subscription : subscriptions.getData()) {
				
				// 商品ID
				String stripe_plan_id = subscription.getItems().getData().get(0).getPlan().getId();
				// プラン保存
				boolean existsPlan = updatePlanCount(user.getId(), stripe_plan_id, subscription);
					
				// プランが存在しない場合
				if(!existsPlan) {
					
					// オプション保存
					boolean existsOption = updateOptionCount(user.getId(), stripe_plan_id, subscription);
					
					// プランもオプションも存在しない場合、不正な商品が登録されているため、商品を削除
					if(!existsOption) throw new Exception("ユーザープラン・ユーザーオプションの最新化(checkUpdatePlans) - 不正な商品が登録されている(id:" + stripe_plan_id + ")");
				}
			}
		}
	}
	
	/**
	 * カウントデータ保存
	 */
	private boolean updatePlanCount(int user_id, String stripe_plan_id, Subscription subscription) throws Exception {
		// プランカウント保存・
		return savePlanCount(stripe_plan_id, user_id, subscription, true);
	}
	private boolean updateOptionCount(int user_id, String stripe_plan_id, Subscription subscription) throws Exception {
		// プランカウント保存・
		return savePlanCount(stripe_plan_id, user_id, subscription, false);
	}
	/**
	 * カウントデータの登録
	 */
	private boolean savePlanCount(String stripe_plan_id, int user_id, Subscription subscription, boolean isPlan) {

		// オプション取得
		BaseModel baseModel = isPlan ? planService.containByStripePlanId(stripe_plan_id) : optionService.containByStripePlanId(stripe_plan_id);
		
		// プランが存在する場合
		if(baseModel != null) {
			
			// ユーザープラン
			User_relation user_relation = isPlan
					? user_planService.containByPlanId(baseModel.getStore_id(), user_id, baseModel.getId())
					: user_optionService.containByPlanId(baseModel.getStore_id(), user_id, baseModel.getId());
			
			// ユーザープランが存在しない場合
			if(user_relation == null) {
				
				// ユーザープラン保存
				user_relation = isPlan ? new User_plan(baseModel.getStore_id(), user_id, baseModel.getId()) : new User_option(baseModel.getStore_id(), user_id, baseModel.getId());
				
				// サブスクID設定
				user_relation.setStripe_subscription_id(subscription.getId());
				// 次回更新可否設定
				//user_relation.setStripe_cancel_at_period_end(subscription.getCancelAtPeriodEnd());
				
				// 保存
				if(isPlan) user_planService.saveOne((User_plan)user_relation);
				else user_optionService.saveOne((User_option)user_relation);
			}
			
			// カウント情報
			User_count user_count = isPlan
					? user_plan_countService.contain(user_id, baseModel.getId(), subscription.getCurrentPeriodStart(),subscription.getCurrentPeriodEnd())
					: user_option_countService.contain(user_id, baseModel.getId(), subscription.getCurrentPeriodStart(),subscription.getCurrentPeriodEnd());
			
			// カウント情報が存在しない場合
			if(user_count == null) {
				
				// カウント情報生成
				user_count = isPlan ? new User_plan_count() : new User_option_count();
				user_count.setUser_id(user_id);
				user_count.setStart_date(subscription.getCurrentPeriodStart());
				user_count.setEnd_date(subscription.getCurrentPeriodEnd());
				
				// 保存
				if(isPlan) {
					((User_plan_count)user_count).setPlan_id(baseModel.getId());
					user_plan_countService.save((User_plan_count)user_count);
				}else{
					((User_option_count)user_count).setOption_id(baseModel.getId());
					user_option_countService.save((User_option_count)user_count);
				}
			}
			
			// 返却
			return true;
			
		// 存在しない場合
		}else {
			// 返却
			return false;
		}
	}
	
/* テキスト生成 */
	
	/**
	 * プラン文章取得
	 */
	public String getPlanText(User_plan user_plan, List<User_option> user_options) throws StripeException {
		
		// リスト生成
		List<User_plan> user_plans = new ArrayList<User_plan>();
		if(user_plan != null) user_plans.add(user_plan);
		
		// テキスト生成
		return getPlanText(user_plans, user_options);
	}
	public String getPlanText(List<User_plan> user_plans, List<User_option> user_options) throws StripeException {

		// 登録情報テキスト
		StringBuffer text = new StringBuffer();
		
	/* 契約プラン */
		
		// ユーザープラン数分ループ
		for(User_plan user_plan : user_plans) {
			// テキスト設定
			setTextAndCountVal(text, user_plan.getPlan(), user_plan, true);
		}
		
	/* 契約オプション */
		
		// オプション数分ループ
		for(User_option user_option : user_options) {
			// テキスト設定
			setTextAndCountVal(text, user_option.getOption(), user_option, false);
		}
		
		// 返却
		return text.toString();
	}
	
	/**
	 * 契約詳細テキスト作成
	 */
	private void setTextAndCountVal(StringBuffer text, BaseModel baseModel, User_relation user_relation, boolean isPlan) throws StripeException {

		// 登録情報テキスト
		StringBuffer optionText = new StringBuffer();
		
		// 選択中のオプション
		optionText.append("契約プラン:" + baseModel.getName() + Const.NL);
		optionText.append("契約プラン料金:￥" + baseModel.getPrice() + Const.NL);
		optionText.append("利用回数：" + baseModel.getPlan_interval() + "ヵ月で" + baseModel.getCount() + "回まで利用可能" + Const.NL);
		
		// サブスク
		Subscription subscription = getSubscription(user_relation.getStripe_subscription_id());
		
		// カウント情報
		User_count user_count = getUser_Count(isPlan, user_relation, baseModel, user_relation.getStripe_subscription_id());
		
	/* カウント情報設定 */
		
		// カウントセット
		user_relation.setUser_count(user_count);
		// カウント
		int userCount = CommonUtil.getUserCount(user_count);
		// 残りの利用可能数
		int remainCnt = baseModel.getCount() - userCount;
		// 利用上限可否
		boolean isLimit = baseModel.getCount() <= userCount;
		// 利用上限可否設定
		user_relation.setIs_limit(isLimit);
		
		// 次回更新可否
		user_relation.setStripe_cancel_at_period_end(subscription.getCancelAtPeriodEnd());
		
	/* テキスト設定 */
		
		// 決済エラー可否(true:エラー)
		boolean errUsrPay = chkErrPaymentIntent(subscription);
		
		// カウントテキスト追加
		setLimitText(optionText, isLimit, errUsrPay);
		
		// 次回更新日または終了日
		String period_end_str = CommonUtil.formatTimestamp(subscription.getCurrentPeriodEnd());
		long period_end = CommonUtil.getTimestamp(subscription.getCurrentPeriodEnd());
		
		// 決済エラーである場合
		if(errUsrPay) {
			// 決済エラーテキスト設定
			optionText.append("この契約は決済失敗(登録されている決済情報の有効期限や残高不足など何かしらの原因により決済失敗)により契約が完了できませんでした。" + Const.NL);
			optionText.append("そのためこの契約は利用することができません。トップページから「決済エラー」を確認してください。" + Const.NL);
			
		// 停止済みサブスク場合
		}else if(subscription.getCancelAtPeriodEnd()) {
			// 終了日
			optionText.append("終了日(次回のプラン更新は停止されています):" + period_end_str + Const.NL);
			// 残利用回数
			optionText.append("終了日までに利用できる回数:" + remainCnt + Const.NL);
			
		// 削除済みプランである場合
		}else if(baseModel.getIs_delete() == Const.IS_DELETED) {
			// 終了日
			optionText.append("終了日(店舗からプランが削除されたため次回更新は停止されました。なおプランは終了日まで利用できます。):" + period_end_str + Const.NL);
			// 残利用回数
			optionText.append("終了日までに利用できる回数:" + remainCnt + Const.NL);
			
		// 契約中のプランである場合
		}else {
			// 更新日
			optionText.append("更新日:" + period_end_str + Const.NL);
			// 残利用回数
			optionText.append("次回更新までに利用できる回数:" + remainCnt + Const.NL);
		}
		
		// これまでの利用日
		optionText.append("これまでの利用日:" + setUseDatesText(user_count) + Const.NL + Const.NL);
		
	/* フィールド値設定 */
		
		// 個別テキスト設定
		user_relation.setContract_text(optionText.toString());
		user_relation.setCount_text(getCountText(remainCnt));
		user_relation.setPeriod_end_str(period_end_str);
		user_relation.setPeriod_end(period_end);
		
		// 決済エラー可否設定
		user_relation.setError_user_pay(errUsrPay);
		
		// 利用期間内チェック
		ResultWithinDate isRefund = CommonUtil.chkWithinDate(user_relation.getUser_count().getStart_date(), user_relation.getUser_count().getEnd_date());
		user_relation.setWithin_date(isRefund == ResultWithinDate.NOT_ERR);
		// 利用期間外である場合
		if(isRefund == ResultWithinDate.ERR_AFT) {
			// ユーザー関連データ削除
			if(isPlan) user_planService.delete((User_plan)user_relation);
			else user_optionService.delete((User_option)user_relation);
		}
		
		// 返品可否設定(未利用 & 期限内)
		user_relation.setIs_refund(CommonUtil.isRefund(user_relation.getCreated_date(), userCount));
		
		// テキスト追加
		text.append(optionText.toString());
	}
	
	/**
	 * 利用日テキスト取得
	 */
	private String setUseDatesText(User_count user_count) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/M/d HH:mm");
		String useDates = user_count != null ? user_count.getUseDates().stream().map(d->dateFormat.format(d)).collect(Collectors.joining(",")): null;
		return useDates == null || useDates.equals("") ? "利用なし" : useDates;
	}
	
	/**
	 * 利用上限テキスト取得
	 */
	private static void setLimitText(StringBuffer planText, boolean is_limit, boolean errUsrPay) {
		
		// 決済エラーである場合
		if(errUsrPay) planText.append("利用可否：このプランは決済失敗により利用することができません");
		// 利用上限である場合
		else if(is_limit) planText.append("利用可否：このプランは次回更新までに利用できる回数の上限に達しているため利用することができません");
		// 利用上限出ない場合
		else planText.append("利用可否：このプランは利用できます");
		// テキスト設定
		planText.append(Const.NL);
	}
	/**
	 * 利用回数テキスト取得
	 */
	private static String getCountText(int remainCnt) {
		// 残回数が0である場合 / 0でない場合
		return remainCnt == 0 ? "利用回数上限に達しているため利用不可" : "あと" + remainCnt + "回利用できます";
	}
	
/* 店舗削除時返金 */
	
	// サブスク返金
	public long refundStoreSubscription(User_relation user_relation, boolean isPlan) throws StripeException {
		
		// プラン情報
		BaseModel baseModel = isPlan ? ((User_plan)user_relation).getPlan(): ((User_option)user_relation).getOption();
		// ユーザーカウント情報
		User_count user_count = getUser_Count(isPlan, user_relation, baseModel, user_relation.getStripe_subscription_id());
		user_relation.setUser_count(user_count);
		
		// プランの利用上限
		int limit = isPlan ? baseModel.getCount() : baseModel.getCount();
		// 利用情報が存在する場合
		if(user_relation.getUser_count() != null) {
			
			// ユーザー利用数
			int userCnt = user_relation.getUser_count().getCount();
			// 返金
			long refundAmount = refundStoreSubscription(user_relation.getStripe_subscription_id(), limit, userCnt);
			// サブスクキャンセル
			cancelSubscription(user_relation.getStripe_subscription_id());
			// データ削除
			if(isPlan) user_planService.delete((User_plan)user_relation);
			else user_optionService.delete((User_option)user_relation);
			// 返却
			return refundAmount;
			
		}else return 0;
	}
	
	private long refundStoreSubscription(String stripe_subscription_id, int limit, int userCnt) throws StripeException {

		// STRIPE キー設定
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// サブスク
		Subscription subscription = getSubscription(stripe_subscription_id);
		// 支払い情報
		Invoice invoice = subscription.getLatestInvoiceObject();
		// 返金額
		long refundAmount = invoice.getAmountPaid() - (invoice.getAmountPaid() / limit * userCnt);
		// 返金
		RefundCreateParams params = RefundCreateParams.builder()
									.setPaymentIntent(invoice.getPaymentIntent())
									//.setCharge(invoice.getCharge())
									.setAmount(refundAmount)
									.build();
		Refund.create(params);
		// 返却
		return refundAmount;
	}
	
	/**
	 * ユーザーカウント
	 */
	private User_count getUser_Count(boolean isPlan, User_relation user_relation, BaseModel baseModel, String stripe_subscription_id) throws StripeException {
		// サブスクアイテム
		Subscription subscription = getSubscription(stripe_subscription_id);
		// カウント情報
		User_count user_count;
		if(isPlan) user_count = user_plan_countService.contain(user_relation.getUser_id(), baseModel.getId(), subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
		else user_count = user_option_countService.contain(user_relation.getUser_id(), baseModel.getId(), subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
		return user_count;
	}
}
