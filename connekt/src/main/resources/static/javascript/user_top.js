$(function() {
	// ログイン時セッション店舗IDが存在する場合
	if(!isEmpty(login_store_id)){
		// 分割
		const spParam = login_store_id.split("_");
		// 確認
		if(window.confirm("「"+ spParam[1] + "」店のプラン一覧ページを表示しますか？")){
			
			// ローディング表示
			dispLoading();
			// 画面遷移
			getForm("search_store",{"store_id" : spParam[0]})
		}
	}
	
	// QR生成
	createQr($("#qr_code"), getStoreUserUrl(qrtext));
});

function toggleQr(){
	$("#qr").toggle();
}