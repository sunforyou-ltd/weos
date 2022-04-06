package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

@Entity
/**
 * 【WAAS】特殊改造
 *
 * @author SunForYou.Ltd
 *
 */
public class MtmTokusyuKaizo extends Model {

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
     * 特殊改造型番
     */
    public String tokusyuKataban;
    /**
     * 特殊改造内容
     */
    public String tokusyuNaiyo;
    /**
     * 特殊改造金額
     */
    public long tokusyuKingaku;
    /**
     * 数量
     */
    public long suryo;
    /**
     * マシン番号
     */
    public String mashinNo;
    /**
     * 入荷予定日
     */
    public String nyukaYotei;
    /**
     * 配セ戻入日
     */
    public String haiseReinyuDay;
    /**
     * 製造完了日
     */
    public String seizou_kanryou_day;
    /**
     * 更新ユーザID
     */
    public String koshinUid;
    /**
     * 更新プログラムID
     */
    public String koshinPid;
    /**
     * 更新日付
     */
    public Timestamp koushinDate;
    /**
     * 入荷希望日
     */
    public String nyukaKibou;
    /**
     * 分納区分
     */
    public String bunno_kubun;
    /**
     * 合計数量
     */
    public long gokei_suryo;
    /**
     * 引取希望日
     */
    public String hikitori_kibou;
    /**
     * 帳票用特殊改造金額
     */
    public long ptokusyuKingaku;

    public static Finder<Long, MtmTokusyuKaizo> find = new Finder<Long, MtmTokusyuKaizo>(Long.class, MtmTokusyuKaizo.class);

    /**
     * 渡された見積番号、枝番に一致する特殊改造を取得します
     * @return
     */
    public static List<MtmTokusyuKaizo> GetMTTokusyu(String pMtmno,long pEdaban) {
    	List<MtmTokusyuKaizo> dataList = MtmTokusyuKaizo.find.where().eq("mitumori_no", pMtmno).eq("edaban", pEdaban).findList();
    	return dataList;
    }

    /**
     * 渡された見積番号、枝番、分納区分に一致する特殊改造を取得します
     * @return
     */
    public static List<MtmTokusyuKaizo> GetMeisai(String pMtmno,long pEdaban,String pBunnokbn) {
    	List<MtmTokusyuKaizo> dataList = MtmTokusyuKaizo.find.where().eq("mitumori_no", pMtmno).eq("edaban", pEdaban).eq("bunno_kubun", pBunnokbn).orderBy("gyo_no").findList();
    	return dataList;
    }

    /**
     * 対象見積の特殊情報を削除します
     * @param mtmno
     * @param edaban
     * @return
     */
    public static boolean deleteData(String pMtmno, long pEdaban) {
		boolean result = false;
		StringBuffer sb = new StringBuffer();

		//更新SQLの作成
		sb.append("DELETE FROM mtm_tokusyu_kaizo ");
		sb.append(" WHERE mitumori_no = '" + pMtmno + "' ");
		sb.append(" AND edaban = " + pEdaban + " ");
		Logger.debug(sb.toString());
		//SQLの実行
		int update = Ebean.createSqlUpdate(sb.toString()).execute();
		if (update >= 0) {
			result = true;
		}

        return result;
    }

}
