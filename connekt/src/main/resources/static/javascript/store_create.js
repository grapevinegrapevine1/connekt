$(function() {
	
	var banksList = [];
	var $bankName = $('#bank_name');
	
	var $bankCode = $('input#bank_code');
	var $branchName = $('#bank_branch_name');
	var $branchCode = $('#bank_branch_code');
	
	// 金融機関名変更時
	$bankName.on('change', function(e) {
		changeBank(this);
	});
	
	
	// 金融機関 支店名変更時
	$branchName.on('change', function(e) {
		changeBranch(this);
	});

	// 金融機関データJSONを読み込む
	$.getJSON('/json/banks.json', function(json) {
		banksList = Object.values(json);
		var banksLen = banksList.length;
		for(var i = 0; i < banksLen;i++){
			var bank = banksList[i];
			$bankName.append($("<option>",{'value':bank.name,'text':bank.name}));
		}
		
		$bankName.chosen({
			search_contains: true,
			no_results_text: "ありません"
		});
		
		changeBank($bankName);

	}).fail(function(jqXHR, textStatus, errorThrown) {
		console.log('JSONデータ[banks]の取得に失敗');
	});
	
	// 口座番号を数字以外入力不可とする
	$("#account_number").keydown(function(event) {
		// 入力されたキーのコード
		var keyCode = event.keyCode;
		// キーコードを出力
		if(keyCode == 69 || keyCode == 189)return false;
		else return true;
	});

	function changeBank(elem){
		$branchName.empty();
		let bankName = $(elem).val().toString();
		let bank = banksList.find((data, idx) => {
			return data.name === bankName;
		});
		if (bank != void 0) {
			$bankCode.val(bank.code);
			
			$.getJSON('json/branches/' + bank.code + '.json', function(json) {
				
				let bankBranchList = Object.values(json);
				var branchesLen = bankBranchList.length;
				for(var i = 0; i < branchesLen;i++){
					var branch = bankBranchList[i];
					$branchName.append($("<option>",{'value':branch.code,'text':branch.name}));
				}
				
				$branchName.chosen({
					search_contains: true,
					no_results_text: "ありません"
				});
				$branchName.trigger('chosen:updated');
				
				changeBranch($branchName);
			}).fail(function(jqXHR, textStatus, errorThrown) {
				console.log('JSONデータ[branches]の取得に失敗');
			});
			
		} else {
			$bankCode.val('');
		}

		// 支店情報をクリア
		$branchName.val('');
		$branchCode.val('');
	}
	
	function changeBranch(elem){
		
		let bankBranchCode = $(elem).val().toString();
		
		if (bankBranchCode != void 0) {
			$branchCode.val(bankBranchCode);
		} else {
			$branchCode.val('');
		}
	}
});

// 店舗削除確認
function confirmStoreDelete(){
	return window.confirm("本当に店舗を削除しても良いですか？\n店舗削除後はこの店舗へログインすることができなくなります。\n契約中のプラン・オプションは規約に記載の「契約金額 - (契約金額 / 利用上限 * 利用数)」の計算で顧客へ返金されます。");
}