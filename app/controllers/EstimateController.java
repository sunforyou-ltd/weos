package controllers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.MsiKaizoKanryo;
import models.MstKaisya;
import models.MstKataban;
import models.MstOptionBuhin;
import models.MstProperty;
import models.MstShiten;
import models.MstTax;
import models.MstUser;
import models.MtmHead;
import models.MtmOptionKaizo;
import models.MtmTaisyoSeihin;
import models.MtmTokusyuKaizo;
import models.TyohyouRireki;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;
import bean.PIraiBean;
import bean.PMitumoriBean;
import bean.PSekouBean;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 見積作成画面コントローラー
 * @author kimura
 *
 */
public class EstimateController extends BaseController {

  /**
   * ロックオブジェクト
   */
  private static final Object objLock = new Object();

	/**
	 * 見積作成画面遷移
	 * @return 見積作成画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {

        return ok(views.html.estimate.render());

    }
    /**
     * 初期処理
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result init() {

        ObjectNode resultJson  = Json.newObject();
        ObjectNode mtsListJson = Json.newObject();
        ObjectNode mtkListJson = Json.newObject();
        ObjectNode katabanListJson = Json.newObject();
        String result = "success";
        String stts = "00";
        boolean hachu = false;
        long tax = 0;

        getSessionUser();
        synchronized (objLock) {
          //------------------------------------------------------------------------------------
          //- パラメータの取得
          //------------------------------------------------------------------------------------
          parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
          sessionkeys = getSessionKeys();

          Logger.info(">>>>> [ESTIMATE] ACTION USER={} INIT MTMNO={} EDABN={} MODE={}.", user.userId, sessionkeys.mtmno, sessionkeys.edaban, sessionkeys.mode);

          MstKaisya kaisya = MstKaisya.find.where().eq("kaisya_cd", user.kaisyaCd).findUnique();
          MstShiten shiten = MstShiten.find.where().eq("kaisya_cd", user.kaisyaCd).eq("shiten_cd", user.shitenCd).findUnique();

          if ("Retrieval".equals(sessionkeys.mode)) { //見積検索からの遷移の場合
            MtmHead head = MtmHead.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findUnique();
            if (head != null) { //見積ヘッダが取得出来た場合
              //----- 基本情報 -----
              resultJson.put("mtmno"      , head.mitumoriNo);     //見積番号
              resultJson.put("edaban"     , head.edaban);       //枝番
              resultJson.put("kanri"      , head.kanriNo);      //管理番号
              resultJson.put("seko"       , head.sekoAtena);      //施工証明書宛名
              resultJson.put("kouji"      , head.kojiKenmei);     //工事件名
              resultJson.put("haturei"    , head.hatsureimotoCode.trim()); //発令元コード
              MstUser.getUserToJson(head.sakuseiUid, resultJson);     //見積依頼者情報
              resultJson.put("irainouki"    , dateStringFormat(head.iraiNoki));   //依頼納期
              resultJson.put("kaitounouki"    , dateStringFormat(head.kaitoNoki));  //回答納期
              resultJson.put("iraibi"     , dateStringFormat(head.mitumoriIraibi)); //見積依頼日
              resultJson.put("kaitoubi"     , dateStringFormat(head.mitumoriKaitoubi)); //見積回答日
              resultJson.put("tokuicode"  , head.tokuisakiCode.trim());  //得意先コード
              resultJson.put("tokuiname"  , head.tokuisakiName.trim());  //得意先名
              resultJson.put("postcode"   , head.nohinsakiYubin); //郵便番号
              resultJson.put("addr1"    , head.nohinsakiAdd1);  //住所１
              resultJson.put("addr2"    , head.nohinsakiAdd2);  //住所２
              resultJson.put("tel"      , head.nohinsakiTel); //電話番号
              resultJson.put("fax"      , head.nohinsakiFax); //FAX不番号
              resultJson.put("mitumoriyukokigen"    , dateStringFormat(head.mitumoriYukoKigen));  //見積有効期限

              //----- 見積依頼日時点の消費税情報を取得 -----
              MstTax mTax = MstTax.getTaxData(head.mitumoriIraibi);
              if (mTax != null) {
                tax = mTax.tax;
              }

              //----- 改造情報 -----
              long gno  = -1;         //行番号
              int bgno = -1;         //行番号(分納)
              ObjectNode mtsJson = null;
              ObjectNode mtsbListJson = Json.newObject();
              List<MtmTaisyoSeihin> mtss = MtmTaisyoSeihin.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findList();
              for (MtmTaisyoSeihin mts : mtss) {
                if (MtmTaisyoSeihin.BunoFlag.NOMAL.equals(mts.bunno_kubun)) {         //通常行の場合
                  if (mtsJson != null) {
                    mtsJson.put("bunnoinfo", mtsbListJson);  //分納情報を格納
                    mtsListJson.put(String.valueOf(gno), mtsJson);
                    mtsbListJson = Json.newObject();         //分納情報を初期化
                    bgno = -1;                               //行番号(分納)を初期化
                  }
                  gno++;         //行番号
                  //gno = mts.gyoNo; //行番号
                  mtsJson = Json.newObject();
                  mtsJson.put("kataban"       , mts.taisyoKataban);                     //製品型番
                  mtsJson.put("bousyoku"      , checkedKaizo(mts.bosyokuKaizo));        //
                  mtsJson.put("kBousyoku"     , mts.boKaiTanka);                        //
                  mtsJson.put("pBousyoku"     , mts.pboKaiTanka);                       //
                  mtsJson.put("Jubousyoku"    , checkedKaizo(mts.jubosyokuKaizo));      //
                  mtsJson.put("kJubousyoku"   , mts.juKaiTanka);                        //
                  mtsJson.put("pJubousyoku"   , mts.pjuKaiTanka);                       //
                  mtsJson.put("taiengai"      , checkedKaizo(mts.taiengaiKaizo));       //
                  mtsJson.put("kTaiengai"     , mts.taiKaiTanka);                       //
                  mtsJson.put("pTaiengai"     , mts.ptaiKaiTanka);                      //
                  mtsJson.put("taijuengai"    , checkedKaizo(mts.taijuengaiKaizo));     //
                  mtsJson.put("kTaijuengai"   , mts.taijuKaiTanka);                     //
                  mtsJson.put("pTaijuengai"   , mts.ptaijuKaiTanka);                    //
                  mtsJson.put("jubousabi"     , checkedKaizo(mts.jubouseiKaizo));       //
                  mtsJson.put("kJubousabi"    , mts.jubouKaiTanka);                     //
                  mtsJson.put("pJubousabi"    , mts.pjubouKaiTanka);                    //
                  mtsJson.put("mashine"       , mts.mashinNo);                          //
                  mtsJson.put("suryo"         , mts.suryo);                             //
                  mtsJson.put("nyuka"         , dateStringFormat(mts.nyukaYotei));      //
                  mtsJson.put("kibou"         , dateStringFormat(mts.nyukaKibou));      //
                  mtsJson.put("hikitori"      , dateStringFormat(mts.hikitori_kibou));  //
                  mtsJson.put("haise"         , dateStringFormat(mts.haiseReinyuDay));  //
                  mtsJson.put("kend"          , dateStringFormat(mts.seizou_kanryou_day));  //
                  mtsJson.put("gno"           , mts.gyoNo);                             //
                  //----- 対象改造のオプション部品情報を取得する -----
                  ObjectNode optList = Json.newObject();
                  List<MtmOptionKaizo> mtok = MtmOptionKaizo.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).eq("gyo_no", mts.gyoNo).orderBy("option_cd").findList();
                  long gidx=0;
                  for (MtmOptionKaizo mto : mtok) {
                    ObjectNode mtoJson = Json.newObject();

                    mtoJson.put("optioncd"       , mto.optionCd);                       //オプションコード
                    mtoJson.put("optionname"     , mto.optionName);                     //オプション名
                    mtoJson.put("optiontanka"    , mto.optionTanka);                    //オプション単価
                    mtoJson.put("optioncd"       , mto.optionCd);                       //オプションコード
                    mtoJson.put("mashine"        , mto.mashinNo);                       //マシンNo.
                    mtoJson.put("nyuka"          , dateStringFormat(mto.nyukaYotei));   //入荷予定日
                    mtoJson.put("kibou"          , dateStringFormat(mto.nyukaKibou));   //入荷希望日
                    optList.put(String.valueOf(gidx), mtoJson);
                    gidx++;
                  }
                  ObjectNode optPack = Json.newObject();
                  optPack.put("key", mts.gyoNo);
                  optPack.put("option", optList);
                  mtsJson.put("option", optPack);                                 //オプション部品
                }
                else {                                                                  //分納の場合
                  bgno++;         //行番号
                  ObjectNode mtsbJson = Json.newObject();
                  mtsbJson.put("kataban"       , mts.taisyoKataban);                     //製品型番
                  mtsbJson.put("bousyoku"      , checkedKaizo(mts.bosyokuKaizo));        //
                  mtsbJson.put("kBousyoku"     , mts.boKaiTanka);                        //
                  mtsbJson.put("pBousyoku"     , mts.pboKaiTanka);                       //
                  mtsbJson.put("Jubousyoku"    , checkedKaizo(mts.jubosyokuKaizo));      //
                  mtsbJson.put("kJubousyoku"   , mts.juKaiTanka);                        //
                  mtsbJson.put("pJubousyoku"   , mts.pjuKaiTanka);                       //
                  mtsbJson.put("taiengai"      , checkedKaizo(mts.taiengaiKaizo));       //
                  mtsbJson.put("kTaiengai"     , mts.taiKaiTanka);                       //
                  mtsbJson.put("pTaiengai"     , mts.ptaiKaiTanka);                      //
                  mtsbJson.put("taijuengai"    , checkedKaizo(mts.taijuengaiKaizo));     //
                  mtsbJson.put("kTaijuengai"   , mts.taijuKaiTanka);                     //
                  mtsbJson.put("pTaijuengai"   , mts.ptaijuKaiTanka);                    //
                  mtsbJson.put("jubousabi"     , checkedKaizo(mts.jubouseiKaizo));       //
                  mtsbJson.put("kJubousabi"    , mts.jubouKaiTanka);                     //
                  mtsbJson.put("pJubousabi"    , mts.pjubouKaiTanka);                    //
                  mtsbJson.put("mashine"       , mts.mashinNo);                          //
                  mtsbJson.put("suryo"         , mts.suryo);                             //
                  mtsbJson.put("nyuka"         , dateStringFormat(mts.nyukaYotei));      //
                  mtsbJson.put("kibou"         , dateStringFormat(mts.nyukaKibou));      //
                  mtsbJson.put("hikitori"      , dateStringFormat(mts.hikitori_kibou));  //
                  mtsbJson.put("haise"         , dateStringFormat(mts.haiseReinyuDay));  //
                  mtsbJson.put("kend"          , dateStringFormat(mts.seizou_kanryou_day));  //
                  mtsbListJson.put(String.valueOf(bgno), mtsbJson);
                }
              }
              if (mtsJson != null) {
                mtsJson.put("bunnoinfo", mtsbListJson);  //分納情報を格納
                mtsListJson.put(String.valueOf(gno), mtsJson);
              }
              //----- 特殊改造 -----
              int tgno  = -1;        //行番号
              int btgno = -1;        //行番号(分納)
              ObjectNode mtkJson = null;
              ObjectNode mtkbListJson = Json.newObject();
              List<MtmTokusyuKaizo> mtks = MtmTokusyuKaizo.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findList();
              for (MtmTokusyuKaizo mtk : mtks) {
                if (MtmTaisyoSeihin.BunoFlag.NOMAL.equals(mtk.bunno_kubun)) {         //通常行の場合
                  if (mtkJson != null) {
                    mtkJson.put("bunnoinfo", mtkbListJson);  //分納情報を格納
                    mtkListJson.put(String.valueOf(tgno), mtkJson);
                    mtkbListJson = Json.newObject();         //分納情報を初期化
                    btgno = -1;                              //行番号(分納)を初期化
                  }
                  tgno++;         //行番号
                  mtkJson = Json.newObject();
                  mtkJson.put("tkataban"      , mtk.tokusyuKataban);                    //製品型番
                  mtkJson.put("tkaizou"       , mtk.tokusyuNaiyo);                      //
                  mtkJson.put("tkingaku"      , mtk.tokusyuKingaku);                    //
                  mtkJson.put("ptkingaku"     , mtk.ptokusyuKingaku);                    //
                  mtkJson.put("tmashine"      , mtk.mashinNo);                          //
                  mtkJson.put("tsuryo"        , mtk.suryo);                             //
                  mtkJson.put("tnyuka"        , dateStringFormat(mtk.nyukaYotei));      //
                  mtkJson.put("tkibou"        , dateStringFormat(mtk.nyukaKibou));      //
                  mtkJson.put("thikitori"     , dateStringFormat(mtk.hikitori_kibou));  //
                  mtkJson.put("thaise"        , dateStringFormat(mtk.haiseReinyuDay));  //
                  mtkJson.put("tkend"         , dateStringFormat(mtk.seizou_kanryou_day));  //
                }
                else {                                                                  //分納行の場合
                  btgno++;         //行番号
                  ObjectNode mtkbJson = Json.newObject();
                  mtkbJson.put("tkataban"      , mtk.tokusyuKataban);                    //製品型番
                  mtkbJson.put("tkaizou"       , mtk.tokusyuNaiyo);                      //
                  mtkbJson.put("tkingaku"      , mtk.tokusyuKingaku);                    //
                  mtkbJson.put("ptkingaku"     , mtk.ptokusyuKingaku);                    //
                  mtkbJson.put("tmashine"      , mtk.mashinNo);                          //
                  mtkbJson.put("tsuryo"        , mtk.suryo);                             //
                  mtkbJson.put("tnyuka"        , dateStringFormat(mtk.nyukaYotei));      //
                  mtkbJson.put("tkibou"        , dateStringFormat(mtk.nyukaKibou));      //
                  mtkbJson.put("thikitori"     , dateStringFormat(mtk.hikitori_kibou));  //
                  mtkbJson.put("thaise"        , dateStringFormat(mtk.haiseReinyuDay));  //
                  mtkbJson.put("tkend"         , dateStringFormat(mtk.seizou_kanryou_day));  //
                  mtkbListJson.put(String.valueOf(btgno), mtkbJson);
                }
              }
              if (mtkJson != null) {
                mtkJson.put("bunnoinfo", mtkbListJson);  //分納情報を格納
                mtkListJson.put(String.valueOf(tgno), mtkJson);
              }
              //----- 備考情報 -----
              resultJson.put("kaizono"    , head.kaizoSyoninNo);  //改造承認No.
              resultJson.put("koubaino"   , head.koubaiKanriNo);  //購買管理No.
              resultJson.put("psitenname" , head.sitenName);    //見積用支店名
              resultJson.put("ptantoname" , head.tantouName);   //見積用担当名
              resultJson.put("kaizoseikyu", head.kaizouhiSeikyu); //改造費請求先
              resultJson.put("unchin"   , head.untinSeikyu);  //運賃請求先
              resultJson.put("panel"    , head.paneruColor);  //パネル色
              resultJson.put("setti"    , head.settiBasyo);   //設置場所
              resultJson.put("biko1"    , head.bikou_1);    //備考１
              resultJson.put("biko2"    , head.bikou_2);    //備考２
              resultJson.put("biko3"    , head.bikou_3);    //備考３
              resultJson.put("koutei1"    , head.kouteiNouki1); //工程納期１
              resultJson.put("koutei2"    , head.kouteiNouki2); //工程納期２
              resultJson.put("koutei3"    , head.kouteiNouki3); //工程納期３
              //----- 見積金額 -----
              resultJson.put("kmitumori"  , head.saisyuMitumoriGokei);  //見積合計額
              resultJson.put("khontai"    , head.mitumoriGokei);      //本体価格
              resultJson.put("knebiki"    , head.nebikiGaku);       //値引額
              resultJson.put("kritu"    , head.nebikiRitu);       //値引率
              resultJson.put("kshouhizei" , head.saisyuMitumoriGokei - head.mitumoriGokei); //消費税
              //----- 受注確定 -----
              resultJson.put("kakutei"  , head.kakuteiFlg);   //受注確定フラグ
              stts = head.mitumoriJotai;              //見積状態
              //----- 発注フラグ -----
              if (MtmHead.Stts.MT_INPUT.equals(stts)
                  || MtmHead.Stts.MT_ANSER.equals(stts)) {
                hachu = true;
              }
            }
          }
          else { //新規作成の場合

              //----- プロパティよりLE系の会社コード・支店コードを取得 -----
              String sLeKcode = MstProperty.GetValue(MstProperty.Code.LE_KCODE);
              String sMrScode = MstProperty.GetValue(MstProperty.Code.MR_SCODE);
              String sMrKname = MstProperty.GetValue(MstProperty.Code.MR_KNAME);

              //----- システム日付時点の消費税情報を取得 -----
              MstTax mTax = MstTax.getTaxData(systemDateFormat());
              if (mTax != null) {
                tax = mTax.tax;
              }

              if (sLeKcode.equals(user.kaisyaCd)) { //LE九州の場合、納入先を自動設定
                resultJson.put("postcode"   , MstProperty.GetValue(MstProperty.Code.MT_YUBIN));   //郵便番号
                resultJson.put("addr1"    , MstProperty.GetValue(MstProperty.Code.MT_ADDR1));   //住所１
                resultJson.put("addr2"    , MstProperty.GetValue(MstProperty.Code.MT_ADDR2));   //住所２
                resultJson.put("tel"      , MstProperty.GetValue(MstProperty.Code.MT_TEL));   //電話番号
                resultJson.put("fax"      , MstProperty.GetValue(MstProperty.Code.MT_FAX));   //FAX不番号
              }
              if (shiten != null && MstShiten.Kennaigai.KENNAI.equals(shiten.kennaigaiFlg)) { //LE系である場合、改造費請求を自動設定
                resultJson.put("kaizoseikyu", MstProperty.GetValue(MstProperty.Code.MT_KAIZOUHI));  //改造費請求先
              }
              else {
                //一般得意先の場合
                  resultJson.put("kaizoseikyu", kaisya.kaisyaName + "　" + shiten.shitenName);  //改造費請求先
              }
              //----- 但し、メルの場合は株式会社メルに変更 -----
              if (sLeKcode.equals(user.kaisyaCd) && sMrScode.equals(user.shitenCd)) { //株式会社メルの場合は改造費請求先をメルにする
                resultJson.put("kaizoseikyu", sMrKname);  //改造費請求先
              }

              stts = MtmHead.Stts.MT_INPUT; //見積依頼

          }

          //----- 製品型番マスタ情報を生成します -----
          List<MstKataban> dataList = MstKataban.getKatabans("");
          int count = dataList.size();
          if (count > 0) {
            for (MstKataban data : dataList) { //一致した見積件名分
              ObjectNode dataJson = Json.newObject();                            //データの生成
              dataJson.put("kataban", data.seihinKataban);                       //製品型番
              dataJson.put("bousyoku", data.bousyokuTanka);                      //防蝕
              dataJson.put("jubousyoku", data.jubousyokuTanka);                  //重防蝕
              dataJson.put("taiengai", data.taiengaiTanka);                      //耐塩害
              dataJson.put("taijuengai", data.taijuengaiTanka);                  //耐重塩害
              dataJson.put("jubousei", data.jubouseiTanka);                      //重防錆
              katabanListJson.put(data.seihinKataban, dataJson);                 //JSONリストに格納
            }
          }

          Logger.info(">>>>> [ESTIMATE] ACTION USER={} INIT KENGEN={} STTS={}.", user.userId, user.kengen, stts);

          resultJson.put("mode"     	, sessionkeys.mode);
          resultJson.put("kengen"   	, user.kengen);
          resultJson.put("naigai"   	, shiten.kennaigaiFlg);
          resultJson.put("stts"     	, stts);
          resultJson.put("hachu"    	, hachu);
          resultJson.put("tax"      	, tax);
          resultJson.put("mtsList"  	, mtsListJson);
          resultJson.put("mtkList"  	, mtkListJson);
          resultJson.put("katabanList" 	, katabanListJson);
          resultJson.put("result"   	, result);
        }

        return ok(resultJson);
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

        Logger.info(">>>>> [ESTIMATE] ACTION USER={} KATABAN SEARCH KATABAN={} .", user.userId, kataban);

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
  /**
   * 渡された型番が存在するかチェックします
   * @return
   */
@Authenticated(WeosAuthenticator.class)
  public static Result checkkataban() {

      ObjectNode resultJson = Json.newObject();
      String result = "success";

      getSessionUser();

      //------------------------------------------------------------------------------------
      //- パラメータの取得
      //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      String kataban  = inputParameter.get("kataban").asText();

      Logger.info(">>>>> [ESTIMATE] ACTION USER={} KATABAN CHECK KATABAN={} .", user.userId, kataban);

      //------------------------------------------------------------------------------------
      //- 製品型番を検索する
      //------------------------------------------------------------------------------------
      MstKataban katabandata = MstKataban.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).eq("seihin_kataban", kataban).findUnique();
      if (katabandata != null) {
      }
      else {
        result = "notfound";
      }

      resultJson.put("result"  , result);

      return ok(resultJson);
  }

  /**
   * オプション検索
   * @return
   */
@Authenticated(WeosAuthenticator.class)
  public static Result searchoption() {

      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson   = Json.newObject();
      String result = "success";

      getSessionUser();

      //------------------------------------------------------------------------------------
      //- パラメータの取得
      //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();

      Logger.info(">>>>> [ESTIMATE] ACTION USER={} OPTION SEARCH  .", user.userId);

      //------------------------------------------------------------------------------------
      //- 製品型番を検索する
      //------------------------------------------------------------------------------------
      List<MstOptionBuhin> dataList = MstOptionBuhin.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).orderBy("option_cd").findList();

      if (dataList.size() > 0) {
        for (MstOptionBuhin data : dataList) { //一致した見積件名分
          ObjectNode dataJson = Json.newObject();                            //データの生成
          dataJson.put("optioncd"     , data.optionCd);
          dataJson.put("optionname"   , data.optionName);
          dataJson.put("optiontanka"  , data.optionTanka);
          listJson.put(data.optionCd, dataJson);                        //JSONリストに格納
        }
      }
      else {
        result = "notfound";
      }

      resultJson.put("result"   , result);
    resultJson.put("datalist", listJson);

      return ok(resultJson);
  }
/**
 * 該当行に一致するオプション情報を取得します
 * @param pOptionList
 * @param key
 * @return
 */
public static JsonNode getOptionFromJson(JsonNode pOptionList, long key) {

	JsonNode optionData = null;	//行単位オプション情報

    //------------------------------------------------------------------------------------
    //- 該当行(キー)に該当するオプション情報を返します
    //------------------------------------------------------------------------------------
	for (int idx=0; idx< pOptionList.size(); idx++) {
	    JsonNode optioninfo = pOptionList.get(idx);
	    if (key == optioninfo.get("key").asLong()) { //該当行と一致する場合
	    	optionData = optioninfo.get("option");
	    }
	}

	return optionData;

}
/**
 * 登録／変更処理
 * @return
 */
