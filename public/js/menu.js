/*------------------------------------------*/
/*----- 【WeoS】メニュー画面           -----*/
/*------------------------------------------*/
(function($) {

  var alertDataList = undefined;	//アラートデータ

  /*------------------------------------------*/
  /*----- 見積番号クリック時(見積検索)   -----*/
  /*------------------------------------------*/
  $.fn.mmtmnoclick = function() {
    $(this).click(function() {
      var json = {
        	"mtmno":$(this).attr("mtmno")
           ,"mode":"Retrieval"
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
  /*----- 見積ステータスクリック時       -----*/
  /*------------------------------------------*/
  $.fn.sttsclick = function() {
    $(this).click(function() {

        var json = {"stts":$(this).attr("stts")};

        $.ajax({
          url: pUrl + '/getmtstts',
          type: 'POST',
          data: JSON.stringify(json),
          complete:function(data, status, jqXHR){					//処理成功時
            var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
            var datalist = jsonResult.datalist						//見積件名一覧の取得
            var json = [];
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
			// クリックイベントを定義
			$(".mtmno").unbind("click");
			$(".mtmno").mmtmnoclick();
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
    });
  }
  $(document).ready(function(){
  	/*------------------------------------------*/
  	/*----- 見積ステータス件数の表示        ----*/
  	/*------------------------------------------*/
  	$.ajax({
  	    url: pUrl + '/countofstts',
  	    type: 'GET',
  	    complete:function(data, status, jqXHR){					//処理成功時
  	      var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
  	      $("#mtinput").text(jsonResult.input + " 件");
  	      $("#mtanser").text(jsonResult.anser + " 件");
  	      $("#mtorder").text(jsonResult.order + " 件");
  	      $("#mtcommit").text(jsonResult.commit + " 件");
  	      $("#mtsekou").text(jsonResult.sekou + " 件");
  	},
  	    dataType:'json',
  	    contentType:'text/json',
  	    async: false
  	});
    /*------------------------------------------*/
    /*----- 一旦アラートエリアを非表示      ----*/
    /*------------------------------------------*/
  	$("#alert-area").hide();
    /*------------------------------------------*/
    /*----- 決済待ちアラートデータの取得    ----*/
    /*------------------------------------------*/
    $.ajax({
        url: pUrl + '/getkessaidata',
        type: 'GET',
        complete:function(data, status, jqXHR){         //処理成功時
          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

          if (jsonResult.alert) {
        	alertDataList = jsonResult.datalist;
            $("#alertmessage").text("決済承認待ちの見積データが存在します");
            $("#alert-area").show();
          }
    },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
    /*------------------------------------------*/
    /*----- 回答待ちアラートデータの取得    ----*/
    /*------------------------------------------*/
    $.ajax({
        url: pUrl + '/getanserdata',
        type: 'GET',
        complete:function(data, status, jqXHR){         //処理成功時
          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

          if (jsonResult.alert) {
          	alertDataList = jsonResult.datalist;
            $("#alertmessage").text("回答待ちの見積データが存在します");
            $("#alert-area").show();
          }
    },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
    /*------------------------------------------*/
    /*----- 一時保存アラートデータの取得    ----*/
    /*------------------------------------------*/
    $.ajax({
        url: pUrl + '/getsavedata',
        type: 'GET',
        complete:function(data, status, jqXHR){         //処理成功時
          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

          if (jsonResult.alert) {
          	alertDataList = jsonResult.datalist;
            $("#alertmessage").text("一時保存見積データが存在します");
            $("#alert-area").show();
          }
    },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
    /*------------------------------------------*/
    /*----- アラートデータ表示             -----*/
    /*------------------------------------------*/
    $.fn.alertDisplay = function() {
      $(this).click(function() {
          var json = [];
          for (var key in alertDataList) {
          	var data = alertDataList[key];
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
              width: 10,
                      itemTemplate: function(value) {
                          return $('<a href="#" class="mtmno" mtmno="' + value + '">' + value + '</a>');
                      }
            }
            ,{
              name: "管理番号",
              type: "text",
              align: "center",
                width: 10
            }
            ,{
              name: "状況",
              type: "text",
              align: "center",
              width: 8
            }
            ,{
              name: "依頼納期",
              type: "text",
              align: "center",
              width: 10
            }
            ,{
              name: "回答納期",
              type: "text",
              align: "center",
              width: 10
            }
            ,{
              name: "工事件名",
              type: "text",
              width: 80
            }
            ,{
              name: "見積依頼者",
              type: "text",
              width: 80
            }
            ]
          });
			// クリックイベントを定義
			$(".mtmno").unbind("click");
			$(".mtmno").mmtmnoclick();
      });
    }
  	/*------------------------------------------*/
  	/*----- イベント登録                    ----*/
  	/*------------------------------------------*/
  	$(".stts").sttsclick();
  	$("#alert-message").alertDisplay();
  });
})(jQuery);
