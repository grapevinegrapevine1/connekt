$(function() {
	
	// 店舗プラン・オプション編集画面 期間初期値
	setIntervalHead();
	
	// スマホの場合はQRキャンバス非表示
	let $preview = $("#img-qr");
	if(isSmartPhone()) $preview.hide();
	else $preview.show();
	
	// サブミットボタンのクリックイベント
	$("button[type='submit'],input[type='submit']").click(function(e) {
		// Ctrl + Clickである場合
		if (e.ctrlKey) {
			// イベント中断
			e.preventDefault();
		}
	});
	
	// サブミット時ローディング表示イベント設定
	$("form").each(function(index, elm) {
		let $fm = $(elm);
		if($fm.prop("target") != "_blank") $fm.attr("onSubmit", $fm.attr("onSubmit") + ";dispLoading();");
	});
	
	// 数値入力欄の最大、最小チェック
	$("input[type='number'][max!='']").on('input', function() {
		validNumberMax(this, $(this).prop("max"));
	});
	$("input[type='number'][min!='']").on('input', function() {
		validNumberMin(this, $(this).prop("min"));
	});
	
	// Top画面遷移アイコン生成
	const topIconUrl = isSessionUser ? "user_top" : isSessionStore ? "store_top" : null;
	if(topIconUrl) $("#header").append($("<button>",{"id":"top_icon", "onclick":"getForm('"+topIconUrl+"')"}));
});

// 数値入力欄の最大桁数制御
function sliceMaxLength(elem, maxLength) {
	elem.value = elem.value.slice(0, maxLength);
}
// 数値入力欄の最小値制御
function validNumberMin(elem, min) {
	if(parseInt(elem.value) < parseInt(min)) elem.value = min;
}
// 数値入力欄の最大値制御
function validNumberMax(elem, max) {
	if(parseInt(max) < parseInt(elem.value)) elem.value = max;
}

// ローディング表示
function dispLoading(){
	
	// ローディング要素生成
	let $loading = $("<div>", { "id": "loading" });
	let $spinnerBox = $("<div>", { "class": "spinner-box" });
	$loading.append($spinnerBox);
	$spinnerBox.append($("<div>",{"class":"blue-orbit leo"}));
	$spinnerBox.append($("<div>",{"class":"green-orbit leo"}));
	$spinnerBox.append($("<div>",{"class":"red-orbit leo"}));
	$spinnerBox.append($("<div>",{"class":"white-orbit w1 leo"}));
	$spinnerBox.append($("<div>",{"class":"white-orbit w2 leo"}));
	$spinnerBox.append($("<div>",{"class":"white-orbit w3 leo"}));
	// 画面表示
	$("body").append($loading);
}
function removeLoading(){
	$("#loading").remove();
}

// 店舗プラン・オプション編集画面 期間値更新
function setIntervalHead(){
	$("#interval_head").val($("#planInterval").val());
}

// 空チェック
function isEmpty(val){
	return val == null || val == "";
}

// パスワード確認
function checkPassword(){
	
	// パスワード
	var $pw = $("#pw");
	// パスワード確認用
	var $pw_temp = $("#pw_temp");
	
	// パスワードが入力されている場合
	if(!isEmpty($pw.val())){
		// パスワードが異なる場合は、エラーメッセージ表示
		if($pw.val() != $pw_temp.val()){
			alert("パスワードとパスワード(確認用)に入力された値が一致しておりません。\nパスワードとパスワード(確認用)の値は一致させてください。");
			return false;
		}else return true;
	}
}

// ユーザー店舗画面アクセスパス取得
function getUserStoreUrl(storeId){
	return window.location.origin + "/login_with_storeId?store_id=" + storeId;
}
// 店舗ユーザー画面アクセスパス取得
function getStoreUserUrl(userId){
	return window.location.origin + "/login_with_userId?user_id=" + userId;
}

// QR生成
function createQr($img, qrtext){
	
	// QRコード生成
	var utf8qrtext = unescape(encodeURIComponent(qrtext));
	$img.html("");
	$img.qrcode({ text: utf8qrtext });
}

// 日付をYYYY-MM-DDの書式で返すメソッド
function formatDate(date) {
	return date.getFullYear() + '/' + ('0' + (date.getMonth() + 1)).slice(-2) + '/' +('0' + date.getDate()).slice(-2) + ' ' +  ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2) + ':' + ('0' + date.getSeconds()).slice(-2);
}

// プランテキストモーダル表示
function togglePlanModal(btnElem){
	
	var $btnElem = $(btnElem);
	var $title = $btnElem.parent().children(".planModalTitle");
	var $text = $btnElem.parent().children(".contract_text");

	var $modalTitle = $("#modal_title");
	var $modalArea = $("#modal_area");
	var $modal_text = $("#modal_text");

	$modalArea.toggle();

	if ($modalArea.css("display") == "none"){
		
		$modalTitle.text("");
		$modal_text.text("");
		
	}else{
		
		$modalTitle.text($title.val());
		$modal_text.text($text.val());
	}
}

