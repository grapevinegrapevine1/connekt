$(function() {
	
	// 対象タブ切り替え
	if($("#is_user_form").val() == "2") $("#cng_btn_store").click();
});


// ログインボタン押下時非活性制御
function disableLoginBtn(){
	let $loginBtn = $("#login_btn");
	$loginBtn.prop("disabled",true);
	$loginBtn.text("ログイン中")
}

// フォームの切り替え
function changeForm(isUserForm){
	
	// フラグ更新
	$("#is_user_form").val(isUserForm);
	
	// ユーザータブボタン
	var $cng_btn_user = $("#cng_btn_user");
	// 店舗タブボタン
	var $cng_btn_store = $("#cng_btn_store");
	// ユーザー新規登録ボタン
	var $user_form = $("#user_form");
	// 店舗新規登録ボタン
	var $store_form = $("#store_form");
	// フォームエリア
	var $user_area = $("#user_area");
	// ﾊﾟｽﾜｰﾄﾞ忘れリンク
	var $user_forget = $("#user_forget");
	var $store_forget = $("#store_forget");
	// 認証再発行
	var $user_recert = $("#user_recert");
	var $store_recert = $("#store_recert");
	
	// ユーザー用選択時である場合
	if(isUserForm == 1){
		// タブ
		$cng_btn_user.addClass("focus_tab_user");
		$cng_btn_store.removeClass("focus_tab_store");
		// 背景
		$user_area.css("background","white");
		// 新規登録ボタン
		$user_form.show();
		$store_form.hide();
		// ﾊﾟｽﾜｰﾄﾞ忘れリンク
		$user_forget.show();
		$store_forget.hide();
		// 認証再発行
		$user_recert.show();
		$store_recert.hide();
		
	// 店舗用選択時である場合
	}else{
		// タブ
		$cng_btn_user.removeClass("focus_tab_user");
		$cng_btn_store.addClass("focus_tab_store");
		// 背景
		$user_area.css("background","#f3f3f3");
		// 新規登録ボタン
		$user_form.hide();
		$store_form.show();
		// ﾊﾟｽﾜｰﾄﾞ忘れリンク
		$user_forget.hide();
		$store_forget.show();
		// 認証再発行
		$user_recert.hide();
		$store_recert.show();
	}
	
	// メールアドレス入力欄にフォーカス
	$("#email").focus();
}