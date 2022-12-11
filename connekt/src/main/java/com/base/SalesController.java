package com.base;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.ajax.response.AjaxSalesResponse;
import com.form.StoreSalesForm;
import com.form.StoreSalesListForm;
import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.service.OptionService;
import com.service.PlanService;
import com.service.StoreService;
import com.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.RefundCollection;
import com.stripe.model.Subscription;
import com.util.CommonUtil;
import com.util.ConnectionUtil;
import com.util.StripeUtil;

@Controller
public class SalesController{

	@Autowired private StoreService storeService;
	@Autowired private UserService userService;
	@Autowired private PlanService planService;
	@Autowired private OptionService optionService;
	@Autowired private StripeUtil stripeUtil;
	
	// コネクション汎用
	@Autowired private ConnectionUtil connectionUtil;
	// 静的ファイル参照用
	@Autowired ResourceLoader resourceLoader;
	
	/**
	 * 初期表示
	 * @param isStore 店舗処理可否(true:店舗処理 / false:ユーザー処理)
	 * @param id 店舗ID/ユーザーID
	 */
	public String disp(Model model, HttpServletRequest req, HttpSession ses, boolean isStore, String nm, int id) throws Exception {

		// 現在日
		LocalDate now = LocalDate.now();
		// 月初
		LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
		Date start_date = localDate2Date(start);
		// 月末
		LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
		Date end_date = localDate2Date(end);
		
		// 画面表示
		return disp(new StoreSalesForm(start_date, end_date) , null, model, req, ses, isStore, nm, id);
	}
	
