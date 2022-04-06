/*------------------------------------------*/
/*----- 【WeoS】ログイン画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- ログイン認証                   -----*/
  /*------------------------------------------*/
  $.fn.eventlogin = function() {
    $(this).click(function() {
    	var userid = new String($("#userid").val());
    	var password = new String($("#password").val());

    	//エラーテキストクリア
    	ContorolInfo.clearError();
    	//必須チェック
    	if (!ContorolInfo.checkRequired("0000")) {
    		return;
    	}
    	//単項目チェック
    	if (!ContorolInfo.singleCheck(0)) {
    		return;
    	}

        var json = {"userid":userid, "password":password};

        $.ajax({
          url: pUrl + '/login',
          type: 'POST',
          data: JSON.stringify(json),
          complete:function(data, status, jqXHR){					//処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
        	if (jsonResult.result == "usernotfound") {
    			Materialize.toast('入力されたユーザＩＤは存在しません', 4000, 'rounded');
    			return;
        	}
        	if (jsonResult.result == "passworderror") {
    			Materialize.toast('パスワードが一致しません', 4000, 'rounded');
    			return;
        	}
        	window.location = pUrl + "/movemenu";
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
	});
  }
  /*------------------------------------------*/
  /*----- 項目制御情報生成               -----*/
  /*------------------------------------------*/
  function controlInit() {
	  ContorolInfo();							//項目制御オブジェクトの生成
	  ContorolInfo.add({"info":0 , "item":"userid", control:{"0000":{"disabled":false, "required":true}}, type:"num"});
	  ContorolInfo.add({"info":0 , "item":"password", control:{"0000":{"disabled":false, "required":true}}, type:"alnum"});
	  ContorolInfo.set(0, "0000");				//制御情報の反映
  }
  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	//項目制御情報の生成
	controlInit();
	//項目の初期化を行う
	$("#userid").val("");
	$("#password").val("");
	//各イベントトリガーを設定する
    $("#loginBtn").eventlogin();
    $("#userid").select();
    enterEvent($("#password"), $("#loginBtn"));
  });

})(jQuery);