function logout(){
	return window.confirm("ログアウトしてよろしいでしょうか？");
}

/**
 * 指定されたurlへGET遷移
 * @param url GET先
 * @param params POSTパラメータの連想配列
 */
function getForm(url, params) { submitUrl(url, params, 'get') };
function getFormWithClickOption(url, params) { submitUrl(url, params, 'get', true) };
function postFormWithClickOption(url, params) { submitUrl(url, params, 'post', true) };
function getLogoutForm() {
	// 処理中である場合
	if(isProcessing){
		// アラート表示
		alert("データ更新中であるため、処理終了までログアウトすることは出来ません")
	// 処理中でない場合
	}else{
		//Cookieの削除
		cookieDeleteSection();
		// ログアウト実行
		submitUrl('logout.qc', null, 'get');
	}
}

/**
 * 指定されたurlへPOST遷移
 * @param url POST先
 * @param params POSTパラメータの連想配列
 */
function postForm(url, params) { submitUrl(url, params, 'post') };

/**
 * URLのFormサブミットを行う
 * @param url URL
 * @param params パラメータ配列
 * @param method メソッド(post/get)
 * @param withClickOption クリックオプション許容可否
 */
function submitUrl(url, params, method, withClickOption){
	
	// Form生成
	var $form = $('<form/>', {'action': url, 'method': method});
	// オプション実行可否
	var doOptionOpen = false;
	
	// クリックオプションが許可されている場合
	if(withClickOption != null){
		
		// Ctrl が押下されている場合
		if(window.event.ctrlKey){
			// Tab生成
			getOpenTab(url, params);
			// オプション実行可否設定
			doOptionOpen = true;
			
		// Shift が押下されている場合
		}else if(window.event.shiftKey){
			// Window生成
			getOpenWindow(url, params);
			// オプション実行可否設定
			doOptionOpen = true;
		}
	}
		
	// オプションでのwindow表示を行っていない(通常サブミットを行う)場合
	if(!doOptionOpen){
		
		// サブミットパラメータの追加
		addSubmitParams($form, params);
		// body配下に追加
		$form.appendTo(document.body);
		// サブミット
		$form.submit();
	}
}
/* ------------------------------------------------------------------------------------ */

// QRリーダー
function initQrReader(){
	// 読み込み中表示
	let $qr_loading_txt = $("#qr_loading_txt");
	$qr_loading_txt.show();
	// スマホ用表示
	let $qr_sp_txt = $("#qr_sp_txt");
	if(isSmartPhone()) $qr_sp_txt.hide();
	
	// Videoタグ
	let videoElm = document.getElementById('js-video');
	const args = { video: videoElm , mirror: false};
	// 設定
	window.URL.createObjectURL = (stream) => {
		args.video.srcObject = stream;
		return stream;
	};
	// リーダー
	const scanner = new Instascan.Scanner(args);
	// 読み取り後の値設定要素
	const qr = document.getElementById("qr_read_val");
	// スキャンイベント
	scanner.addListener('scan', function(content) {
		if(!isEmpty(content)){
			const urlData = isUrl(content);
			if(urlData.searchParams.get("store_id")){
				qr.value = urlData.searchParams.get("store_id");
			}else{
				qr.value = urlData.searchParams.get("user_id");
			}
			$("#store_req_fm").submit();
		}
	});
	// カメラ起動
	Instascan.Camera.getCameras().then(function(cameras) {
		// カメラが存在する場合はカメラ起動
		if (cameras.length > 0){
			scanner.start(cameras[0]);
			// 読み込み中非表示 
			$qr_loading_txt.hide();
			// スマホ用表示
			if(isSmartPhone()) $qr_sp_txt.show();
		}else alert('カメラが見つかりません。');
	}).catch(function(e) {
		alert("QRコードリーダー起動時エラー：" + e);
	});
}

/**
 ブラウザであるかを判定
 */
function isBrowser(){
	let res = false;
	const agent = window.navigator.userAgent.toLowerCase()
	if (agent.indexOf("msie") != -1 || agent.indexOf("trident") != -1 ||
		agent.indexOf("edg") != -1 || agent.indexOf("edge") != -1 ||
		agent.indexOf("opr") != -1 || agent.indexOf("opera") != -1 ||
		agent.indexOf("chrome") != -1 ||
		agent.indexOf("safari") != -1 ||
		agent.indexOf("firefox") != -1 ||
		agent.indexOf("opr") != -1 || agent.indexOf("opera") != -1) {
		res = true;
	}
	return res;
}

/**
 URLであるか判定
 */
function isUrl(url) {
	try {
		const urlData = new URL(url);
		return urlData;
	} catch (e) {
		return false;
	}
}

/**
 スマホかブラウザかを判定
 */
function isSmartPhone() {
	return navigator.userAgent.match(/(iPhone|iPad|iPod|Android)/i);
}


/**
 出力確認
 */
