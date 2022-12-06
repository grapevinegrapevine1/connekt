$(function() {
	
	// QR生成
	createQr($("#qr_code"), qrtext);
});

function toggleQr(){
	$("#qr").toggle();
}