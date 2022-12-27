// ラジオボタン
var $radio = null;

// 説明モーダル開閉
function toggleDescription(){
	// 説明モーダル
	var $modal_desc_area = $("#modal_desc_area");
	// モーダル開閉
	$modal_desc_area.toggle();
	// 非表示時は文章削除
	if($modal_desc_area.css("display") == "none"){
		$("#modal_desc").text("");
		$("#modal_desc_title").text("");
		$radio = null;
	}
}

// 説明モーダルへ文章設定
function setDescText(btnElem, isPlan, relation_id, isRefund){
	
	// 詳細ボタン
	var $btnElem = $(btnElem);
	// hidden説明テキスト
	var $desc = $btnElem.parent().children(".desc");
	var $descTitle = $btnElem.parent().children(".desc_title");
	// ラジオボタン
	$radio = $btnElem.parent().parent().children(".plan_summary").children(".inptPlan");
	
	// 返品可否
	var $is_refund = $btnElem.parent().children(".is_refund");
	// 返品用フォーム
	var $refund_form = $("#refund_form");
	// 返品用プラン/オプションID
	var $refund_relId = $refund_form.children("input[name='relation_id']");
	// 返品用プラン/オプション可否
	var $refund_isPlan = $refund_form.children("input[name='is_plan']");
	
	// 返品可能である場合
	if($is_refund.val() == "true"){
		// 返品情報設定
		$refund_form.show();
		$refund_relId.val(relation_id);
		$refund_isPlan.val(isPlan);
	// 返品不可である場合
	}else{
		// 返品情報初期化
		$refund_form.hide();
		$refund_relId.val("");
		$refund_isPlan.val("");
	}
	
	// モーダルへ説明設定
	$("#modal_desc").text($desc.text());
	$("#modal_desc_title").text($descTitle.val());
}

// ラジオボタン、またはチェックボックスを選択
function selectModalPlan(){
	// ラジオボタン、またはチェックボックスを選択
	$radio.prop("checked", true);
}

// 保存時確認ダイアログ
function confSave(){
	return window.confirm("保存してよろしいですか？\n※プランを変更する際に、解除されたプランは利用停止されますが利用期間が残っている場合も返金は行われません。\n");
}

// 停止ボタンサブミット
function submitStopBtn(relation_id, is_plan, is_stop){
	
	// 値設定
	$("#relation_id").val(relation_id);
	$("#is_plan").val(is_plan);
	$("#is_stop").val(is_stop);
	
	// サブミット
	var $form = $("#form_stop_plan");
	$form.submit();
}

// 次回更新停止前の確認
function confirmStop(){
	var isStop = $("#is_stop").val() == "true";
	return isStop ? window.confirm("このプランを次回更新を停止してもよろしいですか？\n次回の更新を停止した後も、プラン停止日までプランを利用することができます。\nプランの更新日は「契約中プランの詳細」欄で確認できます。\n(実行すると画面が更新されます)") : 
					window.confirm("このプランを次回更新を再開してもよろしいですか？");
}

// 返品前の確認
function confirmRefund(){
	return window.confirm("このプランを返品してもよろしいですか？\n手数料を除いた全ての金額が返金されます。");
}