	/**
	 * 日付検索表示
	 * @param isStore 店舗処理可否(true:店舗処理 / false:ユーザー処理)
	 * @param id 店舗ID/ユーザーID
	 */
	public String disp(StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses, boolean isStore, String nm, int id) throws Exception {
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return isStore ? Const.PAGE_STORE_VALID_ERROR : Const.PAGE_USER_VALID_ERROR;

		// コネクション生成
		try(Connection conn = connectionUtil.connect()){
			
			// 請求情報リスト
			List<StoreSalesListForm> list_ch = getSalesList(isStore, storeSalesForm, nm, id);
			// 返金情報リスト
			List<StoreSalesListForm> list_re = getRefundList(isStore, storeSalesForm, nm, id);
			
			// モデル
			model.addAttribute("form", storeSalesForm);
			// リクエスト
			req.setAttribute("list_ch", list_ch);
			req.setAttribute("list_re", list_re);
			
			// 遷移
			return isStore ? Const.PAGE_STORE_SALES : Const.PAGE_USER_SALES;
			
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * Ajax データ取得
	 */
	public Object dispAjax(boolean isStore, boolean isCharge, StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// 遷移先、顧客名/店舗名、ユーザーID/店舗ID
		String forward = null, nm;
		int id;
		if(isStore) {
			// セッション店舗チェック
			String sesErrFoward = CommonUtil.isSesStore(ses);
			if(sesErrFoward != null) forward = sesErrFoward;
			// セッション店舗情報
			Store store = CommonUtil.getSessionStore(ses);
			nm = store.getName();
			id = store.getId();
		}else {
			// セッションユーザーチェック
			String sesErrFoward = CommonUtil.isSesUser(ses);
			if(sesErrFoward != null) forward = sesErrFoward;
			// セッションユーザー情報
			User user = CommonUtil.getSessionUser(ses);
			nm = user.getName();
			id = user.getId();
		}
		// 情報リスト
		List<StoreSalesListForm> list = isCharge ? getSalesList(isStore, storeSalesForm, nm, id) : getRefundList(isStore, storeSalesForm, nm, id);
		
		// 正常処理時
		if(forward == null) {
			// 返却データ生成
			AjaxSalesResponse ares = new AjaxSalesResponse();
			ares.setList(list);
			ares.setLast(getLast(list, isCharge));
			// 返却
			return ares;
		// 異常時
		}else {
			// エラー返却
			return "error";
		}
	}
	
	/**
	 * 請求合計額、Stripe手数料合計額取得
	 * return 合計金額、Stripe手数料合計
	 */
	private long[] getSalesSumAndFee(List<StoreSalesListForm> list) {
		
		long sum = 0, fee = 0;
		for(StoreSalesListForm fm : list) {
			sum += fm.getPrice();
			fee += fm.getBalance();
		}
		
		return new long[] {sum, fee};
	}
	
	/**
	 * 請求リスト取得
	 */
	public List<StoreSalesListForm> getSalesList(boolean isStore, StoreSalesForm storeSalesForm, String nm, int id) throws StripeException {
		
		// 請求情報リスト
		List<StoreSalesListForm> list_ch = new ArrayList<StoreSalesListForm>();
		
		// 対象期間の支払情報リスト
		ChargeCollection chargeCollection;
		
		// 店舗処理である場合
		if(isStore) {
			// 対象期間の支払情報リスト
			chargeCollection = stripeUtil.getCharges(storeSalesForm.getStart_date(), storeSalesForm.getEnd_date(), storeSalesForm.getLast_ch());
		// ユーザー処理である場合
		}else {
			// セッションユーザー情報
			User user = userService.find(id);
			// 対象期間の支払情報リスト
			chargeCollection = stripeUtil.getChargesByCustomer(user.getStripe_customer_id(), storeSalesForm.getStart_date(), storeSalesForm.getEnd_date(), storeSalesForm.getLast_ch());
		}

		// 支払情報リスト数分
		for(Charge charge : chargeCollection.getData()){
			
			// 支払情報
			PaymentIntent paymentIntent = charge.getPaymentIntentObject();
			// 支払失敗でない & 支払情報が存在する場合(存在しないものは下書きデータ)
			if(!charge.getStatus().equals("failed") && paymentIntent != null) {
				
				// インボイス
				Invoice invoice = charge.getInvoiceObject();
				// 対象店舗可否情報
				IsTarget isTarget = isTargetSubscription(invoice.getSubscriptionObject(), isStore, id);
				// 対象店舗である場合
				if(isTarget.getIs_target()) {
					
					// 0円以上の支払いである & 支払情報が存在する場合(支払情報が存在しないものはキャンセル済みインボイスなど)
					if(0< invoice.getAmountPaid() && invoice.getStatusTransitions().getPaidAt() != null) {
						
						// 手数料
						long fee = charge.getBalanceTransactionObject().getFee();
						
						// ユーザー
						User user = userService.findByStripeCustomerId(invoice.getCustomer());
						// 契約名
						String planName = getPlanNameBySubs(isTarget.getSubscription());
						// リスト追加
						setStoreSalesList(list_ch, charge.getId(), user, nm, invoice.getStatusTransitions().getPaidAt(), invoice.getAmountPaid(), fee, planName, false);
					}
				}
			}
		}
		
		// 頁最終データID設定
		storeSalesForm.setLast_ch(getLast(list_ch, true));
		// 返却
		return list_ch;
	}

	/**
	 * 請求リスト取得
	 */
	public List<StoreSalesListForm> getRefundList(boolean isStore, StoreSalesForm storeSalesForm, String nm,int id) throws StripeException {
		
		// 返金情報リスト
		List<StoreSalesListForm> list_re = new ArrayList<StoreSalesListForm>();
		// 対象期間の支払情報リスト
		RefundCollection refundCollection = stripeUtil.getRefunds(storeSalesForm.getStart_date(), storeSalesForm.getEnd_date(), storeSalesForm.getLast_re());
		// 支払情報リスト数分
		for(Refund refund : refundCollection.getData()){

			// 支払情報
			PaymentIntent paymentIntent = refund.getPaymentIntentObject();
			// 支払情報が存在する場合(存在しないものは下書きデータ)
			if(paymentIntent != null) {
				
				// 対象店舗可否情報
				IsTarget isTarget = isTargetSubscription(refund.getPaymentIntentObject().getInvoiceObject().getSubscriptionObject(), isStore, id);
				
				// 対象店舗である場合
				if(isTarget.getIs_target()){
					
					// 0円以上の返金である場合
					if(0< refund.getAmount()) {
						
						// 手数料
						long fee = getFee(paymentIntent);
						
						// ユーザー
						User user = userService.findByStripeCustomerId(refund.getPaymentIntentObject().getCustomer());
						// 契約名
						String planName = "(返金)" + getPlanNameBySubs(isTarget.getSubscription());
						// リスト追加
						setStoreSalesList(list_re, refund.getId(), user, nm, refund.getCreated(), refund.getAmount(), fee, planName, true);
					}
				}
			}
		}
		
		// 頁最終データID設定
		storeSalesForm.setLast_re(getLast(list_re, false));
		// 返却
		return list_re;
	}
	
	/**
	 * 頁最終データID取得
	 */
	public String getLast(List<StoreSalesListForm> list, boolean isCharge) {
		// 情報リストが1回の取得最大件数である場合は、データID返却
		if(list != null && Const.MAX_SALES_COUNT <= list.size()) return isCharge ? list.get(list.size() - 1).getLast_ch() : list.get(list.size() - 1).getLast_re();
		return null;
	}
	/**
	 * 手数料取得
	 */
	private long getFee(PaymentIntent paymentIntent) throws StripeException {
		// チャージ
		Charge charge = paymentIntent.getCharges().getData().get(0);
		// 手数料情報設定
		stripeUtil.setBalanceTransaction(charge.getBalanceTransaction(), charge);
		// 手数料返却
		return charge.getBalanceTransactionObject().getFee();
	}
	
	/**
	 * サブスクIDからプラン名を取得
	 */
	private String getPlanNameBySubs(Subscription subscription) {
		// プラン名
		String planNm = "";
		// プランID
		Object plan_id_obj = subscription.getMetadata().get("plan_id");
		// TODO (本来不要) プランIDが存在する場合
		if(plan_id_obj != null) {
			// プランID
			int plan_id = Integer.parseInt(plan_id_obj.toString());
			// プラン情報
			BaseModel plan = planService.findWithDeleted(plan_id);
			// プラン情報が存在する場合はプラン名設定
			if(plan != null) planNm = plan.getName();
			// プランに該当しない場合
			else{
				// オプション情報
				BaseModel option = optionService.findWithDeleted(plan_id);
				// オプション名設定
				planNm = option.getName();
			}
		}
		
		// 返却
		return planNm;
	}
	
	/**
	 * リスト設定
	 */
	private void setStoreSalesList(List<StoreSalesListForm> list, String last, User user, String nm, long paidAt, long amountPaid, long balance,String planName, boolean isRefund) {

		// フォーム生成
		StoreSalesListForm storeSalesListForm = new StoreSalesListForm();
		// メールアドレス
		storeSalesListForm.setEmail(user.getEmail().replace("@DELETED_", "(削除済ユーザー)"));
		// プラン名
		storeSalesListForm.setName(planName);
		// 支払日
		Date user_date = CommonUtil.formatTimestampDate(paidAt);
		storeSalesListForm.setUse_date(user_date);
		// 金額
		storeSalesListForm.setPrice(isRefund ? -amountPaid : amountPaid);
		// 手数料
		storeSalesListForm.setBalance(balance);
		// 顧客名/店舗名
		storeSalesListForm.setUser_name(nm);
		// 支払/返金情報(次頁取得用)
		if(isRefund) storeSalesListForm.setLast_re(last);
		else storeSalesListForm.setLast_ch(last);
		// リスト追加
		list.add(storeSalesListForm);
	}
	
	/**
	 * 対象店舗可否
	 */
	private IsTarget isTargetSubscription(Subscription subs, boolean isStore, int store_id) throws StripeException {
		// 対象店舗可否
		boolean isTarget = isStore ? subs.getMetadata().get("store_id") == null ? false : subs.getMetadata().get("store_id").toString().equals(String.valueOf(store_id)) : true;
		// 返却
		return new IsTarget(isTarget, subs);
	}
	/**
	 * 対象店舗可否情報
	 */
	private class IsTarget{
		// 対象店舗可否
		private boolean is_target;
		// サブスク情報
		private Subscription subscription;
		// コンストラクタ
		public IsTarget(boolean is_target, Subscription subscription) {
			this.is_target = is_target;
			this.subscription = subscription;
		}
		public boolean getIs_target() {
			return is_target;
		}
		public Subscription getSubscription() {
			return subscription;
		}
	}
	
	/***
	 * LocalDate→Dateへ変換
	 */
	public static Date localDate2Date(final LocalDate localDate) {
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * PDF出力
	 */
	public void export(StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses,  HttpServletResponse res, boolean isStore) throws Exception {
		
		// ドキュメントオブジェクトの作成
		try(PDDocument document = new PDDocument()){

			// ページオブジェクトの作成
			PDPage page = new PDPage();
			document.addPage(page);
	
			Resource resource = resourceLoader.getResource("classpath:static/font/msmincho.ttc");
			// フォント指定
			File file = resource.getFile();
			@SuppressWarnings("resource")
			TrueTypeCollection collection = new TrueTypeCollection(file);
			PDFont font = PDType0Font.load(document, collection.getFontByName("MS-Mincho"), true);
	
			// 文字出力処理
			try(PDPageContentStream cs = new PDPageContentStream(document, page)){
				
				// テキスト
				cs.beginText();
				cs.setFont(font, 20);
				cs.newLineAtOffset(240, 755);
				cs.showText("Connekt 請求書");
				
				// 店舗ID/ユーザーID
				int id;
				// 店舗名/ユーザー名
				String to;
				
				if(isStore) {
					Store store = CommonUtil.getSessionStore(ses);
					id = store.getId();
					to = store.getName();
				}else {
					User user = CommonUtil.getSessionUser(ses);
					id = user.getId();
					to = user.getName();
				}
				
				// コネクション生成
				try(Connection conn = connectionUtil.connect()){
					
					// 請求情報リスト
					List<StoreSalesListForm> list_ch = getSalesList(isStore, storeSalesForm, to, id);
					// 次頁の開始ID
					storeSalesForm.setLast_ch(getLast(list_ch, true));
					// データ続くまで繰り返す
					while(storeSalesForm.getLast_ch() != null) {
						List<StoreSalesListForm> list_tmp = getSalesList(isStore, storeSalesForm, to, id);
						list_ch.addAll(list_tmp);
						storeSalesForm.setLast_ch(getLast(list_tmp, true));
					}
					
					// 返金情報リスト
					List<StoreSalesListForm> list_re = getRefundList(isStore, storeSalesForm, to, id);
					// 次頁の開始ID
					storeSalesForm.setLast_re(getLast(list_re, false));
					// データ続くまで繰り返す
					while(storeSalesForm.getLast_re() != null) {
						List<StoreSalesListForm> list_tmp = getSalesList(isStore, storeSalesForm, to, id);
						list_re.addAll(list_tmp);
						storeSalesForm.setLast_re(getLast(list_tmp, false));
					}
					
					// 一覧データ結合
					List<StoreSalesListForm> list = new ArrayList<StoreSalesListForm>();;
					list.addAll(list_ch);
					list.addAll(list_re);
					
					// 売上額
					long[] sums = getSalesSumAndFee(list);
					long sum = sums[0];
					// stripe手数料(クレジットカード引き落とし時の外部決済システム手数料)
					long com_stripe = sums[1];
					
					// 日付フォーマット
					final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
					String targetDates = sdf.format(storeSalesForm.getStart_date()) + "～" + sdf.format(storeSalesForm.getEnd_date());

					// テキスト設定
					cs.setFont(font, 15);
					cs.newLineAtOffset(200, -20);
					cs.showText("発行日:" + sdf.format(new Date()));
					
					cs.newLineAtOffset(0, -20);
					cs.showText("Connekt 長野将大");
					
					cs.newLineAtOffset(-400, -100);
					cs.showText(to + "様");
					
					cs.newLineAtOffset(0, -50);
					String head = isStore ? "売上" : "支払";
					cs.showText("・" + head);
					
					cs.newLineAtOffset(20, -50);
					cs.showText("対象月 : " + targetDates);

					cs.newLineAtOffset(0, -50);
					cs.showText(head + "額 : \\" + sum);
					
					// 店舗である場合
					if(isStore) {

						// セッション店舗情報
						Store store = CommonUtil.getSessionStore(ses);
						
						// ディスカウント登録店舗数
						int discountCnt = CommonUtil.isEmpty(store.getDiscount_id()) ? 0 : storeService.countDiscount_target(store.getDiscount_id()).size();
						
						// connekt手数料
						double com_connekt_ratio = Const.COMMISSION - (0.01 * discountCnt);
						int com_connekt = (int) (sum * com_connekt_ratio);
						// 銀行振込手数料(銀行振込時の外部振込システム手数料)
						int com_connekt_trans = 250;
						// 手数料合計
						long com_sum = com_connekt + com_stripe + com_connekt_trans;
						
						cs.newLineAtOffset(-20, -75);
						cs.showText("・請求(小数値切捨)");
						
						cs.newLineAtOffset(20, -50);
						cs.showText("請求月 : " + targetDates);
						
						cs.newLineAtOffset(0, -50);
						double com_connekt_ratio_parsent = ((double)Math.round((com_connekt_ratio * 100) * 10))/10;
						cs.showText("Connekt手数料("+ com_connekt_ratio_parsent +"%・少数切捨) : \\" + com_connekt);
						
						cs.newLineAtOffset(0, -50);
						cs.showText("クレジットカード引き落とし時の外部手数料(3.6%) : \\" + com_stripe);
						cs.newLineAtOffset(0, -20);
						cs.setFont(font, 10);
						cs.showText("※クレジットカード決済時に、一般的に売手側が負担するカード決済利用手数料です");
	
						cs.setFont(font, 15);
						cs.newLineAtOffset(0, -50);
						cs.showText("銀行振込時の外部振込システム手数料 : \\" + com_connekt_trans);
						
						cs.newLineAtOffset(0, -75);
						cs.showText("請求額(手数料)合計 : \\" + com_sum);
					}
					
				}catch(Exception e){
					throw e;
				}
				cs.endText();
			}

			// レスポンスヘッダー設定
			res.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode("invoice.pdf", "UTF-8"));
			res.setContentType("application/pdf");
			// ドキュメントを保存します
			document.save(res.getOutputStream());
		}
	}
}
