package controllers;

import java.util.List;
import models.MstKataban;
import parameter.MtmRetrievalMatch;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import util.BaseExcelModel;
import auth.WeosAuthenticator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 製品単価確認コントローラー
 * @author kimura
 *
 */
public class SeihinValueController extends BaseController {

	/**
	 * 製品単価取り込み画面遷移
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
  public static Result move() {
      return ok(views.html.seihinupload.render());
  }
  /**
   * 型番検索
   * @return
   */
@Authenticated(WeosAuthenticator.class)
  public static Result searchkataban() {

      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson   = Json.newObject();
      String result = "success";
      int count = 0;

      getSessionUser();

      //------------------------------------------------------------------------------------
      //- パラメータの取得
      //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      String kataban  = inputParameter.get("kataban").asText();

      Logger.info(">>>>> [SEIHIN VALUE] USER={} ACTION >>> KATABAN SEARCH KATABAN={} .", user.userId, kataban);

      //------------------------------------------------------------------------------------
      //- 製品型番を検索する
      //------------------------------------------------------------------------------------
      List<MstKataban> dataList = MstKataban.getKatabans(kataban);
      count = dataList.size();
      if (count > 0) {
        for (MstKataban data : dataList) { //一致した見積件名分
          ObjectNode dataJson = Json.newObject();                            //データの生成
          dataJson.put("kataban", data.seihinKataban);                       //製品型番
          dataJson.put("bousyoku", data.bousyokuTanka);                      //防蝕
          dataJson.put("jubousyoku", data.jubousyokuTanka);                  //重防蝕
          dataJson.put("taiengai", data.taiengaiTanka);                      //耐塩害
          dataJson.put("taijuengai", data.taijuengaiTanka);                  //耐重塩害
          dataJson.put("jubousei", data.jubouseiTanka);                      //重防錆
          listJson.put(data.seihinKataban, dataJson);                        //JSONリストに格納
        }
      }
      else {
        result = "notfound";
      }

      resultJson.put("result"  , result);
      resultJson.put("datalist", listJson);
      resultJson.put("count"   , count);

      return ok(resultJson);
  }
}