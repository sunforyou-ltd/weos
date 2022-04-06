/*------------------------------------------*/
/*----- 【WeoS】ログイン画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- ログインの入力チェック         -----*/
  /*------------------------------------------*/
  $("#testform").validate({
    rules: {
      "mtmno":{
        required: true,
    	  maxlength: 10,
    	  minlength: 10,
    	  digits: true
      },
      "edaban":{
        required: true,
    	  maxlength: 2,
        digits: true
      }
  },
  messages: {
    "mtmno":{
      required: "必須入力です",
      maxlength: "10桁で入力してください",
      minlength: "10桁で入力してください",
      digits: "数値で入力してください"
    },
  	"edaban":{
      required: "必須入力です",
  	  maxlength: "2桁以下で入力してください",
      digits: "数値で入力してください"
  	}
  },
    errorElement: "em",
    errorClass: "errortext"
  });
  /*------------------------------------------*/
  /*----- ログイン認証                   -----*/
  /*------------------------------------------*/
  $.fn.eventmove = function() {
    $(this).click(function() {
    	var mtmno = new String($("#mtmno").val());
    	var edaban = new String($("#edaban").val());

      var json = {"mtmno":mtmno, "edaban":edaban};

      $.ajax({
        url: '/testseizou',
        type: 'POST',
        data: JSON.stringify(json),
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
      	if (jsonResult.result == "notfound") {
    			Materialize.toast('入力された見積番号(枝番)は存在しません', 4000, 'rounded');
    			return;
      	}
      	//--------------------------------------------
      	// ここのURLを製造番号入力画面遷移のURLにする
      	//↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
      	window.location = "/moveserialnumber";
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
	});
  }
  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	//項目の初期化を行う
	$("#mtmno").val("");
	$("#edaban").val("");
	//各イベントトリガーを設定する
    $("#moveBtn").eventmove();
    $("#mtmno").select();
    $("#kataban-btn").katabansearch();
    enterEvent($("#katabans"), $("#kataban-btn"));
    CardList($("#sekoulist"));
    CardList.clear();
    var jsonlist = {
    		"1": {"kataban":"MAC-728TG", "kaizo":"指定色塗装（色見本通り）小さい方", "seizo":"aaa", "tanto":"bbb"}
		   ,"2": {"kataban":"MAC-728TG", "kaizo":"指定色塗装（色見本通り）小さい方", "seizo":"aaa", "tanto":"bbb"}
		   ,"3": {"kataban":"MAC-728TG", "kaizo":"指定色塗装（色見本通り）小さい方", "seizo":"aaa", "tanto":"bbb"}
    };
    CardList.put(jsonlist);
    var jsonresult =[];
    CardList.get(jsonresult);
    var json = {"mtmno":"0000015000", "edaban":"00", jsonlist:jsonresult};
    $.ajax({
        url: '/testjson',
        type: 'POST',
        data: JSON.stringify(json),
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  });

})(jQuery);
