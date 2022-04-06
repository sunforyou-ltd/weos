package controllers;

import java.util.List;

import models.MstBumon;
import models.MstKaisya;
import models.MstShiten;
import models.MstUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ユーザメンテナンスコントローラー
 * @author kimura
 *
 */
public class UserMntController extends BaseController {

	/**
	 * ユーザメンテナンス画面遷移
	 * @return ユーザメンテナンス画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {
        return ok(views.html.usermnt.render());
    }
    /**
     * ユーザＩＤ確定時
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result useridcommit() {

        ObjectNode resultJson = Json.newObject();
        String result = "success";

        getSessionUser();

        //------------------------------------------------------------------------------------
        //- パラメータの取得
        //------------------------------------------------------------------------------------
        JsonNode  inputParameter  = request().body().asJson();
        String userId = inputParameter.get("userid").asText();

        Logger.info(">>>>> [USER MNT] ACTION USER={} COMMIT USER={}", user.userId, userId);

        String sShimeiKanji = "";
        String sShimeiKana  = "";
        String sKaisyaCd    = "";
        String sShitenCd    = "";
        String sBumonCd     = "";
        String skengen      = "";

        //------------------------------------------------------------------------------------
        //- ユーザ情報の設定
        //------------------------------------------------------------------------------------
        if (null == userId || "".equals(userId)) {
          /* ユーザＩＤが空の場合は新規追加 */
          //何もしない
          sKaisyaCd     = user.kaisyaCd;
          sShitenCd     = user.shitenCd;
          sBumonCd      = user.bumonCd;
          skengen       = user.kengen;
          resultJson.put("mode"  , "add");
          Logger.info("[USER MNT] USER={} ADD MODE.", user.userId);
        }
        else {
          /* ユーザＩＤありの場合は変更 */
          MstUser mUser;
          if (MstUser.Kengen.ST.equals(user.kengen)) {
            mUser = MstUser.getUser(userId);
          }
          else {
            mUser = MstUser.getUser(userId, user.kaisyaCd, user.shitenCd);
          }
          if (mUser == null) {
              result = "notfound";
              sKaisyaCd     = user.kaisyaCd;
              sShitenCd     = user.shitenCd;
              sBumonCd      = user.bumonCd;
              skengen       = user.kengen;
              Logger.warn("[USER MNT] USER={} NOTFOUND ERROR.", user.userId);
          }
          else {
              sShimeiKanji  = mUser.shimeiKanji;
              sShimeiKana   = mUser.shimeiKana;
              sKaisyaCd     = mUser.kaisyaCd;
              sShitenCd     = mUser.shitenCd;
              sBumonCd      = mUser.bumonCd;
              skengen       = mUser.kengen;
              resultJson.put("mode"  , "mod");
              Logger.info("[USER MNT] USER={} MODIFY MODE.", user.userId);
          }

        }

        //JSONにユーザ情報をセット
        resultJson.put("userid"   , userId);
        resultJson.put("kanji"    , sShimeiKanji);
        resultJson.put("kana"     , sShimeiKana);
        resultJson.put("kaisya"   , sKaisyaCd);
        resultJson.put("shiten"   , sShitenCd);
        resultJson.put("bumon"    , sBumonCd);
        resultJson.put("kengen"   , skengen);
        resultJson.put("ukengen"  , user.kengen);

        resultJson.put("result"   , result);

        return ok(resultJson);
    }
    /**
     * 会社一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getkaisyalist() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

        //------------------------------------------------------------------------------------
        //- 会社マスタの全件取得
        //------------------------------------------------------------------------------------
        List<MstKaisya> kaisyas = MstKaisya.GetKaisyaAllList();

        getSessionUser();

        for (MstKaisya data : kaisyas) {

            if (!MstUser.Kengen.ST.equals(user.kengen)) { //ユーザ権限がサンデンテクノ以外の場合

            	//自社以外のデータはスキップ
            	if (!data.kaisyaCd.equals(user.kaisyaCd)) {
            		continue;
            	}

            }

            ObjectNode dataJson   = Json.newObject();
            dataJson.put("value", data.kaisyaCd);		//コード
            dataJson.put("name", data.kaisyaName);		//名称

            listJson.put(data.kaisyaCd, dataJson);
            Logger.debug("[USER MNT] USER={} KAISYA INFO VALUE={}.", user.userId, data.kaisyaCd);

        }

        resultJson.put("datalist", listJson);
        resultJson.put("result"  , "success");

        return ok(resultJson);
    }
    /**
     * 支店一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getshitenlist(String kaisyacd) {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

        //------------------------------------------------------------------------------------
        //- 指定された会社コードの支店マスタ取得
        //------------------------------------------------------------------------------------
        List<MstShiten> shitens = MstShiten.GetShitenList(kaisyacd);

        for (MstShiten data : shitens) {

            ObjectNode dataJson   = Json.newObject();
            dataJson.put("value", data.shitenCd);		//コード
            dataJson.put("name", data.shitenName);		//名称

            listJson.put(data.shitenCd, dataJson);

		}

        resultJson.put("datalist", listJson);
        resultJson.put("result"  , "success");

        return ok(resultJson);
    }
    /**
     * 部門一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getbumonlist(String kaisyacd, String shitencd) {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

        //------------------------------------------------------------------------------------
        //- 指定された会社コードの支店マスタ取得
        //------------------------------------------------------------------------------------
        List<MstBumon> bumons = MstBumon.GetBumonList(kaisyacd, shitencd);

        for (MstBumon data : bumons) {

            ObjectNode dataJson   = Json.newObject();
            dataJson.put("value", data.bumonCd);		//コード
            dataJson.put("name", data.bumonName);		//名称

            listJson.put(String.valueOf(Long.parseLong(data.bumonCd)), dataJson);

		}

        resultJson.put("datalist", listJson);
        resultJson.put("result"  , "success");

        return ok(resultJson);
    }
    /**
     * 権限一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getkengenlist() {

        ObjectNode resultJson = Json.newObject();

        //------------------------------------------------------------------------------------
        //- ユーザ権限の一覧を取得
        //------------------------------------------------------------------------------------
        ObjectNode listJson   = MstUser.getKengenList();

        resultJson.put("datalist", listJson);
        resultJson.put("result"  , "success");

        return ok(resultJson);
    }
    /**
     * ユーザ情報変更確定時
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result usermntcommit() {

        ObjectNode resultJson = Json.newObject();
        boolean result;

        //------------------------------------------------------------------------------------
        //- パラメータの取得
        //------------------------------------------------------------------------------------
        JsonNode  inputParameter  = request().body().asJson();
        String userId 	= inputParameter.get("userid").asText();
        String mode 	= inputParameter.get("mode").asText();
        MstUser mUser = MstUser.getUser(userId);

        getUserInfo();

        if (mUser != null) {    //正常に取得できた場合

        	mUser.shimeiKanji 	= inputParameter.get("kanji").asText();
        	mUser.shimeiKana  	= inputParameter.get("kana").asText();
        	mUser.kaisyaCd 		= inputParameter.get("kaisya").asText();
        	mUser.shitenCd		= inputParameter.get("shiten").asText();
        	mUser.bumonCd 		= inputParameter.get("bumon").asText();
          if ("null".equals(mUser.bumonCd)) {
            mUser.bumonCd = "";
          }
        	mUser.kengen 		= inputParameter.get("kengen").asText();
        	result = mUser.updateUser();

        	if (result) {
                resultJson.put("userId"  , mUser.userId);
                resultJson.put("result"  , "success");
            }
            else {
                resultJson.put("result"  , "error");
            }
        }
        else {
        	if ("add".equals(mode)) {
        		  mUser = new MstUser();
            	mUser.shimeiKanji 	= inputParameter.get("kanji").asText();
            	mUser.shimeiKana  	= inputParameter.get("kana").asText();
            	mUser.kaisyaCd 		= inputParameter.get("kaisya").asText();
            	mUser.shitenCd		= inputParameter.get("shiten").asText();
            	mUser.bumonCd 		= inputParameter.get("bumon").asText();
            	if ("null".equals(mUser.bumonCd)) {
            	  mUser.bumonCd = "";
            	}
            	mUser.kengen 		= inputParameter.get("kengen").asText();
            	Logger.info("shimeiKanji={}",mUser.shimeiKanji);
              Logger.info("shimeiKana={}",mUser.shimeiKana);
              Logger.info("kaisyaCd={}",mUser.kaisyaCd);
              Logger.info("shitenCd={}",mUser.shitenCd);
              Logger.info("bumonCd={}",mUser.bumonCd);
              Logger.info("kengen={}",mUser.kengen);
            	result = mUser.insertUser();

            	if (result) {
                    resultJson.put("userId"  , mUser.userId);
                    resultJson.put("result"  , "success");
                }
                else {
                    resultJson.put("result"  , "error");
                }
        	}
        	else {
            	resultJson.put("result"  , "notfound");
        	}
        }

        return ok(resultJson);
    }
  /**
   * ユーザ削除時
   * @return
   */
