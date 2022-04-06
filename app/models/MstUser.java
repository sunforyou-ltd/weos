package models;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.db.ebean.Model;
import play.libs.Json;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
/**
 * 【WAAS】ユーザマスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstUser extends Model {

    /**
     * ユーザＩＤ
     */
    @Id
    public String userId;
    /**
     * ユーザ漢字
     */
    public String shimeiKanji;
    /**
     * ユーザカナ
     */
    public String shimeiKana;
    /**
     * 会社コード
     */
    public String kaisyaCd;
    /**
     * 支店コード
     */
    public String shitenCd;
    /**
     * 部門コード
     */
    public String bumonCd;
    /**
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;
    /**
     * 権限
     */
    public String kengen;
    /**
     * パスワード
     */
    public String password;

    public static Finder<Long, MstUser> find = new Finder<Long, MstUser>(Long.class, MstUser.class);

    /**
     * 権限
     * @author kimura
     *
     */
    public class Kengen {
    	/**
    	 * サンデンテクノ
    	 */
    	public static final String ST = "01";
    	/**
    	 * 営業担当者
    	 */
    	public static final String EIGYO = "02";
    	/**
    	 * フロント営業部
    	 */
    	public static final String FRONT = "03";
    }
    /**
     * 指定されたユーザＩＤのユーザマスタを取得します
     * @return
     */
    public static MstUser getUser(String pUserId) {

      MstUser user = MstUser.find.where().eq("user_id", pUserId).eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).findUnique();

      return user;

    }
    /**
     * 指定されたユーザＩＤのユーザマスタを取得します(支店縛りあり)
     * @return
     */
    public static MstUser getUser(String pUserId, String pKaisyaCd, String pShitenCd ) {

      MstUser user = MstUser.find.where().eq("user_id", pUserId).eq("kaisya_cd", pKaisyaCd).eq("shiten_cd", pShitenCd).eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).findUnique();

      return user;

    }
	/**
	 * ユーザ情報を渡されたJSONに追加します
	 * @return
	 */
	public static void getUserToJson(String pUserId, ObjectNode resultJson) {

		MstUser user = MstUser.find.where().eq("user_id", pUserId).eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).findUnique();

        if (user != null) {
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
        }
        else {
        	//ユーザID
            resultJson.put("userId", "");
            //会社情報
            resultJson.put("kaisyaCd", "");
            resultJson.put("kaisyaName", "");
            //支店情報
            resultJson.put("shitenName", "");
            //県内外フラグ
            resultJson.put("kennaigai", MstShiten.Kennaigai.KENGAI);
            //部門情報
            resultJson.put("bumonName", "");
            //ユーザ氏名
            resultJson.put("shimeiKanji", "");
            //ユーザカナ
            resultJson.put("shimeiKana", "");
            //権限
            resultJson.put("kengen", "");
        }

	}
	/**
	 * 新規でユーザを追加します
	 * @return
	 */
	public boolean insertUser() {
	  boolean result = false;
	  try {

	    Ebean.beginTransaction();

	    this.userId = newUserId();
	    this.sakujoFlg = MtmHead.DeleteFlag.NOMAL;
	    this.password = this.kaisyaCd + this.shitenCd + this.userId.substring(3);
	    this.koshinDate = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
	    this.save();
	    result = true;

	    Ebean.commitTransaction();

	  } catch (Exception e) {
	    Logger.error(e.getMessage(), e);
	    Ebean.rollbackTransaction();
	  }
	  finally {
	    Ebean.endTransaction();
	  }

	  return result;

	}
	/**
	 * ユーザ情報を変更します
	 * @return
	 */
	public boolean updateUser() {
		boolean result = false;
		try {

			Ebean.beginTransaction();
			StringBuffer sb = new StringBuffer();

			//更新SQLの作成
			sb.append("UPDATE mst_user ");
			sb.append(" SET shimei_kanji = :shimeiKanji ");
			sb.append(" , shimei_kana = :shimeiKana ");
			sb.append(" , kaisya_cd = :kaisyaCd ");
			sb.append(" , shiten_cd = :shitenCd ");
			sb.append(" , bumon_cd = :bumonCd ");
			sb.append(" , kengen = :kengen ");
			sb.append(" , koshin_date = NOW() ");
			sb.append(" WHERE user_id = :userid ");
			//SQLの実行
			int count = Ebean.createSqlUpdate(sb.toString())
						.setParameter("shimeiKanji", this.shimeiKanji)
						.setParameter("shimeiKana", this.shimeiKana)
						.setParameter("kaisyaCd", this.kaisyaCd)
						.setParameter("shitenCd", this.shitenCd)
						.setParameter("bumonCd", this.bumonCd)
						.setParameter("kengen", this.kengen)
						.setParameter("userid", this.userId)
						.execute();
			Ebean.commitTransaction();
			if (count > 0) {
				result = true;
			}

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			Ebean.rollbackTransaction();
		}
		finally {
			Ebean.endTransaction();
		}

		return result;

	}
	/**
	 * パスワード変更を行います
	 * @return
	 */
	public boolean passwordCommit() {
		boolean result = false;
		try {

			Ebean.beginTransaction();
			StringBuffer sb = new StringBuffer();

			//更新SQLの作成
			sb.append("UPDATE mst_user ");
			sb.append(" SET password = :password ");
			sb.append(" , koshin_date = NOW() ");
			sb.append(" WHERE user_id = :userid ");
			//SQLの実行
			int count = Ebean.createSqlUpdate(sb.toString()).setParameter("password", this.password).setParameter("userid", this.userId).execute();
			Ebean.commitTransaction();
			if (count > 0) {
				result = true;
			}

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			Ebean.rollbackTransaction();
		}
		finally {
			Ebean.endTransaction();
		}

		return result;

	}
  /**
   * ユーザ情報を削除します
   * @return
   */
  public boolean deleteUser() {
    boolean result = false;
    try {

      Ebean.beginTransaction();
      StringBuffer sb = new StringBuffer();

      //更新SQLの作成
      sb.append("UPDATE mst_user ");
      sb.append(" SET sakujo_flg = :sakujoFlg ");
      sb.append(" , koshin_date = NOW() ");
      sb.append(" WHERE user_id = :userid ");
      //SQLの実行
      int count = Ebean.createSqlUpdate(sb.toString()).setParameter("sakujoFlg", MtmHead.DeleteFlag.DELETE).setParameter("userid", this.userId).execute();
      Ebean.commitTransaction();
      if (count > 0) {
        result = true;
      }

    } catch (Exception e) {
      Logger.error(e.getMessage(), e);
      Ebean.rollbackTransaction();
    }
    finally {
      Ebean.endTransaction();
    }

    return result;

  }
  /**
   * パスワードの初期化を行います
   * @return
   */
  public boolean passwordInit() {
    boolean result = false;
    try {

      Ebean.beginTransaction();
      StringBuffer sb = new StringBuffer();

      //更新SQLの作成
      sb.append("UPDATE mst_user ");
      sb.append(" SET password = :password ");
      sb.append(" , koshin_date = NOW() ");
      sb.append(" WHERE user_id = :userid ");
      //SQLの実行
      int count = Ebean.createSqlUpdate(sb.toString()).setParameter("password", this.kaisyaCd + this.shitenCd + this.userId.substring(3)).setParameter("userid", this.userId).execute();
      Ebean.commitTransaction();
      if (count > 0) {
        result = true;
      }

    } catch (Exception e) {
      Logger.error(e.getMessage(), e);
      Ebean.rollbackTransaction();
    }
    finally {
      Ebean.endTransaction();
    }

    return result;

  }
	/**
	 * 権限リストを取得します。
	 * @return
	 */
	public static ObjectNode getKengenList() {

        ObjectNode listJson   = Json.newObject();
        ObjectNode dataJson;

        //サンデンテクノ
        dataJson   = Json.newObject();
        dataJson.put("value", Kengen.ST);
        dataJson.put("name", "サンデンテクノ");
        listJson.put(Kengen.ST, dataJson);

        //営業担当者
        dataJson   = Json.newObject();
        dataJson.put("value", Kengen.EIGYO);
        dataJson.put("name", "営業担当者");
        listJson.put(Kengen.EIGYO, dataJson);

        //フロント営業部
        dataJson   = Json.newObject();
        dataJson.put("value", Kengen.FRONT);
        dataJson.put("name", "フロント営業部");
        listJson.put(Kengen.FRONT, dataJson);

        return listJson;

	}
	/**
	 * 新規ユーザＩＤを発行します
	 * @return
	 */
	public static final synchronized String newUserId() {
	  String result = "";
	  long lUserId = 0;

    //------------------------------------------------------------------------------------
    //- 最大ユーザＩＤを取得する
    //------------------------------------------------------------------------------------
    StringBuffer sb = new StringBuffer();
    sb.append(" SELECT MAX(mst_user.user_id) AS mUserId");
    sb.append(" FROM mst_user ");
    List<SqlRow> sqlRows = Ebean.createSqlQuery(sb.toString()).findList();

    if (sqlRows.size() == 0 ){
      //----- データが存在しない場合 -----
      lUserId = 1;
    }
    else {
      SqlRow data = sqlRows.get(0);
      lUserId = Long.parseLong(data.getString("mUserId"));
      lUserId++; //カウントアップ
    }

    DecimalFormat df = new DecimalFormat("000000");
    result = df.format(lUserId);

	  return result;
	}
}
