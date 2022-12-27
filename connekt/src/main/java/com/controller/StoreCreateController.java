package com.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

import com.base.User_relation;
import com.form.ForgetForm;
import com.model.Option;
import com.model.Plan;
import com.model.Store;
import com.model.User_option;
import com.model.User_plan;
import com.properties.Const;
import com.security.LoginController;
import com.service.CertUtil;
import com.service.OptionService;
import com.service.PlanService;
import com.service.StoreService;
import com.service.User_optionService;
import com.service.User_planService;
import com.stripe.exception.StripeException;
import com.util.CommonUtil;
import com.util.MailUtil;
import com.util.NewsUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StoreCreateController")
public class StoreCreateController extends BaseStoreController{
	
	@Autowired private StoreService storeService;
	@Autowired private CertUtil certUtil;
	@Autowired private MailUtil mailUtil;
	@Autowired private PlanService planService;
	@Autowired private OptionService optionService;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private StripeUtil stripeUtil;
	@Autowired private NewsUtil newsUtil;
	
	// 認証セッションキー
	private static final String CERT_KEY = "cert_key";
	
/* 店舗設定 */
	
	/**
	 * 認証画面表示
	 */
	@PostMapping("/create_cert")
	public String createCert(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// 4桁の乱数
		String cert_key = String.format("%04d%n", new Random().nextInt(10000));
		// 認証キーとして設定
		ses.setAttribute(CERT_KEY, cert_key);

		// セッション店舗情報
		Store sesStore = CommonUtil.getSessionStore(ses);
		
		// メール通知
		mailUtil.sendStoreSettingCertKey(sesStore.getEmail(), cert_key);

		// メッセージ
		List<String> info_messages = new ArrayList<String>();
		info_messages.add("登録されているメールアドレスへ認証キーを送信しました。ご確認の上、メールに記載されている認証キーを画面に入力してください。");
		// リクエスト
		req.setAttribute(Const.MSG_INFO, info_messages);
		
		// 遷移
		return Const.PAGE_STORE_SETTING_CERT;
	}
	
