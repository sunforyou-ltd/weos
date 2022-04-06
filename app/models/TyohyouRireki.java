package models;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

@Entity
/**
 * 【WAAS】帳票履歴
 *
 * @author SunForYou.Ltd
 *
 */
public class TyohyouRireki extends Model {

    /**
     * 帳票ＩＤ
     */
    @Id
    public String tyouhyouId;
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
     * 発効日
     */
    public java.sql.Date hakkouDate;
    /**
     * 更新ユーザID
     */
    public String koshinUserId;
    /**
     * 更新プログラムID
     */
    public String koshinPrgId;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;

    /**
     * 帳票コード
     * @author kimura
     *
     */
    public class PrintCode {
    	public static final String MIRI    = "C010100010";	// 見積依頼書
    	public static final String MTSU    = "C010100020";	// 見積書
    	public static final String KZOU    = "C010200010";	// 改造依頼書
    	public static final String URIA    = "C010300010";	// 売上伝票
    	public static final String NOUH    = "C010400010";	// 納品書
    	public static final String SEIK    = "C010400020";	// 請求書
    	public static final String JYUR    = "C010400030";	// 受領書
    	public static final String NOUS    = "C010500010";	// 納品仕様書
    	public static final String SEKO    = "C010600010";	// 施工証明書
    }
    /**
     * 帳票タイトル
     * @author kimura
     *
     */
    public class PrintTitle {
    	public static final String MIRI    = " 見積依頼書 ";
    	public static final String MTSU    = " 見　積　書 ";
    	public static final String KZOU    = " 改造依頼書 ";
    	public static final String URIA    = " 売 上 伝 票";
    	public static final String NOUH    = " 納　品　書 ";
    	public static final String SEIK    = " 請　求　書 ";
    	public static final String JYUR    = " 受　領　書 ";
    	public static final String NOUS    = " 納品仕様書 ";
    	public static final String SEKO    = " 施工証明書 ";
    }

    public static Finder<Long, TyohyouRireki> find = new Finder<Long, TyohyouRireki>(Long.class, TyohyouRireki.class);

    /**
     * 帳票履歴より最小発行日を取得します
     * @param pPrintid
     * @param pMtmno
     * @param pEdaban
     * @return
     */
    public static java.sql.Date getRireki(String pPrintid, String pMtmno, long pEdaban) {
	    boolean result = false;
	    StringBuffer sb = new StringBuffer();
	    sb.append("SELECT ");
	    sb.append(" MIN(hakkou_date) AS HAKOU ");
	    sb.append(" FROM ");
	    sb.append(" tyohyou_rireki ");
        sb.append(" WHERE tyouhyou_id = '" + pPrintid +"' ");
        sb.append(" AND mitumori_no = '" + pMtmno +"' ");
        sb.append(" AND edaban = " + pEdaban +" ");

	    SqlRow sqlRows = Ebean.createSqlQuery(sb.toString()).findUnique();
	    if (sqlRows != null) {
	    	return sqlRows.getDate("HAKOU");
	    }
	    return null;
    }
    /**
     * 帳票履歴を作成します
     * @param pPrintid
     * @param pMtmno
     * @param pEdaban
     * @param pUid
     * @param pPid
     */
    public static void makeRireki(String pPrintid, String pMtmno, long pEdaban, String pUid, String pPid) {

    	//インスタンス生成
    	TyohyouRireki rireki = new TyohyouRireki();
    	Calendar cal = Calendar.getInstance();


    	try {
        	//----- 帳票履歴を作成します -----
        	rireki.tyouhyouId = pPrintid;
        	rireki.mitumoriNo = pMtmno;
        	rireki.edaban	  = pEdaban;
        	rireki.hakkouDate = new java.sql.Date(cal.getTime().getTime());
        	rireki.koshinPrgId  = pPid;
        	rireki.koshinUserId = pUid;
        	rireki.koshinDate	= new java.sql.Timestamp(cal.getTime().getTime());
    		rireki.save();
		} catch (Exception e) {
			// TODO: handle exception
			Logger.error(e.getMessage(), e);
		}


    }

}
