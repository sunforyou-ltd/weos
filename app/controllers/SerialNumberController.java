package controllers;

import java.util.List;

import models.MsiKaizoKanryo;
import models.MtmHead;
import models.MtmTaisyoSeihin;
import models.MtmTokusyuKaizo;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 製造番号入力コントローラー
 * @author morinaga
 *
 */
public class SerialNumberController extends BaseController {

	/**
	 * 製造番号入力画面遷移
	 * @return 製造番号入力画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {
        return ok(views.html.serialnumber.render());
    }
    /**
     * 初期表示
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result doInit() {

    	int dtIdx = 0;
    	ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

        //------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
        sessionkeys = getSessionKeys();
        String sMtmno = sessionkeys.mtmno;
        long lEdaban = sessionkeys.edaban;

        Logger.info("[param] sMtmno={}. ", sMtmno);
        Logger.info("[param] sEdaban={}. ", lEdaban);

		// --------------------------------------------
		//  見積ﾍｯﾀﾞ検索
		// --------------------------------------------
        MtmHead head = MtmHead.find.where().eq("mitumori_no", sMtmno).eq("edaban", lEdaban).findUnique();

		// --------------------------------------------
		//  改造完了明細検索
		// --------------------------------------------
        List<MsiKaizoKanryo> dataList = MsiKaizoKanryo.GetKaizoKanryo(sMtmno,lEdaban);

        if (dataList.size() > 0) {
          //改造完了明細あり
          for (MsiKaizoKanryo data : dataList) { //改造完了明細分
            ObjectNode dataJson = Json.newObject();                            //データの生成
            getHeadToJson(head, dataJson);                                     //見積ヘッダ情報
            dataJson.put("gyo", data.gyoNo);                                   //行番号
            dataJson.put("kataban", data.taisyoKataban);                       //対象型番
            dataJson.put("kaizo", data.taisyoNaiyo);						   //対象内容
            dataJson.put("seizo", data.seizoNo);							   //製造番号
            dataJson.put("tanto", data.sekouName);                             //施工者名
            listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(data.gyoNo), dataJson);  //JSONリストに格納

            Logger.info("[outData] gyo={}. ", data.gyoNo);
            Logger.info("[outData] kataban={}. ", data.taisyoKataban);
            Logger.info("[outData] naniyo={}. ", data.taisyoNaiyo);
            Logger.info("[outData] sno={}. ", data.seizoNo);
            Logger.info("[outData] sekou={}. ", data.sekouName);
          }
    	  resultJson.put("mode", "update");
        }
        else {
          //改造完了明細なし
		  // --------------------------------------------
		  //  対象製品検索
		  // --------------------------------------------
          List<MtmTaisyoSeihin> taisyoList = MtmTaisyoSeihin.GetMTTaisyouSeihin(sMtmno,lEdaban);

          for (MtmTaisyoSeihin taidata : taisyoList) { //対象製品分
            boolean kaizo = false;
			// 出来上がる明細は （指定改造 ＊ 数量）
			// 防蝕改造
            if(taidata.bosyokuKaizo.equals("1")){
              kaizo = true;
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_BOUSYOKU,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
			// 重防蝕改造
            if(taidata.jubosyokuKaizo.equals("1")){
              kaizo = true;
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_JUBOUSYOK,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
			// 耐塩害改造
            if(taidata.taiengaiKaizo.equals("1")){
              kaizo = true;
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_TAIENGAI,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
			// 耐重塩害改造
            if(taidata.taijuengaiKaizo.equals("1")){
              kaizo = true;
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_TAIJUENGAI,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
			// 重防錆改造
            if(taidata.jubouseiKaizo.equals("1")){
              kaizo = true;
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_JUBOUSEI,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
			// 改造なし
            if(!kaizo){
              for (int j = 0; j < taidata.suryo; j++) {
                ObjectNode dataJson = Json.newObject();                          //データの生成
                getHeadToJson(head, dataJson);                                   //見積ヘッダ情報
                getMeisaiToJson(taidata, dataJson, MsiKaizoKanryo.kaizoName.KZ_BUHINKM,dtIdx); //明細情報
                listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);  //JSONリストに格納
                dtIdx = dtIdx + 1;
              }
            }
          }
          // --------------------------------------------
          //  特殊改造検索
          // --------------------------------------------
          List<MtmTokusyuKaizo> kaizoList = MtmTokusyuKaizo.GetMTTokusyu(sMtmno,lEdaban);

          for (MtmTokusyuKaizo kaidata : kaizoList) { //特殊改造分
              for (int j = 0; j < kaidata.suryo; j++) {
	              ObjectNode dataJson = Json.newObject();					//データの生成
	              getHeadToJson(head, dataJson);							//見積ヘッダ情報
	              dataJson.put("gyo", dtIdx);								//行番号
	              dataJson.put("kataban", kaidata.tokusyuKataban);			//対象型番
	              dataJson.put("kaizo", kaidata.tokusyuNaiyo);				//対象内容
	              dataJson.put("seizo", "");									//製造番号
	              dataJson.put("tanto", "");								//施工者名
	              listJson.put(sMtmno+String.valueOf(lEdaban)+String.valueOf(dtIdx), dataJson);	//JSONリストに格納
	              dtIdx = dtIdx + 1;
	              Logger.info("[outData] gyo={}. ", dtIdx);
	              Logger.info("[outData] kataban={}. ", kaidata.tokusyuKataban);
	              Logger.info("[outData] naniyo={}. ", kaidata.tokusyuNaiyo);
              }
          }

    	  resultJson.put("mode", "create");
        }

        resultJson.put("result", "success");
	    resultJson.put("datalist", listJson);

        return ok(resultJson);
    }

    /**
     * 見積ヘッダ情報をJSONに追加する
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
	private static void getHeadToJson(MtmHead head, ObjectNode resultJson) {

        resultJson.put("mno", head.mitumoriNo);                              //見積番号
        resultJson.put("eda", head.edaban);                                  //枝番
        resultJson.put("kno", head.kanriNo);                                 //管理番号
        resultJson.put("lmkei", head.saisyuMitumoriGokei);                   //最終見積金額
        resultJson.put("mkei", head.mitumoriGokei);                          //見積合計金額
        resultJson.put("zei", head.saisyuMitumoriGokei - head.mitumoriGokei);//消費税額
        resultJson.put("tokui", head.tokuisakiName);                         //得意先名称
        resultJson.put("koji", head.kojiKenmei);                             //工事件名

        Logger.info("[outData] mno={}. ", head.mitumoriNo);
        Logger.info("[outData] eda={}. ", head.edaban);
        Logger.info("[outData] kno={}. ", head.kanriNo);
        Logger.info("[outData] lmkei={}. ", head.saisyuMitumoriGokei);
        Logger.info("[outData] mkei={}. ", head.mitumoriGokei);
        Logger.info("[outData] zei={}. ", head.saisyuMitumoriGokei - head.mitumoriGokei);
        Logger.info("[outData] tokui={}. ", head.tokuisakiName);
        Logger.info("[outData] koji={}. ", head.tokuisakiName);
	}

    /**
     * 明細情報（対象製品）をJSONに追加する
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
	private static void getMeisaiToJson(MtmTaisyoSeihin meisai, ObjectNode resultJson,String kaizo, int dtIdx) {

		resultJson.put("gyo", dtIdx);										//行番号
		resultJson.put("kataban", meisai.taisyoKataban);					//対象型番
        resultJson.put("kaizo", kaizo);										//対象内容
        resultJson.put("seizo", "");										//製造番号
        resultJson.put("tanto","");											//施工者名

        Logger.info("[outData] gyo={}. ", dtIdx);
        Logger.info("[outData] kataban={}. ", meisai.taisyoKataban);
        Logger.info("[outData] naniyo={}. ", kaizo);
	}

    /**
     * 製造番号入力変更確定時
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result serialnumbercommit() {

        ObjectNode resultJson = Json.newObject();
        boolean result;

        //------------------------------------------------------------------------------------
        //- パラメータの取得
        //------------------------------------------------------------------------------------
        JsonNode  inputParameter  = request().body().asJson();
        JsonNode  jsonList = inputParameter.get("jsonlist");
        String mode 	= inputParameter.get("mode").asText();
        String mtmno 	= inputParameter.get("mtmno").asText();
        long edaban = Long.parseLong(inputParameter.get("edaban").asText());

        getUserInfo();

    	Logger.info("[param]mode={}",mode);
    	Logger.info("[param]mtmno={}",mtmno);
    	Logger.info("[param]edaban={}",edaban);

		// --------------------------------------------
		//  見積ﾍｯﾀﾞ検索
		// --------------------------------------------
        MtmHead head = MtmHead.find.where().eq("mitumori_no", mtmno).eq("edaban", edaban).findUnique();

        List<MsiKaizoKanryo> dataList = MsiKaizoKanryo.GetKaizoKanryo(mtmno,edaban);

        try {
            if (dataList.size() > 0) {    //正常に取得できた場合
            	// 更新
                for (JsonNode json : jsonList) {
                	MsiKaizoKanryo kanryo = MsiKaizoKanryo.GetMTEdabanGyo(mtmno,edaban,Long.parseLong(json.get("gno").asText()));

                	kanryo.seizoNo = json.get("seizo").asText();
                	kanryo.sekouName = json.get("tanto").asText();

                	Logger.info("[UPDATE]mitumoriNo={}",kanryo.mitumoriNo);
                	Logger.info("[UPDATE]edaban={}",kanryo.edaban);
                	Logger.info("[UPDATE]gyoNo={}",kanryo.gyoNo);
                	Logger.info("[UPDATE]seizoNo={}",kanryo.seizoNo);
                	Logger.info("[UPDATE]seizoNo={}",kanryo.sekouName);

                	result = kanryo.updateKaizoKanryo();

                	if (result) {
                        resultJson.put("result"  , "success");
                    }
                    else {
                        resultJson.put("result"  , "error");
                        break;
                    }
                }
            }
            else {
        		//追加
            	if ("create".equals(mode)) {
                    for (JsonNode json : jsonList) {
    	        		MsiKaizoKanryo kanryo = new MsiKaizoKanryo();
    	        		kanryo.mitumoriNo 	= inputParameter.get("mtmno").asText();
    	        		kanryo.edaban	  	= Long.parseLong(inputParameter.get("edaban").asText());
    	        		kanryo.gyoNo 		= Long.parseLong(json.get("gno").asText());
    	        		kanryo.taisyoKataban= json.get("kataban").asText();
    	        		kanryo.taisyoNaiyo 		= json.get("kaizo").asText();
    	            	kanryo.seizoNo = json.get("seizo").asText();
    	            	kanryo.sekouName = json.get("tanto").asText();

    	            	Logger.info("[INSERT]mitumoriNo={}",kanryo.mitumoriNo);
    	            	Logger.info("[INSERT]edaban={}",kanryo.edaban);
    	            	Logger.info("[INSERT]gyoNo={}",kanryo.gyoNo);
    	            	Logger.info("[INSERT]seizoNo={}",kanryo.seizoNo);
    	            	Logger.info("[INSERT]seizoNo={}",kanryo.sekouName);

    	            	result = kanryo.insertKaizoKanryo();

    	            	if (result) {
    	                    resultJson.put("result"  , "success");
    	                }
    	                else {
    	                    resultJson.put("result"  , "error");
                            break;
    	                }
                    }
            	}
            	else {
                	resultJson.put("result"  , "notfound");
            	}
            }
            //見積ヘッダを更新する
            head.updateStts(MtmHead.Stts.MT_SEKOU);

		} catch (Exception e) {
			// TODO: handle exception
			Logger.error("[SerialNumber] USERID={} COMMIT EXCEPTION MESSAGE={}", user.userId, e.getMessage(), e);
		}
        finally {
        }

        return ok(resultJson);
    }
}
