/*------------------------------------------*/
/*----- 【WeoS】共通処理               -----*/
/*------------------------------------------*/
var pUrl = "";
(function($) {
  /*------------------------------------------*/
  /*----- 【WeoS】URL                    -----*/
  /*------------------------------------------*/
  /*------------------------------------------*/
  /*----- 【WeoS】メニューキー           -----*/
  /*------------------------------------------*/
  var menuSettingMain;
  var menuSettingMst;

  var menuSandenMain = [
       {"link":pUrl + "/Retrieval/moveretrieval","title":"見積検索(見積回答)","icon":"search"}
      ,{"link":pUrl + "/SerialNumber/moveretrieval","title":"製造番号入力","icon":"label"}
      ,{"link":pUrl + "/movemakedenpyo","title":"売上伝票ファイル作成","icon":"cloud_download"}
//      ,{"link":"/movetest","title":"テスト画面","icon":"call_split"}
  ];
  var menuSandenMst = [
      {"link":pUrl + "/moveusermnt","title":"ユーザーマスタ","icon":"person"}
     ,{"link":pUrl + "/moveseihinupload","title":"製品単価EXCEL取り込み","icon":"file_upload"}
  ];
  var menuNomalMain = [
      {"link":pUrl + "/addestimate","title":"見積作成(回答)","icon":"note_add"}
     ,{"link":pUrl + "/Retrieval/moveretrieval","title":"見積検索","icon":"search"}
  ];
  var menuNomalMst = [
     {"link":pUrl + "/moveusermnt","title":"ユーザーマスタ","icon":"person"}
  ];
  var menuKesaiMain = [
     {"link":pUrl + "/Retrieval/moveretrieval","title":"見積検索","icon":"search"}
  ];
  var menuKesaiMst = [
  ];

	/*------------------------------------------*/
	/*----- 【WeoS】Enterキー処理          -----*/
	/*------------------------------------------*/
	// input項目をEnter キー、Shift+Enterキーでtab移動 without(button, hidden)
	function fEnterChangeTab(){

	  // ② input要素及び実行ボタンの選択。但し、ボタンとhidden型は除く。
	  var oObject = ":input:not(:button):not(:hidden)";

	  $(oObject).keypress(function(e) {
	    var c = e.which ? e.which : e.keyCode; // クロスブラウザ対応
	    if (c == 13) {
	        var index = $(oObject).index(this); // indexは0～
	        var cNext = "";
	        var nLength = $(oObject).length;
	        for(i=index;i<nLength;i++){
	            cNext = e.shiftKey ? ":lt(" + index + "):last" : ":gt(" + index + "):first";
	            // ③ 止まってはいけいない属性 readonly
	            if ($(oObject + cNext).attr("readonly") == "readonly") {
	                if (e.shiftKey) index--; // １つ前
	                else index++;            // 次へ
	            }
	            // ③ 止まってはいけいない属性 disabled
	            else if ($(oObject + cNext).prop("disabled") == true) {
	                if (e.shiftKey) index--; // １つ前
	                else index++;            // 次へ
	            }
	            else break;
	        }
	        if (index == nLength - 1) {
	            if (! e.shiftKey){
	                // 最後の項目なら、最初に移動。
	                cNext = ":eq(1)";
	            }
	        }
	        if (index == 0) {
	            if (e.shiftKey) {
	                // 最初の項目なら、最後に移動。
	                cNext = ":eq(" + (nLength - 1) + ")";
	            }
	        }
	        $(oObject + cNext).focus();
	        e.defaultPrevented;
	    }
	  });
	}
	// ④onloadのタイミングでこの関数を実行
	if(window.attachEvent){
	    // IE用
	    window.attachEvent('onload',fEnterChangeTab);
	}
	else if (window.opera){
	    // opera用
	    window.addEventListener('load',fEnterChangeTab,false);
	}
	else {
	    // Mozilla用
	    window.addEventListener('load',fEnterChangeTab,false);
	}
	/*------------------------------------------*/
	/*----- 【WeoS】ユーザ情報表示         -----*/
	/*------------------------------------------*/
  $.ajax({
      url: pUrl + '/getuserinfo',
      type: 'GET',
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
        var kaisya = $("#kaisyaname");
        if (kaisya != undefined) {
      	  kaisya.text(jsonResult.kaisyaName);
        }
        var shiten = $("#shitenname");
        if (shiten != undefined) {
      	  shiten.text(jsonResult.shitenName);
        }
        var user = $("#username");
        if (user != undefined) {
      	  user.text(jsonResult.shimeiKanji);
        }

  },
    dataType:'json',
    contentType:'text/json',
    async: false
  });
  /*------------------------------------------------*/
  /*----- 【WeoS】ユーザ権限によるメニュー制御 -----*/
  /*------------------------------------------------*/
  $.ajax({
      url: pUrl + '/getuserinfo',
      type: 'GET',
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        var kaisya = $("#kaisyaname");
        if (kaisya != undefined) {
          kaisya.text(jsonResult.kaisyaName);
        }
        var shiten = $("#shitenname");
        if (shiten != undefined) {
          shiten.text(jsonResult.shitenName);
        }
        var user = $("#username");
        if (user != undefined) {
          user.text(jsonResult.shimeiKanji);
        }

        //ページにメニューが存在した場合
        var menuMain = $("#menuMain");
        if (menuMain != undefined) {
          if (jsonResult.kengen == "01") {
            menuSettingMain = menuSandenMain;
          }
          else if (jsonResult.kengen == "03") {
            menuSettingMain = menuKesaiMain;
          }
          else {
            menuSettingMain = menuNomalMain;
          }
          menuMain.empty();
          for (var key in menuSettingMain) {
            var data = menuSettingMain[key];
            menuMain.append('<li><a href="' + data.link + '"><i class="material-icons">' + data.icon + '</i><span>' + data.title +  '</span></a></li>');
          }
          var mainCount = getlistCount("menuMain");
          if (mainCount == 0) {
        	  $("#listmain").hide();
          }
          else {
        	  $("#listmain").show();
          }
        }
        //ページにマスタが存在した場合
        var menuMst = $("#menuMst");
        if (menuMst != undefined) {
          if (jsonResult.kengen == "01") {
            menuSettingMst = menuSandenMst;
          }
          else if (jsonResult.kengen == "03") {
            menuSettingMst = menuKesaiMst;
          }
          else {
            menuSettingMst = menuNomalMst;
          }
          menuMst.empty();
          for (var key in menuSettingMst) {
            var data = menuSettingMst[key];
            if (jsonResult.kennaigai == "0" && data.link == "/moveusermnt") { //購買決済会社はユーザメンテナンス不可
            	continue;
            }
            menuMst.append('<li><a href="' + data.link + '"><i class="material-icons">' + data.icon + '</i><span>' + data.title +  '</span></a></li>');
          }
          var mstCount = getlistCount("menuMst");
          if (mstCount == 0) {
        	  $("#listmst").hide();
          }
          else {
        	  $("#listmst").show();
          }
        }


  },
    dataType:'json',
    contentType:'text/json',
    async: false
  });
  /*--------------------------------------------*/
  /*----- パスワード変更バリデーション定義 -----*/
  /*--------------------------------------------*/
  var passform = $("#password-form");
  if (passform != undefined) {
    $("#password-form").validate({
      rules: {
        "password":{
          required: true,
          maxlength: 10
        },
        "new-password":{
          required: true,
          maxlength: 10
        },
        "re-password":{
            required: true,
            maxlength: 10,
            equalTo: "#new-password"
        },
      },
      messages: {
        "password":{
          required: "必須入力です",
          maxlength: "10桁以下で入力してください"
  	    },
        "new-password":{
          required: "必須入力です",
          maxlength: "10桁以下で入力してください"
  	    },
        "re-password":{
          required: "必須入力です",
          maxlength: "10桁以下で入力してください",
          equalTo: "新しいパスワードと確認用パスワードが一致しません"
  	    }
      },
      errorElement: "em",
      errorClass: "errortext"
    });
  }
  /*------------------------------------------*/
  /*----- パスワード変更確定ボタン押下時 -----*/
  /*------------------------------------------*/
  $.fn.passcommitclick = function() {
    $(this).click(function() {

    	if(passform.validate().errorList.length > 0) {
    		return;
    	}

        swal({title: "確認",
            text: "パスワードを変更します。<br />よろしいですか？",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "はい",
            cancelButtonText: "いいえ",
            html: true,
            closeOnConfirm: true },
            function(){

            var json = {"password":$("#password").val(), "newpassword":$("#new-password").val()};

    	    $.ajax({
    	        url: pUrl + '/passcommit',
    	        type: 'POST',
    	        data: JSON.stringify(json),
    	        complete:function(data, status, jqXHR){					//処理成功時
    	          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

    	          if (jsonResult.commit) {
    	        	  $("#password-modal").modal('close');
    	        	  Materialize.toast('パスワードを変更しました', 4000, 'rounded');
    	          }
    	          else {
    	        	  Materialize.toast('現在のパスワードが一致しません', 4000, 'rounded');
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
  /*----- メインメニュークリック時       -----*/
  /*------------------------------------------*/
  $.fn.menuclick = function() {
    $(this).click(function() {
      swal({title: "画面遷移確認",
        text: "メインメニューに戻りますか？<br />(保存されてない作業中の内容は破棄されます)",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "メインメニューへ",
        cancelButtonText: "キャンセル",
        html: true,
        closeOnConfirm: false },
        function(){
          window.location = pUrl + "/movemenu";
      });
    });
  }
  /*------------------------------------------*/
  /*----- ログアウトクリック時       -----*/
  /*------------------------------------------*/
  $.fn.logoutclick = function() {
    $(this).click(function() {
	  $.ajax({
	      url: pUrl + '/logout',
	      type: 'GET',
	      complete:function(data, status, jqXHR){					//処理成功時
	    	  var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成
	    	  if (jsonResult.result == "success") {
	    		  window.location = pUrl + "/";
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
    /*----- メインメニューボタン -----*/
    var menu = $("#menu-btn");
    if (menu != undefined) {
      menu.menuclick();
    }
    /*----- ログアウトボタン -----*/
    var logout = $("#logout-btn");
    if (logout != undefined) {
    	logout.logoutclick();
    }
    /*----- 日付オブジェクト設定 -----*/
    $('.datepicker').pickadate({
        selectMonths: true, // Creates a dropdown to control month
        selectYears: 10, // Creates a dropdown of 15 years to control year,
        today: '本日',
        clear: 'クリア',
        close: '確定',
        container: undefined, // ex. 'body' will append picker to body
        closeOnSelect: true // Close upon selecting a date,
    });
    /*----- パスワード変更画面 -----*/
    var passcommit = $("#password-modal");
    if (passcommit != undefined) {
    	$("#btnPasscommit").passcommitclick();
    	passcommit.modal();
    }
    /*----- モーダル設定 -----*/
    //$('.modal').modal();
  });
 })(jQuery);
//--------------------------------------------------------------------------------------------------------------------//
//- ここから外部関数                                                                                                  //
//--------------------------------------------------------------------------------------------------------------------//
/*------------------------------------------*/
/*----- システム日付を西暦で取得       -----*/
/*------------------------------------------*/
function getNowYMD(){
  var dt = new Date();
  var y = dt.getFullYear();
  var m = ("00" + (dt.getMonth()+1)).slice(-2);
  var d = ("00" + dt.getDate()).slice(-2);
  var result = y + "/" + m + "/" + d;
  return result;
}
/*------------------------------------------*/
/*----- 文字列⇒日付変換               -----*/
/*------------------------------------------*/
function toDate (str, delim) {
	  var arr = str.split(delim)
	  return new Date(arr[0], arr[1] - 1, arr[2]);
};
/*------------------------------------------*/
/*----- 数値型大小比較                 -----*/
/*------------------------------------------*/
function checkNumberFromTo(pFrom, pTo) {
	var iFrom = parseInt(pFrom);
	var iTo = parseInt(pTo);

	return iFrom <= iTo;
}
/*------------------------------------------*/
/*----- 日付型大小比較                 -----*/
/*------------------------------------------*/
function checkDateFromTo(pFrom, pTo) {
	var dFrom = toDate(pFrom, '/');
	var dTo = toDate(pTo, '/');

	return dFrom <= dTo;
}
/*------------------------------------------*/
/*----- Enterイベント                  -----*/
/*------------------------------------------*/
function enterEvent(pObj, event) {
	pObj.keypress( function ( e ) {
		if ( e.which == 13 ) {
			event.click();
			return false;
		}
	} );
}
/*------------------------------------------------*/
/*----- 指定したＩＤのリスト件数を取得します -----*/
/*------------------------------------------------*/
function getlistCount(id) {
	  var oUl = $("#" + id);
	  var count = 0;
	  if (oUl != undefined) {
		  count = oUl.children("li").length;
	  }
	  return count;
}
//--------------------------------------------------------------------------------------------------------------------//
//- ここから項目制御＆入力チェック用関数                                                                              //
//--------------------------------------------------------------------------------------------------------------------//
var oControlInfo = [];                      //項目制御情報
/*------------------------------------------*/
/*-----  項目制御オブジェクト生成      -----*/
/*------------------------------------------*/
var ContorolInfo = function() { //
	oControlInfo = [];
}
/*------------------------------------------*/
/*----- 要素を追加します               -----*/
/*------------------------------------------*/
ContorolInfo.add = function(info) {
  if (oControlInfo != undefined) {
	  oControlInfo.push(info);
  }
}
/*----------------------------------------------------*/
/*----- 指定した情報区分の項目制御を行います     -----*/
/*----------------------------------------------------*/
ContorolInfo.set = function(info, controlKey) {
	var infolist = ContorolInfo.getInfo(info);
	var licount = 0;
	if (infolist != undefined) {
		switch (info) {
		case 1: //改造情報の場合
			licount = getlistCount("kaizolist");
			break;

		case 2: //特殊情報の場合
			licount = getlistCount("tokusyulist");
			break;

		default:
			break;
		}
		for (var key in infolist) {
			var data = infolist[key];
			var control = ContorolInfo.getControl(data.control, controlKey);
			if(control == undefined) { //指定したキー制御情報が存在しない場合
				control = ContorolInfo.getControl(data.control, "0000"); //デフォルトキーで再検索
				if(control == undefined) { //デフォルトキー制御情報が存在しない場合
					continue;
				}
			}
			if (licount == 0) {
				ContorolInfo.setRequired(data.item, control.required);	//必須項目編集
				ContorolInfo.setControl(data.item, control.disabled);	//制御項目編集
			}
			else {
				for (var key = 0; key < licount; key++) {
					ContorolInfo.setRequired(data.item + key, control.required);	//必須項目編集
					ContorolInfo.setControl(data.item + key, control.disabled);	//制御項目編集
					//----- 改造情報(分納) -----
			    var oPreantCard = $("#kaizo" + key);
			    if (oPreantCard != undefined) {
			      var bcount = oPreantCard.children("li").length;
		        for (var key2 = 0; key2 < bcount; key2++) {
	            var gkey = ((parseInt(key) * 100) + parseInt(key2));
	            ContorolInfo.setRequired(data.item + gkey, control.required);  //必須項目編集
	            ContorolInfo.setControl(data.item + gkey, control.disabled);  //制御項目編集
		        }
			    }
          //----- 特殊改造(分納) -----
          var oPreantCardt = $("#tokusyu" + key);
          if (oPreantCardt != undefined) {
            var bcountt = oPreantCardt.children("li").length;
            for (var key2 = 0; key2 < bcountt; key2++) {
              var gkey = ((parseInt(key) * 100) + parseInt(key2));
              ContorolInfo.setRequired(data.item + gkey, control.required);  //必須項目編集
              ContorolInfo.setControl(data.item + gkey, control.disabled);  //制御項目編集
            }
          }

				}
			}
		}
	}
}
/*--------------------------------------------------*/
/*----- 指定したコントロールの情報を取得します -----*/
/*--------------------------------------------------*/
ContorolInfo.getControl = function(controlList, id) {
	var control = controlList[id];
	return control;
}
/*----------------------------------------------*/
/*----- 指定したＩＤの制御情報を取得します -----*/
/*----------------------------------------------*/
ContorolInfo.get = function(id) {
var matchData = oControlInfo.filter(function(item, index){
  if (item.item == id) return true;
});
return matchData;
}
/*----------------------------------------------*/
/*----- 必須対象の制御情報を取得します     -----*/
/*----------------------------------------------*/
ContorolInfo.getRequired = function(controlKey) {
  var matchData = oControlInfo.filter(function(item, index){
  var control = ContorolInfo.getControl(item.control, controlKey);
  if (control == undefined) {
	  control = ContorolInfo.getControl(item.control, "0000");
	  if (control == undefined) {
		  return false;
	  }
  }
  if (control.required && (item.item != "kataban" && item.item != "tkataban")) return true;
});
return matchData;
}
/*----------------------------------------------*/
/*----- 該当区分の制御情報を取得します     -----*/
/*----------------------------------------------*/
ContorolInfo.getInfo = function(info) {
	var matchData = oControlInfo.filter(function(item, index){
	  if (item.info == info) return true;
	});
	return matchData;
}
/*------------------------------------------*/
/*----- 必須項目編集                   -----*/
/*------------------------------------------*/
ContorolInfo.setRequired = function(id, required) {
  if (required) { //必須項目の場合
	  $("#" + id).addClass('required');
	  $("#" + id + '-l').addClass('required');
	  $("#" + id + '-l').text($("#" + id + '-l').attr('data') + '※');
  }
  else {
	  $("#" + id).removeClass('required');
	  $("#" + id + '-l').removeClass('required');
	  $("#" + id + '-l').text($("#" + id).attr('data'));
  }
}
/*------------------------------------------*/
/*----- 項目制御                       -----*/
/*------------------------------------------*/
ContorolInfo.setControl = function(id, disabled) {
	  if ($("#" + id).hasClass('btn') || $("#" + id).hasClass('stts-chip') || $("#" + id).hasClass('delete') || $("#" + id).hasClass('lineadd')) { //ボタンの場合
		  if (disabled) {
			  $("#" + id).hide();
		  }
		  else {
			  $("#" + id).show();
		  }
	  }
	  else {
		  $("#" + id).prop("disabled", disabled);
	  }
}
/*------------------------------------------*/
/*----- エラーテキストクリア           -----*/
/*------------------------------------------*/
ContorolInfo.clearError = function() {
  $('.error-text').removeClass('error-text');
}
/*------------------------------------------*/
/*----- 必須チェック(全件)             -----*/
/*------------------------------------------*/
ContorolInfo.checkRequired = function(controlKey) {
  var result = true;
  var licount = 0;

  var requireds = ContorolInfo.getRequired(controlKey);

  if (requireds != undefined) {
    for (var key in requireds) {
    	var required = requireds[key];
		switch (required.info) {
		case 1: //改造情報の場合
			licount = getlistCount("kaizolist");
			continue;
			break;

		case 2: //特殊情報の場合
			licount = getlistCount("tokusyulist");
			continue;
			break;

		default:
			licount = 0;
			break;
		}
		if (licount == 0) {
	      var item = $("#" + required.item);
	      if (item.val() == "") { //対象項目が未入力の場合
	        Materialize.toast('必須入力項目です(' + $("#" + required.item + "-l").attr("data") + ')', 4000, 'rounded');
	        item.addClass('error-text');
	        item.select();
	        result = false;
	        break;
	      }
		}
		else {
			for (var key = 0; key < licount; key++) {
		      var item = $("#" + required.item + key);
		      if (item.val() == "") { //対象項目が未入力の場合
		        Materialize.toast('必須入力項目です(' + $("#" + required.item + key + "-l").attr("data") + '(' + (key + 1) + '行目))', 4000, 'rounded');
		        item.addClass('error-text');
		        item.select();
		        result = false;
		        break;
		      }
			}
		}

    }
  }
  return result;
}
/*------------------------------------------*/
/*----- 必須チェック(単独)             -----*/
/*------------------------------------------*/
ContorolInfo.singleRequired = function(id, key) {
  var result = true;

  var required = ContorolInfo.get(id);

  if (required != undefined && required[0].required) {
      var item = $("#" + required[0].item + key);
      if (item.val() == "") { //対象項目が未入力の場合
        Materialize.toast('必須入力項目です(' + $("#" + required[0].item + key + "-l").attr("data") + '[' + (key+1) + '行目])', 4000, 'rounded');
        item.addClass('error-text');
        item.select();
        result = false;
      }
 }
  return result;
}
/*------------------------------------------------------------------*/
/*----- 渡された制御区分の単項目チェックを行います             -----*/
/*------------------------------------------------------------------*/
ContorolInfo.singleCheck = function(info) {
  var result = true;

  var controls = ContorolInfo.getInfo(info);

  if (controls != undefined) {
    for (var key in controls) {
      var control = controls[key];
      switch (control.type) {
	  	case "alpha":

			result = ContorolInfo.checkAlpha(control.item);

			break;

		case "alnum":

			result = ContorolInfo.checkAlphaNumber(control.item);

			break;

		case "num":

			result = ContorolInfo.checkNumber(control.item);

			break;

		case "date":

			break;

		case "wide":

			//result = ContorolInfo.checkWideString(control.item);

			break;

		case "kana":

			result = ContorolInfo.checkKanaString(control.item);

			break;

		case "post":

			break;

		case "tel":

			break;

		default:
			break;
		}
      if (!result) {
        break;
      }
    }
  }
  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの日付項目が未来日かチェックする -----*/
/*------------------------------------------------------*/
ContorolInfo.checkFuture = function(id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
  	  return true;
    }
    var systemdata = getNowYMD(); //システム日付を取得
    if (!checkDateFromTo(systemdata, new String(oVal.val()))) { //対象フィールドの日付とシステム日付を比較
      Materialize.toast('未来の日付を入力してください(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの数値チェックを行う             -----*/
/*------------------------------------------------------*/
ContorolInfo.checkNumber = function (id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
      return true;
    }
    if(!oVal.val().match(/^\d+$/)) {
      Materialize.toast('数値のみ入力できます(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの英字チェックを行う             -----*/
/*------------------------------------------------------*/
ContorolInfo.checkAlpha = function(id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
      return true;
    }
    if(!oVal.val().match(/^([a-zA-Z]|\s)+$/)) {
      Materialize.toast('英字のみ入力できます(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの英数字チェックを行う             -----*/
/*------------------------------------------------------*/
ContorolInfo.checkAlphaNumber = function(id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
      return true;
    }
    if(!oVal.val().match(/^[\u0020-\u007e]+$/)) {
      Materialize.toast('英数字のみ入力できます(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの全角チェックを行う             -----*/
/*------------------------------------------------------*/
ContorolInfo.checkWideString = function(id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
      return true;
    }
    if(!oVal.val().match(/^([a-zA-Z]|[\uff65-\uff9f]|\s|\d)+$/)) {
      Materialize.toast('全角文字のみ入力できます(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
/*------------------------------------------------------*/
/*----- 渡されたIDの半角カナチェックを行う         -----*/
/*------------------------------------------------------*/
ContorolInfo.checkKanaString = function(id) {

  var result = true;
  var info = ContorolInfo.get(id);
  var oVal = $("#" + id);
  if (!info[0].disabled) {
    if (!oVal.val()) { //未入力の場合はチェックしない
      return true;
    }
    if(!oVal.val().match(/^([\uff65-\uff9f]|\u0020|\s)+$/)) {
      Materialize.toast('半角カナのみ入力できます(' + $("#" + id +"-l").attr("data") + ')', 4000, 'rounded');
      oVal.addClass('error-text');
      oVal.select();
      result = false;
    }
  }

  return result;
}
