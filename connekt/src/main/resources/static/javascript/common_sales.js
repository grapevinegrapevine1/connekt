$(function() {
	
	// 追加データ読み込みイベント設定 - 請求一覧
	setScrollEvent("last_ch", "chargeBody", true);
	// 追加データ読み込みイベント設定 - 返金一覧
	setScrollEvent("last_re", "refundBody", false);
	
	// 追加データ読み込みイベント設定
	function setScrollEvent(lastSelector, tableId, isCharge){
		
		// 表示データ最終支払情報
		let $last = $("#" + lastSelector);
		// 一覧スクロールイベント
		let $scrollBody = $("#" + tableId);
		$scrollBody.on('scroll', function() {
			// 最下部までスクロールした && 次のデータが存在する場合
			if ($scrollBody.scrollTop() + $scrollBody.innerHeight() >= $scrollBody[0].scrollHeight && !isEmpty($last.val()) ) {
				// ロード画像表示
				dispLoading();
				// 最後の支払情報
				let lastVal = $last.val();
				$last.val("");
				
				// POStデータ
				let postData = {"start_date_str":$("#start_date_str").val(),
								"end_date_str":$("#end_date_str").val()};
				// 頁最終データID
				if(isCharge) postData.last_ch = lastVal;
				else postData.last_re = lastVal;
				
				let ajaxUrlHeader = $("#ajax_url_header").val();
				
				// Ａjax
				$.ajax({
					// 非同期
					async : true ,
					// POST送信
					type : "POST",
					// POSTデータ型
					contentType: 'application/json',
					// リクエストマッピング
					url : isCharge ? ajaxUrlHeader + "_sales_search_ajax" : ajaxUrlHeader + "_refund_search_ajax" ,
					// シリアライズ
					traditional: true,
					// 値
					data : JSON.stringify(postData)
					
				// 処理完了時イベント
				}).done(function( ares ) {
					// エラーでない場合
					if(ares != "error"){
						// 支払情報リスト
						let resList = ares.list;
						// テーブル
						let $tbl = $scrollBody.children("table");
						// 行追加
						$.each(resList, function(i, obj){
							let $tr = $("<tr>");
							$tr.append($("<td>",{"text":formatDate(new Date(obj.use_date))}));
							$tr.append($("<td>",{"text":'￥' + obj.price}));
							$tr.append($("<td>",{"text":obj.name}));
							$tr.append($("<td>",{"text":obj.user_name}));
							$tr.append($("<td>",{"text":obj.email}));
							$tbl.append($tr);
						});
						// 次の支払情報が存在する場合、支払情報ID設定
						if(ares.last) $last.val(ares.last);
					// エラーである場合
					}else{
						// エラーメッセージ表示
						alert("通信に失敗しました。検索欄に入力されている値が不正であるか、セッションが切れている場合は再度ログインしてください。");
					}
					
				// エラー時イベント
				}).fail(function() {
					// エラーメッセージ表示
					alert("通信に失敗しました。セッションが切れている可能性があるため、再度ログインした後に実行して下さい。");
					
				// 処理終了時イベント
				}).always( function() {
					// ローディング画像
					removeLoading();
				});
			}
		})
	}
});