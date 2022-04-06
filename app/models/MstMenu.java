package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】メニューマスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstMenu extends Model {

    /**
     * 権限
     */
    @Id
    public String kengen;
    /**
     * 大分類コード
     */
    @Id
    public String daibunruiCd;
    /**
     * 表示
     */
    public int hyouji;
    /**
     * 業務ＩＤ
     */
    @Id
    public String gyomuId;
    /**
     * 業務名称
     */
    public String gyomuName;
    /**
     * リンクパス
     */
    public String linkPas;

    public static Finder<Long, MstMenu> find = new Finder<Long, MstMenu>(Long.class, MstMenu.class);

}