function confExport() {

	if (isEmpty($("#_start_date_str").val())) {
		alert("「請求期間選択」の開始日を入力してください");
		return false;
	}else{
		return window.confirm("請求書は「請求期間選択」の開始日で選択された月の請求を出力します。\nまた、出力処理に時間がかかる場合があります\n請求書の出力を実行してもよろしいでしょうか？");
	}
}
/* ------------------------------------------------------------------------------------ */

/**
 * 指定されたurlへGET遷移
 * @param url GET先
 * @param params POSTパラメータの連想配列
 */
function getForm(url, params) { submitUrl(url, params, 'get') };
function getFormWithClickOption(url, params) { submitUrl(url, params, 'get', true) };
function postFormWithClickOption(url, params) { submitUrl(url, params, 'post', true) };

/**
 * 指定されたurlへPOST遷移
 * @param url POST先
 * @param params POSTパラメータの連想配列
 */
function postForm(url, params) { submitUrl(url, params, 'post') };

/**
 * URLのFormサブミットを行う
 * @param url URL
 * @param params パラメータ配列
 * @param method メソッド(post/get)
 * @param withClickOption クリックオプション許容可否
 */
function submitUrl(url, params, method, withClickOption){
	
	// Form生成
	var $form = $('<form/>', {'action': url, 'method': method});
	// オプション実行可否
	var doOptionOpen = false;
	
	// クリックオプションが許可されている場合
	if(withClickOption != null){
		
		// Ctrl が押下されている場合
		if(window.event.ctrlKey){
			// Tab生成
			getOpenTab(url, params);
			// オプション実行可否設定
			doOptionOpen = true;
			
		// Shift が押下されている場合
		}else if(window.event.shiftKey){
			// Window生成
			getOpenWindow(url, params);
			// オプション実行可否設定
			doOptionOpen = true;
		}
	}
		
	// オプションでのwindow表示を行っていない(通常サブミットを行う)場合
	if(!doOptionOpen){
		
		// サブミットパラメータの追加
		addSubmitParams($form, params);
		// body配下に追加
		$form.appendTo(document.body);
		// サブミット
		$form.submit();
	}
}

/**
 * 別ウインドウ・タブ生成
 * @param url URL
 * @param params パラメータ
 * @param style スタイル
 */
function getOpenWindow(url, params, style){ openWindow(url, params, style, 'get') }
function postOpenWindow(url, params, style){ openWindow(url, params, style, 'post') }
function getOpenTab(url, params, style){ openTab(url, params, style, 'get') }
function postOpenTab(url, params, style){ openTab(url, params, style, 'post') }
function getOpenNewTab(url, params, style, target){ openNewWindow(url, params, style, 'get', target) }
/**
 * 別ウインドウ・タブ生成
 */
function openWindow(url, params, style, method){ openNewWindow(url, params, style, method, "_newWindow") }
function openTab(url, params, style, method){ openNewWindow(url, params, style, method, "_blank") }

/**
 * 別ウインドウ・タブ生成
 * @param url URL
 * @param params パラメータ
 * @param style スタイル
 * @param method post/get
 * @param target ターゲット属性
 */
function openNewWindow(url, params, style, method, target){
	
	// 新規ウインドウ生成可否
	var isNewWindow = target == "_newWindow";
	
	// スタイルがnullである場合は、ウインドウ幅のみを指定 (ウインドウ幅の指定が無いと別タブ生成になります)
	if(style == null && isNewWindow) style = openDefaultWindow();
	
	// form生成
	var $form = $('<form/>', {'action': url, 'method': method, 'target':target});
	
	// サブミットパラメータの追加
	addSubmitParams($form, params);
	// window open 
	if(isNewWindow) addParamToForm($form, 'window_open', 'window_open')
	
	// GETリクエストである場合
	if(method == "get" || method == "GET"){
		
		// URL末尾の?削除
		url = (url + "?" + $form.serialize()).replace(/\?$/g,"");
		// window生成
		// 新規ウィンドウ生成の場合は、スタイル属性を指定
		if(isNewWindow) window.open(url , target, style);
		// タブ生成の場合は、スタイル属性は指定しない
		else window.open(url , target);
		
	// POSTリクエストである場合
	}else{
		// body取得
		var body = document.getElementsByTagName("body")[0];
		// bodyへformを追加
		body.appendChild($form[0]);
		// サブミット
		$form.submit();
		// formを削除
		body.removeChild($form[0]);
	}
	
}

/**
 * サブミットパラメータの追加
 * @param $form フォーム
 * @param params パラメータ
 */
function addSubmitParams($form, params){
	
	// nullでない場合
	if(params != null){
		
		// パラメータ数分繰り返す
		for(var key in params) {
			
			// パラメータをFormに追加
			addParamToForm($form, key, params[key]);
		}
	}
}

/**
 * パラメータをFormに追加
 * @param $form フォーム
 * @param key 設定キー
 * @param value 設定値
 */
function addParamToForm($form, key, value){
	
	// パラメータをFormに追加
	$form.append($('<input/>', {'type': 'hidden', 'name': key, 'value': value}));
}
/* ------------------------------------------------------------------------------------ */
