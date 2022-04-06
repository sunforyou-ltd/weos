package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.annotation.UpdatedTimestamp;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】製品型番マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstKataban extends Model {

    /**
     * 製品型番
     */
    @Id
    public String seihinKataban;
    /**
     * 防蝕単価
     */
    public long bousyokuTanka;
    /**
     * 重防蝕単価
     */
    public long jubousyokuTanka;
    /**
     * 耐塩害単価
     */
    public long taiengaiTanka;
    /**
     * 耐重塩害単価
     */
    public long taijuengaiTanka;
    /**
     * 重塩害単価
     */
    public long jubouseiTanka;
    /**
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    @UpdatedTimestamp
    public Timestamp koshinDate;

    public static Finder<Long, MstKataban> find = new Finder<Long, MstKataban>(Long.class, MstKataban.class);

    /**
     * 製品型番を取得します
     * @param pKataban
     * @return
     */
    public static List<MstKataban> getKatabans(String pKataban) {

    	List<MstKataban> katabans = MstKataban.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).like("seihin_kataban", pKataban + "%").orderBy("seihin_kataban").findList();
    	return katabans;

    }

    /**
     * 製品型番を取得します
     * @param pKataban
     * @return
     */
    public static MstKataban getKataban(String pKataban) {

    	MstKataban kataban = MstKataban.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).eq("seihin_kataban", pKataban).orderBy("seihin_kataban").findUnique();
    	return kataban;

    }

}
