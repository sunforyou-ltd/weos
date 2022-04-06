/*------------------------------------------*/
/*----- 【WeoS】見積検索画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- 見積検索モード取得             -----*/
  /*------------------------------------------*/
  function getmode() {
	  $.ajax({
			url: pUrl + '/getretrievalmode',
			type: 'GET',
			complete:function(data, status, jqXHR){					//処理成功時
			  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
			  $("#mode").val(jsonResult.mode);
			  switch (jsonResult.mode) {
			  case "SerialNumber":
				  //----- 製造番号入力 -----
				  //見積状態を「受注確定」「施工完了」とする
				  $("#mtinput").hide();
				  $("#mtanser").hide();
				  $("#mtorder").hide();
				  $("#mtcommit").show();
				  $("#mtsekou").show();
				  break;

			  default:
				  //----- 見積検索 -----
				  //見積状態を全て選択可能とする
				  $("#mtinput").show();
				  $("#mtanser").show();
				  $("#mtorder").show();
				  $("#mtcommit").show();
				  $("#mtsekou").show();
				  break;
			  }
			},
			dataType:'json',
			contentType:'text/json',
			async: false
	  });
  }
  /*------------------------------------------*/
  /*----- 見積ステータスクリック時       -----*/
  /*------------------------------------------*/
  $.fn.sttsclick = function() {
    $(this).click(function() {
    	$(this).toggleClass("nonselect");
    });
  }
  /*------------------------------------------*/
  /*----- 見積番号クリック時(見積検索)   -----*/
  /*------------------------------------------*/
  $.fn.mmtmnoclick = function() {
    $(this).click(function() {
      var json = {
        	"mtmno":$(this).attr("mtmno")
           ,"mode":$("#mode").val()
      };
  	  $.ajax({
			url: pUrl + '/moveother',
			type: 'POST',
			data: JSON.stringify(json),
			complete:function(data, status, jqXHR){					//処理成功時
			  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
			  $("#mode").val(jsonResult.mode);
			  switch (jsonResult.mode) {
			  case "SerialNumber":
				  //----- 製造番号入力 -----
				  //製造番号入力画面に遷移する
				  window.location = pUrl + "/moveserialnumber";
				  break;

			  default:
				  //----- 見積検索 -----
				  //見積作成画面に遷移する
				  window.location = pUrl + "/moveestimate";
				  break;
			  }
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
	  $(".stts-chip").sttsclick();
	  $(".stts-chip").addClass("nonselect");
	  $("#retrieval-btn").retrievalclick();
	  getmode();
  });

})(jQuery);

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
    "anser-from":{
    	customDate: true,
    },
    "anser-to":{
    	customDate: true,
    },
    "kenmei":{
    },
    "tokuisaki-no":{
    },
    "tokuisaki-name":{
    }
  },
  messages: {
    "mitumori-no-from":{
      maxlength: "10桁以下で入力してください",
      digits: "数値で入力してください"
    },
  	"mitumori-no-to":{
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
  			Materialize.toast('要求納期の範囲指定に誤りがあります', 3000, 'rounded');
  			return false;
  		}
  	}
	//----- 要求納期比較 -----
	var dAfrom = $("#anser-from").val();
	var dAto = $("#anser-to").val();

	if (dAfrom != "" && dAto != "") {
		if (checkDateFromTo(dAfrom, dAto) == false) {
			Materialize.toast('回答納期の範囲指定に誤りがあります', 3000, 'rounded');
  			return false;
		}
	}

	//検索条件をJSONに格納
    var json = {
    	"mtmnof":$("#mitumori-no-from").val()
       ,"mtmnot":$("#mitumori-no-to").val()
       ,"knrino":$("#kanri-no").val()
       ,"requef":$("#request-from").val()
       ,"requet":$("#request-to").val()
       ,"anserf":$("#anser-from").val()
       ,"ansert":$("#anser-to").val()
       ,"kenmei":$("#kenmei").val()
       ,"tokuno":$("#tokuisaki-no").val()
       ,"tokunm":$("#tokuisaki-name").val()
       ,"mtinput":!$("#mtinput").hasClass('nonselect')
       ,"mtanser":!$("#mtanser").hasClass('nonselect')
       ,"mtorder":!$("#mtorder").hasClass('nonselect')
       ,"mtcommit":!$("#mtcommit").hasClass('nonselect')
       ,"mtsekou":!$("#mtsekou").hasClass('nonselect')
    };
    $.ajax({
		url: pUrl + '/commitretrieval',
		type: 'POST',
		data: JSON.stringify(json),
		complete:function(data, status, jqXHR){					//処理成功時
		  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
		  if (jsonResult.retrieval) {
	            var datalist = jsonResult.datalist; //見積件名一覧の取得
	            var json = [];
				  $("#mode").val(jsonResult.mode);
				  switch (jsonResult.mode) {
				  case "SerialNumber":
				  //----- 製造番号入力 -----
			            for (var key in datalist) {
			            	var data = datalist[key];
			            	var jd = {
			            			  "見積番号":data.mno
			            			 ,"管理番号":data.kno
			            			 ,"状況":data.stts
			            			 ,"依頼納期":data.irai
			            			 ,"回答納期":data.kaitou
			            			 ,"工事件名":data.kouji
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
							  name: "見積番号",
							  type: "text",
							  align: "center",
							  width: 30,
		                      itemTemplate: function(value) {
		                          return $('<a href="#" class="mtmno" mtmno="' + value + '">' + value + '</a>');
		                      }
						  }
						  ,{
							  name: "管理番号",
							  type: "text",
							  align: "center",
						      width: 20
						  }
						  ,{
							  name: "状況",
							  type: "text",
							  align: "center",
							  width: 18
						  }
						  ,{
							  name: "依頼納期",
							  type: "text",
							  align: "center",
							  width: 20
						  }
						  ,{
							  name: "回答納期",
							  type: "text",
							  align: "center",
							  width: 20
						  }
						  ,{
							  name: "工事件名",
							  type: "text",
							  width: 80
						  }
						  ]
						});
					  break;

				  default:
					  //----- 見積検索 -----
			            for (var key in datalist) {
			            	var data = datalist[key];
			            	var jd = {
			            			  "見積番号":data.mno
			            			 ,"管理番号":data.kno
			            			 ,"状況":data.stts
			            			 ,"依頼納期":data.irai
			            			 ,"回答納期":data.kaitou
			            			 ,"工事件名":data.kouji
			            			 ,"見積依頼者":data.kaisyaName + "&nbsp;&nbsp;" + data.shitenName + "<br />" + data.shimeiKanji
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
							  name: "見積番号",
							  type: "text",
							  align: "center",
							  width: 25,
		                      itemTemplate: function(value) {
		                          return $('<a href="#" class="mtmno" mtmno="' + value + '">' + value + '</a>');
		                      }
						  }
						  ,{
							  name: "管理番号",
							  type: "text",
							  align: "center",
						      width: 20
						  }
						  ,{
							  name: "状況",
							  type: "text",
							  align: "center",
							  width: 23
						  }
						  ,{
							  name: "依頼納期",
							  type: "text",
							  align: "center",
							  width: 20
						  }
						  ,{
							  name: "回答納期",
							  type: "text",
							  align: "center",
							  width: 20
						  }
						  ,{
							  name: "工事件名",
							  type: "text",
							  width: 50
						  }
						  ,{
							  name: "見積依頼者",
							  type: "text",
							  width: 50
						  }
						  ]
						});
					  break;
				  }
				// クリックイベントを定義
				$(".mtmno").unbind("click");
				$(".mtmno").mmtmnoclick();
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