	/**
	 * 設定画面表示
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@PostMapping("/store_update")
	public String disp(@RequestParam(name="cert_key",required=false) String inptCertKey, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;

		// セッション店舗情報
		Store sesStore = CommonUtil.getSessionStore(ses);
		// 認証を行う場合
		if(sesStore.getIs_cert()) {
			
			// 認証セッションキー
			Object certKeyObj = ses.getAttribute(CERT_KEY);
			// 認証キーが存在する場合
			if(certKeyObj != null) {
				
				// 認証キー取得
				String is_cert = (String)certKeyObj;
				is_cert = is_cert.trim();
				
				// 認証キーが入力値と同じ場合
				if(is_cert.equals(inptCertKey)) {
					// セッション認証キー削除
					ses.removeAttribute(CERT_KEY);
					// 遷移
					return updateDisp(model, ses);
					
				// 認証キーと異なる場合
				}else {
					// トップ画面遷移
					return Const.REDIRECT_HEADER + Const.PAGE_STORE_TOP_CERT_ERROR;
				}
				
			// 認証キーが存在しない(不正アクセスである)場合
			}else {
				// 認証画面遷移
				return Const.REDIRECT_HEADER + Const.PAGE_STORE_SETTING_CERT;
			}
			
		// 認証不要の場合
		}else {
			// 遷移
			return updateDisp(model, ses);
		}
	}
	
	/**
	 * 更新画面表示
	 */
	private String updateDisp(Model model, HttpSession ses) {

		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		
		// モデル
		model.addAttribute("store", store);
		model.addAttribute("is_create", false);
		
		// 遷移
		return Const.PAGE_STORE_CREATE;
	}
	
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/save_store")
	public String update(@ModelAttribute @Validated Store store, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// セッション店舗情報
		Store sesStore = CommonUtil.getSessionStore(ses);

		// (テストコード)割引用IDが存在しない場合は生成
		//if(sesStore.getDiscount_id() == null || sesStore.getDiscount_id().equals("")) store.setDiscount_id(getDiscountIdEnc(store.getId(),store.getStore_name()));
		
		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		
		// IDが変更されている場合
		if(sesStore.getId() != store.getId()) {
			
			// ID上書き
			store.setId(sesStore.getId());
			
			// エラーメッセージ
			error_messages.add("IDが不正に変更されているため更新することができません。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
		// メールアドレスが変更されている場合
		}else if(!sesStore.getEmail().equals(store.getEmail())) {
			
			// メールアドレス上書き
			store.setEmail(sesStore.getEmail());
			
			// エラーメッセージ
			error_messages.add("メールアドレスが不正に変更されているため更新することができません。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
		// ディスカウントIDが存在する場合
		}else if(!CommonUtil.isEmpty(store.getDiscount_target())){
			
			// ディスカウント対象IDが自店舗のディスカウントIDを設定している場合
			if(store.getDiscount_target().equals(store.getDiscount_id())) {
				
				// エラーメッセージ
				error_messages.add("ディスカウント対象IDに自店舗のディスカウントIDを設定することはできません。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
			// ディスカウント対象IDが入力されている & ディスカウント対象店舗が存在しない場合
			}else if(!CommonUtil.isEmpty(store.getDiscount_target()) && 
				!storeService.existDiscount_target(store.getDiscount_target())) {
				
				// エラーメッセージ
				error_messages.add("入力されたディスカウント対象IDは存在しません。正しいディスカウント対象IDを入力して下さい。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
			// エラーでない場合
			}else {
				
				// ディスカウント対象店舗リスト
				List<Store> storeList = storeService.countDiscount_target(store.getDiscount_target());
				// 自店舗を除く
				storeList = storeList.stream().filter(s->s.getId() != store.getId()).collect(Collectors.toList());
				
				// ディスカウント対象店舗が既に2店舗設定されている場合
				if(2 <= storeList.size()) {
					// エラーメッセージ
					error_messages.add("入力されたディスカウント対象IDは、既に別の2つの店舗に利用されているため設定することができません。");
					// リクエスト
					req.setAttribute(Const.MSG_ERROR, error_messages);
					
				// 正常である場合
				}else {
					// 更新
					updating(store, sesStore, req, ses);
				}
			}
			
		// 正常である場合
		}else {
			// 更新
			updating(store, sesStore, req, ses);
		}
		
		// モデル
		model.addAttribute("is_create", false);
		// 遷移
		return Const.PAGE_STORE_CREATE;
	}
	
	/**
	 * 更新
	 */
	private void updating(Store store, Store sesStore, HttpServletRequest req, HttpSession ses) {

		// パスワードが入力されていない場合は、既存のパスワードを設定
		if(CommonUtil.isEmpty(store.getPassword())) store.setPassword(sesStore.getPassword());
		
		// 更新
		sesStore = storeService.save(store);
		// セッション上書き
		CommonUtil.setSessionStore(ses, sesStore);
		
		// メッセージ
		List<String> info_messages = new ArrayList<String>();
		info_messages.add("保存しました。");
		
		// リクエスト
		req.setAttribute(Const.MSG_INFO, info_messages);
	}
	
/* 店舗作成 */

	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/store_create")
	public String dispCreate(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// モデル
		model.addAttribute("store", new Store());
		model.addAttribute("is_create", true);
		
		// 遷移
		return Const.PAGE_STORE_CREATE;
	}

	@TransactionTokenCheck
	@Transactional
	@PostMapping("/create_store")
	public String create(@ModelAttribute @Validated Store store, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// 店舗
		Store srhStore = storeService.contain(store.getEmail());
		
		// 店舗が存在しない場合
		if(srhStore == null) {
			
			// 保存
			store = storeService.save(store);
			// 割引ID上書き
			store.setDiscount_id(getDiscountIdEnc(store.getId(),store.getStore_name()));
			// 保存
			store = storeService.save(store);
			// 認証メール送信
			certUtil.sendCertMail(store.getId(), store.getEmail(), req, false);
			
			// 遷移
			return LoginController.dispLogin(model, req, false);
			
		// 店舗が存在する場合
		}else {

			// 認証前店舗である場合
			if(srhStore.getStatus() == 0) {
				
				// 認証メール送信
				certUtil.resendCertMail(srhStore.getId(), srhStore.getEmail(), req, false);
				// 遷移
				return LoginController.dispLogin(model, req, false);
			
			// 店舗が存在する場合
			}else {
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				error_messages.add("入力されたメールアドレスは、既に別の店舗に使われているため登録することができません。別のメールアドレスを登録してください。");
				
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				model.addAttribute("is_create", true);
				
				// 遷移
				return Const.PAGE_STORE_CREATE;
			}
		}
	}
	
	/**
	 * 認証ID生成
	 */
	private String getDiscountIdEnc(int store_id, String store_name) {
		// 4文字以下なら文字数、5文字以上なら5を設定
		int subsLen = store_name.length() <= 4 ? store_name.length() : 5;
		return Base64.getEncoder().encodeToString((String.valueOf(store_id) + "_" + subsLen).getBytes());
	}
	
/* 認証 */

	/**
	 * 再認証入力
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/store_recert_input")
	public String recertInput(Model model, HttpServletRequest req) throws Exception {
		return certUtil.recertInput(false, model);
	}
	
	/**
	 * 認証再送信
	 */
	@TransactionTokenCheck
	@PostMapping("/store_recert")
	public String recert(@ModelAttribute @Validated ForgetForm forgetForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return certUtil.recert(forgetForm, bindingResult, model, req, false);
	}
	
	/**
	 * ユーザー認証
	 */
	@Transactional
	@GetMapping("/cert_store")
	public String cert(@RequestParam(value="cert",required=true) String cert, Model model, HttpServletRequest req) throws Exception {
		return certUtil.cert(cert, model, req, false);
	}
	
/* 店舗削除 */
	
	/**
	 * 店舗削除
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/delete_store")
	public String deleteStore(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;

		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		store.setIs_delete(true);
		
	/* プラン返金 */
		
		// 店舗のプラン契約一覧
		List<User_plan> user_plans = user_planService.containByStoreId(store.getId());
		// 全契約を次の更新日で停止
		for(User_plan user_plan : user_plans) {
			
			// 返金
			refundPlan(store, true, user_plan, user_plan.getPlan().getName());
		}

		// プラン情報リスト
		List<Plan> plans = planService.find_list(store.getId());
		// プラン情報リスト数分
		for(Plan plan : plans) {
			// プラン論理削除
			planService.delete(plan.getId());
		}
		
	/* オプション返金 */
		
		// 店舗のオプション契約一覧
		List<User_option> user_options = user_optionService.containByStoreId(store.getId());
		// 全契約を次の更新日で停止
		for(User_option user_option : user_options) {
			// 返金
			refundPlan(store, false, user_option, user_option.getOption().getName());
		}

		// オプション情報リスト
		List<Option> options = optionService.find_list(store.getId());
		// オプション情報リスト数分
		for(Option option : options) {
			// オプション論理削除
			optionService.delete(option.getId());
		}
		
	/* 店舗削除 */
		
		// 論理削除
		storeService.save(store);
		// ログイン画面遷移
		return Const.REDIRECT_HEADER + Const.PAGE_DELETED_STORE;
	}
	
	/**
	 * 店舗削除時即時返金
	 */
	private void refundPlan(Store store, boolean isPlan, User_relation user_relation, String planNm) throws StripeException {

		// サブスク停止・終了日取得
		long refundAmount = stripeUtil.refundStoreSubscription(user_relation, isPlan);
		// 返金が存在する場合
		if(0 != refundAmount) {
			// ニュース登録
			int news_id = newsUtil.saveNew_removeStore(isPlan, store.getId(), store.getStore_name(), planNm, refundAmount);
			// ニュースユーザー登録
			newsUtil.saveNews_user(user_relation.getUser_id(), news_id);
			
			// メール通知
			Runnable runnable = () -> {
				mailUtil.sendNews_deleteStore(user_relation.getUser().getEmail(),store.getStore_name(), planNm, refundAmount, isPlan);
			};
			runnable.run();
		}
	}
}
