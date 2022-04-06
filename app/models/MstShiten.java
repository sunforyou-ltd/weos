package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】支店マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstShiten extends Model {

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
     * 支店名称
     */
    public String shitenName;
    /**
     * 支店略称
     */
    public String shitenRyaku;
    /**
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;
    /**
     * 県内外フラグ
     */
    public String kennaigaiFlg;
    /**
     * 得意先名称
     */
    public String tokuisakiName;

    /**
     * 県内外フラグ
     * @author kimura
     *
     */
    public class Kennaigai {
    	public static final String KENNAI    = "0";	//ＬＥ(発注権限が購買)
    	public static final String KENGAI    = "1"; //一般取引先
    }

    public static Finder<Long, MstShiten> find = new Finder<Long, MstShiten>(Long.class, MstShiten.class);

    /**
     * 指定された会社コードの支店マスタを取得します
     * @return
     */
    public static List<MstShiten> GetShitenList(String kaisyacd) {

    	List<MstShiten> shitens = MstShiten.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).eq("kaisya_cd", kaisyacd).orderBy("shiten_cd").findList();

    	return shitens;

    }

    /**
     * 指定された会社コード、支店コードの支店マスタを取得します
     * @return
     */
    public static MstShiten GetShiten(String kaisyacd,String shitencd) {

    	MstShiten user = MstShiten.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).eq("kaisya_cd", kaisyacd).eq("shiten_cd", shitencd).findUnique();

      return user;

    }
}
