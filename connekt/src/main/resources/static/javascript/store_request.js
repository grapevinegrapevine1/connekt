function confirmProcedure(){
	
	// 選択プラン
	var $checkedPlan = $(".radio_plan:checked");
	// 選択オプション
	var $checkedOption = $(".checkbox_option:checked");
	
	// プランテキスト生成
	let text = getText($checkedPlan);
	// オプションテキスト生成
	$checkedOption.each(function(index, elem) {
		text += getText(elem);
	});
	
	// テキストが存在する場合は確認アラート表示
	if(text != "") return window.confirm("手続きを行ってよろしいですか？\n選択した以下プランをご確認いただき、利用プランにお間違いないこと確認の上、お手続きを行って下さい。\n\n" + text);
	// テキストが存在しない場合
	else{
		// エラーメッセージ
		alert("利用するプランが選択されていません。\n利用するプランを選択した後、お手続きを行ってください。");
		// 処理終了
		return false;
	}
	
	// テキスト取得
	function getText(elem){
		
		// 要素
		var $elem = $(elem);
		// 要素が存在する場合
		if(0 < $elem.length){
			
			// 親要素
			var $parent = $elem.parent();
			// テキスト要素
			var plan_name = $parent.children(".plan_name").text();
			var plan_price = $parent.children(".plan_price").text();
			var plan_count_text = $parent.children(".plan_count_text").text();
			// 返却
			return plan_name + plan_price + plan_count_text + "\n";
			
		// 要素が存在しない場合は空文字返却
		}return "";
	}
}