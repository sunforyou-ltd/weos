package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import models.MstBumon;
import models.MstKaisya;
import models.MstShiten;
import models.MstUser;
import models.MtmHead;
import models.TrnSessionInfo;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import auth.WeosAuthenticator;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ベースコントローラー
 * @author kimura
 *
 */
public class BaseController extends Controller {

  public static String URL_PREFIX = "";

	/**
	 * セッションユーザ
	 */
	protected static MstUser user;

	/**
	 * セッションキー
	 * @author kimura
	 *
	 */
  public class SessionKeys {
    /**
     * 見積番号
     */
    public static final String MTMNO   = "mtmno";
    /**
     * 枝番
     */
    public static final String EDABAN   = "edaban";
    /**
     * 見積検索モード
     */
    public static final String MODE   	= "mode";
  }


	/**
	 * セッションユーザを取得します
	 */
	@Authenticated(WeosAuthenticator.class)
	protected static void getSessionUser() {

		String sUserId = WeosAuthenticator.getSessionId(ctx());
		//ここでユーザIDがNULLで戻ってくる事はない(セッションチェックではじかれる為)
		user = MstUser.find.where().eq("user_id", sUserId).findUnique();
		Logger.debug("getSessionUser -> [user]={}.", user);

	}

	/**
	 * ユーザ情報をJSON形式で取得する
	 * @return
	 */
	@Authenticated(WeosAuthenticator.class)
	public static Result getUserInfo() {

        ObjectNode resultJson = Json.newObject();

        getSessionUser();

        if (user != null) {
          Logger.debug("getUserInfo -> user info exists.");
        	//ユーザID
            resultJson.put("userId", user.userId);
            //会社情報
            resultJson.put("kaisyaCd", user.kaisyaCd);
            MstKaisya kaisya = MstKaisya.find.where().eq("kaisya_cd", user.kaisyaCd).findUnique();
            if (kaisya != null) {
                resultJson.put("kaisyaName", kaisya.kaisyaName);
            }
            else {
                resultJson.put("kaisyaName", "");
            }
            //支店情報
            resultJson.put("shitenCd", user.shitenCd);
            MstShiten shiten = MstShiten.find.where().eq("kaisya_cd", user.kaisyaCd).eq("shiten_cd", user.shitenCd).findUnique();
            if(shiten != null) {
                resultJson.put("shitenName", shiten.shitenName);
                //県内外フラグ
                resultJson.put("kennaigai", shiten.kennaigaiFlg);
            }
            else {
                resultJson.put("shitenName", "");
                //県内外フラグ
                resultJson.put("kennaigai", MstShiten.Kennaigai.KENGAI);
            }
            //部門情報
            resultJson.put("bumonCd", user.bumonCd);
            MstBumon bumon = MstBumon.find.where().eq("kaisya_cd", user.kaisyaCd).eq("shiten_cd", user.shitenCd).eq("bumon_cd", user.bumonCd).findUnique();
            if(bumon != null) {
                resultJson.put("bumonName", bumon.bumonName);
            }
            else {
                resultJson.put("bumonName", "");
            }
            //ユーザ氏名
            resultJson.put("shimeiKanji", user.shimeiKanji);
            //ユーザカナ
            resultJson.put("shimeiKana", user.shimeiKana);
            //権限
            resultJson.put("kengen", user.kengen);
            Logger.debug("getUserInfo ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
            Logger.debug("[userId]={}.", resultJson.get("userId"));
            Logger.debug("[kaisyaCd]={}.", resultJson.get("kaisyaCd"));
            Logger.debug("[kaisyaName]={}.", resultJson.get("kaisyaName"));
            Logger.debug("[shitenCd]={}.", resultJson.get("shitenCd"));
            Logger.debug("[shitenName]={}.", resultJson.get("shitenName"));
            Logger.debug("[kennaigai]={}.", resultJson.get("kennaigai"));
            Logger.debug("[bumonCd]={}.", resultJson.get("bumonCd"));
            Logger.debug("[bumonName]={}.", resultJson.get("bumonName"));
            Logger.debug("[shimeiKanji]={}.", resultJson.get("shimeiKanji"));
            Logger.debug("[shimeiKana]={}.", resultJson.get("shimeiKana"));
            Logger.debug("[kengen]={}.", resultJson.get("kengen"));
        }

        return ok(resultJson);

	}
	/**
	 * 渡された見積ステータスを見積状況に変換します
	 * @param pStts
	 * @return
	 */
	public static String getSttsName(String pStts) {
		String sResult = ""; //戻り値

		switch (pStts) {	//見積ステータス
		case MtmHead.Stts.MT_INPUT:	//見積依頼中
			sResult = MtmHead.SttsName.MT_INPUT;
			break;

		case MtmHead.Stts.MT_ANSER:	//見積回答済
			sResult = MtmHead.SttsName.MT_ANSER;
			break;

		case MtmHead.Stts.MT_ORDER:	//発注
			sResult = MtmHead.SttsName.MT_ORDER;
			break;

		case MtmHead.Stts.MT_COMMIT://受注確定
			sResult = MtmHead.SttsName.MT_COMMIT;
			break;

		case MtmHead.Stts.MT_SEKOU:	//施工完了
			sResult = MtmHead.SttsName.MT_SEKOU;
			break;

		default:
			sResult = "";
			break;
		}

		return sResult;
	}
	/**
	 * 渡された文字列日付を日付フォーマットに変換します
	 * @param pDateString
	 * @return
	 */
	public static String dateStringFormat(String pDateString) {

		String sResult = ""; //戻り値

    Logger.debug("dateStringFormat -> before [pDateString]={}.", pDateString);
		if (pDateString == null) {
			sResult = "";
		}
		else {
			switch (pDateString.length()) {
			case 4:
				sResult = pDateString;
				break;

			case 6:
				sResult = pDateString.substring(0, 4) + "/"+ pDateString.substring(4, 6);
				break;

			case 8:
				sResult = pDateString.substring(0, 4) + "/"+ pDateString.substring(4, 6) + "/"  + pDateString.substring(6, 8);
				break;

      case 10:
        sResult = pDateString.replaceAll("/", "");
        break;

			default:
				sResult = "";
				break;
			}

		}

    Logger.debug("dateStringFormat -> after [sResult]={}.", sResult);
		return sResult;

	}
  /**
   * システム日付を8桁の西暦フォーマットで返します
   * @param pDateString
   * @return
   */
  public static String systemDateFormat() {

    String sResult = ""; //戻り値
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Calendar calender = Calendar.getInstance();

    sResult = sdf.format(calender.getTime());
    Logger.debug("systemDateFormat -> SYSTEM DATE = {}", sResult);

    return sResult;

  }
  /**
   * 渡された日付文字列を和暦フォーマットに変換して返します
   * @param pDateString
   * @return
   */
  public static String warekiDateFormat(String pDateString) {

    Logger.debug("warekiDateFormat -> [pDateString]= {}", pDateString);

    if (pDateString == null || "".equals(pDateString)) {
      return "";
    }

    //ロケールを指定してCalendarインスタンスを取得
    Locale local = new Locale("ja","JP","JP");
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, Integer.valueOf(pDateString.substring(0, 4)));
    calendar.set(Calendar.MONTH, (Integer.valueOf(pDateString.substring(4, 6)) - 1));
    calendar.set(Calendar.DATE, Integer.valueOf(pDateString.substring(6)));

    DateFormat japaseseFormat = new SimpleDateFormat("GGGGy年M月d日", local);
    String dateStr = japaseseFormat.format(calendar.getTime());
    dateStr = dateStr.replaceAll("昭和1年", "昭和元年");
    dateStr = dateStr.replaceAll("平成1年", "平成元年");
    dateStr = dateStr.replaceAll("令和1年", "令和元年");
    Logger.debug("warekiDateFormat -> [dateStr]= {}", dateStr);
    return dateStr;

  }
  /**
   * 日付文字列を比較し、判定結果を返します
   * @param pDate1
   * @param pDate2
   * @return
   */
  public static int dateCompare(String pDate1, String pDate2) {

	  int result = -9;

	  if (pDate1 == null || pDate2 == null) {
		  return result;
	  }
	  if ("".equals(pDate1) || "".equals(pDate2)) {
		  return result;
	  }

	  Calendar cal1 = Calendar.getInstance();
	  Calendar cal2 = Calendar.getInstance();

	  cal1.set(Integer.parseInt(pDate1.substring(0,4)), Integer.parseInt(pDate1.substring(4,6)) - 1, Integer.parseInt(pDate1.substring(6,8)), 0, 0, 0);
	  cal2.set(Integer.parseInt(pDate2.substring(0,4)), Integer.parseInt(pDate2.substring(4,6)) - 1, Integer.parseInt(pDate2.substring(6,8)), 0, 0, 0);

	  result = cal1.compareTo(cal2);
    Logger.debug("dateCompare -> [pDate1]= {}", pDate1);
    Logger.debug("dateCompare -> [pDate2]= {}", pDate2);
    Logger.debug("dateCompare -> [result]= {}", result);

	  return result;

  }
	/**
	 * 渡された改造内容をチェックボックス選択文字列に変換する
	 * @param pKaizo
	 * @return
	 */
  public static String checkedKaizo(String pKaizo) {

    String sResult = ""; //戻り値

    if (pKaizo == null) {
      sResult = "";
    }
    else {
      if ("1".equals(pKaizo)) {
        sResult = "checked";
      }
    }

    return sResult;

  }
  /**
   * 渡された改造内容をBooleanで返す
   * @param pKaizo
   * @return
   */
  public static boolean boolKaizo(String pKaizo) {

    boolean sResult = false; //戻り値

    if (pKaizo == null) {
      sResult = false;
    }
    else {
      if ("1".equals(pKaizo)) {
        sResult = true;
      }
    }

    return sResult;

  }
	/**
	 * 渡されたチェックボックス値を改造内容値に変換する
	 * @param pKaizo
	 * @return
	 */
	public static String kaizoValue(boolean pChecked) {

	  String sResult = "0"; //戻り値

    if (pChecked) {
      sResult = "1";
    }

	  return sResult;

	}
	public static String editPostcode(String pPostCode) {
	  String result = "";
	  if (pPostCode == null || "".equals(pPostCode)) {
	    return result;
	  }
	  if (pPostCode.length() > 3) {
	    result = pPostCode.substring(0, 3) + "-" + pPostCode.substring(3);
	  }
	  else {
	    result = pPostCode;
	  }
	  return result;
	}
	/**
	 * セッションキーに格納された値を取得します
	 * @return
	 */
	public static parameter.SessionKeys getSessionKeys() {

    getUserInfo(); //クッキーよりユーザ情報を取得する

    String session = getWepAppSessionKeys();
    if ( session == null) {
    	session = "";
    }

    TrnSessionInfo tsi = TrnSessionInfo.getSessionData(user.userId + session);
    if (tsi == null) {
      tsi = new TrnSessionInfo();
    }

	  parameter.SessionKeys sessionKeys = new parameter.SessionKeys();
	  sessionKeys.mtmno  = tsi.mtmNo;
	  sessionKeys.edaban = tsi.edaban;
	  sessionKeys.mode   = tsi.mode;

	  Logger.debug(">>>>> SessionKey Get.");
    Logger.debug("[sessionKey]{}", user.userId);
	  Logger.debug("[mtmno]{}", sessionKeys.mtmno);
	  Logger.debug("[edaban]{}", sessionKeys.edaban);
	  Logger.debug("[mode]{}", sessionKeys.mode);

    return sessionKeys;

	}
  /**
   * 渡されたパラメータをセッションに登録します
   * @return
   */
  public static void setSessionKeys(parameter.SessionKeys pSessionKeys) {

    getUserInfo(); //クッキーよりユーザ情報を取得する

    String session = getWepAppSessionKeys();
    if ( session == null) {
    	session = "";
    }

    TrnSessionInfo tsi = TrnSessionInfo.getSessionData(user.userId + session);

    if (tsi != null) {
      TrnSessionInfo.deleteSessionData(tsi.sessionKey);  //一旦セッションデータを削除する
    }
    tsi = new TrnSessionInfo();

//    Cache.set(SessionKeys.MTMNO + user.userId, pSessionKeys.mtmno);
//    Cache.set(SessionKeys.EDABAN + user.userId, pSessionKeys.edaban);
//    Cache.set(SessionKeys.MODE + user.userId, pSessionKeys.mode);

    tsi.sessionKey = user.userId + session;
    tsi.mtmNo      = pSessionKeys.mtmno;
    tsi.edaban     = pSessionKeys.edaban;
    tsi.mode       = pSessionKeys.mode;
    tsi.save();

    Logger.debug(">>>>> SessionKey Update.");
    Logger.debug("[sessionKey]{}", user.userId + session);
    Logger.debug("[mtmno]{}", pSessionKeys.mtmno);
    Logger.debug("[edaban]{}", pSessionKeys.edaban);
    Logger.debug("[mode]{}", pSessionKeys.mode);

  }
  /**
   * セッションクッキー情報を取得します
   * @return
   */
  public static String getWepAppSessionKeys() {
      final Http.Cookie userCookie = request().cookie(WeosAuthenticator.WEOSAPPKEY);	// クッキーを取得する
      if (userCookie == null) return null;    											// ログインクッキーなし
      return userCookie.value();
  }

}
