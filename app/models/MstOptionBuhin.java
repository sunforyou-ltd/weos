package models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】オプション部品マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstOptionBuhin extends Model {

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
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;

    public static Finder<Long, MstOptionBuhin> find = new Finder<Long, MstOptionBuhin>(Long.class, MstOptionBuhin.class);

}
