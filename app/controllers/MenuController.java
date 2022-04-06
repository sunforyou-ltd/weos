package controllers;

import java.util.ArrayList;
import java.util.List;

import models.MstUser;
import models.MtmHead;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * メニューコントローラー
 * @author kimura
 *
 */
public class MenuController extends BaseController {

	/**
	 * メニュー画面遷移
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {
        return ok(views.html.menu.render());
    }
    /**
     * 見積件名のステータス別件数を取得する。
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result countofstts() {

        ObjectNode resultJson = Json.newObject();

        int iInput  = 0;	//見積依頼件数
        int iAnser  = 0;	//見積回答済件数
        int iOrder  = 0;	//発注件数
        int iCommit = 0;	//受注確定件数
        int iSekou  = 0;	//施工完了件数

        getSessionUser();

        //各見積状態の件数を取得する
        if (MstUser.Kengen.ST.equals(user.kengen)) { //ユーザ権限がサンデンテクノの場合
        	iInput  = MtmHead.GetMTInput();
        	iAnser  = MtmHead.GetMTAnser();
        	iOrder  = MtmHead.GetMTOrder();
        	iCommit = MtmHead.GetMTCommit();
        	iSekou  = MtmHead.GetMTSekou();
        }
        else { //サンデンテクノ以外の場合
        	iInput  = MtmHead.GetMTInput(user.kaisyaCd, user.shitenCd);
        	iAnser  = MtmHead.GetMTAnser(user.kaisyaCd, user.shitenCd);
        	iOrder  = MtmHead.GetMTOrder(user.kaisyaCd, user.shitenCd);
        	iCommit = MtmHead.GetMTCommit(user.kaisyaCd, user.shitenCd);
        	iSekou  = MtmHead.GetMTSekou(user.kaisyaCd, user.shitenCd);
        }

        //各ステータス件数をJSONに格納
        resultJson.put("input", iInput);
        resultJson.put("anser", iAnser);
        resultJson.put("order", iOrder);
        resultJson.put("commit", iCommit);
        resultJson.put("sekou", iSekou);

        return ok(resultJson);
    }
    /**
     * 指定された見積ステータスの見積件名一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getMtStts() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();		//データ一覧

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String stts = inputParameter.get("stts").asText();
    	//------------------------------------------------------------------------------------
    	//- 見積件名の取得
    	//------------------------------------------------------------------------------------
        List<MtmHead> dataList = new ArrayList<MtmHead>();

        getSessionUser();

        if (MstUser.Kengen.ST.equals(user.kengen)) { //ユーザ権限がサンデンテクノの場合
            dataList = MtmHead.GetMTStts(stts);
        }
        else { //サンデンテクノ以外の場合
            dataList = MtmHead.GetMTStts(stts, user.kaisyaCd, user.shitenCd);
        }

        for (MtmHead data : dataList) { //一致した見積件名分
            ObjectNode dataJson = Json.newObject();                            	//データの生成
            dataJson.put("mno", data.mitumoriNo);								//見積番号
            dataJson.put("kno", data.kanriNo);									//管理番号
            dataJson.put("stts", getSttsName(data.mitumoriJotai));				//見積ステータス
            dataJson.put("irai", dateStringFormat(data.iraiNoki));				//依頼納期
            dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));			//回答納期
            dataJson.put("kouji", data.kojiKenmei);								//工事件名
            dataJson.put("tokui", data.tokuisakiName);							//得意先
            MstUser.getUserToJson(data.sakuseiUid, dataJson);					//見積依頼者情報
            listJson.put(data.mitumoriNo, dataJson);							//JSONリストに格納
        }

        resultJson.put("datalist", listJson);

        return ok(resultJson);
    }
    /**
     * 決済待ちアラートデータを取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getKessaiAlert() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();   //データ一覧
        boolean alert = false;

      //------------------------------------------------------------------------------------
      //- 決済アラートデータの取得
      //------------------------------------------------------------------------------------

        getSessionUser();

        if (MstUser.Kengen.FRONT.equals(user.kengen)) { //ユーザ権限がフロント営業部の場合
          List<MtmHead> dataList = MtmHead.GetKessaiAlert(user.kaisyaCd);
          if (dataList.size() > 0) {
            alert = true;
            for (MtmHead data : dataList) { //一致した見積件名分
              ObjectNode dataJson = Json.newObject();                            //データの生成
              dataJson.put("mno", data.mitumoriNo);                              //見積番号
              dataJson.put("kno", data.kanriNo);                                 //管理番号
              dataJson.put("stts", getSttsName(data.mitumoriJotai));             //見積ステータス
              dataJson.put("irai", dateStringFormat(data.iraiNoki));			 //依頼納期
              dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));			 //回答納期
              dataJson.put("kouji", data.kojiKenmei);                            //工事件名
              dataJson.put("tokui", data.tokuisakiName);                         //得意先
              MstUser.getUserToJson(data.sakuseiUid, dataJson);                  //見積依頼者情報
              listJson.put(data.mitumoriNo, dataJson);                           //JSONリストに格納
            }
          }
        }

        resultJson.put("alert", alert);
        resultJson.put("datalist", listJson);

        return ok(resultJson);
    }
    /**
     * 回答待ちアラートデータを取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getAnserAlert() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();   //データ一覧
        boolean alert = false;

      //------------------------------------------------------------------------------------
      //- 回答待ちアラートデータの取得
      //------------------------------------------------------------------------------------

        getSessionUser();

        if (MstUser.Kengen.ST.equals(user.kengen)) { //ユーザ権限がサンデンテクノの場合
          List<MtmHead> dataList = MtmHead.GetAnserAlert();
          if (dataList.size() > 0) {
            alert = true;
            for (MtmHead data : dataList) { //一致した見積件名分
              ObjectNode dataJson = Json.newObject();                            //データの生成
              dataJson.put("mno", data.mitumoriNo);                              //見積番号
              dataJson.put("kno", data.kanriNo);                                 //管理番号
              dataJson.put("stts", getSttsName(data.mitumoriJotai));             //見積ステータス
              dataJson.put("irai", dateStringFormat(data.iraiNoki));			 //依頼納期
              dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));			 //回答納期
              dataJson.put("kouji", data.kojiKenmei);                            //工事件名
              dataJson.put("tokui", data.tokuisakiName);                         //得意先
              MstUser.getUserToJson(data.sakuseiUid, dataJson);                  //見積依頼者情報
              listJson.put(data.mitumoriNo, dataJson);                           //JSONリストに格納
            }
          }
        }

        resultJson.put("alert", alert);
        resultJson.put("datalist", listJson);

        return ok(resultJson);
    }
    /**
     * 一時保存アラートデータを取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getSaveAlert() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();   //データ一覧
        boolean alert = false;

      //------------------------------------------------------------------------------------
      //- 一時保存アラートデータの取得
      //------------------------------------------------------------------------------------

        getSessionUser();

        List<MtmHead> dataList = MtmHead.GetSaveAlert(user.userId);
        if (dataList.size() > 0) {
	        alert = true;
	        for (MtmHead data : dataList) { //一致した見積件名分
	          ObjectNode dataJson = Json.newObject();                            //データの生成
	          dataJson.put("mno", data.mitumoriNo);                              //見積番号
	          dataJson.put("kno", data.kanriNo);                                 //管理番号
	          dataJson.put("stts", getSttsName(data.mitumoriJotai));             //見積ステータス
	          dataJson.put("irai", dateStringFormat(data.iraiNoki));			 //依頼納期
	          dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));			 //回答納期
	          dataJson.put("kouji", data.kojiKenmei);                            //工事件名
	          dataJson.put("tokui", data.tokuisakiName);                         //得意先
	          MstUser.getUserToJson(data.sakuseiUid, dataJson);                  //見積依頼者情報
	          listJson.put(data.mitumoriNo, dataJson);                           //JSONリストに格納
	        }
        }

        resultJson.put("alert", alert);
        resultJson.put("datalist", listJson);

        return ok(resultJson);
    }
    /**
     * パスワード変更を行います
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result passcommit() {

        ObjectNode resultJson = Json.newObject();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String password = inputParameter.get("password").asText();
        String newpassword = inputParameter.get("newpassword").asText();

    	//------------------------------------------------------------------------------------
    	//- パスワードの変更を行います
    	//------------------------------------------------------------------------------------
        getSessionUser();

        if (!user.password.equals(password)) { //現在のパスワードが違う場合
            resultJson.put("commit", false);
        }
        else {
        	//新しいパスワードに変更する
        	user.password = newpassword;
        	if (user.passwordCommit()) {
                resultJson.put("commit", true);
        	}
        	else {
                resultJson.put("commit", false);
        	}
        }

        resultJson.put("result", "success");
        return ok(resultJson);
    }
	/**
	 * 見積作成画面遷移(新規作成)
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result moveEstimate() {
        ObjectNode resultJson = Json.newObject();

        getUserInfo();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------

        Logger.info("[Menu] USER={} MODE={} MTMNO={} EDABAN={} MOVE OTHER.", user.userId, "ADD");

    	//------------------------------------------------------------------------------------
    	//- セッションキーに格納する
    	//------------------------------------------------------------------------------------
        parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
        sessionkeys.mtmno  	= "";
        sessionkeys.edaban 	= 0;
        sessionkeys.mode 	= "ADD";
        setSessionKeys(sessionkeys);

        return redirect(BaseController.URL_PREFIX  + "/moveestimate");
    }
}
