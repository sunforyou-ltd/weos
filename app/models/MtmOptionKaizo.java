package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

@Entity
/**
 * 【WAAS】オプション改造
 *
 * @author SunForYou.Ltd
 *
 */
public class MtmOptionKaizo extends Model {

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
     * 製品型番
     */
    public String taisyoKataban;
    /**
     * オプションコード
     */
    @Id
    public String optionCd;
    /**
     * オプション名称
     */
    public String optionName;
    /**
     * オプション単価
     */
    public long optionTanka;
    /**
     * 数量
     */
    public long suryo;
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
     * 行番号
     */
    @Id
    public long gyoNo;
    /**
     * マシン番号
     */
    public String mashinNo;
    /**
     * 入荷予定日
     */
    public String nyukaYotei;
    /**
     * 入荷希望日
     */
    public String nyukaKibou;

    public static Finder<Long, MtmOptionKaizo> find = new Finder<Long, MtmOptionKaizo>(Long.class, MtmOptionKaizo.class);

    /**
     * 渡された見積番号、枝番、型番、行番号に一致する特殊改造を取得します
     * @return
     */
    public static List<MtmOptionKaizo> GetMeisai(String pMtmno,long pEdaban, String pKataban, long pGyono) {
    	List<MtmOptionKaizo> dataList = MtmOptionKaizo.find.where().eq("mitumori_no", pMtmno).eq("edaban", pEdaban).eq("taisyo_kataban", pKataban).eq("gyo_no", pGyono).orderBy("option_cd").findList();
    	return dataList;
    }
    /**
     * 対象見積のオプション改造を削除します
     * @param mtmno
     * @param edaban
     * @return
     */
    public static boolean deleteData(String pMtmno, long pEdaban) {
    	boolean result = false;
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM mtm_option_kaizo ");
        sb.append(" WHERE mitumori_no = :mtmno ");
        sb.append(" AND edaban = :edaban ");
        int update = Ebean.createSqlUpdate(sb.toString())
        		.setParameter("mtmno", pMtmno)
        		.setParameter("edaban", pEdaban)
        		.execute();
        if (update >= 0) {
        	result = true;
        }
        return result;
    }
}
