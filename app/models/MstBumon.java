package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】部門マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstBumon extends Model {

    /**
     * 会社コード
     */
    @Id
    public String kaisyaCd;
    /**
     * 支店コード
     */
    @Id
    public String shitenCd;
    /**
     * 部門コード
     */
    @Id
    public String bumonCd;
    /**
     * 部門名称
     */
    public String bumonName;
    /**
     * 部門略称
     */
    public String bumonRyaku;
    /**
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;

    public static Finder<Long, MstBumon> find = new Finder<Long, MstBumon>(Long.class, MstBumon.class);

    /**
     * 指定された会社コード＞支店コードの部門マスタを取得します
     * @return
     */
    public static List<MstBumon> GetBumonList(String kaisyacd, String shitencd) {

    	List<MstBumon> bumons = MstBumon.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).eq("kaisya_cd", kaisyacd).eq("shiten_cd", shitencd).orderBy("bumon_cd").findList();

    	return bumons;

    }
}