@Authenticated(WeosAuthenticator.class)
  public static Result userdelete() {

      ObjectNode resultJson = Json.newObject();

      //------------------------------------------------------------------------------------
      //- パラメータの取得
      //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      String userId = inputParameter.get("userid").asText();

      //------------------------------------------------------------------------------------
      //- ユーザ情報を取得し、パスワードのリセットを行う
      //------------------------------------------------------------------------------------
      MstUser mUser = MstUser.getUser(userId);

      if (mUser != null) {    //正常に取得できた場合
        boolean result = mUser.deleteUser();  //ユーザを削除する
        if (result) {
          resultJson.put("result"  , "success");
        }
        else {
          resultJson.put("result"  , "error");
        }
      }
      else {
        resultJson.put("result"  , "notfound");
      }

      return ok(resultJson);
  }
    /**
     * パスワードリセット時
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result passwordreset() {

        ObjectNode resultJson = Json.newObject();

        //------------------------------------------------------------------------------------
        //- パラメータの取得
        //------------------------------------------------------------------------------------
        JsonNode  inputParameter  = request().body().asJson();
        String userId = inputParameter.get("userid").asText();

        //------------------------------------------------------------------------------------
        //- ユーザ情報を取得し、パスワードのリセットを行う
        //------------------------------------------------------------------------------------
        MstUser mUser = MstUser.getUser(userId);

        if (mUser != null) {    //正常に取得できた場合
          boolean result = mUser.passwordInit(); //パスワードを初期化する
          if (result) {
            resultJson.put("result"  , "success");
          }
          else {
            resultJson.put("result"  , "error");
          }
        }
        else {
          resultJson.put("result"  , "notfound");
        }

        return ok(resultJson);
    }
}
