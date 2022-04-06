/*------------------------------------------*/
/*----- 【WeoS】製造番号入力画面       -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- データ一覧を作成               -----*/
  /*------------------------------------------*/
  function datalist() {
	//エリア内要素のクリア
    $(this).empty();
    var url = pUrl + "/initserialnumber";

    $.ajax({
      url: url,
      type:'GET',
      complete:function(data, status, jqXHR) {
       var jsonResult = JSON.parse( data.responseText );
        var datalist = jsonResult.datalist; //製造番号入力一覧の取得
        var json = [];
        for (var key in datalist) {
        	var data = datalist[key];
        	//ヘッダ部の表示
       		$("#mnodsp").text('見積番号 : ' + ('0000000000' + data.mno).slice( -10 ));
       		$("#edadsp").text('枝番 : ' + ('00' + data.eda).slice( -2 ));
       		$("#knodsp").text('管理番号 : ' + data.kno);
       		$("#smgaku").text('見積合計金額 : ' + data.lmkei.toLocaleString() + ' 円');
       		$("#mgaku").text('本体価格 : ' + data.mkei.toLocaleString() + ' 円');
       		$("#mzeigaku").text('消費税 : ' + data.zei.toLocaleString() + ' 円');
       		if(data.tokui == ""){
       			$("#tokui").text('　');
       		}else{
       			$("#tokui").text(data.tokui);
       		}
       		if(data.koji == ""){
       			$("#kouji").text('　');
       		}else{
       			$("#kouji").text(data.koji);
       		}
       		break;
        }
        CardList($("#sekoulist"));
        CardList.clear();
        CardList.put(datalist);
        //登録モードにより釦切替
        if(jsonResult.mode == "create"){
        	$("#idAddBtn").show();
        	$("#idUpdateBtn").hide();
        }else{
        	$("#idAddBtn").hide();
        	$("#idUpdateBtn").show();
        }
        $("#mode").val(jsonResult.mode);
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  /*------------------------------------------*/
  /*----- 登録ボタン押下時               -----*/
  /*------------------------------------------*/
  $.fn.eventadd = function() {
    $(this).click(function() {

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

          var jsonresult =[];
          CardList.get(jsonresult);
          var json = {"mtmno":$("#mnodsp").text().slice( -10 ), "edaban":$("#edadsp").text().slice( -2 ), "mode":$("#mode").val(), jsonlist:jsonresult};

          $.ajax({
            url: pUrl + '/serialnumbercommit',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
            	datalist();
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

            var jsonresult =[];
            CardList.get(jsonresult);
            var json = {"mtmno":$("#mnodsp").text().slice( -10 ), "edaban":$("#edadsp").text().slice( -2 ), "mode":$("#mode").val(), jsonlist:jsonresult};

          $.ajax({
            url: pUrl + '/serialnumbercommit',
            type: 'POST',
            data: JSON.stringify(json),
            complete:function(data, status, jqXHR){               //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == "success") {               //初期化が完了した場合
              	datalist();
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
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	  datalist();
	  //各イベントトリガーを設定する
	  $("#idAddBtn").eventadd();
	  $("#idUpdateBtn").eventupdate();

  });
})(jQuery);

/*----------------------------*/
/*----- 入力チェック     -----*/
/*----------------------------*/
$("#sekoulist-form").validate({
  rules: {
    "seizo":{
    	maxlength: 20
    },
    "tanto":{
    	maxlength: 20
    }
  },
  messages: {
    "seizo":{
      maxlength: "20桁以下で入力してください"
    },
  	"tanto":{
  	  maxlength: "40桁以下で入力してください"
  	}
  },
  errorElement: "em",
  errorClass: "errortext"
});

/*-------------------- 製造番号入力専用カードリスト --------------------*/
var oCardList; //カードリストオブジェクト

/*------------------------------------------*/
/*----- カードリストオブジェクト生成   -----*/
/*------------------------------------------*/
var CardList = function(list) { //
  oCardList = list;
  oCardList.addClass('sekou-list');
}
/*------------------------------------------*/
/*----- 要素を全て削除します           -----*/
/*------------------------------------------*/
CardList.clear = function() {
  if (oCardList != undefined) {
    oCardList.empty();
  }
}
/*------------------------------------------*/
/*----- 要素を表示します               -----*/
/*------------------------------------------*/
CardList.put = function(json) {
  var idx = 0;
  if (oCardList != undefined) {
    for (var key in json) {
      var data = json[key];
      var oList = oCardList.append('<li class="card-panel serialnumber-card row "><div class="col s12 l6"><span class="kataban">' + data.kataban + '</span></div><div class="input-field seizo col s12 l6"><input id="seizo"  name="seizo" type="text" value="' + data.seizo  + '"><label for="seizo" class="center-align active">製造番号</label></div><div class="col s12 l6"><span class="kaizo">' + data.kaizo + '</span></div><div class="input-field tanto col s12 l6"><input id="tanto"  name="tanto" type="text" value="' + data.tanto  + '"><label for="tanto" class="center-align active">施工担当者名</label></div></li>');
    }
  }
}
/*------------------------------------------*/
/*----- 要素の値をJSON形式で取得します -----*/
/*------------------------------------------*/
CardList.get = function(json) {
  if (oCardList != undefined) {
    var list = oCardList.children("li");
    for (var i=0; i < list.length; i++) {
      var data = $(list.eq(i));
      var jsondata = {gno:i, kataban:$($(data.children("div")[0]).children(".kataban")[0]).text(), kaizo:$($(data.children("div")[2]).children(".kaizo")[0]).text(), seizo:$($(data.children(".seizo")[0]).children("#seizo")[0]).val(), tanto:$($(data.children(".tanto")[0]).children("#tanto")[0]).val()};
      json.push(jsondata);
    }
  }
}
