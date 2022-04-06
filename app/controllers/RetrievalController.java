package controllers;

import java.util.List;

import models.MstUser;
import models.MtmHead;
import parameter.MtmRetrievalMatch;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 見積検索コントローラー
 * @author kimura
 *
 */
public class RetrievalController extends BaseController {

	/**
	 * 見積検索画面遷移
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move(String mode) {
		//セッション定義に見積検索モードを格納する
        parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
        sessionkeys.mode = mode;
        setSessionKeys(sessionkeys);

        return ok(views.html.retrieval.render());
    }
	/**
	 * 見積検索画面モード取得
	 * @return
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result getmode() {
		//セッション定義より見積検索モードを取得する
        parameter.SessionKeys sessionkeys = getSessionKeys();
        ObjectNode resultJson = Json.newObject();

        getUserInfo();

        Logger.info("[Retrieval] USER={} MODE={} GET MODE.", user.userId, sessionkeys.mode);

  	  	resultJson.put("mode", sessionkeys.mode);

        return ok(resultJson);
    }
	/**
	 * 見積検索他画面遷移前処理
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result moveother() {
        ObjectNode resultJson = Json.newObject();

        getUserInfo();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String sMtmno = inputParameter.get("mtmno").asText();
        String sMode  = inputParameter.get("mode").asText();
        //該当見積の最大枝番を取得
        long edaban = MtmHead.GetMaxEdaban(sMtmno);

        Logger.info("[Retrieval] USER={} MODE={} MTMNO={} EDABAN={} MOVE OTHER.", user.userId, sMode, sMtmno, edaban);

    	//------------------------------------------------------------------------------------
    	//- セッションキーに格納する
    	//------------------------------------------------------------------------------------
        parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
        sessionkeys.mtmno  	= sMtmno;
        sessionkeys.edaban 	= edaban;
        sessionkeys.mode 	= sMode;
        setSessionKeys(sessionkeys);

  	  	resultJson.put("result", "success");
  	  	resultJson.put("mode", sessionkeys.mode);

        return ok(resultJson);
    }
    /**
     * 見積検索
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result retrieval() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String sMtmnof = inputParameter.get("mtmnof").asText();
        String sMtmnot = inputParameter.get("mtmnot").asText();
        String sKnrino = inputParameter.get("knrino").asText();
        String sRequef = inputParameter.get("requef").asText();
        String sRequet = inputParameter.get("requet").asText();
        String sAnserf = inputParameter.get("anserf").asText();
        String sAnsert = inputParameter.get("ansert").asText();
        String sKenmei = inputParameter.get("kenmei").asText();
        String sTokuno = inputParameter.get("tokuno").asText();
        String sTokunm = inputParameter.get("tokunm").asText();
        boolean bInput = inputParameter.get("mtinput").asBoolean();
        boolean bAnser = inputParameter.get("mtanser").asBoolean();
        boolean bOrder = inputParameter.get("mtorder").asBoolean();
        boolean bCommit = inputParameter.get("mtcommit").asBoolean();
        boolean bSekou = inputParameter.get("mtsekou").asBoolean();

        Logger.info(">>> MTM RETRIEVAL START. ");
        Logger.info("[mtmnof] MATCH={}. ", sMtmnof);
        Logger.info("[mtmnot] MATCH={}. ", sMtmnot);
        Logger.info("[knrino] MATCH={}. ", sKnrino);
        Logger.info("[requef] MATCH={}. ", sRequef);
        Logger.info("[requet] MATCH={}. ", sRequet);
        Logger.info("[anserf] MATCH={}. ", sAnserf);
        Logger.info("[ansert] MATCH={}. ", sAnsert);
        Logger.info("[kenmei] MATCH={}. ", sKenmei);
        Logger.info("[tokuno] MATCH={}. ", sTokuno);
        Logger.info("[tokunm] MATCH={}. ", sTokunm);
        Logger.info("[mtinput] MATCH={}. ", bInput);
        Logger.info("[mtanser] MATCH={}. ", bAnser);
        Logger.info("[mtorder] MATCH={}. ", bOrder);
        Logger.info("[mtcommit] MATCH={}. ", bCommit);
        Logger.info("[mtsekou] MATCH={}. ", bSekou);
        Logger.info(">>> MTM RETRIEVAL END. ");

    	//------------------------------------------------------------------------------------
    	//- 条件に一致する見積データを取得する
    	//------------------------------------------------------------------------------------
        MtmRetrievalMatch mrm = new MtmRetrievalMatch();
        mrm.mtmnof = sMtmnof;
        mrm.mtmnot = sMtmnot;
        mrm.kanri  = sKnrino;
        mrm.requef = sRequef;
        mrm.requet = sRequet;
        mrm.anserf = sAnserf;
        mrm.ansert = sAnsert;
        mrm.kenmei = sKenmei;
        mrm.tokuno = sTokuno;
        mrm.tokunm = sTokunm;
        mrm.input = bInput;
        mrm.anser = bAnser;
        mrm.order = bOrder;
        mrm.commit = bCommit;
        mrm.sekou = bSekou;

        getUserInfo();

        parameter.SessionKeys sessionkeys = getSessionKeys();
        Logger.info("[Retrieval] USER={} MODE={} GET MODE.", user.userId, sessionkeys.mode);

        //------------------------------------------------------------------------------------
        //- 製造番号入力時は見積状態を受注確定と施工完了のみにしぼる
        //------------------------------------------------------------------------------------
        if ("SerialNumber".equals(sessionkeys.mode)) {
          if (mrm.commit == false && mrm.sekou == false) {
            mrm.commit = true;
            mrm.sekou = true;
          }
        }

        if (MstUser.Kengen.EIGYO.equals(user.kengen)) { //ログインユーザが営業担当者の場合
          //自支店のみ参照可能とする
          mrm.kaisya = user.kaisyaCd;
          mrm.shiten = user.shitenCd;
          mrm.bumon  = user.bumonCd;
        }
        //LEフロント営業対応
        mrm.kengen = user.kengen;

        List<MtmHead> dataList = MtmHead.GetMitumoriRetrieval(mrm);

        if (dataList.size() > 0) {
          for (MtmHead data : dataList) { //一致した見積件名分
            ObjectNode dataJson = Json.newObject();                            //データの生成
            dataJson.put("mno", data.mitumoriNo);                              //見積番号
            dataJson.put("kno", data.kanriNo);                                 //管理番号
            dataJson.put("stts", getSttsName(data.mitumoriJotai));             //見積ステータス
            dataJson.put("irai", dateStringFormat(data.iraiNoki));			       //依頼納期
            dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));		       //回答納期
            dataJson.put("kouji", data.kojiKenmei);                            //工事件名
            dataJson.put("tokui", data.tokuisakiName);                         //得意先
            MstUser.getUserToJson(data.sakuseiUid, dataJson);                  //見積依頼者情報
            listJson.put(data.mitumoriNo, dataJson);                           //JSONリストに格納
          }
    	  resultJson.put("retrieval", true);
        }
        else {
    	  resultJson.put("retrieval", false);
        }

        resultJson.put("result", "success");
  	  	resultJson.put("mode", sessionkeys.mode);
	    resultJson.put("datalist", listJson);

        return ok(resultJson);
    }
}
