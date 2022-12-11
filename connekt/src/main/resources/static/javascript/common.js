$(function() {
	
	// 店舗プラン・オプション編集画面 期間初期値
	setIntervalHead();
	
	// スマホの場合はQRキャンバス非表示
	let $preview = $("#img-qr");
	if(isSmartPhone()) $preview.hide();
	else $preview.show();
	
	// サブミット時ローディング表示イベント設定
	$("form").each(function(index, elm) {
		let $fm = $(elm);
		if($fm.prop("target") != "_blank") $fm.attr("onSubmit", $fm.attr("onSubmit") + ";dispLoading();");
	});
});

// ローディング表示
function dispLoading(){
	
	// ローディング要素生成
	let $loading = $("<div>",{"id":"loading"});
	let $spinnerBox = $("<div>",{"class":"spinner-box"});
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
	
	var tmp, video, tmp_ctx, qr, prev, prev_ctx, w, h, m, x1, y1;
	video = document.querySelector('#js-video')
	video.setAttribute("autoplay", "");
	video.setAttribute("muted", "");
	video.setAttribute("playsinline", "");
	video.onloadedmetadata = function(e) { video.play(); };
	prev = document.getElementById("preview");
	prev_ctx = prev.getContext("2d");
	tmp = document.createElement('canvas');
	tmp_ctx = tmp.getContext("2d");
	// 読み取り後の値設定要素
	qr = document.getElementById("qr_read_val");
	//カメラ使用の許可ダイアログが表示される
	navigator.mediaDevices.getUserMedia(
		//マイクはオフ, カメラの設定   できれば背面カメラ    できれば640×480
		{ "audio": false, "video": { "facingMode": "environment", "width": { "ideal": 640 }, "height": { "ideal": 480 } } }
	).then( //許可された場合
		function(stream) {
			//0.2秒後にスキャンする
			video.srcObject = stream;
			// 読み込み中非表示 
			$qr_loading_txt.hide();
			// スマホ用表示
			if(isSmartPhone()) $qr_sp_txt.show();
			// QRスキャン実行
			setTimeout(Scan, 500);
		}
	).catch( //許可されなかった場合
		function(err) { qr.innerHTML = qr.innerHTML + err + '\n'; }
	);
	function Scan() {
		//選択された幅高さ
		w = video.videoWidth;
		h = video.videoHeight;
		//画面上の表示サイズ
		prev.style.width = (w / 2) + "px";
		prev.style.height = (h / 2) + "px";
		//内部のサイズ
		prev.setAttribute("width", w);
		prev.setAttribute("height", h);
		if (w > h) { m = h * 0.5; } else { m = w * 0.5; }
		x1 = (w - m) / 2;
		y1 = (h - m) / 2;
		prev_ctx.drawImage(video, 0, 0, w, h);
		prev_ctx.beginPath();
		prev_ctx.strokeStyle = "rgb(255,0,0)";
		prev_ctx.lineWidth = 2;
		prev_ctx.rect(x1, y1, m, m);
		prev_ctx.stroke();
		tmp.setAttribute("width", m);
		tmp.setAttribute("height", m);
		tmp_ctx.drawImage(prev, x1, y1, m, m, 0, 0, m, m);
		let imageData = tmp_ctx.getImageData(0, 0, m, m);
		let scanResult = jsQR(imageData.data, m, m);
		if (scanResult) {
			//QRコードをスキャンした結果を出力
			qr.value = scanResult.data;
			$("#store_req_fm").submit();
		}else{
			setTimeout(Scan, 10);
		}
	}
}

/**
 スマホかブラウザかを判定
 */
function isSmartPhone() {
	return navigator.userAgent.match(/(iPhone|iPad|iPod|Android)/i);
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
