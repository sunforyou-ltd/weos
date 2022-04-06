package models;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

@Entity
/**
 * 【WAAS】改造完了明細
 *
 * @author SunForYou.Ltd
 *
 */
public class MsiKaizoKanryo extends Model {

    /**
     * 見積No
     */
    @Id
    public String mitumoriNo;
    /**
     * 枝番
     */
    @Id
    public long edaban;
    /**
     * 行番号
     */
    @Id
    public long gyoNo;
    /**
     * 対象型番
     */
    public String taisyoKataban;
    /**
     * 対象内容
     */
    public String taisyoNaiyo;
    /**
     * 製造番号
     */
    public String seizoNo;
    /**
     * 施工者名
     */
    public String sekouName;
    /**
     * 更新ユーザID
     */
    public String koshinUserId;
    /**
     * 更新プログラムID
     */
    public String koshin_PrgId;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;

    public static Finder<Long, MsiKaizoKanryo> find = new Finder<Long, MsiKaizoKanryo>(Long.class, MsiKaizoKanryo.class);

    /**
     * 改造内容名称
     * @author kimura
     *
     */
    public class kaizoName {
    	/**
    	 * 防蝕改造
    	 */
    	public static final String KZ_BOUSYOKU 	= "防蝕改造";
    	/**
    	 * 重防蝕改造
    	 */
    	public static final String KZ_JUBOUSYOK = "重防蝕改造";
    	/**
    	 * 耐塩害改造
    	 */
    	public static final String KZ_TAIENGAI 	= "耐塩害改造";
    	/**
    	 * 耐重塩害改造
    	 */
    	public static final String KZ_TAIJUENGAI = "耐重塩害改造";
    	/**
    	 * 重防錆改造
    	 */
    	public static final String KZ_JUBOUSEI 	= "重防錆改造";
    	/**
    	 * 重防錆改造
    	 */
    	public static final String KZ_BUHINKM 	= "部品組み込み";
    }

    /**
     * 渡された見積番号、枝番に一致する改造完了明細を取得します
     * @return
     */
    public static List<MsiKaizoKanryo> GetKaizoKanryo(String pMtmno,long pEdaban) {
    	List<MsiKaizoKanryo> dataList = MsiKaizoKanryo.find.where().eq("mitumori_no", pMtmno).eq("edaban", pEdaban).findList();
    	return dataList;
    }

    /**
     * 指定された見積番号、枝番、行番号に一致する改造完了明細を取得します
     * @return
     */
    public static MsiKaizoKanryo GetMTEdabanGyo(String pMtmno,long pEdaban,long pGyoNo) {

      MsiKaizoKanryo kanryo = MsiKaizoKanryo.find.where().eq("mitumori_no", pMtmno).eq("edaban", pEdaban).eq("gyo_no", pGyoNo).findUnique();

      return kanryo;

    }
	/**
	 * 新規で改造完了明細を追加します
	 * @return
	 */
	public boolean insertKaizoKanryo() {
	  boolean result = false;
	  try {

	    Ebean.beginTransaction();

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
	 * 改造完了明細を更新します
	 * @return
	 */
	public boolean updateKaizoKanryo() {
		boolean result = false;
		try {

			Ebean.beginTransaction();
			StringBuffer sb = new StringBuffer();

			//更新SQLの作成
			sb.append("UPDATE msi_kaizo_kanryo ");
			sb.append(" SET seizo_no = :seizoNo ");
			sb.append(" , sekou_name = :sekouName ");
			sb.append(" , koshin_date = NOW() ");
			sb.append(" WHERE mitumori_no = :mtmNo ");
			sb.append("   AND edaban = :edaban ");
			sb.append("   AND gyo_no = :gyoNo ");
			//SQLの実行
			int count = Ebean.createSqlUpdate(sb.toString())
						.setParameter("seizoNo", this.seizoNo)
						.setParameter("sekouName", this.sekouName)
						.setParameter("mtmNo", this.mitumoriNo)
						.setParameter("edaban", this.edaban)
						.setParameter("gyoNo", this.gyoNo)
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
}
