package controllers;

import models.MtmHead;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * テスト遷移コントローラー
 * @author kimura
 *
 */
public class TestController extends BaseController {

	/**
	 * テスト遷移画面
	 * @return テスト遷移画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {
        return ok(views.html.test.render());
    }
  /**
   * テスト確認用
   * @return 確認用JSONコード
   */
  @Authenticated(WeosAuthenticator.class)
    public static Result testcommit() {
        ObjectNode resultJson = Json.newObject();
        parameter.SessionKeys sessionkeys = getSessionKeys();

        resultJson.put("mtmno", sessionkeys.mtmno);
        resultJson.put("edaban", sessionkeys.edaban);

        return ok(resultJson);
    }
  /**
   * テスト製造画面への画面遷移
   * @return
   */
  public static Result testseizou() {

      ObjectNode resultJson = Json.newObject();

    //------------------------------------------------------------------------------------
    //- パラメータの取得
    //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      String mtmno = inputParameter.get("mtmno").asText();
      String edaban = inputParameter.get("edaban").asText();

    //------------------------------------------------------------------------------------
    //- 見積番号の存在チェック
    //------------------------------------------------------------------------------------
      MtmHead head = MtmHead.find.where().eq("mitumori_no", mtmno).eq("edaban", Long.valueOf(edaban)).findUnique();

      if (head == null) {
        resultJson.put("result", "notfound");
      }
      else {

        parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
        sessionkeys.mtmno  = mtmno;
        sessionkeys.edaban = Long.valueOf(edaban);

        setSessionKeys(sessionkeys);

        resultJson.put("result", "success");

      }

      return ok(resultJson);
  }
  /**
   * JSONパラメータ取得テスト
   * @return
   */
  public static Result testjson() {

      ObjectNode resultJson = Json.newObject();

    //------------------------------------------------------------------------------------
    //- パラメータの取得
    //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      String mtmno = inputParameter.get("mtmno").asText();
      String edaban = inputParameter.get("edaban").asText();
      JsonNode  jsonList = inputParameter.get("jsonlist");

      for (JsonNode json : jsonList) {
    	  Logger.info(json.get("kataban").asText());
    	  Logger.info(json.get("kaizo").asText());
    	  Logger.info(json.get("seizo").asText());
    	  Logger.info(json.get("tanto").asText());
      }

      return ok(resultJson);
  }
}
