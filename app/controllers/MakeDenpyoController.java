package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import models.MsiKaizoKanryo;
import models.MstKaisya;
import models.MstShiten;
import models.MstTax;
import models.MstUser;
import models.MtmHead;
import models.MtmOptionKaizo;
import models.MtmTaisyoSeihin;
import models.MtmTokusyuKaizo;
import parameter.MtmRetrievalMatch;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 売上伝票ファイル作成コントローラー
 * @author kimura
 *
 */
public class MakeDenpyoController extends BaseController {

	//改造商品コード
	private static final String customCode = "99990";
	//オプションコード
	private static final String optionCode = "99991";
	//特殊改造コード
	private static final String specialCode = "88888";
	//値引コード
	private static final String nebikiCode = "77777";
	//値引名
	private static final String nebikiName = "値引";
	//消費税名
	private static final String syouhiName = "消費税";

	/**
	 * 売上伝票ファイル作成画面遷移
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
    public static Result move() {
        return ok(views.html.makedenpyo.render());
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
     * 依頼会社一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getiraikaisyalist() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

        //------------------------------------------------------------------------------------
        //- 会社マスタの全件取得
        //------------------------------------------------------------------------------------
        List<MstKaisya> kaisyas = MstKaisya.GetKaisyaAllList();

        getSessionUser();

        //リスト先頭
        ObjectNode dataJson   = Json.newObject();
        dataJson.put("value", "0");		//コード
        dataJson.put("name", "");			//名称
        listJson.put("0", dataJson);

        for (MstKaisya data : kaisyas) {

            if (!MstUser.Kengen.ST.equals(user.kengen)) { //ユーザ権限がサンデンテクノ以外の場合

            	//自社以外のデータはスキップ
            	if (!data.kaisyaCd.equals(user.kaisyaCd)) {
            		continue;
            	}

            }

            int code = Integer.parseInt(data.kaisyaCd);		//頭の0を除外する為、数値に一旦変換
            dataJson   = Json.newObject();
            dataJson.put("value", String.valueOf(code));	//コード
            dataJson.put("name", data.kaisyaName);			//名称

            listJson.put(String.valueOf(code), dataJson);
            Logger.debug("[USER MNT] USER={} KAISYA INFO VALUE={}.", user.userId, data.kaisyaCd);

        }

        resultJson.put("datalist", listJson);
        resultJson.put("result"  , "success");

        return ok(resultJson);
    }
    /**
     * 依頼支店一覧を取得します
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result getiraishitenlist(String kaisyacd) {

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
     * 売上伝票検索
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result denpyoretrieval() {

        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson   = Json.newObject();

    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        String sMtmnof = inputParameter.get("mtmnof").asText();
        String sMtmnot = inputParameter.get("mtmnot").asText();
        String sRequef = inputParameter.get("requef").asText();
        String sRequet = inputParameter.get("requet").asText();
        String sKenmei = inputParameter.get("kenmei").asText();
        String sKaisya = inputParameter.get("kaisya").asText();
        String sShiten = inputParameter.get("shiten").asText();

        Logger.info(">>> DENPYO RETRIEVAL START. ");
        Logger.info("[mtmnof] MATCH={}. ", sMtmnof);
        Logger.info("[mtmnot] MATCH={}. ", sMtmnot);
        Logger.info("[requef] MATCH={}. ", sRequef);
        Logger.info("[requet] MATCH={}. ", sRequet);
        Logger.info("[kenmei] MATCH={}. ", sKenmei);
        Logger.info("[kaisya] MATCH={}. ", sKaisya);
        Logger.info("[shiten] MATCH={}. ", sShiten);
        Logger.info(">>> DENPYO RETRIEVAL END. ");

    	//------------------------------------------------------------------------------------
    	//- 条件に一致する売上伝票データを取得する
    	//------------------------------------------------------------------------------------
        MtmRetrievalMatch mrm = new MtmRetrievalMatch();
        mrm.mtmnof = sMtmnof;
        mrm.mtmnot = sMtmnot;
        mrm.requef = sRequef;
        mrm.requet = sRequet;
        mrm.kenmei = sKenmei;
        mrm.kaisya = sKaisya;
        mrm.shiten = sShiten;

        getUserInfo();

        if (MstUser.Kengen.EIGYO.equals(user.kengen)) { //ログインユーザが営業担当者の場合
          //自支店のみ参照可能とする
          mrm.kaisya = user.kaisyaCd;
          mrm.shiten = user.shitenCd;
          mrm.bumon  = user.bumonCd;
        }

        List<MtmHead> dataList = MtmHead.GetDenpyoRetrieval(mrm);

        if (dataList.size() > 0) {
          for (MtmHead data : dataList) { //一致した見積件名分
            ObjectNode dataJson = Json.newObject();                            //データの生成
            dataJson.put("mno", data.mitumoriNo);                              //見積番号
            dataJson.put("edaban", data.edaban);                               //枝番
            dataJson.put("kno", data.kanriNo);                                 //管理番号
            dataJson.put("stts", getSttsName(data.mitumoriJotai));             //見積ステータス
            dataJson.put("irai", dateStringFormat(data.iraiNoki));			   //依頼納期
            dataJson.put("kaitou", dateStringFormat(data.kaitoNoki));		   //回答納期
            dataJson.put("kouji", data.kojiKenmei);                            //工事件名
            dataJson.put("kaisya", data.mitumoriIraiKaisya);                   //見積依頼会社
            dataJson.put("shiten", data.mitumoriIraiSiten);                    //見積依頼支店
            dataJson.put("sgoukei", data.saisyuMitumoriGokei);                 //最終見積金額
            dataJson.put("tokui", data.tokuisakiName);                         //得意先
            //弥生出力フラグ
            if(data.renkeiput.equals(MtmHead.RenkeiFlag.PUT)){
            	dataJson.put("renkeiput", "出");
            }else{
            	dataJson.put("renkeiput", "未");
            }
            MstUser.getUserToJson(data.sakuseiUid, dataJson);                  //見積依頼者情報

            listJson.put(data.mitumoriNo, dataJson);                           //JSONリストに格納
          }
    	  resultJson.put("retrieval", true);
        }
        else {
    	  resultJson.put("retrieval", false);
        }

        resultJson.put("dtcnt", dataList.size());
        resultJson.put("result", "success");
	    resultJson.put("datalist", listJson);

        return ok(resultJson);
    }

    /**
     * 売上伝票出力
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
    public static Result denpyoouttext() {

        ObjectNode resultJson = Json.newObject();
        String output = "";
		boolean init = true;
        Logger.info(">>> DENPYO OUTTEXT START. ");
    	//------------------------------------------------------------------------------------
    	//- パラメータの取得
    	//------------------------------------------------------------------------------------
        JsonNode 	inputParameter 	= request().body().asJson();
        JsonNode  jsonList = inputParameter.get("jsonlist");
        long stDenNo = Long.parseLong(inputParameter.get("stdenno").asText());	//作成開始伝票番号

        Logger.info("[stDenNo] MATCH={}. ", stDenNo);

	    try {

	    	Ebean.beginTransaction();
	        for (JsonNode json : jsonList) {
	        	String[] mtmnoeda = json.get("mtmno").asText().split("-", 0);
	        	String mtmno = mtmnoeda[0];					//見積No
	            long edaban = Long.parseLong(mtmnoeda[1]);	//枝番

	            Logger.info("[mtmno] MATCH={}. ", mtmno);
	            Logger.info("[edaban] MATCH={}. ", edaban);

				if (!init) {
					stDenNo++;
				}
				//出力内容作成
				output += putMitumoriData(mtmno, edaban, String.format("%011d", stDenNo));
				//弥生出力フラグ更新
				getUserInfo();
				MtmHead.updateRenkeiput(mtmno,edaban,MtmHead.RenkeiFlag.PUT, user.userId);
				init = false;
	        }
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		    resultJson.put("result"   , "outputerror");
    	    Ebean.rollbackTransaction();
		}
	    finally {
    	    Ebean.commitTransaction();
	    	Ebean.endTransaction();
	    }

        resultJson.put("output", output);
        resultJson.put("result", "success");

        Logger.info(">>> DENPYO OUTTEXT END. ");

        return ok(resultJson);
    }

    /**
     * テキスト出力内容作成
     * @return
     */
	@Authenticated(WeosAuthenticator.class)
	public static String putMitumoriData(String mitumoriNo, long edaban, String denno)  throws Exception{

		String result = "";
		String[] temp = null;
		long tanka = 0;
		boolean nebikitanka=false;
		int gyouNo = 0;

		//現在日付取得
		Locale locale = new Locale("ja", "JP", "JP");
		Calendar calendar = Calendar.getInstance(locale);
    //弥生会計令和未対応による暫定対応
		//DateFormat jpFormat = new SimpleDateFormat("yMMdd",locale);
    DateFormat jpFormat = new SimpleDateFormat("yyMMdd",locale);
		String denDate = jpFormat.format(calendar.getTime());
//		if ((denDate.length() == 6) && "01".equals(denDate.substring(0, 2))) { //令和01年にJavaが置き換える為、
//		  denDate = "31" + denDate.substring(2);
//		}
		Logger.info("[dateStr] MATCH={}. ", denDate);

		//******************************************************************************
		// * 見積ヘッダ情報の取得（共通出力項目がある為）
		// ******************************************************************************
		MtmHead head = MtmHead.find.where().eq("mitumori_no", mitumoriNo).eq("edaban", edaban).findUnique();
		MstShiten shiten = MstShiten.GetShiten(head.mitumoriIraiKaisya,head.mitumoriIraiSiten);

//  // 見積回答日より対象となる弥生販売の税区分を取得する
//		MstTax tax = MstTax.getTaxData(head.mitumoriKaitoubi);
    // 見積依頼日より対象となる弥生販売の税区分を取得する
    MstTax tax = MstTax.getTaxData(head.mitumoriIraibi);
		String taxKind = tax.taxkind; /* 税区分（初期値は11（5%））*/
		// ******************************************************************************
		// * 改造情報の取得
		// ******************************************************************************
		List<MtmTaisyoSeihin> custom = MtmTaisyoSeihin.GetMTTaisyouSeihin(mitumoriNo,edaban);

		if (custom.size() > 0) {
			// ******************************************************************************
			// * 改造情報分の明細出力（件数分回す）
			// ******************************************************************************
			for (MtmTaisyoSeihin customBean : custom) { //改造完了明細分
			  // 分納明細は対象外とする
			  if (!MtmTaisyoSeihin.BunoFlag.NOMAL.equals(customBean.bunno_kubun)) {
			    continue;
			  }
				gyouNo++;
				temp = new String[42];
				//共通部分のセット
				setCommonItem(temp,head,shiten,denDate,denno);
				//各明細項目のセット
				// 行番号
				temp[13] = Integer.toString(gyouNo);
				//明細区分（明細）
				temp[14] = "1";
				//商品コード
				temp[15] = customCode;
				//商品名/摘要
				String siyo = "";
				// 防蝕改造を表示する場合
				if (customBean.bosyokuKaizo.equals("1")) {
					siyo = MsiKaizoKanryo.kaizoName.KZ_BOUSYOKU;
					if (customBean.pboKaiTanka > 0) {
						nebikitanka=true;
						tanka = customBean.pboKaiTanka;
					}
					else {
						tanka = customBean.boKaiTanka;
					}
				}
				// 重防蝕改造を表示する場合
				if (customBean.jubosyokuKaizo.equals("1")) {
					siyo = MsiKaizoKanryo.kaizoName.KZ_JUBOUSYOK;
					if (customBean.pjuKaiTanka > 0) {
						nebikitanka=true;
						tanka = customBean.pjuKaiTanka;
					}
					else {
						tanka = customBean.juKaiTanka;
					}
				}
				// 耐塩害改造を表示する場合
				if (customBean.taiengaiKaizo.equals("1")) {
					siyo = MsiKaizoKanryo.kaizoName.KZ_TAIENGAI;
					if (customBean.ptaiKaiTanka > 0) {
						nebikitanka=true;
						tanka = customBean.ptaiKaiTanka;
					}
					else {
						tanka = customBean.taiKaiTanka;
					}
				}
				// 耐重塩害改造を表示する場合
				if (customBean.taijuengaiKaizo.equals("1")) {
					siyo = MsiKaizoKanryo.kaizoName.KZ_TAIJUENGAI;
					if (customBean.ptaijuKaiTanka > 0) {
						nebikitanka=true;
						tanka = customBean.ptaijuKaiTanka;
					}
					else {
						tanka = customBean.taijuKaiTanka;
					}
				}
				// 重防錆改造を表示する場合
				if (customBean.jubouseiKaizo.equals("1")) {
					siyo = MsiKaizoKanryo.kaizoName.KZ_JUBOUSEI;
					if (customBean.pjubouKaiTanka > 0) {
						nebikitanka=true;
						tanka = customBean.pjubouKaiTanka;
					}
					else {
						tanka = customBean.jubouKaiTanka;
					}
				}
				temp[17] = customBean.taisyoKataban + " " + siyo;

				//課税区分
				temp[18] = taxKind;
				//単位
				temp[19] = "";
				//入数
				temp[20] = "";
				//数量
				temp[23] = Long.toString(customBean.gokei_suryo);
				//単価
				temp[24] = Long.toString(tanka);
				//金額
				temp[25] = Long.toString(customBean.gokei_suryo * tanka);
				//税抜額
				temp[27] = Long.toString(customBean.gokei_suryo * tanka);
				// 明細出力
				result += put(temp);
				// ******************************************************************************
				// * オプション部品の取得
				// ******************************************************************************
				List<MtmOptionKaizo> option = MtmOptionKaizo.GetMeisai(mitumoriNo,edaban,customBean.taisyoKataban,customBean.gyoNo);

				if (option.size() > 0) {
					// ******************************************************************************
					// * オプション部品の出力（改造情報単位）
					// ******************************************************************************
					for (MtmOptionKaizo optionBean : option) { //オプション部品明細分
						gyouNo++;
						temp = new String[42];
						//共通部分のセット
						setCommonItem(temp,head,shiten,denDate,denno);
						//各明細項目のセット
						// 行番号
						temp[13] = Integer.toString(gyouNo);
						//明細区分（明細）
						temp[14] = "1";
						//商品コード
						temp[15] = optionCode;
						//商品名/摘要
						temp[17] = optionBean.optionName;
						//課税区分
						temp[18] = taxKind;
						//単位
						temp[19] = "";
						//入数
						temp[20] = "";
						//数量
						temp[23] = Long.toString(customBean.gokei_suryo);
						//単価
						temp[24] = Long.toString(optionBean.optionTanka);
						//金額
						temp[25] = Long.toString(customBean.gokei_suryo * optionBean.optionTanka);
						//税抜額
						temp[27] = Long.toString(customBean.gokei_suryo * optionBean.optionTanka);
						// 明細出力
						result += put(temp);
					}
				}
			}
		}
		// ******************************************************************************
		// * 特殊改造の取得
		// ******************************************************************************
        List<MtmTokusyuKaizo> special = MtmTokusyuKaizo.GetMeisai(mitumoriNo,edaban,"0");

		if (special.size() > 0) {
			// ******************************************************************************
			// * 特殊改造分の明細出力（件数分回す）
			// ******************************************************************************
			for (MtmTokusyuKaizo specialBean : special ) { //特殊改造明細分
        // 分納明細は対象外とする
        if (!MtmTaisyoSeihin.BunoFlag.NOMAL.equals(specialBean.bunno_kubun)) {
          continue;
        }
				gyouNo++;
				temp = new String[42];
				//共通部分のセット
				setCommonItem(temp,head,shiten,denDate,denno);
				//各明細項目のセット
				// 行番号
				temp[13] = Integer.toString(gyouNo);
				//明細区分（明細）
				temp[14] = "1";
				//商品コード
				temp[15] = specialCode;
				//商品名/摘要
				temp[17] = specialBean.tokusyuKataban + " " + specialBean.tokusyuNaiyo;
				//課税区分
				temp[18] = taxKind;
				//単位
				temp[19] = "";
				//入数
				temp[20] = "";
				//数量
				temp[23] = Long.toString(specialBean.gokei_suryo);
				if (specialBean.ptokusyuKingaku > 0) {
					nebikitanka=true;
					tanka = specialBean.ptokusyuKingaku;
				}
				else {
					tanka = specialBean.tokusyuKingaku;
				}
				//単価
				temp[24] = Long.toString(tanka);
				//金額
				temp[25] = Long.toString(specialBean.gokei_suryo * tanka);
				//税抜額
				temp[27] = Long.toString(specialBean.gokei_suryo * tanka);
				// 明細出力
				result += put(temp);
			}
		}
		// ******************************************************************************
		// * 値引情報の出力
		// ******************************************************************************
		if (head.nebikiGaku != 0 && !nebikitanka) {
			gyouNo++;
			//共通部分のセット
			setCommonItem(temp,head,shiten,denDate,denno);
			//各明細項目のセット
			// 行番号
			temp[13] = Integer.toString(gyouNo);
			//明細区分（明細）
			temp[14] = "3";
			//商品コード
			temp[15] = nebikiCode;
			//商品名/摘要
			temp[17] = nebikiName;
			//課税区分
			temp[18] = taxKind;
			// ******************************************************************************
			// * 消費税改正対応 (2014.04.07) 終了
			// ******************************************************************************
			//単位
			temp[19] = "";
			//入数
			temp[20] = "";
			//数量
			temp[23] = Long.toString(-1);
			//単価
			temp[24] = Long.toString(head.nebikiGaku);
			//金額
			temp[25] = Long.toString(-1 * head.nebikiGaku);
			//税抜額
			temp[27] = Long.toString(-1 * head.nebikiGaku);
			// 明細出力
			result += put(temp);
		}
		// ******************************************************************************
		// * 消費税情報の出力
		// ******************************************************************************
		gyouNo++;
		//共通部分のセット
		setCommonItem(temp,head,shiten,denDate,denno);
		//各明細項目のセット
		// 行番号
		temp[13] = Integer.toString(gyouNo);
		//明細区分（明細）
		temp[14] = "99";
		//商品コード
		temp[15] = "";
		//商品名/摘要
		temp[17] = syouhiName;
		//課税区分
		temp[18] = "0";
		//単位
		temp[19] = "";
		//入数
		temp[20] = "";
		//数量
		temp[23] = "0";
		//単価
		temp[24] = "0";
		//金額
		temp[25] = Long.toString(head.saisyuMitumoriGokei - head.mitumoriGokei);
		//税抜額
		temp[27] = "0";
		// 明細出力
		result += put(temp);
		// ******************************************************************************
		// * 摘要の出力
		// ******************************************************************************
		String tekiyo = "";
		if (!"".equals(head.koubaiKanriNo)) {
			tekiyo = head.koubaiKanriNo;
		}
		else {
			if (!"".equals(head.kanriNo)) {
				tekiyo = head.kanriNo;
			}
		}
		if (!"".equals(head.kojiKenmei)) {
			if (!"".equals(tekiyo)) {
				tekiyo += " ";
			}
			tekiyo += head.kojiKenmei;
		}
		//  依頼ユーザ名称の取得
		MstUser user = MstUser.getUser(head.sakuseiUid);
		if (user != null) {
			if (!"".equals(tekiyo)) {
				tekiyo += " ";
			}
			tekiyo += user.shimeiKanji;
		}
		if (!"".equals(tekiyo)) {
			gyouNo++;
			//共通部分のセット
			setCommonItem(temp,head,shiten,denDate,denno);
			//各明細項目のセット
			// 行番号
			temp[13] = Integer.toString(gyouNo);
			//明細区分（明細）
			temp[14] = "0";
			//商品コード
			temp[15] = "";
			//商品名/摘要
			temp[17] = tekiyo;
			//課税区分
			temp[18] = "0";
			//単位
			temp[19] = "";
			//入数
			temp[20] = "";
			//数量
			temp[23] = "0";
			//単価
			temp[24] = "0";
			//金額
			temp[25] = "0";
			//税抜額
			temp[27] = "0";
			// 明細出力
			result += put(temp);
		}

        //Logger.info("[result] MATCH={}. ", result);
		return result;
	}
	private static String put(String[] temp) {
		String result = "";
		String st = "";
		for (int i=0;i<temp.length;i++) {
			st = "";
			if (i != 0) {
				result += ",";
			}
			if (temp[i] != null) {
				st = temp[i];
			}
			result += "\"" + st + "\"";
		}
		result += "\r\n";
		return result;
	}
	private static void setCommonItem(String[] temp, MtmHead head,MstShiten shiten,
			String denDate, String denno) {
		// 削除マーク
		temp[0] = "1";
		// 締めフラグ
		temp[1] = "1";
		// チェック
		temp[2] = "0";
		// 伝票日付
		temp[3] = denDate;
		// 伝票番号
		temp[4] = denno;
		// 伝票区分
		temp[5] = "24";
		// 取引区分
		temp[6] = "1";
		// 税転嫁
		temp[7] = "1";
		// 金額端数処理
		temp[8] = "1";
		// 端数処理
		temp[9] = "1";
		// 得意先コード
		temp[10] = head.mitumoriIraiKaisya + "00"
				+ head.mitumoriIraiSiten;
		// 納入先コード
		temp[11] = "";
		// 担当者コード
		temp[12] = "";
		// 予備1
		temp[16] = "";
		// ケース
		temp[21] = "0";
		// 回収予定日
		temp[26] = "";
		// 原価
		temp[28] = "0";
		// 原単価
		temp[29] = "0";
		// 備考
		temp[30] = "";
		// 数量小数桁
		temp[31] = "0";
		// 単価小数桁
		temp[32] = "0";
		// 規格・型番
		temp[33] = "";
		// 色
		temp[34] = "";
		// サイズ
		temp[35] = "";
		// 納入期日
		temp[36] = "";
		// 分類コード
		temp[37] = "";
		// 伝票区分
		temp[38] = "";
		// 得意先名称
		temp[39] = shiten.tokuisakiName;
		// プロジェクト主
		temp[40] = "";
		// プロジェクト副
		temp[41] = "";
	}
}
