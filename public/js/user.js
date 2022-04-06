/*------------------------------------------*/
/*----- 【WeoS】ログイン画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- 検索ボタン押下時               -----*/
  /*------------------------------------------*/
  $.fn.eventUser = function() {
    $(this).click(function() {

    	//エラーテキストクリア
    	ContorolInfo.clearError();
    	//単項目チェック
    	if (!ContorolInfo.singleCheck(0)) {
    		return;
    	}

        //ユーザＩＤを取得する
    	var oUserId = $("#userId");
        var json = {"userid":oUserId.val()};

        $.ajax({
          url: pUrl + '/useridcommit',
          type: 'POST',
          data: JSON.stringify(json),
          complete:function(data, status, jqXHR){					      //処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          	if (jsonResult.result == "notfound") {              //入力ＩＤが見つからなかった場合
        			Materialize.toast('入力されたユーザＩＤは存在しません', 4000, 'rounded');
        			return;
          	}
          	else { //ＩＤ確定時
            	//選択コードリスト生成する
        			getkaisya();
        			getshiten(jsonResult.kaisya);
        			getbumon(jsonResult.kaisya, jsonResult.shiten);
        			//取得した値をセッティング
        			$("#kanji").val(jsonResult.kanji);
        			$("#kana").val(jsonResult.kana);
        			$("#kaisya").val(jsonResult.kaisya);
        			$("#shiten").val(jsonResult.shiten);
        			$("#bumon").val(jsonResult.bumon);
              $("#kengen").val(jsonResult.kengen);
              $("#mode").val(jsonResult.mode);
              //ログインユーザの権限によりリストを制御する
              if (jsonResult.ukengen == "01") {
                $("#kaisya").prop("disabled", false);
                $("#shiten").prop("disabled", false);
                $("#bumon").prop("disabled" , false);
                $("#kengen").prop("disabled", false);
              }
              else {
                $("#kaisya").prop("disabled", true);
                $("#shiten").prop("disabled", true);
                $("#bumon").prop("disabled" , true);
                $("#kengen").prop("disabled", true);
              }
              //選択リストに設定した値を反映
              $("#kaisya").material_select('update');
              $("#shiten").material_select('update');
              $("#bumon").material_select('update');
              $("#kengen").material_select('update');
              //ユーザＩＤを非活性化
        			oUserId.prop("disabled", true);
              //検索ボタン→クリアボタンに切替
        		  $("#idSearchBtn").hide();
        		  $("#idClearBtn").show();
        		  //確定ボタンを切替
        		  if (jsonResult.mode == "add") {
                $("#idAddBtn").show();
                $("#idUpdateBtn").hide();
                $("#idDeleteBtn").hide();
        		  }
        		  else {
                $("#idAddBtn").hide();
                $("#idUpdateBtn").show();
                $("#idDeleteBtn").show();
        		  }
        		  //入力エリアを表示する
        		  $("#input-area").show();
          	}
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
	});
  }
  /*------------------------------------------*/
  /*----- クリアボタン押下時             -----*/
  /*------------------------------------------*/
  $.fn.eventclear = function() {
    $(this).click(function() {

      //入力エリアをクリア
  		$("#kanji").val("");
  		$("#kana").val("");
      $("#kaisya").val("");
      $("#shiten").val("");
      $("#bumon").val("");
      $("#kengen").val("");
  		$("#userId").val("");
      //ユーザＩＤを活性化
  		$("#userId").prop("disabled", false);
      //クリアボタン→検索ボタンに切替
	    $("#idSearchBtn").show();
	    $("#idClearBtn").hide();
	    //入力エリアを非表示にする
	    $("#input-area").hide();

    });
  }
  /*------------------------------------------*/
  /*----- 初期化ボタン押下時             -----*/
  /*------------------------------------------*/
  $.fn.eventresert = function() {
    $(this).click(function() {

      swal({title: "確認",
        text: "パスワードを初期化しますか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          //ユーザＩＤを取得する
          var oUserId = $("#userId");
          var json = {"userid":oUserId.val()};

          $.ajax({
            url: pUrl + '/passwordreset',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
                Materialize.toast('パスワードの初期化が完了しました', 4000, 'rounded');
                return;
              }
          },
            dataType:'json',
            contentType:'text/json',
            async: false
          });
      });


    });
  }
  /*------------------------------------------*/
  /*----- エラーチェック                 -----*/
  /*------------------------------------------*/
  function errorCheck() {
  	//エラーテキストクリア
  	ContorolInfo.clearError();
  	//必須チェック
  	if (!ContorolInfo.checkRequired("0000")) {
  		return false;
  	}
  	//単項目チェック
  	if (!ContorolInfo.singleCheck(4)) {
  		return false;
  	}
  	return true;
  }
  /*------------------------------------------*/
  /*----- 登録ボタン押下時               -----*/
  /*------------------------------------------*/
  $.fn.eventadd = function() {
    $(this).click(function() {

	  //入力チェックを行う
	  if (!errorCheck()) {
		  return;
	  }

      swal({title: "確認",
        text: "入力データを登録します。<br />よろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){

          var json = {"userid":$("#userId").val(), "kanji":$("#kanji").val(), "kana":$("#kana").val(), "kaisya":$("#kaisya").val(), "shiten":$("#shiten").val(), "bumon":$("#bumon").val(), "kengen":$("#kengen").val(), "mode":$("#mode").val()};

          $.ajax({
            url: pUrl + '/usermntcommit',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
                $("#userId").val(jsonResult.userId);
                $("#idSearchBtn").click();
                Materialize.toast('登録が完了しました', 4000, 'rounded');
                return;
              }
          },
            dataType:'json',
            contentType:'text/json',
            async: false
          });
      });


    });
  }
  /*------------------------------------------*/
  /*----- 更新ボタン押下時               -----*/
  /*------------------------------------------*/
  $.fn.eventupdate = function() {
    $(this).click(function() {

  	  //入力チェックを行う
  	  if (!errorCheck()) {
  		  return;
  	  }

      swal({title: "確認",
        text: "入力データを更新します。<br />よろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          var json = {"userid":$("#userId").val(), "kanji":$("#kanji").val(), "kana":$("#kana").val(), "kaisya":$("#kaisya").val(), "shiten":$("#shiten").val(), "bumon":$("#bumon").val(), "kengen":$("#kengen").val(), "mode":$("#mode").val()};

          $.ajax({
            url: pUrl + '/usermntcommit',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
                $("#userId").val(jsonResult.userId);
                $("#idSearchBtn").click();
                Materialize.toast('更新が完了しました', 4000, 'rounded');
                return;
              }
          },
            dataType:'json',
            contentType:'text/json',
            async: false
          });
      });


    });
  }
  /*------------------------------------------*/
  /*----- 削除ボタン押下時               -----*/
  /*------------------------------------------*/
  $.fn.eventdelete = function() {
    $(this).click(function() {

      swal({title: "確認",
        text: "削除します。<br />よろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          var json = {"userid":$("#userId").val()};

          $.ajax({
            url: pUrl + '/userdelete',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
                $("#idClearBtn").click();
                Materialize.toast('削除が完了しました', 4000, 'rounded');
                return;
              }
          },
            dataType:'json',
            contentType:'text/json',
            async: false
          });
      });


    });
  }
  /*------------------------------------------*/
  /*----- 会社選択時                     -----*/
  /*------------------------------------------*/
  $.fn.eventkaisya = function() {
    $(this).change(function() {

      var kaisya = $("#kaisya").val();
      getshiten(kaisya);

    });
  }
  /*------------------------------------------*/
  /*----- 支店選択時                     -----*/
  /*------------------------------------------*/
  $.fn.eventshiten = function() {
    $(this).change(function() {

      var kaisya = $("#kaisya").val();
      var shiten = $("#shiten").val();
      getbumon(kaisya, shiten);

    });
  }
  /*------------------------------------------*/
  /*----- 項目制御情報生成               -----*/
  /*------------------------------------------*/
  function controlInit() {
	  ContorolInfo();							//項目制御オブジェクトの生成
	  ContorolInfo.add({"info":0 , "item":"userId", control:{"0000":{"disabled":false, "required":false}}, type:"num"});
	  ContorolInfo.add({"info":4 , "item":"kanji" , control:{"0000":{"disabled":false, "required":true}},  type:"wide"});
	  ContorolInfo.add({"info":4 , "item":"kana"  , control:{"0000":{"disabled":false, "required":true}},  type:"kana"});
	  ContorolInfo.add({"info":4 , "item":"kaisya", control:{"0000":{"disabled":false, "required":true}},  type:"none"});
	  ContorolInfo.add({"info":4 , "item":"shiten", control:{"0000":{"disabled":false, "required":true}},  type:"none"});
	  ContorolInfo.add({"info":4 , "item":"bumon" , control:{"0000":{"disabled":false, "required":true}},  type:"none"});
	  ContorolInfo.add({"info":4 , "item":"kengen", control:{"0000":{"disabled":false, "required":true}},  type:"none"});
	  ContorolInfo.set(0, "0000");				//制御情報の反映(ユーザＩＤ)
	  ContorolInfo.set(4, "0000");				//制御情報の反映(その他)
  }
  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	//項目制御情報の生成
	controlInit();
  	//項目の初期化を行う
  	$("#userid").val("");
  	getkengen();                   //権限リストを生成
  	$('select').material_select();
  	//各イベントトリガーを設定する
    $("#idSearchBtn").eventUser();
    $("#idClearBtn").eventclear();
    $("#kaisya").eventkaisya();
    $("#shiten").eventshiten();
    $("#idResetBtn").eventresert();
    $("#idAddBtn").eventadd();
    $("#idUpdateBtn").eventupdate();
    $("#idDeleteBtn").eventdelete();
    enterEvent($("#userid"), $("#idSearchBtn"));
    //クリアボタン→検索ボタンに切替
    $("#idClearBtn").hide();
    //ユーザＩＤにフォーカスを設定
    $("#userid").select();
    //入力エリアを非表示にする
    $("#input-area").hide();
  });
  /*------------------------------------------*/
  /*----- 会社リスト読み込み時           -----*/
  /*------------------------------------------*/
  function getkaisya() {
      $.ajax({
          url: pUrl + '/getkaisyalist',
          type: 'GET',
          complete:function(data, status, jqXHR){					//処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
            var kaisyas = $("#kaisya");
            kaisyas.empty();
            var datalist = jsonResult.datalist;
            for (var key in datalist) {
            	var data = datalist[key];
        		kaisyas.append('<option value="' + data.value + '">' + data.name + '</option>');
            }
            kaisyas.material_select('update');
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
  }
  /*------------------------------------------*/
  /*----- 支店リスト読み込み時           -----*/
  /*------------------------------------------*/
  function getshiten(kaisya) {
      $.ajax({
          url: pUrl + '/' + kaisya + '/getshitenlist',
          type: 'GET',
          complete:function(data, status, jqXHR){					//処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
            var shitens = $("#shiten");
            shitens.empty();
            var datalist = jsonResult.datalist;
            for (var key in datalist) {
            	var data = datalist[key];
            	shitens.append('<option value="' + data.value + '">' + data.name + '</option>');
            }
            shitens.material_select('update');
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
  }
  /*------------------------------------------*/
  /*----- 部門リスト読み込み時           -----*/
  /*------------------------------------------*/
  function getbumon(kaisya, siten) {
      $.ajax({
          url: pUrl + '/' + kaisya + '/' + siten + '/getbumonlist',
          type: 'GET',
          complete:function(data, status, jqXHR){					//処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
            var bumons = $("#bumon");
            bumons.empty();
            var datalist = jsonResult.datalist;
            for (var key in datalist) {
            	var data = datalist[key];
            	bumons.append('<option value="' + data.value + '">' + data.name + '</option>');
            }
            bumons.material_select('update');
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
  }
  /*------------------------------------------*/
  /*----- 権限リスト読み込み時           -----*/
  /*------------------------------------------*/
  function getkengen() {
      $.ajax({
          url: pUrl + '/getkengenlist',
          type: 'GET',
          complete:function(data, status, jqXHR){         //処理成功時
            var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
            var kengen = $("#kengen");
            kengen.empty();
            var datalist = jsonResult.datalist;
            for (var key in datalist) {
              var data = datalist[key];
              kengen.append('<option value="' + data.value + '">' + data.name + '</option>');
            }
            kengen.material_select('update');
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
  }
})(jQuery);
