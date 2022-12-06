$(function() {
	
	// QRリーダー初期設定
	if(!isSmartPhone()) initQrReader();
});

// ハイフン削除
function removeBar(inptElem){
	var $inptElem = $(inptElem);
	$inptElem.val($inptElem.val().replace(/[^0-9]/g, ''));
}

// サブミットボタンによる実行処理変更フラグ更新
function cngIsPlanCnt(isPlanCnt){
	$("#is_plan_count").val(isPlanCnt);
}

// 入力値のTrim
function trimInpt(elem){
	var $inptTrim = $(elem);
	$inptTrim.val($inptTrim.val().replace(/\s+/g, ""));
}