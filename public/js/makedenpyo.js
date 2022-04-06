/*------------------------------------------*/
/*----- 【WeoS】売上伝票ファイル作成画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- 会社選択時                     -----*/
  /*------------------------------------------*/
  $.fn.eventkaisya = function() {
    $(this).change(function() {

      var kaisya = $("#kaisya").val();
      kaisya = ('0000' + kaisya).slice(-4);
      getshiten(kaisya);

    });
  }
  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
	  //全選択、全解除、出力ボタン非表示
	  $("#allsel-btn").hide();
	  $("#allunsel-btn").hide();
	  $("#outtext-btn").hide();
	  //イベント登録
	  $("#retrieval-btn").retrievalclick();
	  $("#allsel-btn").allselclick();
	  $("#allunsel-btn").allunselclick();
	  $("#outtext-btn").outtextclick();
	  $("#kaisya").eventkaisya();
	  getkaisya();
  });

})(jQuery);

/*------------------------------------------*/
/*----- 依頼会社リスト読み込み時           -----*/
/*------------------------------------------*/
function getkaisya() {
    $.ajax({
        url: pUrl + '/getiraikaisyalist',
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
/*----- 依頼支店リスト読み込み時           -----*/
/*------------------------------------------*/
function getshiten(kaisya) {
    $.ajax({
        url: pUrl + '/' + kaisya + '/getiraishitenlist',
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
/*----- 見積検索条件の入力チェック     -----*/
/*------------------------------------------*/
$("#retrieval-form").validate({
  rules: {
    "mitumori-no-from":{
    	maxlength: 10,
    	digits: true
    },
    "mitumori-no-to":{
    	maxlength: 10,
    	digits: true
    },
    "request-from":{
    	customDate: true,
    },
    "request-to":{
    	customDate: true,
    },
    "kenmei":{
    },
    "start-denno":{
    	maxlength: 10,
    	digits: true
    },
  },
  messages: {
    "mitumori-no-from":{
      maxlength: "10桁以下で入力してください",
      digits: "数値で入力してください"
    },
  	"mitumori-no-to":{
  	  maxlength: "10桁以下で入力してください",
  	  digits: "数値で入力してください"
  	},
  	"start-denno":{
      maxlength: "10桁以下で入力してください",
      digits: "数値で入力してください"
    }
  },
  errorElement: "em",
  errorClass: "errortext"
});
/*------------------------------------------*/
/*----- 日付カスタムバリデーション     -----*/
/*------------------------------------------*/
$.validator.addMethod('customDate', function(value, element) {

    var PTN_YYYYMMDD = /^\d{4}\/\d{2}\/\d{2}$/;
    var DATE_DELIMITER = '/';

    var date = new Date(value);

    var optional = this.optional(element);
    if (optional) {
      return optional;
    }

    // invalidな日付または、フォーマット通りに入力されていない場合はNGとなる
    if (/Invalid|NaN/.test(date.toString()) || !PTN_YYYYMMDD.test(value)) {
      return false;
    }

    // 入力値とnewDate.toStringを文字列比較する。
    // 実際には無い日付（2013/04/31）をnewDateすると勝手に変換（2013/05/01）するのでその対策。
    // なお、31日だけこの現象が起こる。32日以降はnewDateでもinvalid判定になる。
    var m = '0' + (date.getMonth() + 1);
    var d = '0' + date.getDate();
    var newDateStr = date.getFullYear() + DATE_DELIMITER + m.slice(-2) + DATE_DELIMITER + d.slice(-2);

    return newDateStr === value;

  }, '正しい日付を入力してください。');

/*------------------------------------------*/
/*----- ダウンロードアイコンクリック   -----*/
/*------------------------------------------*/
$.fn.downiconclick = function() {
  $(this).click(function() {
    $(this).toggleClass("active");
    return false;
  });
}
/*------------------------------------------*/
/*----- 全選択ボタンクリック           -----*/
/*------------------------------------------*/
$.fn.allselclick = function() {
  $(this).click(function() {
	  $(".denpyo").toggleClass("active");
    return false;
  });
}
/*------------------------------------------*/
/*----- 全解除ボタンクリック           -----*/
/*------------------------------------------*/
$.fn.allunselclick = function() {
  $(this).click(function() {
	  $(".denpyo").removeClass("active");
    return false;
  });
}
/*------------------------------------------*/
/*----- 検索ボタンクリック時           -----*/
/*------------------------------------------*/
$.fn.retrievalclick = function() {
  $(this).click(function() {
	//単項目チェック
  	$("#retrieval-form").valid();

  	//----- 見積番号比較 -----
  	var mFrom = $("#mitumori-no-from").val();
  	var mTo = $("#mitumori-no-to").val();

  	if (mFrom != "" && mTo != "") {
  		if (checkNumberFromTo(mFrom, mTo) == false) {
  			Materialize.toast('見積番号の範囲指定に誤りがあります', 3000, 'rounded');
  			return false;
  		}
  	}

  	//----- 要求納期比較 -----
  	var dRfrom = $("#request-from").val();
  	var dRto = $("#request-to").val();

  	if (dRfrom != "" && dRto != "") {
  		if (checkDateFromTo(dRfrom, dRto) == false) {
  			Materialize.toast('依頼納期の範囲指定に誤りがあります', 3000, 'rounded');
  			return false;
  		}
  	}

  	//会社/支店コード取得
    var kaisya = $("#kaisya").val();
    var shiten = $("#shiten").val();
    kaisya = ('0000' + kaisya).slice(-4);
    if(kaisya == '0000'){
    	kaisya = '';
    	shiten = '';
    }

	//検索条件をJSONに格納
    var json = {
    	"mtmnof":$("#mitumori-no-from").val()
       ,"mtmnot":$("#mitumori-no-to").val()
       ,"requef":$("#request-from").val()
       ,"requet":$("#request-to").val()
       ,"kenmei":$("#kenmei").val()
       ,"kaisya":kaisya
       ,"shiten":shiten
    };
    $.ajax({
		url: pUrl + '/commitdenpyoretrieval',
		type: 'POST',
		data: JSON.stringify(json),
		complete:function(data, status, jqXHR){					//処理成功時
		  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
		  if (jsonResult.retrieval) {
	            var datalist = jsonResult.datalist; //見積件名一覧の取得
	            var json = [];
	            if(jsonResult.dtcnt == 500){
	            	Materialize.toast('最大行数（500行）を超えるため、条件を絞り込み再検索して下さい', 3000, 'rounded');
	            }
	            for (var key in datalist) {
	            	var data = datalist[key];
	            	var jd = {
	            			 "選択":('0000000000' + data.mno).slice( -10 ) + '-' + ('00' + data.edaban).slice( -2 )
	            			 ,"見積No.": ('0000000000' + data.mno).slice( -10 ) + '-' + ('00' + data.edaban).slice( -2 )
	            			 ,"出力":data.renkeiput
	            			 ,"依頼元":data.kaisyaName + "&nbsp;&nbsp;" + data.shitenName + "<br />" + data.shimeiKanji
	            			 ,"工事件名":data.kouji
	            			 ,"合計金額":data.sgoukei.toLocaleString() + ' 円'
	            			  };
	            	json.push(jd);
	            }

				// JSON配列をGRIDにバインド
				$("#jsGrid-sorting").jsGrid({
				  height: "100%",
				  width: "100%",
				  autoload: true,
				  selecting: false,
				  shrinkToFit : true,
				  data: json,
				  fields: [{
					  name: "選択",
					  type: "text",
					  width: 4,
                      align: "center",
                      itemTemplate: function(value) {
                    	  return $('<a class="mtmno" href="#"><i mtmno="' + value + '" class="material-icons denpyo">cloud_download</i></a>');
                      }
				   	}
				  ,{
					  name: "見積No.",
					  type: "text",
					  align: "center",
					  width: 20,
				  }
				  ,{
					  name: "出力",
					  type: "text",
					  align: "center",
					  width: 2,
            itemTemplate: function(value) {
              var color = "";
              if ("出" == value) {
                color = "red-text";
              }
              return $('<span class="' + color + '">' + value + '</span>');
            }
				  }
				  ,{
					  name: "依頼元",
					  type: "text",
					  width: 80
				  }
				  ,{
					  name: "工事件名",
					  type: "text",
					  width: 80
				  }
				  ,{
					  name: "合計金額",
					  type: "text",
					  align: "right",
					  width: 16
				  }
				  ]
				});
				// クリックイベントを定義
				$(".denpyo").unbind("click");
				$(".denpyo").downiconclick();
				//全選択、全解除、出力ボタン非表示
				$("#allsel-btn").show();
				$("#allunsel-btn").show();
				$("#outtext-btn").show();

		  }
		  else {
			Materialize.toast('条件に一致する見積データが存在しません', 3000, 'rounded');
		  }
		},
		dataType:'json',
		contentType:'text/json',
		async: false
	});
  });
}
/*------------------------------------------*/
/*----- 出力ボタンクリック           -----*/
/*------------------------------------------*/
$.fn.outtextclick = function() {
  $(this).click(function() {
    $("#start-denno").removeClass('error-text');
	  var stdenno = $("#start-denno").val();
      if (stdenno == "") {
  		Materialize.toast('作成開始伝票番号を入力してください', 3000, 'rounded');
  		$("#start-denno").addClass('error-text');
      $("#start-denno").select();
  		return false;
	  }

      var addjson = [];
	  var mtmno = $(".denpyo.active");
	  if(mtmno.length > 0){
		  //jsonデータ作成
	      for (var idx = 0;idx < mtmno.length; idx++) {
	          var data = $(mtmno[idx])[0];
	          var infojson = {"mtmno":$(data).attr("mtmno")};	//見積No
	          addjson.push(infojson);
	      }
	      var json = {"stdenno":stdenno, jsonlist:addjson};

	      //売上伝票出力コントローラー呼び出し
	      $.ajax({
	  		url: pUrl + '/denpyoouttext',
	  		type: 'POST',
	  		data: JSON.stringify(json),
	  		complete:function(data, status, jqXHR){					//処理成功時
	  		  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
	  		  if (jsonResult.result == "success") {
	  		      $("#output-btn").attr("download", "売上伝票ファイル.txt");
	  		      $("#output-btn").attr("href", "data:text/csv; charset=shift_jis," + EscapeSJIS(jsonResult.output));
	              $("#retrieval-btn").click();
	              Materialize.toast('売上伝票の出力が完了しました', 4000, 'rounded');
	              return;
	  		  }
	  		  else {
	  			Materialize.toast('条件に一致する売上データが存在しません', 3000, 'rounded');
	  		  }
	  		},
	  		dataType:'json',
	  		contentType:'text/json',
	  		async: false
	  	});
	  }else{
		  Materialize.toast('売上データが選択されていません', 3000, 'rounded');
	  }
  });
}