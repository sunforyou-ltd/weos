/*------------------------------------------*/
/*----- 【WeoS】売上伝票ファイル作成画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	  //イベント登録
	  $("#excel-upload").upload();
    $("#excel-download").download();
  });

})(jQuery);

/*------------------------------------------*/
/*----- 取込ボタンクリック             -----*/
/*------------------------------------------*/
$.fn.upload = function() {
  $(this).click(function() {

    //----- ファイル指定チェック -----
    if ($("#input-file-now").val() == undefined || $("#input-file-now").val() == "") {
      Materialize.toast('アップロードするファイルが選択されていません。', 3000, 'rounded');
      return false;
    }
    //----- 確認 -----
    swal({title: "確認",
      text: $("#input-file-now").val() + "を<br />製品マスタとして取り込みますがよろしいですか？",
      type: "warning",
      showCancelButton: true,
      confirmButtonColor: "#DD6B55",
      confirmButtonText: "はい",
      cancelButtonText: "いいえ",
      html: true,
      closeOnConfirm: true },
      function(){

        var errors = $("#error-list");
        errors.empty();

        var formData = new FormData();
        formData.append('status', "UPLOAD");
        formData.append('excel', $("#input-file-now")[0].files[0]);
        var url = pUrl + '/seihinupload';
        $.ajax({
          url: url,
          type: 'POST',
          contentType: false,
          processData: false,
          dataType:'json',
          data: formData,
          complete:function(data, status, jqXHR){         //処理成功時
            var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
            if (jsonResult.result == "success") {
              Materialize.toast('製品マスタExcelのアップロードが完了しました。', 3000, 'rounded');
              return;
            }
            else if (jsonResult.result == "nonfoldaerror") {
              Materialize.toast('アップロード用のフォルダが存在しません', 3000, 'rounded');
              return;
            }
            else {
              var errorlist = jsonResult.error;
              for (var key in errorlist) {
                var data = errorlist[key];
                errors.append('<li class="collection-item">' + data.message + '</li>');
              }
              Materialize.toast('製品マスタExcel出力に失敗しました。', 3000, 'rounded');
            }
          },
            async: false
          });
        });
  });
}
/*------------------------------------------*/
/*----- 出力ボタンクリック             -----*/
/*------------------------------------------*/
$.fn.download = function() {
  $(this).click(function() {
    $.ajax({
    url: pUrl + '/seihindownload',
    type: 'GET',
    complete:function(data, status, jqXHR){					//処理成功時
      var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
      if (jsonResult.result == "success") {
        window.location = pUrl + '/exceldownload';
        Materialize.toast('製品マスタExcelのダウンロードが完了しました。', 3000, 'rounded');
        return;
      }
      else {
        Materialize.toast('製品マスタExcel出力に失敗しました。', 3000, 'rounded');
      }
    },
    dataType:'json',
    contentType:'text/json',
    	async: false
    });
  });
}