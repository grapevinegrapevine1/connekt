$(function() {
	
	// QR生成
	createQr($("#qr_code"), qrtext);
});

function toggleQr(){
	$("#qr").toggle();
}

function copy_discountId(btn){

	// コピーする文章の取得
	let text = $('#copyTarget').text();
	// テキストエリアの作成
	let $textarea = $('<textarea></textarea>');
	// テキストエリアに文章を挿入
	$textarea.text(text);
	//　テキストエリアを挿入
	$(btn).append($textarea);
	//　テキストエリアを選択
	$textarea.select();
	// コピー
	document.execCommand('copy');
	// テキストエリアの削除
	$textarea.remove();
	
	alert("ディスカウントIDをコピーしました。\n他店舗の店舗設定画面にある、ディスカウント対象ID入力欄へ貼り付けて保存してください。");
}