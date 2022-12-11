package com.properties;

public class Const {
	
	public static final int TEST_STORE_ID = 2;
	public static final String TEST_EMAIL = "grapevinegrapevine1@gmail.com";
	public static final String TEST_INTERVAL = "day";
	
	public static final String MAIL_FROM= "baken@baken.sakura.ne.jp";
	public static final String ENCRYPT_KEY = "j8~4HF=sd_yd6%Yd-04";
	
	//public static final String STRIPE_API_KEY = "sk_test_51KaJJABbtACSed8BGSinRBBCle47g0rVuq5e1QhwpKNYSUw3JvmItz2c9v05Gr2ivkGNogCVx27TaJNeLuVXbzTZ00w41RynR9";
	public static final String STRIPE_API_KEY = "sk_test_51LcA2yCKmNuieiIYDdw0XCQPTqgBRZN1XqKMjAcluYd7bKElBwcJwOuC3UcownoqhrFXcNOCiwR0IpJh39GjT45c00eH8dWXJ1";
	// サブスク親プラン(商品追加→0円で作成)
	public static final String STRIPE_PROC_ID = "prod_MLCmVdDEX2PxIy";
	// ログイン画面 選択タブ
	public static final String TAB_USER = "1";
	public static final String TAB_STORE = "2";
	// 削除済ユーザーメールアドレスヘッダー
	public static final String USER_DELETED_EMAIL = "@DELETED_";
	// 削除可否
	public static final int IS_DELETED = 1;
	// Connekt手数料
	public static final double COMMISSION = 0.034;
	// Stripe手数料
	public static final double COMMISSION_STRIPE = 0.036;
	
	// 請求データ取得件数
	public static final int MAX_SALES_COUNT = 10;
	
	public static final int USER_STATUS_DEAULT = 0;
	public static final int USER_STATUS_SET_CARD = 1;
	public static final int USER_STATUS_CERT_START = 2;
	public static final int USER_STATUS_CERT_END = 3;
	public static final int USER_STATUS_DELETED = 4;

	public static final int PLAN_UPDATE_TYPE_STOP = 0;
	public static final int PLAN_UPDATE_TYPE_RESTART = 1;
	public static final int PLAN_UPDATE_TYPE_REFUND = 2;
	
	public static final String IS_USER_FORM_NM = "is_user_form";
	public static final String IS_USER_FORM = "1";

	public static final String SESSION_USER = "session_user";
	public static final String SESSION_STORE = "session_store";

	public static final String PAGE_LOGIN = "login";
	public static final String PAGE_COMMON_FORGET = "common_forget";
	public static final String PAGE_COMMON_CERT_ERROR = "common_cert_error";
	public static final String PAGE_COMMON_CERT_COMPLETE = "common_cert_complete";
	public static final String PAGE_COMMON_RECERT = "common_recert";
	public static final String PAGE_USER_TOP = "user_top";
	public static final String PAGE_USER_PAY_ERROR = "user_pay_error";
	public static final String PAGE_USER_SETTING = "user_setting";
	public static final String PAGE_USER_LIST = "user_list";
	public static final String PAGE_USER_STORE = "user_store";
	public static final String PAGE_USER_SALES = "user_sales";
	public static final String PAGE_USER_TRANS_ERROR = "user_trans_error";
	public static final String PAGE_USER_VALID_ERROR = "user_valid_error";
	public static final String PAGE_USER_FORGET_SEND = "forget_send_user";
	public static final String PAGE_STORE_TOP = "store_top";
	public static final String PAGE_STORE_SETTING_CERT = "store_setting_cert";
	public static final String PAGE_STORE_TOP_CERT_ERROR = "store_top_cert_err";
	public static final String PAGE_STORE_CREATE = "store_create";
	public static final String PAGE_STORE_SETTING = "store_setting";
	public static final String PAGE_STORE_PlAN = "store_plan";
	public static final String PAGE_STORE_PlAN_EDIT = "store_plan_edit";
	public static final String PAGE_STORE_OPTION_EDIT = "store_option_edit";
	public static final String PAGE_STORE_READ = "store_read";
	public static final String PAGE_STORE_REQUEST = "store_request";
	public static final String PAGE_STORE_SALES = "store_sales";
	public static final String PAGE_STORE_TRANS_ERROR = "store_trans_error";
	public static final String PAGE_STORE_VALID_ERROR = "store_valid_error";
	public static final String PAGE_STORE_FORGET_SEND = "forget_send_store";
	public static final String PAGE_404 = "404";
	public static final String PAGE_SESSION_ERROR = "session_error";
	public static final String PAGE_DELETED_STORE ="deleted_store";
	public static final String PAGE_STRIPE_CONNECTION_ERROR = "stripe_connection_error";
	
	public static final String MSG_ERROR = "error_messages";
	public static final String MSG_INFO = "info_messages";
	
	public static final String REDIRECT_HEADER = "redirect:";
	
	public static final String NON_APP_USER_PASSWORD = "baken_pw_8420";
	
	public static final String NL = "\n";

	public static final String SESSION_STORE_ID = "store_id";
}