@Authenticated(WeosAuthenticator.class)
public static Result commitEstimate() {

    ObjectNode resultJson  = Json.newObject();
    boolean result = false;

    getSessionUser();

    //------------------------------------------------------------------------------------
    //- パラメータの取得
    //------------------------------------------------------------------------------------
    JsonNode  inputParameter  = request().body().asJson();
    parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
    sessionkeys = getSessionKeys();

    Logger.info(">>>>> [ESTIMATE] ACTION USER={} UPDATE MTMNO={} EDABN={} MODE={}.", user.userId, sessionkeys.mtmno, sessionkeys.edaban, sessionkeys.mode);

    //------------------------------------------------------------------------------------
    //- 見積情報を登録／更新する
    //------------------------------------------------------------------------------------
    synchronized (objLock) {
      try {

        //----- 入力チェックはクライアント側で行っている為、格納処理のみ行う -----
        boolean hachuflg = false;
        String sHaiseMax  = "";
        String sKanryoMax = "";
        //------------------------------------------------------------------------------------
        //- 見積ヘッダの登録／更新を行う
        //------------------------------------------------------------------------------------
        MtmHead head;
        if ("Retrieval".equals(sessionkeys.mode)) { //見積検索からの遷移の場合
          head = MtmHead.find.where().eq("mitumori_no", inputParameter.get("mtmno").asText()).eq("edaban", inputParameter.get("edaban").asLong()).eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).findUnique();
          if (head == null) {
            head = new MtmHead();
          }
        }
        else {
          head = new MtmHead();
        }
        head.kanriNo            = inputParameter.get("kanri").asText();
        head.sekoAtena          = inputParameter.get("seko").asText();
        head.kojiKenmei         = inputParameter.get("kouji").asText();
        head.hatsureimotoCode   = inputParameter.get("haturei").asText();
        head.iraiNoki           = dateStringFormat(inputParameter.get("irainouki").asText());
        head.kaitoNoki          = dateStringFormat(inputParameter.get("kaitounouki").asText());
        head.mitumoriKaitoubi   = dateStringFormat(inputParameter.get("kaitoubi").asText());
        head.tokuisakiCode      = inputParameter.get("tokuicode").asText();
        head.tokuisakiName      = inputParameter.get("tokuiname").asText();
        head.nohinsakiYubin     = inputParameter.get("postcode").asText().replaceAll("-", "");
        head.nohinsakiAdd1      = inputParameter.get("addr1").asText();
        head.nohinsakiAdd2      = inputParameter.get("addr2").asText();
        head.nohinsakiTel       = inputParameter.get("tel").asText();
        head.nohinsakiFax       = inputParameter.get("fax").asText();
        head.mitumoriYukoKigen  = dateStringFormat(inputParameter.get("mitumoriyukokigen").asText());
        //----- 備考情報 -----
        head.kaizoSyoninNo    = inputParameter.get("kaizono").asText();
        head.koubaiKanriNo    = inputParameter.get("koubaino").asText();
        head.sitenName        = inputParameter.get("psitenname").asText();
        head.tantouName       = inputParameter.get("ptantoname").asText();
        head.kaizouhiSeikyu   = inputParameter.get("kaizoseikyu").asText();
        head.untinSeikyu      = inputParameter.get("unchin").asText();
        head.paneruColor      = inputParameter.get("panel").asText();
        head.settiBasyo       = inputParameter.get("setti").asText();
        head.bikou_1          = inputParameter.get("biko1").asText();
        head.bikou_2          = inputParameter.get("biko2").asText();
        head.bikou_3          = inputParameter.get("biko3").asText();
        head.kouteiNouki1     = inputParameter.get("koutei1").asText();
        head.kouteiNouki2     = inputParameter.get("koutei2").asText();
        head.kouteiNouki3     = inputParameter.get("koutei3").asText();
        //----- 見積金額 -----
        head.saisyuMitumoriGokei   = inputParameter.get("kmitumori").asLong();
        head.mitumoriGokei         = inputParameter.get("khontai").asLong();
        head.nebikiGaku            = inputParameter.get("knebiki").asLong();
        head.nebikiRitu            = inputParameter.get("kritu").asLong();

        //----- 見積金額 -----
        head.koshinUid        = user.userId;
        head.koshinPid        = MtmHead.UpdatePID.ESTIMATE;

        if ("Retrieval".equals(sessionkeys.mode)) { //見積検索からの遷移の場合

          String kakutei = inputParameter.get("kakutei").asText();

          //----- 見積状態の遷移 -----
          switch (head.mitumoriJotai) {
          case MtmHead.Stts.MT_INPUT:   //見積依頼
              if (MstUser.Kengen.EIGYO.equals(user.kengen)) {     //ログインユーザが営業担当者
                head.mitumoriJotai = MtmHead.Stts.MT_INPUT;       //見積依頼のまま
                head.mitumoriKaitoubi = "";
              }
              else if (MstUser.Kengen.ST.equals(user.kengen)) {   //ログインユーザがサンデンテクノ
                head.mitumoriJotai = MtmHead.Stts.MT_ANSER;       //見積依頼→見積回答
                head.mitumoriKaitoubi = systemDateFormat();
                head.kaitouUid = user.userId;                       //回答ユーザＩＤをセット
              }
            break;

          case MtmHead.Stts.MT_ANSER:   //見積回答

            if (MtmHead.KakuteiFlag.KAKUTEI.equals(kakutei)) {    //受注確定フラグがオンの場合
              head.commitOrderno();                               //オーダー番号を発行します
              head.mitumoriJotai = MtmHead.Stts.MT_COMMIT;        //見積回答→受注確定
              head.kakuteiFlg    = MtmHead.KakuteiFlag.KAKUTEI;   //受注確定フラグをON
            }
            else {
              String hachu = inputParameter.get("hachu").asText();
              if ("1".equals(hachu)) {
                head.mitumoriJotai = MtmHead.Stts.MT_ORDER;       //見積回答→発注
                hachuflg = true;
              }
              else {
                head.mitumoriJotai = MtmHead.Stts.MT_ANSER;       //見積回答のまま
              }
            }
            if (MstUser.Kengen.ST.equals(user.kengen)) {          //ログインユーザがサンデンテクノ
              head.kaitouUid = user.userId;                       //回答ユーザＩＤをセット
            }

            break;

          case MtmHead.Stts.MT_ORDER:   //発注

            if (MtmHead.KakuteiFlag.KAKUTEI.equals(kakutei)) {    //受注確定フラグがオンの場合
              head.commitOrderno();                               //オーダー番号を発行します
              head.mitumoriJotai = MtmHead.Stts.MT_COMMIT;        //発注→受注確定
              head.kakuteiFlg    = MtmHead.KakuteiFlag.KAKUTEI;   //受注確定フラグをON
            }
            else {
              String hachu = inputParameter.get("hachu").asText();
              if ("0".equals(hachu)) {
                head.mitumoriJotai = MtmHead.Stts.MT_ANSER;         //発注→見積回答
              }
              else {
                head.mitumoriJotai = MtmHead.Stts.MT_ORDER;         //発注のまま
              }
            }

            break;

          case MtmHead.Stts.MT_COMMIT:  //受注確定

            if (MtmHead.KakuteiFlag.KAKUTEI.equals(kakutei)) {    //受注確定フラグがオンの場合
              head.mitumoriJotai = MtmHead.Stts.MT_COMMIT;        //受注確定のまま
              head.kakuteiFlg    = MtmHead.KakuteiFlag.KAKUTEI;   //受注確定フラグをON
            }
            else {                                                //受注確定の撤回
              head.orderNo = "";                                  //オーダー番号の初期化
              head.mitumoriJotai = MtmHead.Stts.MT_ORDER;         //受注確定→発注
              head.kakuteiFlg    = MtmHead.KakuteiFlag.MITEI;     //受注確定フラグをOFF
            }
            break;

          case MtmHead.Stts.MT_SEKOU:   //施工完了

            head.mitumoriJotai = MtmHead.Stts.MT_SEKOU;           //施工完了のまま
            break;

          default:
            break;
          }

          //再セットの為、ここでは保存しない
          //head.update();

        }
        else { //見積作成時
          //新規作成時の初期項目設定
          head.orderNo            = "";
          //見積依頼者情報の格納
          head.mitumoriIraibi   = systemDateFormat();
          head.mitumoriIraiKaisya = user.kaisyaCd;
          head.mitumoriIraiSiten  = user.shitenCd;
          head.mitumoriIraiBumon  = user.bumonCd;
          head.sakuseiUid         = user.userId;
          head.kakuteiFlg         = MtmHead.KakuteiFlag.MITEI;
          head.mitumoriJotai      = MtmHead.Stts.MT_INPUT;
          head.sakujoFlg          = MtmHead.DeleteFlag.NOMAL;
          head.renkeiput          = MtmHead.RenkeiFlag.NOPUT;
          //見積番号の発行
          head.commitMtmno();
          //見積ヘッダの作成
          //再セットの為、ここでは保存しない
          //head.save();

        }

        //------------------------------------------------------------------------------------
        //- 改造情報の登録を行う
        //------------------------------------------------------------------------------------
        JsonNode kaizoList  = inputParameter.get("kaizo");
        JsonNode optionList = inputParameter.get("option");

        Logger.info(">>>>> [ESTIMATE] ACTION USER={} KAIZO={} OPTION={}.", user.userId, kaizoList.size(), optionList.size());

        //----- 改造情報を削除します -----
        //ここは削除０件によるエラーがある為、戻り値チェックは行わない
        MtmTaisyoSeihin.deleteData(head.mitumoriNo, head.edaban);
        MtmOptionKaizo.deleteData(head.mitumoriNo, head.edaban);

        if (kaizoList.size() > 0) {
        	int gno=0;
        	//----- 改造情報を登録します -----
        	for(int idx=0; idx < kaizoList.size(); idx++){
        		JsonNode oJson = kaizoList.get(idx);
                Logger.info(">>>>> [KAIZO] kataban={} bousyoku={} Jubousyoku={} taiengai={} jubousabi={} KATABAN={}.", oJson.get("kataban"), oJson.get("bousyoku"), oJson.get("Jubousyoku"), oJson.get("taiengai"), oJson.get("jubousabi"));
                if (oJson.get("kataban").asText() == null || "".equals(oJson.get("kataban").asText())) { //製品型番が空欄の場合、対象外
                  continue;
                }
                MstKataban mKtanka		= MstKataban.getKataban(oJson.get("kataban").asText());
                if (mKtanka == null) {
                    Logger.error(">>>>> [KAIZO] NON EXISTS KATABAN kataban={}.", oJson.get("kataban"));
                    continue;
                }
                MtmTaisyoSeihin mts = new MtmTaisyoSeihin();
                //----- 共通情報 -----
                mts.mitumoriNo 			= head.mitumoriNo;
                mts.edaban     			= head.edaban;
                mts.gyoNo      			= gno;
                //----- 改造内容 -----
                mts.taisyoKataban      	= mKtanka.seihinKataban;
                mts.bosyokuKaizo      	= kaizoValue(oJson.get("bousyoku").asBoolean());
                mts.jubosyokuKaizo     	= kaizoValue(oJson.get("Jubousyoku").asBoolean());
                mts.taiengaiKaizo      	= kaizoValue(oJson.get("taiengai").asBoolean());
                mts.taijuengaiKaizo    	= kaizoValue(oJson.get("taijuengai").asBoolean());
                mts.jubouseiKaizo      	= kaizoValue(oJson.get("jubousabi").asBoolean());
                mts.suryo      			    = oJson.get("suryo").asLong();
                mts.gokei_suryo			    = oJson.get("gsuryo").asLong();
                //----- 単価情報 -----
                mts.boKaiTanka      	= mKtanka.bousyokuTanka;
                mts.juKaiTanka      	= mKtanka.jubousyokuTanka;
                mts.taiKaiTanka      	= mKtanka.taiengaiTanka;
                mts.taijuKaiTanka      	= mKtanka.taijuengaiTanka;
                mts.jubouKaiTanka      	= mKtanka.jubouseiTanka;
                //----- 帳票情報 -----
                mts.pboKaiTanka      	= oJson.get("pBousyoku").asLong();
                mts.pjuKaiTanka      	= oJson.get("pJubousyoku").asLong();
                mts.ptaiKaiTanka      	= oJson.get("pTaiengai").asLong();
                mts.ptaijuKaiTanka      = oJson.get("pTaijuengai").asLong();
                mts.pjubouKaiTanka      = oJson.get("pJubousabi").asLong();
                //----- その他情報 -----
                mts.mashinNo			= oJson.get("mashine").asText();
                mts.nyukaYotei			= dateStringFormat(oJson.get("nyuka").asText());
                mts.nyukaKibou			= dateStringFormat(oJson.get("kibou").asText());
                mts.hikitori_kibou		= dateStringFormat(oJson.get("hikitori").asText());
                mts.haiseReinyuDay		= dateStringFormat(oJson.get("haise").asText());
                if ("".equals(mts.haiseReinyuDay)) { //配セ戻入日が未設定の場合、依頼納期を設定
                	mts.haiseReinyuDay = head.iraiNoki;
                }
                mts.seizou_kanryou_day	= dateStringFormat(oJson.get("kend").asText());
                if ("".equals(mts.seizou_kanryou_day)) { //改造完了予定日が未設定の場合、回答納期を設定
                	mts.seizou_kanryou_day = head.kaitoNoki;
                }
//製造完了予定日に見積回答日を設定するのは空白の場合のみ
//                switch (head.mitumoriJotai) {
//                //見積回答済の場合は回答納期が改造完了予定日
//                case MtmHead.Stts.MT_ANSER:   //見積回答
//                  mts.seizou_kanryou_day = head.kaitoNoki;
//                  break;
//                default:
//                  break;
//                }
                mts.bunno_kubun			= MtmTaisyoSeihin.BunoFlag.NOMAL;
                mts.koshinUid			= user.userId;
                mts.koshinPid			= MtmHead.UpdatePID.ESTIMATE;
                switch (head.mitumoriJotai) {
                case MtmHead.Stts.MT_ANSER:   //見積回答
                case MtmHead.Stts.MT_ORDER:   //発注
                case MtmHead.Stts.MT_COMMIT:  //受注確定
                case MtmHead.Stts.MT_SEKOU:   //施工完了
                  if ("".equals(sHaiseMax)) { //最大配セ戻入日が依頼納期となる
                	  sHaiseMax = mts.haiseReinyuDay;
                  }
                  else {
                	  if (dateCompare(sHaiseMax, mts.haiseReinyuDay) == -1) {
                    	  sHaiseMax = mts.haiseReinyuDay;
                	  }
                  }
                  if ("".equals(sKanryoMax)) { //最大製造完了予定日が回答納期となる
                	  sKanryoMax = mts.seizou_kanryou_day;
                  }
                  else {
                	  if (dateCompare(sKanryoMax, mts.seizou_kanryou_day) == -1) {
                		  sKanryoMax = mts.seizou_kanryou_day;
                	  }
                  }
                  break;
                default:
                  break;
                }
                mts.save();
                //------------------------------------------------------------------------------------
                //- オプション製品の登録を行う
                //------------------------------------------------------------------------------------
                JsonNode optioninfo = getOptionFromJson(optionList, mts.gyoNo);

                if (optioninfo != null) {
                	for (int oidx = 0; oidx < optioninfo.size(); oidx++) {
                		JsonNode oOptionJson = optioninfo.get(oidx);
                		MtmOptionKaizo mok = new MtmOptionKaizo();
                        //----- 共通情報 -----
                		mok.mitumoriNo 			= head.mitumoriNo;
                		mok.edaban     			= head.edaban;
                		mok.gyoNo      			= gno;
                        //----- 改造内容 -----
                		mok.taisyoKataban     = mKtanka.seihinKataban;
                		mok.optionCd      		= oOptionJson.get("optioncd").asText();
                		mok.optionName      	= oOptionJson.get("optionname").asText();
                		mok.optionTanka      	= oOptionJson.get("optiontanka").asLong();
                		mok.suryo      			= mts.suryo;
                        //----- その他情報 -----
                		mok.mashinNo			  = mts.mashinNo;
                    //2021-02-02 分納分のオプション改造を作成する
//               		mok.nyukaYotei			= dateStringFormat(oOptionJson.get("nyuka").asText());
//               		mok.nyukaKibou			= dateStringFormat(oOptionJson.get("kibou").asText());
                    mok.nyukaYotei      = "";
                    mok.nyukaKibou      = "";
                        mok.koshinUid			= user.userId;
                        mok.koshinPid			= MtmHead.UpdatePID.ESTIMATE;
                        mok.save();
                	}
                }
                //------------------------------------------------------------------------------------
                gno++; //このタイミングで行番号をカウントアップ
                //------------------------------------------------------------------------------------
                //- 分納情報の登録を行う
                //------------------------------------------------------------------------------------
                JsonNode bunnonList = oJson.get("bunnoinfo");
                for(int idx2=0; idx2 < bunnonList.size(); idx2++){
                  JsonNode oBJson = bunnonList.get(idx2);
                  MtmTaisyoSeihin mtsb = new MtmTaisyoSeihin();
                  //----- 共通情報 -----
                  mtsb.mitumoriNo      = head.mitumoriNo;
                  mtsb.edaban          = head.edaban;
                  mtsb.gyoNo           = gno;
                  //----- 改造内容 -----
                  mtsb.taisyoKataban       = mKtanka.seihinKataban;
                  mtsb.bosyokuKaizo        = kaizoValue(oBJson.get("bousyoku").asBoolean());
                  mtsb.jubosyokuKaizo      = kaizoValue(oBJson.get("Jubousyoku").asBoolean());
                  mtsb.taiengaiKaizo       = kaizoValue(oBJson.get("taiengai").asBoolean());
                  mtsb.taijuengaiKaizo     = kaizoValue(oBJson.get("taijuengai").asBoolean());
                  mtsb.jubouseiKaizo       = kaizoValue(oBJson.get("jubousabi").asBoolean());
                  mtsb.suryo               = oBJson.get("suryo").asLong();
                  mtsb.gokei_suryo         = oBJson.get("gsuryo").asLong();
                  //----- 単価情報 -----
                  mtsb.boKaiTanka        = mKtanka.bousyokuTanka;
                  mtsb.juKaiTanka        = mKtanka.jubousyokuTanka;
                  mtsb.taiKaiTanka       = mKtanka.taiengaiTanka;
                  mtsb.taijuKaiTanka     = mKtanka.taijuengaiTanka;
                  mtsb.jubouKaiTanka     = mKtanka.jubouseiTanka;
                  //----- 帳票情報 -----
                  mtsb.pboKaiTanka       = oBJson.get("pBousyoku").asLong();
                  mtsb.pjuKaiTanka       = oBJson.get("pJubousyoku").asLong();
                  mtsb.ptaiKaiTanka      = oBJson.get("pTaiengai").asLong();
                  mtsb.ptaijuKaiTanka    = oBJson.get("pTaijuengai").asLong();
                  mtsb.pjubouKaiTanka    = oBJson.get("pJubousabi").asLong();
                  //----- その他情報 -----
                  mtsb.mashinNo           = oBJson.get("mashine").asText();
                  mtsb.nyukaYotei         = dateStringFormat(oBJson.get("nyuka").asText());
                  mtsb.nyukaKibou         = dateStringFormat(oBJson.get("kibou").asText());
                  mtsb.hikitori_kibou     = dateStringFormat(oBJson.get("hikitori").asText());
                  mtsb.haiseReinyuDay     = dateStringFormat(oBJson.get("haise").asText());
                  if ("".equals(mtsb.haiseReinyuDay)) { //配セ戻入日が未設定の場合、依頼納期を設定
                    mtsb.haiseReinyuDay = head.iraiNoki;
                  }
                  mtsb.seizou_kanryou_day  = dateStringFormat(oBJson.get("kend").asText());
                  if ("".equals(mtsb.seizou_kanryou_day)) { //改造完了予定日が未設定の場合、回答納期を設定
                    mtsb.seizou_kanryou_day = head.kaitoNoki;
                  }
//製造完了予定日に見積回答日を設定するのは空白の場合のみ
//                  switch (head.mitumoriJotai) {
//                  //見積回答済の場合は回答納期が改造完了予定日
//                  case MtmHead.Stts.MT_ANSER:   //見積回答
//                    mtsb.seizou_kanryou_day = head.kaitoNoki;
//                    break;
//                  default:
//                    break;
//                  }
                  mtsb.bunno_kubun     = MtmTaisyoSeihin.BunoFlag.BUNNO;
                  mtsb.koshinUid     = user.userId;
                  mtsb.koshinPid     = MtmHead.UpdatePID.ESTIMATE;
                  switch (head.mitumoriJotai) {
                  case MtmHead.Stts.MT_ANSER:   //見積回答
                  case MtmHead.Stts.MT_ORDER:   //発注
                  case MtmHead.Stts.MT_COMMIT:  //受注確定
                  case MtmHead.Stts.MT_SEKOU:   //施工完了
                    if ("".equals(sHaiseMax)) { //最大配セ戻入日が依頼納期となる
                      sHaiseMax = mtsb.haiseReinyuDay;
                    }
                    else {
                      if (dateCompare(sHaiseMax, mtsb.haiseReinyuDay) == -1) {
                          sHaiseMax = mtsb.haiseReinyuDay;
                      }
                    }
                    if ("".equals(sKanryoMax)) { //最大製造完了予定日が回答納期となる
                      sKanryoMax = mtsb.seizou_kanryou_day;
                    }
                    else {
                      if (dateCompare(sKanryoMax, mtsb.seizou_kanryou_day) == -1) {
                        sKanryoMax = mtsb.seizou_kanryou_day;
                      }
                    }
                    break;
                  default:
                    break;
                  }
                  mtsb.save();
                  //2021-02-02 分納分のオプション改造を作成する
                  if (optioninfo != null) {
                    for (int oidx = 0; oidx < optioninfo.size(); oidx++) {
                      JsonNode oOptionJson = optioninfo.get(oidx);
                      MtmOptionKaizo mok = new MtmOptionKaizo();
                          //----- 共通情報 -----
                      mok.mitumoriNo      = head.mitumoriNo;
                      mok.edaban          = head.edaban;
                      mok.gyoNo           = gno;
                          //----- 改造内容 -----
                      mok.taisyoKataban     = mKtanka.seihinKataban;
                      mok.optionCd          = oOptionJson.get("optioncd").asText();
                      mok.optionName        = oOptionJson.get("optionname").asText();
                      mok.optionTanka       = oOptionJson.get("optiontanka").asLong();
                      mok.suryo             = mtsb.suryo;
                          //----- その他情報 -----
                      mok.mashinNo          = mtsb.mashinNo;
                      mok.nyukaYotei        = "";
                      mok.nyukaKibou        = "";
                      mok.koshinUid     = user.userId;
                      mok.koshinPid     = MtmHead.UpdatePID.ESTIMATE;
                      mok.save();
                    }
                  }
                  //------------------------------------------------------------------------------------
                  gno++; //このタイミングで行番号をカウントアップ
                }
        	}
        }

        //------------------------------------------------------------------------------------
        //- 特殊改造の登録を行う
        //------------------------------------------------------------------------------------
        JsonNode tokuList = inputParameter.get("toku");

        Logger.info(">>>>> [ESTIMATE] ACTION USER={} Toku={}.", user.userId, tokuList.size());

        //----- 特殊改造を削除します -----
        //ここは削除０件によるエラーがある為、戻り値チェックは行わない
        MtmTokusyuKaizo.deleteData(head.mitumoriNo, head.edaban);

        if (tokuList.size() > 0) {
        	//----- 改造情報を登録します -----
          int gno=0;
        	for(int idx=0; idx < tokuList.size(); idx++){
        		JsonNode oJson = tokuList.get(idx);
                Logger.info(">>>>> [TOKU] tkataban={} .", oJson.get("tkataban"));
                if (oJson.get("tkataban").asText() == null || "".equals(oJson.get("tkataban").asText())) { //製品型番が空欄の場合、対象外
                  continue;
                }
                MtmTokusyuKaizo mtk = new MtmTokusyuKaizo();
                //----- 共通情報 -----
                mtk.mitumoriNo 			= head.mitumoriNo;
                mtk.edaban     			= head.edaban;
                mtk.gyoNo      			= gno;
                //----- 改造内容 -----
                mtk.tokusyuKataban      = oJson.get("tkataban").asText();
                mtk.tokusyuNaiyo      	= oJson.get("tkaizou").asText();
                mtk.suryo      			= oJson.get("tsuryo").asLong();
                mtk.gokei_suryo			= oJson.get("tgsuryo").asLong();
                //----- 単価情報 -----
                mtk.tokusyuKingaku     	= oJson.get("tkingaku").asLong();
                //----- 帳票情報 -----
                mtk.ptokusyuKingaku    	= oJson.get("ptkingaku").asLong();
                //----- その他情報 -----
                mtk.mashinNo			= oJson.get("tmashine").asText();
                mtk.nyukaYotei			= dateStringFormat(oJson.get("tnyuka").asText());
                mtk.nyukaKibou			= dateStringFormat(oJson.get("tkibou").asText());
                mtk.hikitori_kibou		= dateStringFormat(oJson.get("thikitori").asText());
                mtk.haiseReinyuDay		= dateStringFormat(oJson.get("thaise").asText());
                if ("".equals(mtk.haiseReinyuDay)) { //配セ戻入日が未設定の場合、依頼納期を設定
                	mtk.haiseReinyuDay = head.iraiNoki;
                }
                mtk.seizou_kanryou_day	= dateStringFormat(oJson.get("tkend").asText());
                if ("".equals(mtk.seizou_kanryou_day)) { //改造完了予定日が未設定の場合、回答納期を設定
                	mtk.seizou_kanryou_day = head.kaitoNoki;
                }
//製造完了予定日に見積回答日を設定するのは空白の場合のみ
//                switch (head.mitumoriJotai) {
//                //見積回答済の場合は回答納期が改造完了予定日
//                case MtmHead.Stts.MT_ANSER:   //見積回答
//                  mtk.seizou_kanryou_day = head.kaitoNoki;
//                  break;
//                default:
//                  break;
//                }
                mtk.bunno_kubun			= MtmTaisyoSeihin.BunoFlag.NOMAL;
                mtk.koshinUid			= user.userId;
                mtk.koshinPid			= MtmHead.UpdatePID.ESTIMATE;
                switch (head.mitumoriJotai) {
                case MtmHead.Stts.MT_ANSER:   //見積回答
                case MtmHead.Stts.MT_ORDER:   //発注
                case MtmHead.Stts.MT_COMMIT:  //受注確定
                case MtmHead.Stts.MT_SEKOU:   //施工完了
                  if ("".equals(sHaiseMax)) { //最大配セ戻入日が依頼納期となる
                	  sHaiseMax = mtk.haiseReinyuDay;
                  }
                  else {
                	  if (dateCompare(sHaiseMax, mtk.haiseReinyuDay) == -1) {
                    	  sHaiseMax = mtk.haiseReinyuDay;
                	  }
                  }
                  if ("".equals(sKanryoMax)) { //最大製造完了予定日が回答納期となる
                	  sKanryoMax = mtk.seizou_kanryou_day;
                  }
                  else {
                	  if (dateCompare(sKanryoMax, mtk.seizou_kanryou_day) == -1) {
                		  sKanryoMax = mtk.seizou_kanryou_day;
                	  }
                  }
                  break;
                default:
                  break;
                }
                mtk.save();
                //------------------------------------------------------------------------------------
                gno++; //このタイミングで行番号をカウントアップ
                //------------------------------------------------------------------------------------
                //- 分納情報の登録を行う
                //------------------------------------------------------------------------------------
                JsonNode bunnonList = oJson.get("bunnoinfo");
                for(int idx2=0; idx2 < bunnonList.size(); idx2++){
                  JsonNode oBJson = bunnonList.get(idx2);
                  MtmTokusyuKaizo mtkb = new MtmTokusyuKaizo();
                  //----- 共通情報 -----
                  mtkb.mitumoriNo      = head.mitumoriNo;
                  mtkb.edaban          = head.edaban;
                  mtkb.gyoNo           = gno;
                  //----- 改造内容 -----
                  mtkb.tokusyuKataban      = oJson.get("tkataban").asText();
                  mtkb.tokusyuNaiyo        = oJson.get("tkaizou").asText();
                  mtkb.suryo               = oBJson.get("tsuryo").asLong();
                  mtkb.gokei_suryo         = oBJson.get("tgsuryo").asLong();
                  //----- 単価情報 -----
                  mtkb.tokusyuKingaku      = oJson.get("tkingaku").asLong();
                  //----- 帳票情報 -----
                  mtkb.ptokusyuKingaku     = oJson.get("ptkingaku").asLong();
                  //----- その他情報 -----
                  mtkb.mashinNo      = oBJson.get("tmashine").asText();
                  mtkb.nyukaYotei      = dateStringFormat(oBJson.get("tnyuka").asText());
                  mtkb.nyukaKibou      = dateStringFormat(oBJson.get("tkibou").asText());
                  mtkb.hikitori_kibou    = dateStringFormat(oBJson.get("thikitori").asText());
                  mtkb.haiseReinyuDay    = dateStringFormat(oBJson.get("thaise").asText());
                  if ("".equals(mtkb.haiseReinyuDay)) { //配セ戻入日が未設定の場合、依頼納期を設定
                    mtkb.haiseReinyuDay = head.iraiNoki;
                  }
                  mtkb.seizou_kanryou_day  = dateStringFormat(oBJson.get("tkend").asText());
                  if ("".equals(mtkb.seizou_kanryou_day)) { //改造完了予定日が未設定の場合、回答納期を設定
                    mtkb.seizou_kanryou_day = head.kaitoNoki;
                  }
//製造完了予定日に見積回答日を設定するのは空白の場合のみ
//                  switch (head.mitumoriJotai) {
//                  //見積回答済の場合は回答納期が改造完了予定日
//                  case MtmHead.Stts.MT_ANSER:   //見積回答
//                    mtkb.seizou_kanryou_day = head.kaitoNoki;
//                    break;
//                  default:
//                    break;
//                  }
                  mtkb.bunno_kubun     = MtmTaisyoSeihin.BunoFlag.BUNNO;
                  mtkb.koshinUid     = user.userId;
                  mtkb.koshinPid     = MtmHead.UpdatePID.ESTIMATE;
                  switch (head.mitumoriJotai) {
                  case MtmHead.Stts.MT_ANSER:   //見積回答
                  case MtmHead.Stts.MT_ORDER:   //発注
                  case MtmHead.Stts.MT_COMMIT:  //受注確定
                  case MtmHead.Stts.MT_SEKOU:   //施工完了
                    if ("".equals(sHaiseMax)) { //最大配セ戻入日が依頼納期となる
                      sHaiseMax = mtkb.haiseReinyuDay;
                    }
                    else {
                      if (dateCompare(sHaiseMax, mtkb.haiseReinyuDay) == -1) {
                          sHaiseMax = mtkb.haiseReinyuDay;
                      }
                    }
                    if ("".equals(sKanryoMax)) { //最大製造完了予定日が回答納期となる
                      sKanryoMax = mtkb.seizou_kanryou_day;
                    }
                    else {
                      if (dateCompare(sKanryoMax, mtkb.seizou_kanryou_day) == -1) {
                        sKanryoMax = mtkb.seizou_kanryou_day;
                      }
                    }
                    break;
                  default:
                    break;
                  }
                  mtkb.save();
                  //------------------------------------------------------------------------------------
                  gno++; //このタイミングで行番号をカウントアップ
                }
        	}
        }
    		// 要求納期、回答納期の再ｾｯﾄ
    		if (!"".equals(sHaiseMax)) {
    			head.iraiNoki 	= sHaiseMax;
    		}
    		if (!"".equals(sKanryoMax)) {
    			head.kaitoNoki 	= sKanryoMax;
    		}
        if ("Retrieval".equals(sessionkeys.mode)) { //見積検索からの遷移の場合

        	head.update();

        }
        else { //見積作成時

        	//見積ヘッダの作成
        	head.save();
        	//新規作成後は見積検索モードに自動的に切り替える
        	parameter.SessionKeys keys = new parameter.SessionKeys();
        	keys.mtmno   = head.mitumoriNo;
        	keys.edaban  = head.edaban;
        	keys.mode    = "Retrieval";
        	setSessionKeys(keys);

        }

        resultJson.put("result"   , "success");
        resultJson.put("hachuok"  , hachuflg);

      } catch (Exception e) {
        Logger.error(e.getMessage(), e);
        resultJson.put("result"   , "adderror");
      }
      finally {
      }
    }

    return ok(resultJson);
}
	/**
	 * 削除処理
	 * @return
	 */
	@Authenticated(WeosAuthenticator.class)
	public static Result deleteEstimate() {

	    ObjectNode resultJson  = Json.newObject();
	    boolean result = false;

	    getSessionUser();

	    //------------------------------------------------------------------------------------
	    //- パラメータの取得
	    //------------------------------------------------------------------------------------
	    parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
	    sessionkeys = getSessionKeys();

	    Logger.info(">>>>> [ESTIMATE] ACTION USER={} DELETE MTMNO={} EDABN={} MODE={}.", user.userId, sessionkeys.mtmno, sessionkeys.edaban, sessionkeys.mode);

	    //------------------------------------------------------------------------------------
	    //- 見積情報を削除する
	    //------------------------------------------------------------------------------------
	    synchronized (objLock) {
	      try {
	        Ebean.beginTransaction();
	        result = MtmHead.deleteData(sessionkeys.mtmno, sessionkeys.edaban, user.userId);
	        if (result) {
	            result = MtmTaisyoSeihin.deleteData(sessionkeys.mtmno, sessionkeys.edaban);
	            if (result) {
	                result = MtmTokusyuKaizo.deleteData(sessionkeys.mtmno, sessionkeys.edaban);
	                if (result) {
	                    resultJson.put("result"   , "success");
	                    Ebean.commitTransaction();
	                }
	                else {
	                    resultJson.put("result"   , "deleteerror");
	                    Ebean.rollbackTransaction();
	                }
	            }
	            else {
	                resultJson.put("result"   , "deleteerror");
	                  Ebean.rollbackTransaction();
	            }
	        }
	        else {
	            resultJson.put("result"   , "deleteerror");
	              Ebean.rollbackTransaction();
	        }

  	    } catch (Exception e) {
  	      Logger.error(e.getMessage(), e);
  	        resultJson.put("result"   , "deleteerror");
  	          Ebean.rollbackTransaction();
  	    }
        finally {
          Ebean.endTransaction();
        }
      }

	    return ok(resultJson);
	}
  /**
   * 見積書印刷
   * @return
   */
  @Authenticated(WeosAuthenticator.class)
  public static Result printmitumorisyo() {

      ObjectNode resultJson  = Json.newObject();
      boolean result = false;
      boolean isRePrint = false;

      getSessionUser();

      //------------------------------------------------------------------------------------
      //- パラメータの取得
      //------------------------------------------------------------------------------------
      JsonNode  inputParameter  = request().body().asJson();
      parameter.SessionKeys sessionkeys = new parameter.SessionKeys();
      sessionkeys = getSessionKeys();
      MstShiten shiten = MstShiten.find.where().eq("kaisya_cd", user.kaisyaCd).eq("shiten_cd", user.shitenCd).findUnique();

      Logger.info(">>>>> [ESTIMATE] ACTION USER={} PRINT MTMNO={} EDABN={} MODE={} NAIGAI={}.", user.userId, sessionkeys.mtmno, sessionkeys.edaban, sessionkeys.mode, shiten.kennaigaiFlg);

      String printid = inputParameter.get("printid").asText();

      //------------------------------------------------------------------------------------
      //- 帳票履歴から再発行フラグの確定
      //------------------------------------------------------------------------------------
      java.sql.Date hakou = TyohyouRireki.getRireki(printid, sessionkeys.mtmno, sessionkeys.edaban);
      if (hakou != null) {
    	  Calendar cal = Calendar.getInstance();
    	  Calendar rireki = Calendar.getInstance();
    	  rireki.setTime(hakou);
    	  cal.set(Calendar.HOUR_OF_DAY, 0);
    	  cal.set(Calendar.MINUTE, 0);
    	  cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    	  rireki.set(Calendar.HOUR_OF_DAY, 0);
    	  rireki.set(Calendar.MINUTE, 0);
    	  rireki.set(Calendar.SECOND, 0);
    	  rireki.set(Calendar.MILLISECOND, 0);

    	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:sss");
    	  int check = cal.compareTo(rireki);
    	  Logger.info("[estimate] Print history check systemdate={} rirekidate={} check={}", sdf.format(cal.getTime()), sdf.format(rireki.getTime()), check);

    	  if (check != 0) { //日付が違う場合
    		  isRePrint = true;
    	  }
      }
      //------------------------------------------------------------------------------------
      //- 帳票データの生成
      //------------------------------------------------------------------------------------
      switch (printid) {
      case TyohyouRireki.PrintCode.MIRI:
      case TyohyouRireki.PrintCode.MTSU:
      case TyohyouRireki.PrintCode.URIA:
      case TyohyouRireki.PrintCode.NOUH:
      case TyohyouRireki.PrintCode.SEIK:
      case TyohyouRireki.PrintCode.JYUR:
        mitumoriedit(inputParameter, sessionkeys, isRePrint, resultJson);
        break;
      case TyohyouRireki.PrintCode.KZOU:
          iraiedit(inputParameter, sessionkeys, isRePrint, resultJson);
          break;

      case TyohyouRireki.PrintCode.NOUS:
          resultJson.put("dataurl", MstProperty.GetValue(MstProperty.Code.NOUHIN_URL));
          break;

      case TyohyouRireki.PrintCode.SEKO:
          sekouedit(inputParameter, sessionkeys, isRePrint, resultJson);
          break;

      default:
        break;
      }
      //------------------------------------------------------------------------------------
      //- 帳票履歴の生成
      //------------------------------------------------------------------------------------
      TyohyouRireki.makeRireki(printid, sessionkeys.mtmno, sessionkeys.edaban, user.userId, MtmHead.UpdatePID.ESTIMATE);

      resultJson.put("printid", printid);
      resultJson.put("result", "success");

      return ok(resultJson);
  }
  /**
   * 見積書を編集します
   * @param inputParameter
   * @param sessionkeys
   * @param isRePrint
   * @param resultJson
   */
  public static void mitumoriedit(JsonNode  inputParameter, parameter.SessionKeys sessionkeys, boolean isRePrint, ObjectNode resultJson) {
      //------------------------------------------------------------------------------------
      //- 帳票出力指示
      //------------------------------------------------------------------------------------
      // 帳票出力指示の取得
      String printid = inputParameter.get("printid").asText();
      //  帳票タイトルセット
      String title = "？？？？？？";
      String title_sw = "OFF";
      String inkan_sw = "OFF";
      long keisan;
      long tanka;
      boolean tankanebiki=false;
      long datacount = 0;

      switch (printid) {
      case TyohyouRireki.PrintCode.MIRI:
        title = TyohyouRireki.PrintTitle.MIRI;  // 見積依頼書
        break;

      case TyohyouRireki.PrintCode.MTSU:
        title = TyohyouRireki.PrintTitle.MTSU;  // 見積書
        title_sw = "ON";
        inkan_sw = "ON";
        break;

      case TyohyouRireki.PrintCode.URIA:
        title = TyohyouRireki.PrintTitle.URIA;  // 売上伝票
        title_sw = "ON";
        break;

      case TyohyouRireki.PrintCode.NOUH:
        title = TyohyouRireki.PrintTitle.NOUH;  // 納品書
        title_sw = "ON";
        break;

      case TyohyouRireki.PrintCode.SEIK:
        title = TyohyouRireki.PrintTitle.SEIK;  // 請求書
        title_sw = "ON";
        break;

      case TyohyouRireki.PrintCode.JYUR:
        title = TyohyouRireki.PrintTitle.JYUR;  // 受領書
        title_sw = "ON";
        break;

      default:
        break;
      }

      //**************************************************************************
      // 見積対象製品テーブル
      //**************************************************************************

      List<PMitumoriBean> printList = new ArrayList<PMitumoriBean>(); //見積明細用リスト
      List<MtmTaisyoSeihin> mts     = MtmTaisyoSeihin.GetMTTaisyouSeihin(sessionkeys.mtmno, sessionkeys.edaban);
      DecimalFormat df = new DecimalFormat("#,##0");

      for (MtmTaisyoSeihin data : mts) {
        if (MtmTaisyoSeihin.BunoFlag.BUNNO.equals(data.bunno_kubun)) { //分納行は対象外
          continue;
        }
        boolean kaizou = false;
        //----- 防蝕 -----
        if (boolKaizo(data.bosyokuKaizo)) {
          kaizou = true;
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei = data.taisyoKataban;
          bean.siyou = MtmTaisyoSeihin.KaizoSiyo.BOUSYOKU;
          bean.daisu = data.gokei_suryo;
          if (data.pboKaiTanka > 0) {
            tanka = data.pboKaiTanka;
            tankanebiki=true;
          }
          else {
            tanka = data.boKaiTanka;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.gokei_suryo);

          bean.daisuSt = String.valueOf(data.gokei_suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.gokei_suryo;
          bean.kingakuSt = df.format(keisan);
          printList.add(bean);
        }
        //----- 重防蝕 -----
        if (boolKaizo(data.jubosyokuKaizo)) {
          kaizou = true;
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei = data.taisyoKataban;
          bean.siyou = MtmTaisyoSeihin.KaizoSiyo.JUBOUSYOKU;
          bean.daisu = data.gokei_suryo;
          if (data.pjuKaiTanka > 0) {
            tanka = data.pjuKaiTanka;
            tankanebiki=true;
          }
          else {
            tanka = data.juKaiTanka;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.gokei_suryo);

          bean.daisuSt = String.valueOf(data.gokei_suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.gokei_suryo;
          bean.kingakuSt = df.format(keisan);
          printList.add(bean);
        }
        //----- 耐塩害 -----
        if (boolKaizo(data.taiengaiKaizo)) {
          kaizou = true;
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei = data.taisyoKataban;
          bean.siyou = MtmTaisyoSeihin.KaizoSiyo.TAIENGAI;
          bean.daisu = data.gokei_suryo;
          if (data.ptaiKaiTanka > 0) {
            tanka = data.ptaiKaiTanka;
            tankanebiki=true;
          }
          else {
            tanka = data.taiKaiTanka;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.gokei_suryo);

          bean.daisuSt = String.valueOf(data.gokei_suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.gokei_suryo;
          bean.kingakuSt = df.format(keisan);
          printList.add(bean);
        }
        //----- 耐重塩害 -----
        if (boolKaizo(data.taijuengaiKaizo)) {
          kaizou = true;
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei = data.taisyoKataban;
          bean.siyou = MtmTaisyoSeihin.KaizoSiyo.TAIJUENGAI;
          bean.daisu = data.gokei_suryo;
          if (data.ptaijuKaiTanka > 0) {
            tanka = data.ptaijuKaiTanka;
            tankanebiki=true;
          }
          else {
            tanka = data.taijuKaiTanka;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.gokei_suryo);

          bean.daisuSt = String.valueOf(data.gokei_suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.gokei_suryo;
          bean.kingakuSt = df.format(keisan);
          printList.add(bean);
        }
        //----- 重防錆 -----
        if (boolKaizo(data.jubouseiKaizo)) {
          kaizou = true;
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei = data.taisyoKataban;
          bean.siyou = MtmTaisyoSeihin.KaizoSiyo.JUBOSABI;
          bean.daisu = data.gokei_suryo;
          if (data.pjubouKaiTanka > 0) {
            tanka = data.pjubouKaiTanka;
            tankanebiki=true;
          }
          else {
            tanka = data.jubouKaiTanka;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.gokei_suryo);

          bean.daisuSt = String.valueOf(data.gokei_suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.gokei_suryo;
          bean.kingakuSt = df.format(keisan);
          printList.add(bean);
        }
        //----- オプション部品のみ -----
        if (!kaizou) {
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei  = data.taisyoKataban;
          bean.siyou    = "";
          bean.daisu    = 0;
          bean.tanka    = 0;
          bean.kingaku  = 0;
          bean.daisuSt  = "";
          bean.tankaSt  = "";
          bean.kingakuSt= "";
          printList.add(bean);
        }
		//**************************************************************************
		// 見積オプション改造テーブル
		//**************************************************************************
        List<MtmOptionKaizo> moks = MtmOptionKaizo.GetMeisai(data.mitumoriNo, data.edaban, data.taisyoKataban, data.gyoNo);
        for (MtmOptionKaizo data2 : moks) {
            PMitumoriBean bean = new PMitumoriBean();
            bean.katamei  = "";
            bean.siyou    = data2.optionName;
            bean.daisu    = data.gokei_suryo;
            bean.tanka    = data2.optionTanka;
            bean.kingaku  = data2.optionTanka * data.gokei_suryo;
            bean.daisuSt  = String.valueOf(bean.daisu);
            bean.tankaSt  = df.format(bean.tanka);
            bean.kingakuSt= df.format(bean.kingaku);
            printList.add(bean);
        }
      }

      //**************************************************************************
      // 特殊改造テーブル
      //**************************************************************************
      List<MtmTokusyuKaizo> mtk     = MtmTokusyuKaizo.GetMTTokusyu(sessionkeys.mtmno, sessionkeys.edaban);

      for (MtmTokusyuKaizo data : mtk) {
        if (MtmTaisyoSeihin.BunoFlag.BUNNO.equals(data.bunno_kubun)) { //分納行は対象外
          continue;
        }
        PMitumoriBean bean = new PMitumoriBean();
        bean.katamei  = data.tokusyuKataban;
        bean.siyou    = data.tokusyuNaiyo;
        bean.daisu    = data.gokei_suryo;
        if (data.ptokusyuKingaku > 0) {
          tanka = data.ptokusyuKingaku;
        }
        else {
          tanka = data.tokusyuKingaku;
        }
        bean.tanka    = tanka;
        bean.kingaku  = tanka * data.gokei_suryo;
        bean.daisuSt  = String.valueOf(data.gokei_suryo);
        bean.tankaSt  = df.format(tanka);
        bean.kingakuSt= df.format(bean.kingaku);
        printList.add(bean);
      }

      //**************************************************************************
      // 余白の作成
      //**************************************************************************

      // 10の余りが0以外の場合余白の作成
      int line = 10;
      int amari = (mts.size() + mtk.size() ) % line;

      //**************************************************************************
      // 【改造依頼内容】ラベルの作成
      //**************************************************************************
      for (int x=0; x<printList.size(); x++ ){
        PMitumoriBean bean = printList.get(x);
        int gyo = x % line;
        if (gyo==0){
          bean.kaizoNaiyo = "";
        }
        if (gyo==1){
          bean.kaizoNaiyo = "";
        }
        if (gyo==2){
          bean.kaizoNaiyo = "改";
        }
        if (gyo==3){
          bean.kaizoNaiyo = "造";
        }
        if (gyo==4){
          bean.kaizoNaiyo = "依";
        }
        if (gyo==5){
          bean.kaizoNaiyo = "頼";
        }
        if (gyo==6){
          bean.kaizoNaiyo = "内";
        }
        if (gyo==7){
          bean.kaizoNaiyo = "容";
        }
        if (gyo==8){
          bean.kaizoNaiyo = "";
        }
        if (gyo==9){
          bean.kaizoNaiyo = "";
        }
      }

      //**************************************************************************
      // ヘッダープロパティセット用の会社名、支店名、部門名を作成する。
      //**************************************************************************

      String kaisyaName = "";
      String shitenName = "";
      String bumonName = "";
      String mituiraininName = "";
      String mitsukaininName = "";

      MtmHead head = MtmHead.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findUnique();
      if (head != null) { //見積ヘッダが取得出来た場合
        //**************************************************************************
        // 値引金額
        //**************************************************************************
        if("ON".equals(title_sw) && head.nebikiGaku > 0) {
          PMitumoriBean bean = new PMitumoriBean();
          bean.katamei  = "";
          bean.siyou    = "値　引";
          bean.kingaku  = head.nebikiGaku * -1;
          bean.daisuSt  = "";
          bean.tankaSt  = "";
          bean.kingakuSt= df.format(bean.kingaku);
          printList.add(bean);
        }

        ObjectNode userJson  = Json.newObject();  //ユーザ情報JSON
        //----- 見積依頼者情報 -----
        MstUser.getUserToJson(head.sakuseiUid, userJson);
        kaisyaName = userJson.get("kaisyaName").asText();
        shitenName = userJson.get("shitenName").asText();
        bumonName  = userJson.get("bumonName").asText();
        kaisyaName = userJson.get("kaisyaName").asText();
        mituiraininName = userJson.get("shimeiKanji").asText();

        //----- プロパティよりLE系の会社コード・支店コードを取得 -----
        String sLeKcode = MstProperty.GetValue(MstProperty.Code.LE_KCODE);
        String sMrScode = MstProperty.GetValue(MstProperty.Code.MR_SCODE);
        String sMrKname = MstProperty.GetValue(MstProperty.Code.MR_KNAME);

        if (head.mitumoriIraiKaisya.equals(sLeKcode)
            && head.mitumoriIraiSiten.equals(sMrScode)) { //株式会社メルの場合
          kaisyaName = sMrKname;
          shitenName = "";
          bumonName  = "";
        }
        //----- 見積回答者情報 -----
        ObjectNode kaitoJson  = Json.newObject();  //ユーザ情報JSON
        MstUser.getUserToJson(head.kaitouUid, kaitoJson);
        mitsukaininName = kaitoJson.get("shimeiKanji").asText();

      }

      //**************************************************************************
      // ヘッダープロパティ
      //**************************************************************************
      Calendar cal = Calendar.getInstance();
      // 改造依頼日
      String date = warekiDateFormat(systemDateFormat());

      // 宛先１１（空白）
      String add11 = "";
      // 宛先１２（自社名）
      String add12 = MstProperty.GetValue(MstProperty.Code.MY_NAME) + "　様";
      // 宛先１３（空白）
      String add13 = "";
      // 宛先１４（空白）
      String add14 = "";
      // 宛先１５（空白）
      String add15 = "";
      // 宛先２１（空白）
      String add21 = "";
      // 宛先２２（会社名称)
      String add22 = kaisyaName;
      // 宛先２３（支店名称＋部門マスタ＋部門略称）
      String add23 = shitenName + "　" + bumonName;
      // 宛先２４（空白）
      String add24 = "";
      // 宛先２５（空白）
      String add25 = "";
      // 見積No
      String mitumorino = "";

      if ("ON".equals(title_sw)) {
        // 宛先１１（空白）
        add11 = kaisyaName;
        // 宛先１２（会社名称）
        add12 = shitenName + "　" + bumonName;
        // 宛先１３（支店名称＋部門マスタ＋部門略称）
        add13 = mituiraininName + "　様";
        // 宛先１４（空白）
        add14 = "";
        // 宛先１５（空白）
        add15 = "";
        // 宛先２１（自社名）
        add21 = MstProperty.GetValue(MstProperty.Code.MY_NAME);
        // 宛先２２（自社住所１)
        add22 = MstProperty.GetValue(MstProperty.Code.MY_ADDR1);
        // 宛先２３（自社住所２）
        add23 = MstProperty.GetValue(MstProperty.Code.MY_ADDR2);
        // 宛先２４（自社住所３）
        add24 = MstProperty.GetValue(MstProperty.Code.MY_ADDR3);
        // 宛先２５（電話・ＦＡＸ）
        add25 = "TEL " + MstProperty.GetValue(MstProperty.Code.MY_TEL) + " FAX " + MstProperty.GetValue(MstProperty.Code.MY_FAX);
      }
      // 工事件名
      String kenmei = head.kojiKenmei;
      // 得意先名称No.
      String tokuisaki = head.tokuisakiName;
      // 見積合計金額
      Long mitumori_s_gokei = head.saisyuMitumoriGokei;
      // 本体金額
      Long mitumori_h_gokei = head.mitumoriGokei;
      // 消費税
      Long mitumori_zei     = head.saisyuMitumoriGokei - head.mitumoriGokei;
      // 見積No
      if (head.edaban == 0) {
        mitumorino = head.mitumoriNo;
      }
      else{
        mitumorino = head.mitumoriNo + "-" + head.edaban;
      }
      // 管理番号
      String kanrino = head.kanriNo;
      // 見積依頼日
      String mitsuiraiday = warekiDateFormat(head.mitumoriIraibi);
      // 依頼納期
      String mitsuirainou = warekiDateFormat(head.iraiNoki);
      // 見積回答日
      String mitsukaiday =  warekiDateFormat(head.mitumoriKaitoubi);
      // 回答納期
      String mitsukainou =  warekiDateFormat(head.kaitoNoki);
      // 見積有効期限
      String mitsuyukokigen =  warekiDateFormat(head.mitumoriYukoKigen);
      //**************************************************************************
      // フッタープロパティ
      //**************************************************************************
      // 完了後送り先（郵便）
      String okurisaki1 = editPostcode(head.nohinsakiYubin);
      // 完了後送り先（住所1）
      String okurisaki2 = head.nohinsakiAdd1;
      // 完了後送り先（住所2）
      String okurisaki3 = head.nohinsakiAdd2;
      // 運賃請求先
      String untin_seikyusaki = head.untinSeikyu;
      // 改造費請求先
      String kaizohi_seikyusaki = head.kaizouhiSeikyu;
      // パネル色
      String panelColor = head.paneruColor;
      // 設置場所
      String settiPlace = head.settiBasyo;
      // 改造センター (ﾌﾟﾛﾊﾟﾃｨﾌｧｲﾙより、電話番号)
      String kaizo_center1 = MstProperty.GetValue(MstProperty.Code.CENTER_TEL);
      // 改造センター (ﾌﾟﾛﾊﾟﾃｨﾌｧｲﾙより、FAX番号)
      String kaizo_center2 = MstProperty.GetValue(MstProperty.Code.CENTER_FAX);
      // 工程納期1（フッター）
      String kouteinouki_fotter1 = head.kouteiNouki1;
      // 工程納期2（フッター）
      String kouteinouki_fotter2 = head.kouteiNouki2;
      // 工程納期3（フッター）
      String kouteinouki_fotter3 = head.kouteiNouki3;

      StringBuffer stb = new StringBuffer();

      //共通部分の定義
      stb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      stb.append("<pxd paper-type=\"a4\" orientation=\"landscape\" name=\"pxdDocument\" delete=\"yes\" save=\"no\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\r\n");
      stb.append("<page type=\"hidden\" id=\"back1\">\r\n");
      stb.append("<svg viewBox=\"0 0 29700 21000\">\r\n");
      if ("ON".equals(inkan_sw)){
          stb.append("<image x=\"25673\" y=\"2087\" width=\"2380\" opacity=\"0.4\" xlink:href=\"data:;base64,\r\n");
          stb.append("    /9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcU\r\n");
          stb.append("    FhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgo\r\n");
          stb.append("    KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCABaAFoDASIA\r\n");
          stb.append("    AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA\r\n");
          stb.append("    AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3\r\n");
          stb.append("    ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm\r\n");
          stb.append("    p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA\r\n");
          stb.append("    AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx\r\n");
          stb.append("    BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK\r\n");
          stb.append("    U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3\r\n");
          stb.append("    uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDZ8Y6d\r\n");
          stb.append("    HrcOu3+qtfW+ladqkllZ2Oi2aF5Cg+ZmPB6nqew6Vxcfg3wzLCGuLLxvHOYcxK2mIVlJ5wpBOPXk\r\n");
          stb.append("    jp1zXp3wy1fxdL4DS+0XQ7bVZ59SuZWN1diNkB9M4PUsOeePeutbU/igkaSDw74eYMDmJb5w6fUn\r\n");
          stb.append("    j16ZrzFDnSlrr5Nn1DxFTDN0o8tou3xRW3lbr3Z4hN4FsIJtl3oHjCZpTIFjEUZZMEbJePl4BwFP\r\n");
          stb.append("    vzxz0vg74ZeF9a1Oa1h0/wAY6cY0w017FHChwRmPcMnOD/8AX4rvNJ8aeKvEM2r6HY2Om6Z4o0ph\r\n");
          stb.append("    563Baa3kUk42suCpI29c9c1ZRfiswlE3/CL/ADKfL2Syr5bdiflORx096lUY6WTa9P8AglVMfiXB\r\n");
          stb.append("    xc1CX+Lp3WnVeZyupfDXwn4c1Kz03Vta155Nfc2qxpL8s20qVV8L0UkYz3Nakv7PvhViPLu9UjXA\r\n");
          stb.append("    UjzVORnJGdvFc54q0/4gp4h8OS67qmlrc/bvJsri1R2xI2DtdODsIUbsZ967iay+LGX8rVvC+Ff5\r\n");
          stb.append("    c28g3r6nrjHp+tOmqUrrkenr/mY1auJgozjiVdp318+mhwg+H+jP8QpvCUmo+JIpWsjci7a9VluT\r\n");
          stb.append("    8vGzbxtwD15K9OKm1jwPp1p4rfw3J4h8UGabTTqEspnBSUI2MH34zjoMCnyxeLLf4zQxo+kS+IJt\r\n");
          stb.append("    PZDdGFxbiLOQ23qHGCvXB49a1finBf6fPoepHV4E8ULYz27W8GnyTLeggEqoHKDPAJIxnr1qXTgo\r\n");
          stb.append("    yly2s/667mzr14yhCFT44+uuuuz0uv6R5xbWEeoweFb0634iW/16eRbWSSYE27p+73k5ywJOMrj5\r\n");
          stb.append("    T+FVblZoND02/wBG8SeImiur17Hynco0UwAIyiuc5LZ4Pfpk1a+E1jDrmo6NLcasr3mjNJJY6PMG\r\n");
          stb.append("    XzWVdwCyElR8yjOBxjOOay/H1l9g15lt7e2064W78x7a11gT7ZCAcqoA8sg5yx6cY4rlkk4cy0X9\r\n");
          stb.append("    fI9ylOf1n6tKd2tdlbd90m9LarS979S1quteLPBni68gt9a1W6fTmQSvdl2hdXwVyrMflJOM8H0r\r\n");
          stb.append("    6H0b4g6Xf6PY3k00cEtxBHK8XJ2FlBK574zivErzSFtvhR4nubnTzDKWtXW8fVEvvN2yjC5X7mM9\r\n");
          stb.append("    z/FXmtvZXLW8TCdwCoIAmI7emaf1mWGS97Rmby6hnEbzspQdm1ZX0Xa63vY+mP2cgIvhvlydwvbg\r\n");
          stb.append("    uzN1O4c/pWzefFjwVagF9ftXB7RhnbOfQDNZf7PkzTfD9mmijhJvrjMaLgL8w4x25zxUvxFv/Cvh\r\n");
          stb.append("    m2uHh0fSrzxLcgNbWUdsjzzSHIDFQMkDkk98HvXpqUo0Yyi0j5arTp1cdUhUi22+jX6rbzMz4L3r\r\n");
          stb.append("    eJPE3izxP9jmht7uVILaXIEbxJnAwOSw4JY564HQ1676V4z4d+IfjEadaRN8Obv5fkZoA1unsQjr\r\n");
          stb.append("    8oz154rqvhZ45u/G1vqL3mjSaY1pIqAly6yZ3ZAJUcqVwfrTw9WCShe7fkTmOErc8q3KlFWVlJOy\r\n");
          stb.append("    2WzNrxfp2iTJYat4hkWGHR5/tccry7ERsbQW9Rz0rnNV+MngvT3kiTUnvZkO3ZZwNJk+zcKfwNN1\r\n");
          stb.append("    jVvH0lxcQJ4M0q605pPLVZb5WMi5+82eAMDpg4JHWtfwPaXwaabWPC2k6JOoHlNZSJIzZzuBwo24\r\n");
          stb.append("    wO5zn2queTnaKt8mZRo04U+av71tkpR/LV/cc78OLHVtc8bar411a1uNMingFlaWNxGFk8oEHc3G\r\n");
          stb.append("    RyDj13ewzBroupPjbejLtZR+GX8xQfl+Z2wD6ZI/Suw+JviCfwv4J1LVrMRG6hVFiEoypZnCjI79\r\n");
          stb.append("    a5H4g+If7C1XT72K+8M6fqFxZbJW1C3mklZc5Cr5fRNxPXv+NZVOWMXBvVam+HdWvU54R0knFJX0\r\n");
          stb.append("    sl89n95y/h6KKHTPhBcRW4V1M4aQE9PLJIx3JwD+FZFjHBpvgDw/rKWcA1rWddWK5uDEu8o0rFky\r\n");
          stb.append("    wJA+Xtj0q5a+OpLGzsraz13wHBHaMRbqtndkRg9QOPl6nkHvT7rxzeWMUGnySeBr+O5nLQzWZkMV\r\n");
          stb.append("    lMDvaaRG3EjJzkY5zz1rjTi3e/8AWn+R7DhiYvlUN3frteT6pfzJ28u9iDxXosejar8SNK02B4dJ\r\n");
          stb.append("    m0yDUBHF91ZPMB79M/Mceg46V5DDJN5KbbiUDaMAQZx+PevbrnWJ9b8G+N7651fw9qTf2eiyNpkM\r\n");
          stb.append("    iSAgnbv3clQC2Pqa8x0/Q7GawtpHikLvErHATGSB71hiYRunbR/5ns5LXdOnNVd00vuiu9j1j4Ra\r\n");
          stb.append("    HP4k+ET2enaxe6O7ajM5uLfO9l4+U5OcYPY9QOTzXofg34faJ4VuGvLZZ7vVHUrJf3chkmYE9M9B\r\n");
          stb.append("    0HQdq81+FHiabwx8NraPT9D1bVpLq9uRbJBHkLgrhXIztzz0BHBroID8S/F+xJxB4S09kHmMv7y4\r\n");
          stb.append("    cHrtB+6ceuMEV6VJwUYtJuVv68kfLY2Fd1asXNQpuT3aV/l8T222Nz4ieOItKA0LQCt74pvsQ21s\r\n");
          stb.append("    hz5RbI8xz0AXBOD6enNbPw68Lp4R8K2mmeZ504zLcSjP7yVjlm55x2HsKj8FeB9I8JWzm0R57+b5\r\n");
          stb.append("    ri9nO6aZs5yT9ew/WtDw/wCJtK8QS6hFpV1572Ext7gbGUK4z0JGCODyPSt4J83NU36I8ytKPs/Z\r\n");
          stb.append("    4dNxVm21u9l6Lsv6Wz2o45ryTV/Gfja31y4s003w3awm6MFvNdaovzKfuEqGzkjBxtzz0qWa0vNf\r\n");
          stb.append("    8t/FnjnTLewGN9jpE6wpIP4leVm3EE8cY4/Q+sJ6RV2CwLSUpyST101f3L9bDPGGqW3xC8TWHhHR\r\n");
          stb.append("    ZludOt51u9WuYvmRVjOViznBJYY7/oa2/iJ4l07Q7+zin1nRbGcRO5hvbcyuVIO1lK8qAw9OcH0q\r\n");
          stb.append("    TSdQ8H+DfL0fQookaaCW8WO0UyeYsaktl+cnAOAT246VieL/ABNHrPh/QtV02+0bSNN1FDJJfavE\r\n");
          stb.append("    ski4+7EsX8TZ3Z5wMH1rGbtF3fvM6abU6kIRi1BXSvu2767PXTZJ7dzjNP8AG88+qy2X/CX+HJYZ\r\n");
          stb.append("    VjkW9XR5HfzGbbjbwu7opJPQjvXRW1vbeMo1sbDxNoTa7aulzbXNnpqiRY0G1g6N0+ZjwD0I4rKh\r\n");
          stb.append("    8TrFaqbHxzeXFxG+1lt/DgDSEDIUjbkKT9P61Uj8UPdWqPL8RNY0q6EZkuILnRE81cYB5VcE7jnj\r\n");
          stb.append("    Py/QmuOFk+V6p/1/MepKnNv2lOPK1to9/T2a37bG/dW1/pWieNNB1vVbG9K6ULtJobeO3kXduDB1\r\n");
          stb.append("    XjGVGCeua8W02TRjp1r50NwZfKTeVdwCcDOK9i0/TNLufh/4w13/AISO71+6vLF4Zrh4jEY1UFlT\r\n");
          stb.append("    y+oPI6+vHBryDS5WGmWgErgeSmAAuPuisMZTnPlUV/w136nr5SlUjUvq7q+lteVX0svyPdv2bIni\r\n");
          stb.append("    8ASrKGVvt02Q4IYEYBDZ716szBQSxAAHevE/hDrGpwfC+5u/DumNq15JqkwEMk6xlVbHzMT1xxnu\r\n");
          stb.append("    c1fl8F+M/Gd+snjbVotP0kKobTtLdsS9M7j7n1z7V6dCs1TjFRu/66nzGOw0auLq1KlRRjd+vyit\r\n");
          stb.append("    fvsvMn8b+PrrVr8eF/h6yXuszbkuLpMGOzQHDMW6ZH4+2TgV1vgrwzZ+BPB/2KAPcGNWnuHSMs88\r\n");
          stb.append("    mPmIUZJJAACj0ArQ8L+GdF8K2P2XQ7KK1iPLNks78/xOxJPXueK2gQehB/Gt4Qd+eb1/I4a+Jhye\r\n");
          stb.append("    woJqG7vvJ93+i6eZ4PrXiH4Z6tq5utT8JanJdE/vpxYSLgk8lwrAkgjGcE9ulZ8Wv/C3TL6P/iht\r\n");
          stb.append("    QE8mVhSay3+aC3VUd+efbjoK+gLq4trVQ1zPFDnODJIFzxnua8Y8WalY+Nfi94PstDuIb+LTme7n\r\n");
          stb.append("    lhbeqAEMfmH+6oznGSBXJWpuFtVd+SPVwWIjWvHlkoxTfxu2iflbXYs2viDQoLqyubH4b+JLZbZm\r\n");
          stb.append("    S2kg07y9xYOjAqGH998A55JNW9BsrOw8eada2ml3lvplt4cae1triEtJAzTlpOGyd5+XIznt3r1o\r\n");
          stb.append("    LgdK8/DPL8Z9RRAQ8fh+MIc9CZn/AC7flWsqLjZt3u10/rscUMUqqkkraPq3+fq/vfc801vXtW06\r\n");
          stb.append("    6/tG/wBU+IVpp0vySST2UMCq5I24HTpu5x6DvSCDxdBK09sPiJNEkyKomaMMV3ckhic89uhBBPAp\r\n");
          stb.append("    2haHrcFrBH4u8NeLtau4pBPLE18j2juHO07c8kD3PfjmneJ/D2vXSvceHvCeuaXrMt4sizz6yGjz\r\n");
          stb.append("    uyfk34Iyc/h+FcTi5au/4nvudOLVOPL6+7bpb7Sf4fI1PGDw2uvfEy1jLq1zoUN0YwcAsoKkgYx/\r\n");
          stb.append("    Egz+Fee6S9+NKswkMxTyUxiFzxtHtXafEyLf438WmSJVkg8MK26PKliZFGT2bqwGelYOiapHFo1h\r\n");
          stb.append("    GbiNSlvGuDMRjCj3rgzOyaW2rOvLYP6spJczfL/6Sl+hmeE7nWNI+Geo3Wl3d1bwW2rKt81rLtl8\r\n");
          stb.append("    vy9uQeejH0547Cuqt9JvtQOu2E+sajqWpfZkvNBkm1Bo0uoTknCq3zHsQR1HYVz3g7CeHPEdzc+J\r\n");
          stb.append("    ptJh+3yQTww2iz/at644Xru4OD2FWLWXUrJdPFkfHccdvEUtpTYofLDArwOTt46Z6D3raFX3Vdf8\r\n");
          stb.append("    D0NcRB1JzcLKV73s+ye9vlo37rt2LVzpvgKx0+wXX9S8XWt6YVSdZRKMFsZByuMDGOOw716F8OLH\r\n");
          stb.append("    wNB4lKeGtR1GbVooDvhnmmIAwASwYAZ5H0rgLK78SRQ3cOv6h47n+zNKqyWFqNjBOAwLjcc56Y7j\r\n");
          stb.append("    njjofAepeKW8aaZBp7+KLrSADHqB1yBFVABwyMO/Tjn8a6aE1zp2/r7zy8ZRnKlNe06P7Wnf+X5J\r\n");
          stb.append("    X+ZqeNf+Ej1uWSz1/wCHsOraZDchoXt9Qw+ORuHTnHUe+Peu3+Hmm6fZaOXsPDTaAzOVaCVV8xgO\r\n");
          stb.append("    jFgTkH3NdVxijjmvThQUZud7nzlXGupRVFR5Uuzlb7m2jlfif4jn8K+C7/VLJI3vYzGkEbjId2cL\r\n");
          stb.append("    jHfgnp6VyXibxTrGn+Iy1rf+BrGVreEMmpyOl0m5d2xsMMjcWxj1p/iy/g8ceNdI8NaWq3Vlpd2t\r\n");
          stb.append("    9qs4yY4zHkLCSOCxJOR2/A1nfEN477WPHVtdw2U8Vh4dE0LSW8bPFKQ5zvI3A8cc45rnrVHK8ovR\r\n");
          stb.append("    f5M78JQhBQjUjq02/RuKWn3vpuavhzx1qe3UrnxDqXhW4trOxkuhHpF0ZJX2ZYnBY8bRjHrXA+Jb\r\n");
          stb.append("    3xt4wn8Gy3i6RZWWoX0Vxpxt/wB68bAbtzk5JwucgYBPB9s6+03T7L7aIrW0VLnwfHd/6kDY4G1m\r\n");
          stb.append("    VkHJPzZIwOeeK6y8Z08PfB7zZJUlF3CSpbHHl8E++CPzrlVSU01JvT/NHowo06E1UpxV5d1t7rei\r\n");
          stb.append("    9d9yDU/Eurar4W+Iel+JLKzi1vTLVUaS0Q4kjbkfMST746c9K890vw7dT6ZaSrZXLCSFGBEcZByo\r\n");
          stb.append("    Pds11nje6SLxJ8VZdk25rK1t9gAxlyg3H1x+eM1xunazZRafaxvZXTMsSqWE4AJAHOK5McoztGbe\r\n");
          stb.append("    l/zPWyujKNKUqKsm4vbS7gm7G38RPC1/4d8Tx6hBK1poN1dpcl44zIto7AbjIoxhMk9D049K63Qf\r\n");
          stb.append("    EHiQu1qnxK8K3KNEUjkmTMmc53FcAZx2Jx7V7RPFHLpSJKiujwgMrDIYYHX1rntV8L+H51VptD0q\r\n");
          stb.append("    RmUEl7SMk5YZ6j2H5V11aHsG3Fni0Mc8bBQrRTa0u0n080/zOGbXtVkgZV+Kfh+JlBZSlmmBjsct\r\n");
          stb.append("    0xirXhfx5qNpqkcPiPxV4PvLEK3mSQTNHKhwNvGMH/65rwfx7bQWus3EdtBFDH5k3yxoFHAGOB6V\r\n");
          stb.append("    zk6hJLcIAoNujEAYySvJrCOJno/8z3lkFGraMn8S/lirelku59G3Xj3xdLN5FprngRcRl/N+1MQR\r\n");
          stb.append("    uwCQTkHkcdPeqj6hf63cSR+J/ido9hDEMSW2jyBfMUBt2WYDB5xxkceteF6pFGkmr7EVdiqVwMbe\r\n");
          stb.append("    U6enU1WsFV7iUOAwC5AIzj5X/wAKPrUpK7/M2WQU4q0Gk+/Kr/jc+sPCfiT4d+GNKg07Q9Y0m3iw\r\n");
          stb.append("    Cx80b3P952IBJ9zXDa3qmj3XiDx9dx+IdJaPWtJS3tD9pUYkCFSrAjjkZz6Gvn65ZsIMnBcZ59jU\r\n");
          stb.append("    czscgs2MJ396uWJlNJW0IhwzToznU9rJt97d7/oeyeJLfTNQOgy2Pi/S9LjtdGXTbsRzszLjJZVV\r\n");
          stb.append("    B8wO7B5pNWW31FNPjvPifaTfYGD2zLaMrRcKMgr3AUYz715JckidwCQA5xjtVvAOt6PGQDG/k7l7\r\n");
          stb.append("    Nnrkd81zSnfSx3xyl01Fqp8P92N9fNpvqaniPUbs3t5FF4gk1iK9CNcTbXUSMh+UFWGcjAwRng4F\r\n");
          stb.append("    dFZeAfHUtnBJB4dTynjVk8x0VtpHGQWyD7V734T8PaLb67fzQaRp0UsRiMbpbIrISnOCBxXoY6Cu\r\n");
          stb.append("    nCYSGIhzSPAzPPa2BcaGHitrtvr8lyn/2Q==\" /> \r\n");
      }
      datacount++;
      resultJson.put("print" + datacount, stb.toString());
      stb = new StringBuffer();
      stb.append("<g style=\"stroke:rgb(0,0,0);fill:rgb(0,0,0)\">\r\n");
      stb.append("<rect x=\"12004\" y=\"1886\" width=\"3893\" height=\"56\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"8346\" x2=\"17559\" y2=\"8346\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"8346\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"8346\" x2=\"19990\" y2=\"8346\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"8346\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"8346\" x2=\"22420\" y2=\"8346\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"8346\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"8346\" x2=\"24851\" y2=\"8346\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"8346\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"9117\" x2=\"17559\" y2=\"9117\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"9117\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"9117\" x2=\"19990\" y2=\"9117\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"9117\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"9117\" x2=\"22420\" y2=\"9117\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"9117\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"9117\" x2=\"24851\" y2=\"9117\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"9117\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"9889\" x2=\"17559\" y2=\"9889\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"9889\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"9889\" x2=\"19990\" y2=\"9889\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"9889\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"9889\" x2=\"22420\" y2=\"9889\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"9889\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"9889\" x2=\"24851\" y2=\"9889\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"9889\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"10660\" x2=\"17559\" y2=\"10660\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"10660\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"10660\" x2=\"19990\" y2=\"10660\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"10660\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"10660\" x2=\"22420\" y2=\"10660\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"10660\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"10660\" x2=\"24851\" y2=\"10660\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"10660\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"11432\" x2=\"17559\" y2=\"11432\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"11432\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"11432\" x2=\"19990\" y2=\"11432\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"11432\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"11432\" x2=\"22420\" y2=\"11432\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"11432\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"11432\" x2=\"24851\" y2=\"11432\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"11432\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"12203\" x2=\"17559\" y2=\"12203\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"12203\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"12203\" x2=\"19990\" y2=\"12203\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"12203\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"12203\" x2=\"22420\" y2=\"12203\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"12203\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"12203\" x2=\"24851\" y2=\"12203\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"12203\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"12975\" x2=\"17559\" y2=\"12975\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"12975\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"12975\" x2=\"19990\" y2=\"12975\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"12975\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"12975\" x2=\"22420\" y2=\"12975\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"12975\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"12975\" x2=\"24851\" y2=\"12975\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"12975\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"13746\" x2=\"17559\" y2=\"13746\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"13746\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"13746\" x2=\"19990\" y2=\"13746\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"13746\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"13746\" x2=\"22420\" y2=\"13746\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"13746\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"13746\" x2=\"24851\" y2=\"13746\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"13746\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"14518\" x2=\"17559\" y2=\"14518\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"14518\" width=\"15939\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"17596\" y1=\"14518\" x2=\"19990\" y2=\"14518\"/>\r\n");
      stb.append("<rect x=\"17596\" y=\"14518\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"20027\" y1=\"14518\" x2=\"22420\" y2=\"14518\"/>\r\n");
      stb.append("<rect x=\"20027\" y=\"14518\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"22457\" y1=\"14518\" x2=\"24851\" y2=\"14518\"/>\r\n");
      stb.append("<rect x=\"22457\" y=\"14518\" width=\"2398\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"1588\" y1=\"7591\" x2=\"1588\" y2=\"15306\"/>\r\n");
      stb.append("<rect x=\"1588\" y=\"7591\" width=\"42\" height=\"7719\"/>\r\n");
      stb.append("<line x1=\"19990\" y1=\"6820\" x2=\"19990\" y2=\"15306\"/>\r\n");
      stb.append("<rect x=\"19990\" y=\"6820\" width=\"42\" height=\"8490\"/>\r\n");
      stb.append("<line x1=\"893\" y1=\"6786\" x2=\"893\" y2=\"19314\"/>\r\n");
      stb.append("<rect x=\"893\" y=\"6786\" width=\"42\" height=\"12532\"/>\r\n");
      stb.append("<line x1=\"17559\" y1=\"6820\" x2=\"17559\" y2=\"18221\"/>\r\n");
      stb.append("<rect x=\"17559\" y=\"6820\" width=\"42\" height=\"11405\"/>\r\n");
      stb.append("<line x1=\"19990\" y1=\"16035\" x2=\"19990\" y2=\"18221\"/>\r\n");
      stb.append("<rect x=\"19990\" y=\"16035\" width=\"42\" height=\"2190\"/>\r\n");
      stb.append("<line x1=\"22420\" y1=\"6820\" x2=\"22420\" y2=\"16035\"/>\r\n");
      stb.append("<rect x=\"22420\" y=\"6820\" width=\"42\" height=\"9219\"/>\r\n");
      stb.append("<line x1=\"24851\" y1=\"6820\" x2=\"24851\" y2=\"16035\"/>\r\n");
      stb.append("<rect x=\"24851\" y=\"6820\" width=\"42\" height=\"9219\"/>\r\n");
      stb.append("<line x1=\"28670\" y1=\"6820\" x2=\"28670\" y2=\"19314\"/>\r\n");
      stb.append("<rect x=\"28670\" y=\"6820\" width=\"42\" height=\"12498\"/>\r\n");
      stb.append("<rect x=\"1588\" y=\"16035\" width=\"42\" height=\"2190\"/>\r\n");
      stb.append("<rect x=\"4019\" y=\"16035\" width=\"42\" height=\"3283\"/>\r\n");
      stb.append("<line x1=\"912\" y1=\"5243\" x2=\"28693\" y2=\"5243\"/>\r\n");
      stb.append("<rect x=\"912\" y=\"5243\" width=\"27786\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"912\" y1=\"6400\" x2=\"28693\" y2=\"6400\"/>\r\n");
      stb.append("<rect x=\"912\" y=\"6400\" width=\"27786\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"6786\" x2=\"28707\" y2=\"6786\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"6786\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"7557\" x2=\"28707\" y2=\"7557\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"7557\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"8346\" x2=\"28670\" y2=\"8346\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"8346\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"9117\" x2=\"28670\" y2=\"9117\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"9117\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"9889\" x2=\"28670\" y2=\"9889\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"9889\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"10660\" x2=\"28670\" y2=\"10660\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"10660\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"11432\" x2=\"28670\" y2=\"11432\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"11432\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"12203\" x2=\"28670\" y2=\"12203\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"12203\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"12975\" x2=\"28670\" y2=\"12975\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"12975\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"13746\" x2=\"28670\" y2=\"13746\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"13746\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"24888\" y1=\"14518\" x2=\"28670\" y2=\"14518\"/>\r\n");
      stb.append("<rect x=\"24888\" y=\"14518\" width=\"3787\" height=\"8\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"15272\" x2=\"28707\" y2=\"15272\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"15272\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"16001\" x2=\"28707\" y2=\"16001\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"16001\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"16729\" x2=\"28707\" y2=\"16729\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"16729\" width=\"27087\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"1625\" y1=\"17458\" x2=\"28707\" y2=\"17458\"/>\r\n");
      stb.append("<rect x=\"1625\" y=\"17458\" width=\"27087\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"18187\" x2=\"28707\" y2=\"18187\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"18187\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("<line x1=\"931\" y1=\"19279\" x2=\"28707\" y2=\"19279\"/>\r\n");
      stb.append("<rect x=\"931\" y=\"19279\" width=\"27781\" height=\"38\"/>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      if ("ON".equals(inkan_sw) && !"".equals(mitsuyukokigen)) {
        stb.append("<text x=\"19500\" y=\"20021\" text-anchor=\"left\">\r\n");
        stb.append("見積有効期限 " + mitsuyukokigen + "\r\n");
        stb.append("</text>\r\n");
      }
      stb.append("<text x=\"26421\" y=\"20021\" text-anchor=\"middle\">\r\n");
      stb.append("発行日 " + date + "\r\n");
      stb.append("</text>\r\n");
      // 左会社情報
      stb.append("<text x=\"977\" y=\"2087\">\r\n");
      stb.append(add11 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"977\" y=\"2472\">\r\n");
      stb.append(add12 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"977\" y=\"2858\">\r\n");
      stb.append(add13 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"977\" y=\"3243\">\r\n");
      stb.append(add14 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"977\" y=\"3628\">\r\n");
      stb.append(add15 + "\r\n");
      stb.append("</text>\r\n");
      // 右会社情報
      stb.append("<text x=\"20073\" y=\"2087\">\r\n");
      stb.append(add21 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20421\" y=\"2472\">\r\n");
      stb.append(add22 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20421\" y=\"2858\">\r\n");
      stb.append(add23 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20421\" y=\"3244\">\r\n");
      stb.append(add24 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20073\" y=\"3630\">\r\n");
      stb.append(add25 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"977\" y=\"4787\">\r\n");
      stb.append("見積金額合計\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"3300\" y=\"5155\" text-anchor=\"end\">\r\n");
      stb.append(df.format(mitumori_s_gokei) + " 円\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"4102\" y=\"4787\">\r\n");
      stb.append("本 体 金 額\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"6400\" y=\"5155\" text-anchor=\"end\">\r\n");
      stb.append(df.format(mitumori_h_gokei) + " 円\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"7226\" y=\"4787\">\r\n");
      stb.append("消 費 税 額\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"9300\" y=\"5155\" text-anchor=\"end\">\r\n");
      stb.append(df.format(mitumori_zei) + " 円\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"10347\" y=\"4778\">\r\n");
      stb.append("工事件名\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"10347\" y=\"5164\">\r\n");
      stb.append(kenmei + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"20415\" y=\"4778\">\r\n");
      stb.append("改造依頼元得意先\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"20421\" y=\"5173\">\r\n");
      stb.append(tokuisaki + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"972\" y=\"5935\">\r\n");
      stb.append("見積No\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"977\" y=\"6330\">\r\n");
      stb.append(mitumorino + "-" + head.edaban + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"3750\" y=\"5935\">\r\n");
      stb.append("管理番号\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"3754\" y=\"6330\">\r\n");
      stb.append(kanrino + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"6181\" y=\"5935\">\r\n");
      stb.append("見積依頼者\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"6185\" y=\"6330\">\r\n");
      stb.append(mituiraininName + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"10347\" y=\"5935\">\r\n");
      stb.append("見積依頼日\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"10347\" y=\"6321\">\r\n");
      stb.append(mitsuiraiday + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"13819\" y=\"5935\">\r\n");
      stb.append("要求納期\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"13819\" y=\"6321\">\r\n");
      stb.append(mitsuirainou + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"17985\" y=\"5935\">\r\n");
      stb.append("見積回答者\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"17990\" y=\"6330\">\r\n");
      stb.append(mitsukaininName + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"22152\" y=\"5935\">\r\n");
      stb.append("見積回答日\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"22152\" y=\"6321\">\r\n");
      stb.append(mitsukaiday + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"25624\" y=\"5935\">\r\n");
      stb.append("回答納期\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"25624\" y=\"6321\">\r\n");
      stb.append(mitsukainou + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:288;font-weight:400\">\r\n");
      stb.append("<text x=\"4102\" y=\"16308\">\r\n");
      stb.append("〒" + okurisaki1 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"6002\" y=\"16308\">\r\n");
      stb.append(okurisaki2 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"6002\" y=\"16700\">\r\n");
      stb.append(okurisaki3 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20073\" y=\"16308\">\r\n");
      stb.append(untin_seikyusaki + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"4102\" y=\"17037\">\r\n");
      stb.append(kaizohi_seikyusaki + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20073\" y=\"17037\">\r\n");
      stb.append(panelColor + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"4102\" y=\"17765\">\r\n");
      stb.append(settiPlace + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20069\" y=\"17755\">\r\n");
      stb.append(kaizo_center1 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"20069\" y=\"18120\">\r\n");
      stb.append(kaizo_center2 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"4102\" y=\"18494\">\r\n");
      stb.append(kouteinouki_fotter1 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"4102\" y=\"18858\">\r\n");
      stb.append(kouteinouki_fotter2 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"4102\" y=\"19223\">\r\n");
      stb.append(kouteinouki_fotter3 + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:716;font-weight:400\">\r\n");
      stb.append("<text x=\"13800\" y=\"1831\" text-anchor=\"middle\">\r\n");
      stb.append(title + "\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:428;font-weight:400\">\r\n");
      stb.append("<text x=\"26337\" y=\"19684\">\r\n");
      if (isRePrint) {
          stb.append("【再発行】\r\n");
      }
      else {
          stb.append("\r\n");
      }
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      datacount++;
      resultJson.put("print" + datacount, stb.toString());
      stb = new StringBuffer();
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"21143\" y=\"7277\">\r\n");
      stb.append("単　価 \r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"6879\" y=\"7277\">\r\n");
      stb.append("　改　造　仕　様\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"18712\" y=\"7277\">\r\n");
      stb.append("台　数 \r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"23573\" y=\"7277\">\r\n");
      stb.append("金　額 \r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"25916\" y=\"7277\">\r\n");
      stb.append("備　　　考\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"7864\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"8228\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"8593\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"8957\">\r\n");
      stb.append("改\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"9322\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"9686\">\r\n");
      stb.append("造\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"10050\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"10414\">\r\n");
      stb.append("依\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"10779\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"11143\">\r\n");
      stb.append("頼\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"11508\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"11872\">\r\n");
      stb.append("内\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"12236\">\r\n");
      stb.append("　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"12600\">\r\n");
      stb.append("容\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1832\" y=\"17928\">\r\n");
      stb.append("設 置 場 所\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:300;font-weight:400\">\r\n");
      stb.append("<text x=\"17638\" y=\"17914\">\r\n");
      stb.append("　改 造 ｾ ﾝ ﾀｰ\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
      stb.append("<text x=\"19147\" y=\"15742\">\r\n");
      stb.append("　合　計　\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"17643\" y=\"16471\">\r\n");
      stb.append("　運賃請求先\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1832\" y=\"17200\">\r\n");
      stb.append("改造費請求先\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"17643\" y=\"17200\">\r\n");
      stb.append("　パ ネ ル 色\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1247 \" y=\"18930\">\r\n");
      stb.append("工　程　納　期\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1435\" y=\"15742\">\r\n");
      stb.append("頁No. \r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"16651\">\r\n");
      stb.append("特\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"17015\">\r\n");
      stb.append("記\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"17379\">\r\n");
      stb.append("事\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1097 \" y=\"17743\">\r\n");
      stb.append("項\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1832\" y=\"16471\">\r\n");
      stb.append("完了後送り先\r\n");
      stb.append("</text>\r\n");
      stb.append("<text x=\"1671\" y=\"7277\">\r\n");
      stb.append("改造型名・仕様\r\n");
      stb.append("</text>\r\n");
      stb.append("</g>\r\n");
      stb.append("</svg>\r\n");
      stb.append("</page>\r\n");

      //明細データ
      int row =1;
      int page =0;
      int total = printList.size() / line;
      long rowY = 0;
      long pageTotal = 0;
      amari = printList.size() % line;
      if (amari>0) {
        total++;
      }
      for (int i=0;i<printList.size();i++) {
        if (row == 1) {
            page++;
            stb.append("<page>\r\n");
            stb.append("<background id=\"back1\" />\r\n");
            stb.append("<svg viewBox=\"0 0 29700 21000\">\r\n");
            stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:322;font-weight:400\">\r\n");
            stb.append("<text x=\"2735\" y=\"15742\">\r\n");
            stb.append(page + "/" + total + "\r\n");
            stb.append("</text>\r\n");
            stb.append("</g>\r\n");
            stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:337;font-weight:400\">\r\n");
            rowY =8259;
        }
        else {
            rowY = rowY + 771;
        }
        PMitumoriBean bean = (PMitumoriBean)printList.get(i);
        pageTotal = pageTotal + bean.kingaku;
        stb.append("<text x=\"1671\" y=\""+(rowY - 320)+"\">\r\n");
        stb.append(bean.katamei + "\r\n");
        stb.append("</text>\r\n");
        stb.append("<text x=\"7226\" y=\""+rowY+"\">\r\n");
        stb.append(bean.siyou + "\r\n");
        stb.append("</text>\r\n");

        stb.append("<text x=\"19768 \" y=\""+rowY+"\" text-anchor=\"end\">\r\n");
        stb.append(bean.daisuSt + "\r\n");
        stb.append("</text>\r\n");
        stb.append("<text x=\"22319\" y=\""+rowY+"\" text-anchor=\"end\">\r\n");
        stb.append(bean.tankaSt + "\r\n");
        stb.append("</text>\r\n");
        stb.append("<text x=\"24573\" y=\""+rowY+"\" text-anchor=\"end\">\r\n");
        stb.append(bean.kingakuSt + "\r\n");
        stb.append("</text>\r\n");

        if (row == line) {
            stb.append("<text x=\"24573\" y=\"15969\" text-anchor=\"end\">\r\n");
            stb.append(df.format(pageTotal) + "\r\n");
            stb.append("</text>\r\n");
              stb.append("</g>\r\n");
              stb.append("</svg>\r\n");
              stb.append("</page>\r\n");
              pageTotal = 0;
              row = 0;
          }
          row++;
        }
        if (amari > 0) {
            stb.append("<text x=\"24573\" y=\"15969\" text-anchor=\"end\">\r\n");
            stb.append(df.format(pageTotal) + "\r\n");
            stb.append("</text>\r\n");
            stb.append("</g>\r\n");
            stb.append("</svg>\r\n");
            stb.append("</page>\r\n");
        }
        stb.append("</pxd>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        resultJson.put("printtitle", title);
        resultJson.put("printcount", datacount);

  }
  /**
   * 改造依頼書を編集します
   * @param inputParameter
   * @param sessionkeys
   * @param isRePrint
   * @param resultJson
   */
  public static void iraiedit(JsonNode  inputParameter, parameter.SessionKeys sessionkeys, boolean isRePrint, ObjectNode resultJson) {
		// 帳票出力用データリストの生成 (改造依頼書)
	  	ArrayList<PIraiBean> printDataList = new ArrayList<PIraiBean>();
		long keisan;
		long tanka;
		long datacount = 0;


		//**************************************************************************
		// 見積対象製品テーブル
		//**************************************************************************

		// データ抽出用のDaoインスタンス化（見積対象製品テーブル）
		List<MtmTaisyoSeihin> mts     = MtmTaisyoSeihin.GetMTTaisyouSeihin(sessionkeys.mtmno, sessionkeys.edaban);
      	DecimalFormat df = new DecimalFormat("#,##0");
      	MtmTaisyoSeihin oya_data = null;

        for (MtmTaisyoSeihin data : mts) {
    			boolean kaizou = false;
    			//2021-02-02 分納オプション対応
    			//if (MtmTaisyoSeihin.BunoFlag.NOMAL.equals(data.bunno_kubun) && oya_data != null) {
    			if (oya_data != null) {
    				List<MtmOptionKaizo> moks = MtmOptionKaizo.GetMeisai(sessionkeys.mtmno, sessionkeys.edaban, oya_data.taisyoKataban, oya_data.gyoNo);

    				// 取得したデータを帳票用Beanにセット
    		        for (MtmOptionKaizo data2 : moks) {
    		        	PIraiBean bean = new PIraiBean();
        					bean.machineNo = data2.mashinNo;
        					bean.nyukaDay  = data2.nyukaYotei;
        					bean.katamei   = "";
        					bean.siyou     = data2.optionName;
        		      //2021-02-02 分納オプション対応
        					//bean.daisu     = oya_data.gokei_suryo;
                  bean.daisu     = data2.suryo;
        					bean.kibouDay  = "";
        					bean.yoteiDay  = "";
        					bean.hikitoriKibou  = "";
        					bean.tanka     = data2.optionTanka;
                  //2021-02-02 分納オプション対応
        					//bean.kingaku   = data2.optionTanka * oya_data.gokei_suryo;
                  bean.kingaku   = data2.optionTanka * data2.suryo;
                  //2021-02-02 分納オプション対応
        					//bean.daisuSt   = String.valueOf(oya_data.gokei_suryo);
                  bean.daisuSt   = String.valueOf(data2.suryo);
        					bean.tankaSt   = df.format(data2.optionTanka);
                  //2021-02-02 分納オプション対応
        					//keisan         = data2.optionTanka * oya_data.gokei_suryo;
                  keisan         = data2.optionTanka * data2.suryo;
        					bean.kingakuSt = df.format(keisan);
        					printDataList.add(bean);

    				}
    			}
	        //----- 防蝕 -----
	        if (boolKaizo(data.bosyokuKaizo)) {
	            kaizou = true;
	            PIraiBean bean = new PIraiBean();
      				bean.machineNo = data.mashinNo;
      				bean.nyukaDay  = data.nyukaYotei;
	            bean.katamei   = data.taisyoKataban;
	            bean.siyou     = MtmTaisyoSeihin.KaizoSiyo.BOUSYOKU;
	            bean.daisu     = data.suryo;
	            bean.kibouDay  = data.haiseReinyuDay;
	            bean.yoteiDay  = data.seizou_kanryou_day;
	            bean.hikitoriKibou = data.hikitori_kibou;
	            if (data.pboKaiTanka > 0) {
	              tanka = data.pboKaiTanka;
	            }
	            else {
	              tanka = data.boKaiTanka;
	            }
	            bean.tanka = tanka;
	            bean.kingaku = (tanka * data.suryo);
	            bean.daisuSt = String.valueOf(data.suryo);
	            bean.tankaSt = df.format(tanka);
	            keisan = tanka * data.suryo;
	            bean.kingakuSt = df.format(keisan);
	            printDataList.add(bean);
	          }
	        //----- 重防蝕 -----
	        if (boolKaizo(data.jubosyokuKaizo)) {
	            kaizou = true;
	            PIraiBean bean = new PIraiBean();
      				bean.machineNo = data.mashinNo;
      				bean.nyukaDay  = data.nyukaYotei;
	            bean.katamei   = data.taisyoKataban;
	            bean.siyou     = MtmTaisyoSeihin.KaizoSiyo.JUBOUSYOKU;
	            bean.daisu     = data.suryo;
	            bean.kibouDay  = data.haiseReinyuDay;
	            bean.yoteiDay  = data.seizou_kanryou_day;
	            bean.hikitoriKibou = data.hikitori_kibou;
	            if (data.pjuKaiTanka > 0) {
	              tanka = data.pjuKaiTanka;
	            }
	            else {
	              tanka = data.juKaiTanka;
	            }
	            bean.tanka = tanka;
	            bean.kingaku = (tanka * data.suryo);
	            bean.daisuSt = String.valueOf(data.suryo);
	            bean.tankaSt = df.format(tanka);
	            keisan = tanka * data.suryo;
	            bean.kingakuSt = df.format(keisan);
	            printDataList.add(bean);
	          }
	        //----- 耐塩害 -----
	        if (boolKaizo(data.taiengaiKaizo)) {
	            kaizou = true;
	            PIraiBean bean = new PIraiBean();
				bean.machineNo = data.mashinNo;
				bean.nyukaDay  = data.nyukaYotei;
	            bean.katamei   = data.taisyoKataban;
	            bean.siyou     = MtmTaisyoSeihin.KaizoSiyo.TAIENGAI;
	            bean.daisu     = data.suryo;
	            bean.kibouDay  = data.haiseReinyuDay;
	            bean.yoteiDay  = data.seizou_kanryou_day;
	            bean.hikitoriKibou = data.hikitori_kibou;
	            if (data.ptaiKaiTanka > 0) {
	              tanka = data.ptaiKaiTanka;
	            }
	            else {
	              tanka = data.taiKaiTanka;
	            }
	            bean.tanka = tanka;
	            bean.kingaku = (tanka * data.suryo);
	            bean.daisuSt = String.valueOf(data.suryo);
	            bean.tankaSt = df.format(tanka);
	            keisan = tanka * data.suryo;
	            bean.kingakuSt = df.format(keisan);
	            printDataList.add(bean);
	          }
	        //----- 耐重塩害 -----
	        if (boolKaizo(data.taijuengaiKaizo)) {
	            kaizou = true;
	            PIraiBean bean = new PIraiBean();
				bean.machineNo = data.mashinNo;
				bean.nyukaDay  = data.nyukaYotei;
	            bean.katamei   = data.taisyoKataban;
	            bean.siyou     = MtmTaisyoSeihin.KaizoSiyo.TAIJUENGAI;
	            bean.daisu     = data.suryo;
	            bean.kibouDay  = data.haiseReinyuDay;
	            bean.yoteiDay  = data.seizou_kanryou_day;
	            bean.hikitoriKibou = data.hikitori_kibou;
	            if (data.ptaijuKaiTanka > 0) {
	              tanka = data.ptaijuKaiTanka;
	            }
	            else {
	              tanka = data.taijuKaiTanka;
	            }
	            bean.tanka = tanka;
	            bean.kingaku = (tanka * data.suryo);
	            bean.daisuSt = String.valueOf(data.suryo);
	            bean.tankaSt = df.format(tanka);
	            keisan = tanka * data.suryo;
	            bean.kingakuSt = df.format(keisan);
	            printDataList.add(bean);
	          }
	        //----- 重防錆 -----
	        if (boolKaizo(data.jubouseiKaizo)) {
	            kaizou = true;
	            PIraiBean bean = new PIraiBean();
				bean.machineNo = data.mashinNo;
				bean.nyukaDay  = data.nyukaYotei;
	            bean.katamei   = data.taisyoKataban;
	            bean.siyou     = MtmTaisyoSeihin.KaizoSiyo.JUBOSABI;
	            bean.daisu     = data.suryo;
	            bean.kibouDay  = data.haiseReinyuDay;
	            bean.yoteiDay  = data.seizou_kanryou_day;
	            bean.hikitoriKibou = data.hikitori_kibou;
	            if (data.pjubouKaiTanka > 0) {
	              tanka = data.pjubouKaiTanka;
	            }
	            else {
	              tanka = data.jubouKaiTanka;
	            }
	            bean.tanka = tanka;
	            bean.kingaku = (tanka * data.suryo);
	            bean.daisuSt = String.valueOf(data.suryo);
	            bean.tankaSt = df.format(tanka);
	            keisan = tanka * data.suryo;
	            bean.kingakuSt = df.format(keisan);
	            printDataList.add(bean);
	          }
	          //----- オプション部品のみ対応 -----
          if (!kaizou) {
            PIraiBean bean = new PIraiBean();
            bean.machineNo = data.mashinNo;
            bean.nyukaDay  = data.nyukaYotei;
            bean.katamei   = data.taisyoKataban;
            bean.siyou     = "";
            bean.daisu     = data.suryo;
            bean.kibouDay  = data.haiseReinyuDay;
            bean.yoteiDay  = data.seizou_kanryou_day;
            bean.hikitoriKibou = data.hikitori_kibou;
            bean.tanka = 0;
            bean.kingaku = 0;
            bean.daisuSt = String.valueOf(data.suryo);
            bean.tankaSt = "";
            keisan = 0;
            bean.kingakuSt = "";
            printDataList.add(bean);
          }

//    			  if (MtmTaisyoSeihin.BunoFlag.NOMAL.equals(data.bunno_kubun)) {
    			    oya_data = data;
//    			  }
        }

		if (oya_data != null) {
		  Logger.info("{} {}", oya_data.taisyoKataban, oya_data.gyoNo);
			List<MtmOptionKaizo> moks = MtmOptionKaizo.GetMeisai(sessionkeys.mtmno, sessionkeys.edaban, oya_data.taisyoKataban, oya_data.gyoNo);
      Logger.info("OPTION DATA={}", moks.size());

			// 取得したデータを帳票用Beanにセット
	        for (MtmOptionKaizo data2 : moks) {
            PIraiBean bean = new PIraiBean();
            bean.machineNo = data2.mashinNo;
            bean.nyukaDay  = data2.nyukaYotei;
            bean.katamei   = "";
            bean.siyou     = data2.optionName;
            //2021-02-02 分納オプション対応
            //bean.daisu     = oya_data.gokei_suryo;
            bean.daisu     = data2.suryo;
            bean.kibouDay  = "";
            bean.yoteiDay  = "";
            bean.hikitoriKibou  = "";
            bean.tanka     = data2.optionTanka;
            //2021-02-02 分納オプション対応
            //bean.kingaku   = data2.optionTanka * oya_data.gokei_suryo;
            bean.kingaku   = data2.optionTanka * data2.suryo;
            //2021-02-02 分納オプション対応
            //bean.daisuSt   = String.valueOf(oya_data.gokei_suryo);
            bean.daisuSt   = String.valueOf(data2.suryo);
            bean.tankaSt   = df.format(data2.optionTanka);
            //2021-02-02 分納オプション対応
            //keisan         = data2.optionTanka * oya_data.gokei_suryo;
            keisan         = data2.optionTanka * data2.suryo;
            bean.kingakuSt = df.format(keisan);
            printDataList.add(bean);

			}
		}
        //**************************************************************************
        // 特殊改造テーブル
        //**************************************************************************
        List<MtmTokusyuKaizo> mtk     = MtmTokusyuKaizo.GetMTTokusyu(sessionkeys.mtmno, sessionkeys.edaban);

        for (MtmTokusyuKaizo data : mtk) {
          PIraiBean bean = new PIraiBean();
		  bean.machineNo = data.mashinNo;
		  bean.nyukaDay  = data.nyukaYotei;
          bean.katamei   = data.tokusyuKataban;
          bean.siyou     = data.tokusyuNaiyo;
          bean.daisu     = data.suryo;
          bean.kibouDay  = data.haiseReinyuDay;
          bean.yoteiDay  = data.seizou_kanryou_day;
          bean.hikitoriKibou = data.hikitori_kibou;
          tanka          = 0;
          bean.tanka     = 0;
          bean.kingaku   = 0;
          bean.daisuSt   = "";
          bean.tankaSt   = "";
          bean.kingakuSt = "";
          if (data.ptokusyuKingaku > 0) {
              tanka = data.ptokusyuKingaku;
            }
            else {
              tanka = data.tokusyuKingaku;
          }
          bean.tanka = tanka;
          bean.kingaku = (tanka * data.suryo);
          bean.daisuSt = String.valueOf(data.suryo);
          bean.tankaSt = df.format(tanka);
          keisan = tanka * data.suryo;
          bean.kingakuSt = df.format(keisan);
          printDataList.add(bean);
        }


		//**************************************************************************
		// 余白の作成
		//**************************************************************************

        // 10の余りが0以外の場合余白の作成
        int line = 10;
        int amari = (mts.size() + mtk.size() ) % line;

        //**************************************************************************
        // ヘッダープロパティセット用の会社名、支店名、部門名を作成する。
        //**************************************************************************

        String kaisyaName = "";
        String shitenName = "";
        String bumonName = "";
        String mituiraininName = "";
        String mitsukaininName = "";
        String kennaigai = "";

        MtmHead head = MtmHead.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findUnique();
        if (head != null) { //見積ヘッダが取得出来た場合
          ObjectNode userJson  = Json.newObject();  //ユーザ情報JSON
          //----- 見積依頼者情報 -----
          MstUser.getUserToJson(head.sakuseiUid, userJson);
          kaisyaName = userJson.get("kaisyaName").asText();
          shitenName = userJson.get("shitenName").asText();
          bumonName  = userJson.get("bumonName").asText();
          kaisyaName = userJson.get("kaisyaName").asText();
          mituiraininName = userJson.get("shimeiKanji").asText();
          kennaigai = userJson.get("kennaigai").asText();

          //----- プロパティよりLE系の会社コード・支店コードを取得 -----
          String sLeKcode = MstProperty.GetValue(MstProperty.Code.LE_KCODE);
          String sMrScode = MstProperty.GetValue(MstProperty.Code.MR_SCODE);
          String sMrKname = MstProperty.GetValue(MstProperty.Code.MR_KNAME);

          if (head.mitumoriIraiKaisya.equals(sLeKcode)
              && head.mitumoriIraiSiten.equals(sMrScode)) { //株式会社メルの場合
            kaisyaName = sMrKname;
            shitenName = "";
            bumonName  = "";
          }
          //----- 見積回答者情報 -----
          ObjectNode kaitoJson  = Json.newObject();  //ユーザ情報JSON
          MstUser.getUserToJson(head.kaitouUid, kaitoJson);
          mitsukaininName = kaitoJson.get("shimeiKanji").asText();

        }

		//**************************************************************************
		// ヘッダープロパティ
		//**************************************************************************
		Calendar cal = Calendar.getInstance();
		// 2015.11.24 改造依頼書改善
		// 改造依頼日（見積依頼日）
		String mitsuiraiday = warekiDateFormat(head.mitumoriIraibi);
		// 発行日
		String date = warekiDateFormat(systemDateFormat());
		// 宛先１１（空白）
		String add11 = "";
		// 宛先１２（自社名）
		String add12 = MstProperty.GetValue(MstProperty.Code.MY_NAME) + "　様";
		// 宛先１３（空白）
		String add13 = "";
		// 宛先２１（空白）
		String add21 = "";
		// 宛先２２（会社名称)
		String add22 = kaisyaName;
		// 宛先２３（支店名称＋部門マスタ＋部門略称）
		String add23 = shitenName + "　" + bumonName;
		// 改造承認No.
		String kaizo = head.kaizoSyoninNo;
    // 購買管理No.
    String kobai = "";
    if (MstShiten.Kennaigai.KENNAI.equals(kennaigai)) { //LE系である場合、購買管理No.
      kobai = head.koubaiKanriNo;
    }
    else {                                              //代理店の場合、管理番号
      kobai = head.kanriNo;
    }
		// 発令元コード.
		String hatureiCode = head.hatsureimotoCode;
		// 得意先コード
		String tokuisakiCode = head.tokuisakiCode;
		// 工事件名
		String kenmei = head.kojiKenmei;
		// 得意先名称No.
		String tokuisaki = head.tokuisakiName;
		// 見積No
		String mitumorino = "";
		// 見積No
		if (head.edaban == 0) {
			mitumorino = head.mitumoriNo;
		}
		else{
			mitumorino = head.mitumoriNo + "-" + head.edaban;
		}
		// 支店名
		String niniShitenName = head.sitenName;
		// 担当者名
		String niniTantouName = head.tantouName;

		//**************************************************************************
		// フッタープロパティ
		//**************************************************************************
		// 完了後送り先（郵便）
		String okurisaki1 = editPostcode(head.nohinsakiYubin);
		// 完了後送り先（住所1）
		String okurisaki2 = head.nohinsakiAdd1;
		// 完了後送り先（住所2）
		String okurisaki3 = head.nohinsakiAdd2;
		// 運賃請求先
		String untin_seikyusaki = head.untinSeikyu;
		// 改造費請求先
		String kaizohi_seikyusaki = head.kaizouhiSeikyu;
		// パネル色
		String panelColor = head.paneruColor;
		// 設置場所
		String settiPlace = head.settiBasyo;
		// 改造センター (ﾌﾟﾛﾊﾟﾃｨﾌｧｲﾙより、電話番号)
		String kaizo_center1 = "TEL:" + MstProperty.GetValue(MstProperty.Code.MY_TEL);
		// 改造センター (ﾌﾟﾛﾊﾟﾃｨﾌｧｲﾙより、FAX番号)
		String kaizo_center2 = "FAX:" + MstProperty.GetValue(MstProperty.Code.MY_FAX);
		// 備考1（フッター）
		String biko_fotter1 = head.bikou_1;
		// 備考2（フッター）
		String biko_fotter2 = head.bikou_2;
		// 備考3（フッター）
		String biko_fotter3 = head.bikou_3;
		// 工程納期1（フッター）
		String koutei_fotter1 = head.kouteiNouki1;
		// 工程納期2（フッター）
		String koutei_fotter2 = head.kouteiNouki2;
		// 工程納期3（フッター）
		String koutei_fotter3 = head.kouteiNouki3;


		//***********************************************************************
		// XPD 対応 START
		//***********************************************************************

	    StringBuffer stb = new StringBuffer();

	    //共通部分の定義
	    stb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
	    stb.append("<pxd paper-type=\"a4\" orientation=\"landscape\" name=\"pxdDocument\" delete=\"yes\" save=\"no\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\r\n");
    	stb.append("<page type=\"hidden\" id=\"back1\">\r\n");
    	stb.append("<svg viewBox=\"0 0 29700 21000\">\r\n");
    	stb.append("<g style=\"stroke:rgb(0,0,0);fill:rgb(0,0,0)\">\r\n");
    	stb.append("<rect x=\"12812\" y=\"2355\" width=\"3870\" height=\"55\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"7591\" x2=\"4118\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"7591\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"7591\" x2=\"6548\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"7591\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"7591\" x2=\"17658\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"7591\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"7591\" x2=\"20089\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"7591\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"7591\" x2=\"22519\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"7591\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"7591\" x2=\"24950\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"7591\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"8352\" x2=\"4118\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"8352\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"8352\" x2=\"6548\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"8352\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"8352\" x2=\"17658\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"8352\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"8352\" x2=\"20089\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"8352\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"8352\" x2=\"22519\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"8352\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"8352\" x2=\"24950\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"8352\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"9113\" x2=\"4118\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"9113\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"9113\" x2=\"6548\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"9113\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"9113\" x2=\"17658\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"9113\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"9113\" x2=\"20089\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"9113\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"9113\" x2=\"22519\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"9113\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"9113\" x2=\"24950\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"9113\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"9873\" x2=\"4118\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"9873\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"9873\" x2=\"6548\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"9873\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"9873\" x2=\"17658\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"9873\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"9873\" x2=\"20089\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"9873\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"9873\" x2=\"22519\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"9873\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"9873\" x2=\"24950\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"9873\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"10634\" x2=\"4118\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"10634\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"10634\" x2=\"6548\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"10634\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"10634\" x2=\"17658\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"10634\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"10634\" x2=\"20089\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"10634\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"10634\" x2=\"22519\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"10634\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"10634\" x2=\"24950\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"10634\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"11395\" x2=\"4118\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"11395\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"11395\" x2=\"6548\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"11395\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"11395\" x2=\"17658\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"11395\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"11395\" x2=\"20089\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"11395\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"11395\" x2=\"22519\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"11395\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"11395\" x2=\"24950\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"11395\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"12155\" x2=\"4118\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"12155\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"12155\" x2=\"6548\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"12155\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"12155\" x2=\"17658\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"12155\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"12155\" x2=\"20089\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"12155\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"12155\" x2=\"22519\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"12155\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"12155\" x2=\"24950\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"12155\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"12916\" x2=\"4118\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"12916\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"12916\" x2=\"6548\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"12916\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"12916\" x2=\"17658\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"12916\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"12916\" x2=\"20089\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"12916\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"12916\" x2=\"22519\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"12916\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"12916\" x2=\"24950\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"12916\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"13676\" x2=\"4118\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"13676\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"4154\" y1=\"13676\" x2=\"6548\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"4154\" y=\"13676\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6585\" y1=\"13676\" x2=\"17658\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"6585\" y=\"13676\" width=\"11079\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"17696\" y1=\"13676\" x2=\"20089\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"17696\" y=\"13676\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"20127\" y1=\"13676\" x2=\"22519\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"20127\" y=\"13676\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"22556\" y1=\"13676\" x2=\"24950\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"22556\" y=\"13676\" width=\"2398\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1687\" y1=\"6848\" x2=\"1687\" y2=\"14454\"/>\r\n");
    	stb.append("<rect x=\"1687\" y=\"6848\" width=\"42\" height=\"7610\"/>\r\n");
    	stb.append("<line x1=\"4118\" y1=\"6087\" x2=\"4118\" y2=\"14454\"/>\r\n");
    	stb.append("<rect x=\"4118\" y=\"6087\" width=\"42\" height=\"8371\"/>\r\n");
    	//台数左罫線
    	stb.append("<rect x=\"16277\" y=\"6087\" width=\"42\" height=\"8371\"/>\r\n");
    	//単価左罫線
    	stb.append("<line x1=\"17658\" y1=\"5326\" x2=\"17658\" y2=\"15980\"/>\r\n");
    	stb.append("<rect x=\"17658\" y=\"5326\" width=\"42\" height=\"9893\"/>\r\n");
    	//金額左罫線
    	stb.append("<line x1=\"20089\" y1=\"6087\" x2=\"20089\" y2=\"15980\"/>\r\n");
    	stb.append("<rect x=\"20089\" y=\"6087\" width=\"42\" height=\"9893\"/>\r\n");
    	stb.append("<line x1=\"993\" y1=\"5293\" x2=\"993\" y2=\"18405\"/>\r\n");
    	stb.append("<rect x=\"993\" y=\"5293\" width=\"42\" height=\"14263\"/>\r\n");
    	stb.append("<rect x=\"993\" y=\"3763\" width=\"13583\" height=\"42\"/>\r\n");
    	stb.append("<rect x=\"3424\" y=\"3763\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"993\" y=\"3763\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"8979\" y=\"3763\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"11409\" y=\"3763\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"14534\" y=\"3763\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"993\" y=\"4528\" width=\"13583\" height=\"42\"/>\r\n");
    	stb.append("<rect x=\"993\" y=\"4528\" width=\"42\" height=\"13881\"/>\r\n");
    	stb.append("<line x1=\"3424\" y1=\"5326\" x2=\"3424\" y2=\"6087\"/>\r\n");
    	stb.append("<rect x=\"3424\" y=\"4561\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"3424\" y=\"5326\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<line x1=\"8979\" y1=\"5326\" x2=\"8979\" y2=\"6087\"/>\r\n");
    	stb.append("<rect x=\"8979\" y=\"4561\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"8979\" y=\"5326\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<line x1=\"11409\" y1=\"5326\" x2=\"11409\" y2=\"6087\"/>\r\n");
    	stb.append("<rect x=\"11409\" y=\"4561\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"11409\" y=\"5326\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<line x1=\"14534\" y1=\"5326\" x2=\"14534\" y2=\"6087\"/>\r\n");
    	stb.append("<rect x=\"14534\" y=\"4561\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<rect x=\"14534\" y=\"5326\" width=\"42\" height=\"765\"/>\r\n");
    	stb.append("<line x1=\"17658\" y1=\"15172\" x2=\"17658\" y2=\"18367\"/>\r\n");
    	stb.append("<rect x=\"17658\" y=\"15172\" width=\"42\" height=\"3239\"/>\r\n");

    	stb.append("<line x1=\"28770\" y1=\"5326\" x2=\"28770\" y2=\"18405\"/>\r\n");
    	stb.append("<rect x=\"28770\" y=\"5326\" width=\"42\" height=\"14230\"/>\r\n");
    	stb.append("<line x1=\"4118\" y1=\"15172\" x2=\"4118\" y2=\"18367\"/>\r\n");
    	////
    	stb.append("<rect x=\"4118\" y=\"15172\" width=\"42\" height=\"4346\"/>\r\n");

    	stb.append("<line x1=\"6548\" y1=\"6087\" x2=\"6548\" y2=\"14454\"/>\r\n");
    	stb.append("<rect x=\"6548\" y=\"6087\" width=\"42\" height=\"8371\"/>\r\n");
    	//備考右罫線
    	stb.append("<line x1=\"20089\" y1=\"15172\" x2=\"20089\" y2=\"18367\"/>\r\n");
    	stb.append("<rect x=\"20089\" y=\"15172\" width=\"42\" height=\"3239\"/>\r\n");
    	stb.append("<line x1=\"22519\" y1=\"6087\" x2=\"22519\" y2=\"15115\"/>\r\n");
    	stb.append("<rect x=\"22519\" y=\"6087\" width=\"42\" height=\"9028\"/>\r\n");
    	stb.append("<line x1=\"24600\" y1=\"6087\" x2=\"24600\" y2=\"14454\"/>\r\n");
    	stb.append("<rect x=\"24600\" y=\"6087\" width=\"42\" height=\"8371\"/>\r\n");
    	stb.append("<line x1=\"1687\" y1=\"15172\" x2=\"1687\" y2=\"17355\"/>\r\n");
    	stb.append("<rect x=\"1687\" y=\"15172\" width=\"42\" height=\"2187\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"5293\" x2=\"28806\" y2=\"5293\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"5293\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"14571\" y1=\"5673\" x2=\"28806\" y2=\"5673\"/>\r\n");
    	stb.append("<rect x=\"8979\" y=\"5673\" width=\"19832\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"6053\" x2=\"28806\" y2=\"6053\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"6053\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"6814\" x2=\"28806\" y2=\"6814\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"6814\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"7591\" x2=\"28770\" y2=\"7591\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"7591\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"8352\" x2=\"28770\" y2=\"8352\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"8352\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"9113\" x2=\"28770\" y2=\"9113\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"9113\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"9873\" x2=\"28770\" y2=\"9873\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"9873\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"10634\" x2=\"28770\" y2=\"10634\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"10634\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"11395\" x2=\"28770\" y2=\"11395\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"11395\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"12155\" x2=\"28770\" y2=\"12155\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"12155\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"12916\" x2=\"28770\" y2=\"12916\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"12916\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"24987\" y1=\"13676\" x2=\"28770\" y2=\"13676\"/>\r\n");
    	stb.append("<rect x=\"24987\" y=\"13676\" width=\"3787\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"14420\" x2=\"28806\" y2=\"14420\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"14420\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"15138\" x2=\"28806\" y2=\"15138\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"15138\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"15857\" x2=\"28806\" y2=\"15857\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"15857\" width=\"27087\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1725\" y1=\"16575\" x2=\"28806\" y2=\"16575\"/>\r\n");
    	stb.append("<rect x=\"1725\" y=\"16575\" width=\"27087\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"17293\" x2=\"28806\" y2=\"17293\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"17293\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"1030\" y1=\"18371\" x2=\"28806\" y2=\"18371\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"18371\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<rect x=\"1030\" y=\"19518\" width=\"27781\" height=\"38\"/>\r\n");
    	stb.append("<line x1=\"26601\" y1=\"6087\" x2=\"26601\" y2=\"14454\"/>\r\n");
    	stb.append("<rect x=\"26601\" y=\"6087\" width=\"42\" height=\"8371\"/>\r\n");
    	stb.append("</g>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        stb = new StringBuffer();
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:388;font-weight:400\">\r\n");
    	stb.append("<g style=\"stroke:none;fill:rgb(0,0,0)\">\r\n");
    	stb.append("<text x=\"28682\" y=\"1863\" text-anchor=\"end\">\r\n");
    	stb.append("改造依頼日 " + mitsuiraiday);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"26421\" y=\"20387\" text-anchor=\"middle\">\r\n");
    	stb.append("発行日 " + date);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1086\" y=\"3034\">\r\n");
    	stb.append(add11);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20529\" y=\"3034\">\r\n");
    	stb.append(add21);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1086\" y=\"3393\">\r\n");
    	stb.append(add12);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20529\" y=\"3393\">\r\n");
    	stb.append(add22);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1086\" y=\"3752\">\r\n");
    	stb.append(add13);
    	stb.append("</text>\r\n");
    	if (MstShiten.Kennaigai.KENGAI.equals(kennaigai)) {	//購買決済会社以外
        	stb.append("<text x=\"20529\" y=\"3752\">\r\n");
        	stb.append(add23);
        	stb.append("</text>\r\n");
    	}
    	stb.append("</g>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"17743\" y=\"5607\">\r\n");
    	stb.append(kenmei);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"17743\" y=\"5987\">\r\n");
    	stb.append(tokuisaki);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"6631\" y=\"6368\">\r\n");
    	stb.append("　改　造　型　名\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"6631\" y=\"6748\">\r\n");
    	stb.append("　改　造　仕　様\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"15445\">\r\n");
    	stb.append("〒" + okurisaki1);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"6101\" y=\"15445\">\r\n");
    	stb.append(okurisaki2);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"6101\" y=\"15837\">\r\n");
    	stb.append(okurisaki3);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20173\" y=\"15445\">\r\n");
    	stb.append(untin_seikyusaki);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"16163\">\r\n");
    	stb.append(kaizohi_seikyusaki);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20173\" y=\"16163\">\r\n");
    	stb.append(panelColor);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"16882\">\r\n");
    	stb.append(settiPlace);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:283;font-weight:400\">\r\n");
    	stb.append("<text x=\"20168\" y=\"17029\">\r\n");
    	stb.append("※取引基本契約通りとする\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:162;font-weight:225\">\r\n");
    	stb.append("<text x=\"20168\" y=\"17690\">\r\n");
    	stb.append(biko_fotter1);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20168\" y=\"17960\">\r\n");
    	stb.append(biko_fotter2);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20168\" y=\"18228\">\r\n");
    	stb.append(biko_fotter3);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:240;font-weight:300\">\r\n");
    	stb.append("<text x=\"4201\" y=\"17600\">\r\n");
    	stb.append(koutei_fotter1);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"17960\">\r\n");
    	stb.append(koutei_fotter2);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"18318\">\r\n");
    	stb.append(koutei_fotter3);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:422;font-weight:400\">\r\n");
    	stb.append("<text x=\"26436\" y=\"18728\">\r\n");
    	if (isRePrint) {
        	stb.append("【再発行】\r\n");
    	}
    	else {
        	stb.append("\r\n");
    	}
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ Ｐゴシック embedded;font-size:183;font-weight:200\">\r\n");
    	stb.append("<text x=\"1072\" y=\"19928\">\r\n");
    	stb.append("※パッケージエアコンの室外機については、耐塩害・耐重塩害・重防錆（温泉対策）の中からお選び下さい。\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1072\" y=\"20187\">\r\n");
    	stb.append("※設置場所については、離島・海岸線・温泉地・都市名・科学工場・ガス雰囲気・（　　　　　　　　　ガス）などご記入下さい。\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1072\" y=\"20446\">\r\n");
    	stb.append("※尚、食品加工関連工場においては、廃液処理施設が必ずあります。発生ガスによる腐食やガス漏れ事故が多発してますので、重防錆処理されることをお勧めします。\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ Ｐゴシック embedded;font-size:706;font-weight:400\">\r\n");
    	stb.append("<text x=\"12862\" y=\"2309\">\r\n");
    	stb.append("改造依頼書\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"1196 \" y=\"15783\">\r\n");
    	stb.append("特\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"16142\">\r\n");
    	stb.append("記\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"16501\">\r\n");
    	stb.append("事\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"16861\">\r\n");
    	stb.append("項\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1931\" y=\"15606\">\r\n");
    	stb.append("完了後送り先\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"17843\" y=\"14887\">\r\n");
    	stb.append(" 合 計 金 額\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"17743\" y=\"15606\">\r\n");
    	stb.append("　運賃請求先\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1931\" y=\"16324\">\r\n");
    	stb.append("改造費請求先\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"17743\" y=\"16324\">\r\n");
    	stb.append("　パ ネ ル 色\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1931\" y=\"17042\">\r\n");
    	stb.append("設 置 場 所\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1431\" y=\"17978\">\r\n");
    	stb.append("工　程　納　期\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1431\" y=\"19125\">\r\n");
    	stb.append("変　更　事　項\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:295;font-weight:400\">\r\n");
    	stb.append("<text x=\"17737\" y=\"17029\">\r\n");
    	stb.append("　支 払 期 日\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"17737\" y=\"17978\">\r\n");
    	stb.append("　備　　　 考\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"1534\" y=\"14887\">\r\n");
    	stb.append("頁No.\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"7120\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"7480\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"7839\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"8198\">\r\n");
    	stb.append("改\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"8557\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"8916\">\r\n");
    	stb.append("造\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"9275\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"9635\">\r\n");
    	stb.append("依\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"9994\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"10353\">\r\n");
    	stb.append("頼\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"10712\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"11071\">\r\n");
    	stb.append("内\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"11430\">\r\n");
    	stb.append("　\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1196 \" y=\"11790\">\r\n");
    	stb.append("容\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"16477\" y=\"6541\">\r\n");
    	stb.append("台　数 \r\n");
    	stb.append("</text>\r\n");
		// 2015.11.24 改造依頼書改善
    	stb.append("<text x=\"18481\" y=\"6541\">\r\n");
    	stb.append("単　価 \r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"20867\" y=\"6541\">\r\n");
    	stb.append("金　額 \r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"23051\" y=\"6368\">\r\n");
    	stb.append("配ｾ戻入\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"23126\" y=\"6748\">\r\n");
    	stb.append("希望日\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:295;font-weight:400\">\r\n");
    	stb.append("<text x=\"24951\" y=\"6368\">\r\n");
    	stb.append("改造完成\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"25116\" y=\"6748\">\r\n");
    	stb.append("予定日\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"27211\" y=\"6368\">\r\n");
    	stb.append("引　取\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"27211\" y=\"6748\">\r\n");
    	stb.append("希望日\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:283;font-weight:400\">\r\n");
    	stb.append("<text x=\"15030\" y=\"5581\">\r\n");
    	stb.append("工　事　件　名\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"14872\" y=\"5961\">\r\n");
    	stb.append("改造依頼元得意先\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"1770\" y=\"6541\">\r\n");
    	stb.append("ﾏｼﾝNO.\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"4201\" y=\"6541\">\r\n");
    	stb.append("入荷予定日\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1275\" y=\"4276\">\r\n");
    	stb.append("見積No\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"3530\" y=\"4335\">\r\n");
    	stb.append(mitumorino);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1275\" y=\"5041\">\r\n");
    	stb.append("改造承認NO.\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"1275\" y=\"5806\">\r\n");
    	stb.append("発令元コード\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:494;font-weight:400\">\r\n");
    	stb.append("<text x=\"3530\" y=\"5110\">\r\n");
    	stb.append(kaizo);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"3530\" y=\"5816\">\r\n");
    	stb.append(hatureiCode);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	stb.append("<text x=\"9261\" y=\"4266\">\r\n");
      if (MstShiten.Kennaigai.KENNAI.equals(kennaigai)) { //LE系である場合、購買管理No.
        stb.append("購買管理NO.\r\n");
      }
      else {                                              //代理店の場合、管理番号
        stb.append("管理番号.\r\n");
      }
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"9261\" y=\"5031\">\r\n");
    	stb.append("得意先コード\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"9261\" y=\"5582\">\r\n");
    	stb.append("支 店 名\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"9261\" y=\"5962\">\r\n");
    	stb.append("担当者名\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"11492\" y=\"4276\">\r\n");
    	stb.append(kobai);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"11492\" y=\"5041\">\r\n");
    	stb.append(tokuisakiCode);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"11492\" y=\"5607\">\r\n");
    	stb.append(niniShitenName);
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"11492\" y=\"5987\">\r\n");
    	stb.append(niniTantouName);
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("</svg>\r\n");
    	stb.append("</page>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        stb = new StringBuffer();

    	//明細データ
    	int row =1;
    	int page =0;
    	int total = printDataList.size() / line;
    	long rowY = 0;
    	long rowY2 = 0;
		// 2015.11.24 改造依頼書改善
    	long pageTotal = 0;
		amari = printDataList.size() % line;
		if (amari>0) {
			total++;
		}
    	for (int i=0;i<printDataList.size();i++) {
    		if (row == 1) {
    	    	page++;
    	    	stb.append("<page>\r\n");
    	    	stb.append("<background id=\"back1\" />\r\n");
    	    	stb.append("<svg viewBox=\"0 0 29700 21000\">\r\n");
    	    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	    	stb.append("<text x=\"2834\" y=\"14887\">\r\n");
    	    	stb.append(page + "/" + total);
    	    	stb.append("</text>\r\n");
    	    	stb.append("</g>\r\n");
    	    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:317;font-weight:400\">\r\n");
    	    	rowY =7128;
    	    	rowY2 =7509;
    		}
    		else {
    	    	rowY = rowY + 761;
    	    	rowY2 = rowY2 + 760;
    		}
    		PIraiBean bean = (PIraiBean)printDataList.get(i);
    		pageTotal = pageTotal + bean.kingaku;

    		stb.append("<text x=\"1770\" y=\""+rowY+"\">\r\n");
        	stb.append(bean.machineNo + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"5303\" y=\""+rowY2+"\" text-anchor=\"middle\">\r\n");
        	stb.append(dateStringFormat(bean.nyukaDay) + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"6631\" y=\""+rowY+"\">\r\n");
        	stb.append(bean.katamei + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"6631\" y=\""+rowY2+"\">\r\n");
        	stb.append(bean.siyou + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"17288 \" y=\""+rowY2+"\" text-anchor=\"end\">\r\n");
        	stb.append(bean.daisuSt + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"19950 \" y=\""+rowY2+"\" text-anchor=\"end\">\r\n");
        	stb.append(bean.tankaSt + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"22354 \" y=\""+rowY2+"\" text-anchor=\"end\">\r\n");
        	stb.append(bean.kingakuSt + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"23554\" y=\""+rowY2+"\" text-anchor=\"middle\">\r\n");
        	stb.append(dateStringFormat(bean.kibouDay) + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"25674\" y=\""+rowY2+"\" text-anchor=\"middle\">\r\n");
        	stb.append(dateStringFormat(bean.yoteiDay) + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"27674\" y=\""+rowY2+"\" text-anchor=\"middle\">\r\n");
        	stb.append(dateStringFormat(bean.hikitoriKibou) + "\r\n");
        	stb.append("</text>\r\n");

			if (row == line) {
	    		// 2015.11.24 改造依頼書改善
		    	stb.append("<text x=\"22354\" y=\"14887\" text-anchor=\"end\">\r\n");
		    	stb.append(df.format(pageTotal));
		    	stb.append("</text>\r\n");
    	    	pageTotal = 0;
    	    	stb.append("</g>\r\n");
    	    	stb.append("</svg>\r\n");
    	    	stb.append("</page>\r\n");
    	    	row = 0;
    	        datacount++;
    	        resultJson.put("print" + datacount, stb.toString());
    	        stb = new StringBuffer();
    		}
    		row++;
    	}
    	if (amari > 0) {
    		// 2015.11.24 改造依頼書改善
	    	stb.append("<text x=\"22354\" y=\"14887\" text-anchor=\"end\">\r\n");
	    	stb.append(df.format(pageTotal));
	    	stb.append("</text>\r\n");
        	stb.append("</g>\r\n");
        	stb.append("</svg>\r\n");
        	stb.append("</page>\r\n");
    	}
    	stb.append("</pxd>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        resultJson.put("printtitle", TyohyouRireki.PrintTitle.KZOU);
        resultJson.put("printcount", datacount);
	}

  public static void sekouedit(JsonNode  inputParameter, parameter.SessionKeys sessionkeys, boolean isRePrint, ObjectNode resultJson) {
		// 帳票出力用データリストの生成 (施工証明書)
	  	ArrayList<PSekouBean> printDataList = new ArrayList<PSekouBean>();
		long keisan;
		long tanka;
		long datacount = 0;

		//**************************************************************************
		// 施工証明書
		//**************************************************************************
		List<MsiKaizoKanryo> mkks = MsiKaizoKanryo.GetKaizoKanryo(sessionkeys.mtmno, sessionkeys.edaban);

		for (MsiKaizoKanryo data : mkks) {
			PSekouBean bean = new PSekouBean();
			bean.kaizouName = data.taisyoKataban;
			bean.sekouNaiyo = data.taisyoNaiyo;
			bean.seizouNo   = data.seizoNo;

			printDataList.add(bean);
		}

		//**************************************************************************
		// 余白の作成
		//**************************************************************************

        // 10の余りが0以外の場合余白の作成
        int line = 20;
        int amari = mkks.size() % line;

		if (amari != 0){
			// 「以下余白」の作成
			PSekouBean bean = new PSekouBean();
			bean.kaizouName = "";
			bean.sekouNaiyo = "";
			bean.seizouNo   = "　　　　―　以下余白　―";
			printDataList.add(bean);
		}

		// 改造ｾﾝﾀ住所１
		String add1 = MstProperty.GetValue(MstProperty.Code.SEKOUADDR1);
		// 改造ｾﾝﾀ住所２
		String add2 = MstProperty.GetValue(MstProperty.Code.SEKOUADDR2);
		// 改造ｾﾝﾀ住所３
		String add3 = MstProperty.GetValue(MstProperty.Code.SEKOUADDR3);
		// 改造ｾﾝﾀ住所４
		String add4 = MstProperty.GetValue(MstProperty.Code.SEKOUADDR4);
		// 改造ｾﾝﾀ名
		String center = MstProperty.GetValue(MstProperty.Code.SEKOUNAME);
		// 改造施工管理士名
		String name = MstProperty.GetValue(MstProperty.Code.SEKOUKANRI);

		// 月日
		String date = warekiDateFormat(systemDateFormat());

		// 得意先名
		String tokuisaki = "";
		// 工事件名
		String kenmei = "";

        MtmHead head = MtmHead.find.where().eq("mitumori_no", sessionkeys.mtmno).eq("edaban", sessionkeys.edaban).findUnique();
        if (head != null) { //見積ヘッダが取得出来た場合

      //得意先名ではなく施工証明書宛名に変更する
			//tokuisaki = head.tokuisakiName;
      tokuisaki = head.sekoAtena;
			kenmei    = head.kojiKenmei;

        }

		//***********************************************************************
		// XPD 対応 START
		//***********************************************************************

        StringBuffer stb = new StringBuffer();

    	//共通部分の定義
	    stb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    	stb.append("<pxd paper-type=\"a4\" orientation=\"portrait\" name=\"pxdDocument\" delete=\"yes\" save=\"no\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\r\n");
    	stb.append("<page type=\"hidden\" id=\"back1\">\r\n");
    	stb.append("<svg viewBox=\"0 0 21000 29700\">\r\n");
    	stb.append("<image x=\"17000\" y=\"3500\" width=\"2380\" opacity=\"0.4\" xlink:href=\"data:;base64,\r\n");
    	stb.append("    /9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcU\r\n");
    	stb.append("    FhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgo\r\n");
    	stb.append("    KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCABaAFoDASIA\r\n");
    	stb.append("    AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA\r\n");
    	stb.append("    AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3\r\n");
    	stb.append("    ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm\r\n");
    	stb.append("    p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA\r\n");
    	stb.append("    AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx\r\n");
    	stb.append("    BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK\r\n");
    	stb.append("    U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3\r\n");
    	stb.append("    uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDZ8Y6d\r\n");
    	stb.append("    HrcOu3+qtfW+ladqkllZ2Oi2aF5Cg+ZmPB6nqew6Vxcfg3wzLCGuLLxvHOYcxK2mIVlJ5wpBOPXk\r\n");
    	stb.append("    jp1zXp3wy1fxdL4DS+0XQ7bVZ59SuZWN1diNkB9M4PUsOeePeutbU/igkaSDw74eYMDmJb5w6fUn\r\n");
    	stb.append("    j16ZrzFDnSlrr5Nn1DxFTDN0o8tou3xRW3lbr3Z4hN4FsIJtl3oHjCZpTIFjEUZZMEbJePl4BwFP\r\n");
    	stb.append("    vzxz0vg74ZeF9a1Oa1h0/wAY6cY0w017FHChwRmPcMnOD/8AX4rvNJ8aeKvEM2r6HY2Om6Z4o0ph\r\n");
    	stb.append("    563Baa3kUk42suCpI29c9c1ZRfiswlE3/CL/ADKfL2Syr5bdiflORx096lUY6WTa9P8AglVMfiXB\r\n");
    	stb.append("    xc1CX+Lp3WnVeZyupfDXwn4c1Kz03Vta155Nfc2qxpL8s20qVV8L0UkYz3Nakv7PvhViPLu9UjXA\r\n");
    	stb.append("    UjzVORnJGdvFc54q0/4gp4h8OS67qmlrc/bvJsri1R2xI2DtdODsIUbsZ967iay+LGX8rVvC+Ff5\r\n");
    	stb.append("    c28g3r6nrjHp+tOmqUrrkenr/mY1auJgozjiVdp318+mhwg+H+jP8QpvCUmo+JIpWsjci7a9VluT\r\n");
    	stb.append("    8vGzbxtwD15K9OKm1jwPp1p4rfw3J4h8UGabTTqEspnBSUI2MH34zjoMCnyxeLLf4zQxo+kS+IJt\r\n");
    	stb.append("    PZDdGFxbiLOQ23qHGCvXB49a1finBf6fPoepHV4E8ULYz27W8GnyTLeggEqoHKDPAJIxnr1qXTgo\r\n");
    	stb.append("    yly2s/667mzr14yhCFT44+uuuuz0uv6R5xbWEeoweFb0634iW/16eRbWSSYE27p+73k5ywJOMrj5\r\n");
    	stb.append("    T+FVblZoND02/wBG8SeImiur17Hynco0UwAIyiuc5LZ4Pfpk1a+E1jDrmo6NLcasr3mjNJJY6PMG\r\n");
    	stb.append("    XzWVdwCyElR8yjOBxjOOay/H1l9g15lt7e2064W78x7a11gT7ZCAcqoA8sg5yx6cY4rlkk4cy0X9\r\n");
    	stb.append("    fI9ylOf1n6tKd2tdlbd90m9LarS979S1quteLPBni68gt9a1W6fTmQSvdl2hdXwVyrMflJOM8H0r\r\n");
    	stb.append("    6H0b4g6Xf6PY3k00cEtxBHK8XJ2FlBK574zivErzSFtvhR4nubnTzDKWtXW8fVEvvN2yjC5X7mM9\r\n");
    	stb.append("    z/FXmtvZXLW8TCdwCoIAmI7emaf1mWGS97Rmby6hnEbzspQdm1ZX0Xa63vY+mP2cgIvhvlydwvbg\r\n");
    	stb.append("    uzN1O4c/pWzefFjwVagF9ftXB7RhnbOfQDNZf7PkzTfD9mmijhJvrjMaLgL8w4x25zxUvxFv/Cvh\r\n");
    	stb.append("    m2uHh0fSrzxLcgNbWUdsjzzSHIDFQMkDkk98HvXpqUo0Yyi0j5arTp1cdUhUi22+jX6rbzMz4L3r\r\n");
    	stb.append("    eJPE3izxP9jmht7uVILaXIEbxJnAwOSw4JY564HQ1676V4z4d+IfjEadaRN8Obv5fkZoA1unsQjr\r\n");
    	stb.append("    8oz154rqvhZ45u/G1vqL3mjSaY1pIqAly6yZ3ZAJUcqVwfrTw9WCShe7fkTmOErc8q3KlFWVlJOy\r\n");
    	stb.append("    2WzNrxfp2iTJYat4hkWGHR5/tccry7ERsbQW9Rz0rnNV+MngvT3kiTUnvZkO3ZZwNJk+zcKfwNN1\r\n");
    	stb.append("    jVvH0lxcQJ4M0q605pPLVZb5WMi5+82eAMDpg4JHWtfwPaXwaabWPC2k6JOoHlNZSJIzZzuBwo24\r\n");
    	stb.append("    wO5zn2queTnaKt8mZRo04U+av71tkpR/LV/cc78OLHVtc8bar411a1uNMingFlaWNxGFk8oEHc3G\r\n");
    	stb.append("    RyDj13ewzBroupPjbejLtZR+GX8xQfl+Z2wD6ZI/Suw+JviCfwv4J1LVrMRG6hVFiEoypZnCjI79\r\n");
    	stb.append("    a5H4g+If7C1XT72K+8M6fqFxZbJW1C3mklZc5Cr5fRNxPXv+NZVOWMXBvVam+HdWvU54R0knFJX0\r\n");
    	stb.append("    sl89n95y/h6KKHTPhBcRW4V1M4aQE9PLJIx3JwD+FZFjHBpvgDw/rKWcA1rWddWK5uDEu8o0rFky\r\n");
    	stb.append("    wJA+Xtj0q5a+OpLGzsraz13wHBHaMRbqtndkRg9QOPl6nkHvT7rxzeWMUGnySeBr+O5nLQzWZkMV\r\n");
    	stb.append("    lMDvaaRG3EjJzkY5zz1rjTi3e/8AWn+R7DhiYvlUN3frteT6pfzJ28u9iDxXosejar8SNK02B4dJ\r\n");
    	stb.append("    m0yDUBHF91ZPMB79M/Mceg46V5DDJN5KbbiUDaMAQZx+PevbrnWJ9b8G+N7651fw9qTf2eiyNpkM\r\n");
    	stb.append("    iSAgnbv3clQC2Pqa8x0/Q7GawtpHikLvErHATGSB71hiYRunbR/5ns5LXdOnNVd00vuiu9j1j4Ra\r\n");
    	stb.append("    HP4k+ET2enaxe6O7ajM5uLfO9l4+U5OcYPY9QOTzXofg34faJ4VuGvLZZ7vVHUrJf3chkmYE9M9B\r\n");
    	stb.append("    0HQdq81+FHiabwx8NraPT9D1bVpLq9uRbJBHkLgrhXIztzz0BHBroID8S/F+xJxB4S09kHmMv7y4\r\n");
    	stb.append("    cHrtB+6ceuMEV6VJwUYtJuVv68kfLY2Fd1asXNQpuT3aV/l8T222Nz4ieOItKA0LQCt74pvsQ21s\r\n");
    	stb.append("    hz5RbI8xz0AXBOD6enNbPw68Lp4R8K2mmeZ504zLcSjP7yVjlm55x2HsKj8FeB9I8JWzm0R57+b5\r\n");
    	stb.append("    ri9nO6aZs5yT9ew/WtDw/wCJtK8QS6hFpV1572Ext7gbGUK4z0JGCODyPSt4J83NU36I8ytKPs/Z\r\n");
    	stb.append("    4dNxVm21u9l6Lsv6Wz2o45ryTV/Gfja31y4s003w3awm6MFvNdaovzKfuEqGzkjBxtzz0qWa0vNf\r\n");
    	stb.append("    8t/FnjnTLewGN9jpE6wpIP4leVm3EE8cY4/Q+sJ6RV2CwLSUpyST101f3L9bDPGGqW3xC8TWHhHR\r\n");
    	stb.append("    ZludOt51u9WuYvmRVjOViznBJYY7/oa2/iJ4l07Q7+zin1nRbGcRO5hvbcyuVIO1lK8qAw9OcH0q\r\n");
    	stb.append("    TSdQ8H+DfL0fQookaaCW8WO0UyeYsaktl+cnAOAT246VieL/ABNHrPh/QtV02+0bSNN1FDJJfavE\r\n");
    	stb.append("    ski4+7EsX8TZ3Z5wMH1rGbtF3fvM6abU6kIRi1BXSvu2767PXTZJ7dzjNP8AG88+qy2X/CX+HJYZ\r\n");
    	stb.append("    VjkW9XR5HfzGbbjbwu7opJPQjvXRW1vbeMo1sbDxNoTa7aulzbXNnpqiRY0G1g6N0+ZjwD0I4rKh\r\n");
    	stb.append("    8TrFaqbHxzeXFxG+1lt/DgDSEDIUjbkKT9P61Uj8UPdWqPL8RNY0q6EZkuILnRE81cYB5VcE7jnj\r\n");
    	stb.append("    Py/QmuOFk+V6p/1/MepKnNv2lOPK1to9/T2a37bG/dW1/pWieNNB1vVbG9K6ULtJobeO3kXduDB1\r\n");
    	stb.append("    XjGVGCeua8W02TRjp1r50NwZfKTeVdwCcDOK9i0/TNLufh/4w13/AISO71+6vLF4Zrh4jEY1UFlT\r\n");
    	stb.append("    y+oPI6+vHBryDS5WGmWgErgeSmAAuPuisMZTnPlUV/w136nr5SlUjUvq7q+lteVX0svyPdv2bIni\r\n");
    	stb.append("    8ASrKGVvt02Q4IYEYBDZ716szBQSxAAHevE/hDrGpwfC+5u/DumNq15JqkwEMk6xlVbHzMT1xxnu\r\n");
    	stb.append("    c1fl8F+M/Gd+snjbVotP0kKobTtLdsS9M7j7n1z7V6dCs1TjFRu/66nzGOw0auLq1KlRRjd+vyit\r\n");
    	stb.append("    fvsvMn8b+PrrVr8eF/h6yXuszbkuLpMGOzQHDMW6ZH4+2TgV1vgrwzZ+BPB/2KAPcGNWnuHSMs88\r\n");
    	stb.append("    mPmIUZJJAACj0ArQ8L+GdF8K2P2XQ7KK1iPLNks78/xOxJPXueK2gQehB/Gt4Qd+eb1/I4a+Jhye\r\n");
    	stb.append("    woJqG7vvJ93+i6eZ4PrXiH4Z6tq5utT8JanJdE/vpxYSLgk8lwrAkgjGcE9ulZ8Wv/C3TL6P/iht\r\n");
    	stb.append("    QE8mVhSay3+aC3VUd+efbjoK+gLq4trVQ1zPFDnODJIFzxnua8Y8WalY+Nfi94PstDuIb+LTme7n\r\n");
    	stb.append("    lhbeqAEMfmH+6oznGSBXJWpuFtVd+SPVwWIjWvHlkoxTfxu2iflbXYs2viDQoLqyubH4b+JLZbZm\r\n");
    	stb.append("    S2kg07y9xYOjAqGH998A55JNW9BsrOw8eada2ml3lvplt4cae1triEtJAzTlpOGyd5+XIznt3r1o\r\n");
    	stb.append("    LgdK8/DPL8Z9RRAQ8fh+MIc9CZn/AC7flWsqLjZt3u10/rscUMUqqkkraPq3+fq/vfc801vXtW06\r\n");
    	stb.append("    6/tG/wBU+IVpp0vySST2UMCq5I24HTpu5x6DvSCDxdBK09sPiJNEkyKomaMMV3ckhic89uhBBPAp\r\n");
    	stb.append("    2haHrcFrBH4u8NeLtau4pBPLE18j2juHO07c8kD3PfjmneJ/D2vXSvceHvCeuaXrMt4sizz6yGjz\r\n");
    	stb.append("    uyfk34Iyc/h+FcTi5au/4nvudOLVOPL6+7bpb7Sf4fI1PGDw2uvfEy1jLq1zoUN0YwcAsoKkgYx/\r\n");
    	stb.append("    Egz+Fee6S9+NKswkMxTyUxiFzxtHtXafEyLf438WmSJVkg8MK26PKliZFGT2bqwGelYOiapHFo1h\r\n");
    	stb.append("    GbiNSlvGuDMRjCj3rgzOyaW2rOvLYP6spJczfL/6Sl+hmeE7nWNI+Geo3Wl3d1bwW2rKt81rLtl8\r\n");
    	stb.append("    vy9uQeejH0547Cuqt9JvtQOu2E+sajqWpfZkvNBkm1Bo0uoTknCq3zHsQR1HYVz3g7CeHPEdzc+J\r\n");
    	stb.append("    ptJh+3yQTww2iz/at644Xru4OD2FWLWXUrJdPFkfHccdvEUtpTYofLDArwOTt46Z6D3raFX3Vdf8\r\n");
    	stb.append("    D0NcRB1JzcLKV73s+ye9vlo37rt2LVzpvgKx0+wXX9S8XWt6YVSdZRKMFsZByuMDGOOw716F8OLH\r\n");
    	stb.append("    wNB4lKeGtR1GbVooDvhnmmIAwASwYAZ5H0rgLK78SRQ3cOv6h47n+zNKqyWFqNjBOAwLjcc56Y7j\r\n");
    	stb.append("    njjofAepeKW8aaZBp7+KLrSADHqB1yBFVABwyMO/Tjn8a6aE1zp2/r7zy8ZRnKlNe06P7Wnf+X5J\r\n");
    	stb.append("    X+ZqeNf+Ej1uWSz1/wCHsOraZDchoXt9Qw+ORuHTnHUe+Peu3+Hmm6fZaOXsPDTaAzOVaCVV8xgO\r\n");
    	stb.append("    jFgTkH3NdVxijjmvThQUZud7nzlXGupRVFR5Uuzlb7m2jlfif4jn8K+C7/VLJI3vYzGkEbjId2cL\r\n");
    	stb.append("    jHfgnp6VyXibxTrGn+Iy1rf+BrGVreEMmpyOl0m5d2xsMMjcWxj1p/iy/g8ceNdI8NaWq3Vlpd2t\r\n");
    	stb.append("    9qs4yY4zHkLCSOCxJOR2/A1nfEN477WPHVtdw2U8Vh4dE0LSW8bPFKQ5zvI3A8cc45rnrVHK8ovR\r\n");
    	stb.append("    f5M78JQhBQjUjq02/RuKWn3vpuavhzx1qe3UrnxDqXhW4trOxkuhHpF0ZJX2ZYnBY8bRjHrXA+Jb\r\n");
    	stb.append("    3xt4wn8Gy3i6RZWWoX0Vxpxt/wB68bAbtzk5JwucgYBPB9s6+03T7L7aIrW0VLnwfHd/6kDY4G1m\r\n");
    	stb.append("    VkHJPzZIwOeeK6y8Z08PfB7zZJUlF3CSpbHHl8E++CPzrlVSU01JvT/NHowo06E1UpxV5d1t7rei\r\n");
    	stb.append("    9d9yDU/Eurar4W+Iel+JLKzi1vTLVUaS0Q4kjbkfMST746c9K890vw7dT6ZaSrZXLCSFGBEcZByo\r\n");
    	stb.append("    Pds11nje6SLxJ8VZdk25rK1t9gAxlyg3H1x+eM1xunazZRafaxvZXTMsSqWE4AJAHOK5McoztGbe\r\n");
    	stb.append("    l/zPWyujKNKUqKsm4vbS7gm7G38RPC1/4d8Tx6hBK1poN1dpcl44zIto7AbjIoxhMk9D049K63Qf\r\n");
    	stb.append("    EHiQu1qnxK8K3KNEUjkmTMmc53FcAZx2Jx7V7RPFHLpSJKiujwgMrDIYYHX1rntV8L+H51VptD0q\r\n");
    	stb.append("    RmUEl7SMk5YZ6j2H5V11aHsG3Fni0Mc8bBQrRTa0u0n080/zOGbXtVkgZV+Kfh+JlBZSlmmBjsct\r\n");
    	stb.append("    0xirXhfx5qNpqkcPiPxV4PvLEK3mSQTNHKhwNvGMH/65rwfx7bQWus3EdtBFDH5k3yxoFHAGOB6V\r\n");
    	stb.append("    zk6hJLcIAoNujEAYySvJrCOJno/8z3lkFGraMn8S/lirelku59G3Xj3xdLN5FprngRcRl/N+1MQR\r\n");
    	stb.append("    uwCQTkHkcdPeqj6hf63cSR+J/ido9hDEMSW2jyBfMUBt2WYDB5xxkceteF6pFGkmr7EVdiqVwMbe\r\n");
    	stb.append("    U6enU1WsFV7iUOAwC5AIzj5X/wAKPrUpK7/M2WQU4q0Gk+/Kr/jc+sPCfiT4d+GNKg07Q9Y0m3iw\r\n");
    	stb.append("    Cx80b3P952IBJ9zXDa3qmj3XiDx9dx+IdJaPWtJS3tD9pUYkCFSrAjjkZz6Gvn65ZsIMnBcZ59jU\r\n");
    	stb.append("    czscgs2MJ396uWJlNJW0IhwzToznU9rJt97d7/oeyeJLfTNQOgy2Pi/S9LjtdGXTbsRzszLjJZVV\r\n");
    	stb.append("    B8wO7B5pNWW31FNPjvPifaTfYGD2zLaMrRcKMgr3AUYz715JckidwCQA5xjtVvAOt6PGQDG/k7l7\r\n");
    	stb.append("    Nnrkd81zSnfSx3xyl01Fqp8P92N9fNpvqaniPUbs3t5FF4gk1iK9CNcTbXUSMh+UFWGcjAwRng4F\r\n");
    	stb.append("    dFZeAfHUtnBJB4dTynjVk8x0VtpHGQWyD7V734T8PaLb67fzQaRp0UsRiMbpbIrISnOCBxXoY6Cu\r\n");
    	stb.append("    nCYSGIhzSPAzPPa2BcaGHitrtvr8lyn/2Q==\" /> \r\n");
    	stb.append("<g style=\"stroke:rgb(0,0,0);fill:rgb(0,0,0)\">\r\n");
    	stb.append("<rect x=\"1027\" y=\"2775\" width=\"11200\" height=\"22\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"11224\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"11224\" x2=\"10419\" y2=\"11224\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"11224\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"11987\" x2=\"6164\" y2=\"11987\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"11987\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"11987\" x2=\"10419\" y2=\"11987\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"11987\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"12751\" x2=\"6164\" y2=\"12751\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"12751\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"12751\" x2=\"10419\" y2=\"12751\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"12751\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"13514\" x2=\"6164\" y2=\"13514\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"13514\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"13514\" x2=\"10419\" y2=\"13514\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"13514\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"14278\" x2=\"6164\" y2=\"14278\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"14278\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"14278\" x2=\"10419\" y2=\"14278\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"14278\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"15042\" x2=\"6164\" y2=\"15042\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"15042\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"15042\" x2=\"10419\" y2=\"15042\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"15042\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"15806\" x2=\"6164\" y2=\"15806\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"15806\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"15806\" x2=\"10419\" y2=\"15806\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"15806\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"16569\" x2=\"6164\" y2=\"16569\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"16569\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"16569\" x2=\"10419\" y2=\"16569\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"16569\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"17332\" x2=\"6164\" y2=\"17332\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"17332\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"17332\" x2=\"10419\" y2=\"17332\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"17332\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"18096\" x2=\"6164\" y2=\"18096\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"18096\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"18096\" x2=\"10419\" y2=\"18096\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"18096\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"18958\" x2=\"6164\" y2=\"18958\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"18958\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"18958\" x2=\"10419\" y2=\"18958\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"18958\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"19722\" x2=\"6164\" y2=\"19722\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"19722\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"19722\" x2=\"10419\" y2=\"19722\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"19722\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"20486\" x2=\"6164\" y2=\"20486\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"20486\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"20486\" x2=\"10419\" y2=\"20486\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"20486\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"21248\" x2=\"6164\" y2=\"21248\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"21248\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"21248\" x2=\"10419\" y2=\"21248\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"21248\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"22012\" x2=\"6164\" y2=\"22012\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"22012\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"22012\" x2=\"10419\" y2=\"22012\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"22012\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"22776\" x2=\"6164\" y2=\"22776\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"22776\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"22776\" x2=\"10419\" y2=\"22776\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"22776\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"23539\" x2=\"6164\" y2=\"23539\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"23539\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"23539\" x2=\"10419\" y2=\"23539\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"23539\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"24303\" x2=\"6164\" y2=\"24303\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"24303\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"24303\" x2=\"10419\" y2=\"24303\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"24303\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"25067\" x2=\"6164\" y2=\"25067\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"25067\" width=\"5206\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"6198\" y1=\"25067\" x2=\"10419\" y2=\"25067\"/>\r\n");
    	stb.append("<rect x=\"6198\" y=\"25067\" width=\"4225\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"927\" y1=\"9297\" x2=\"927\" y2=\"25847\"/>\r\n");
    	stb.append("<rect x=\"927\" y=\"9297\" width=\"39\" height=\"16555\"/>\r\n");
    	stb.append("<line x1=\"6164\" y1=\"9333\" x2=\"6164\" y2=\"25847\"/>\r\n");
    	stb.append("<rect x=\"6164\" y=\"9333\" width=\"39\" height=\"16520\"/>\r\n");
    	stb.append("<line x1=\"10419\" y1=\"9333\" x2=\"10419\" y2=\"25847\"/>\r\n");
    	stb.append("<rect x=\"10419\" y=\"9333\" width=\"39\" height=\"16520\"/>\r\n");
    	stb.append("<line x1=\"19912\" y1=\"9333\" x2=\"19912\" y2=\"25847\"/>\r\n");
    	stb.append("<rect x=\"19912\" y=\"9333\" width=\"39\" height=\"16520\"/>\r\n");
    	stb.append("<line x1=\"943\" y1=\"8358\" x2=\"19934\" y2=\"8358\"/>\r\n");
    	stb.append("<rect x=\"943\" y=\"8358\" width=\"18995\" height=\"40\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"9297\" x2=\"19946\" y2=\"9297\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"9297\" width=\"18990\" height=\"40\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"10443\" x2=\"19946\" y2=\"10443\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"10443\" width=\"18990\" height=\"40\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"11224\" x2=\"19912\" y2=\"11224\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"11224\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"11987\" x2=\"19912\" y2=\"11987\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"11987\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"12751\" x2=\"19912\" y2=\"12751\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"12751\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"13514\" x2=\"19912\" y2=\"13514\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"13514\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"14278\" x2=\"19912\" y2=\"14278\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"14278\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"15042\" x2=\"19912\" y2=\"15042\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"15042\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"15806\" x2=\"19912\" y2=\"15806\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"15806\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"16569\" x2=\"19912\" y2=\"16569\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"16569\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"17332\" x2=\"19912\" y2=\"17332\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"17332\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"18096\" x2=\"19912\" y2=\"18096\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"18096\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"18958\" x2=\"19912\" y2=\"18958\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"18958\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"19722\" x2=\"19912\" y2=\"19722\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"19722\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"20486\" x2=\"19912\" y2=\"20486\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"20486\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"21248\" x2=\"19912\" y2=\"21248\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"21248\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"22012\" x2=\"19912\" y2=\"22012\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"22012\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"22776\" x2=\"19912\" y2=\"22776\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"22776\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"23539\" x2=\"19912\" y2=\"23539\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"23539\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"24303\" x2=\"19912\" y2=\"24303\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"24303\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"10454\" y1=\"25067\" x2=\"19912\" y2=\"25067\"/>\r\n");
    	stb.append("<rect x=\"10454\" y=\"25067\" width=\"9462\" height=\"8\"/>\r\n");
    	stb.append("<line x1=\"961\" y1=\"25812\" x2=\"19946\" y2=\"25812\"/>\r\n");
    	stb.append("<rect x=\"961\" y=\"25812\" width=\"18990\" height=\"40\"/>\r\n");
    	stb.append("</g>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        stb = new StringBuffer();
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:413;font-weight:400\">\r\n");
    	stb.append("<g style=\"stroke:none;fill:rgb(0,0,0)\">\r\n");
    	stb.append("<text x=\"16266\" y=\"1677\">\r\n");
    	stb.append(date + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ Ｐゴシック embedded;font-size:525;font-weight:400\">\r\n");
    	stb.append("<g style=\"stroke:none;fill:rgb(0,0,0)\">\r\n");
    	stb.append("<text x=\"1027\" y=\"2722\">\r\n");
    	stb.append(tokuisaki + " 殿\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:337;font-weight:400\">\r\n");
    	stb.append("<g style=\"stroke:none;fill:rgb(0,0,0)\">\r\n");
    	stb.append("<text x=\"13444\" y=\"2891\">\r\n");
    	stb.append(add1 + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"13771\" y=\"3336\">\r\n");
    	stb.append(add2 + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"13771\" y=\"3794\">\r\n");
    	stb.append(add3 + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"13771\" y=\"4252\">\r\n");
    	stb.append(add4 + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"13771\" y=\"5169\">\r\n");
    	stb.append(center + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"13771\" y=\"5626\">\r\n");
    	stb.append(name + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:822;font-weight:400\">\r\n");
    	stb.append("<text x=\"7150\" y=\"6401\">\r\n");
    	stb.append("施 工 証 明 書\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:525;font-weight:400\">\r\n");
    	stb.append("<text x=\"1027\" y=\"8274\">\r\n");
    	stb.append("工事件名：\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ Ｐゴシック embedded;font-size:525;font-weight:400\">\r\n");
    	stb.append("<text x=\"4300\" y=\"8274\">\r\n");
    	stb.append(kenmei + "\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:449;font-weight:400\">\r\n");
    	stb.append("<text x=\"16731\" y=\"9249\">\r\n");
    	stb.append("頁No.\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:449;font-weight:400\">\r\n");
    	stb.append("<text x=\"1018\" y=\"10044\">\r\n");
    	stb.append("　改造型名・仕様\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"6255\" y=\"10044\">\r\n");
    	stb.append("　製造番号\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"10511\" y=\"10044\">\r\n");
    	stb.append("　施工内容\r\n");
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:449;font-weight:400\">\r\n");
    	stb.append("<text x=\"1672\" y=\"27008\">\r\n");
    	stb.append("上記に対し当工場に於いて施工を完了したことを証明致します。\r\n");
    	stb.append("</text>\r\n");
    	stb.append("<text x=\"18040\" y=\"27008\">\r\n");
    	if (isRePrint) {
        	stb.append("【再発行】\r\n");
    	}
    	else {
        	stb.append("\r\n");
    	}
    	stb.append("</text>\r\n");
    	stb.append("</g>\r\n");
    	stb.append("</svg>\r\n");
    	stb.append("</page>\r\n");

    	//明細データ
    	int row =1;
    	int page =0;
    	int total = printDataList.size() / line;
    	long rowY = 0;
		amari = printDataList.size() % line;
		if (amari>0) {
			total++;
		}
    	for (int i=0;i<printDataList.size();i++) {
    		if (row == 1) {
    	    	page++;
    	    	stb.append("<page>\r\n");
    	    	stb.append("<background id=\"back1\" /> \r\n");
    	    	stb.append("<svg viewBox=\"0 0 21000 29700\">\r\n");
    	    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:449;font-weight:400\">\r\n");
    	    	stb.append("<text x=\"19731\" y=\"9249\" text-anchor=\"end\">\r\n");
    	    	stb.append(page + "/" + total + "\r\n");
    	    	stb.append("</text>\r\n");
    	    	stb.append("</g>\r\n");
    	    	stb.append("<g style=\"font-family:ＭＳ ゴシック embedded;font-size:337;font-weight:400\">\r\n");
    	    	rowY =11160;
    		}
    		else {
    	    	rowY = rowY + 764;
    		}
    		PSekouBean bean = (PSekouBean)printDataList.get(i);
			if (bean.kaizouName.length() > 30) {
	        	stb.append("<text x=\"1005\" y=\""+ (rowY - 350)+ "\">\r\n");
	        	stb.append(bean.kaizouName.substring(0,30) + "\r\n");
	        	stb.append("</text>\r\n");
	        	stb.append("<text x=\"1005\" y=\""+ rowY+ "\">\r\n");
	        	stb.append(bean.kaizouName.substring(30) + "\r\n");
	        	stb.append("</text>\r\n");
			}
			else {
	        	stb.append("<text x=\"1005\" y=\""+ rowY+ "\">\r\n");
	        	stb.append(bean.kaizouName + "\r\n");
	        	stb.append("</text>\r\n");
			}

        	stb.append("<text x=\"6243\" y=\""+ rowY+ "\">\r\n");
        	stb.append(bean.seizouNo + "\r\n");
        	stb.append("</text>\r\n");
        	stb.append("<text x=\"10497\" y=\""+ rowY+ "\">\r\n");
        	stb.append(bean.sekouNaiyo + "\r\n");
        	stb.append("</text>\r\n");
    		if (row == line) {
    	    	stb.append("</g>\r\n");
    	    	stb.append("</svg>\r\n");
    	    	stb.append("</page>\r\n");
    	    	row = 0;
    	        datacount++;
    	        resultJson.put("print" + datacount, stb.toString());
    	        stb = new StringBuffer();
    		}
    		row++;
    	}
    	if (amari > 0) {
        	stb.append("</g>\r\n");
        	stb.append("</svg>\r\n");
        	stb.append("</page>\r\n");
    	}
    	stb.append("</pxd>\r\n");
        datacount++;
        resultJson.put("print" + datacount, stb.toString());
        stb = new StringBuffer();
        resultJson.put("printtitle", TyohyouRireki.PrintTitle.SEKO);
        resultJson.put("printcount", datacount);
	}
}
