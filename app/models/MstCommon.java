package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】共通マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstCommon extends Model {

    /**
     * 分類コード
     */
    @Id
    public String bunruicd;
    /**
     * 情報コード
     */
    @Id
    public String infocd;
    /**
     * 詳細情報１
     */
    public String detailinfo1;
    /**
     * 詳細情報２
     */
    public String detailinfo2;
    /**
     * シーケンス値
     */
    public long seq;

    public static Finder<Long, MstCommon> find = new Finder<Long, MstCommon>(Long.class, MstCommon.class);

}
