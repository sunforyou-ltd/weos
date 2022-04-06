package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】大分類マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstDaibunrui extends Model {

    /**
     * 大分類コード
     */
    @Id
    public String daibunruiCd;
    /**
     * 大分類名称
     */
    public String daibunruiName;

    public static Finder<Long, MstDaibunrui> find = new Finder<Long, MstDaibunrui>(Long.class, MstDaibunrui.class);

}
