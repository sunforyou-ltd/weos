package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】会社マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstKaisya extends Model {

    /**
     * 会社コード
     */
    @Id
    public String kaisyaCd;
    /**
     * 会社名称
     */
    public String kaisyaName;
    /**
     * 会社略称
     */
    public String kaisyaRyaku;
    /**
     * 権限フラグ
     */
    public String kengenFlg;
    /**
     * 削除フラグ
     */
    public String sakujoFlg;
    /**
     * 更新日付
     */
    public Timestamp koshinDate;

    public static Finder<Long, MstKaisya> find = new Finder<Long, MstKaisya>(Long.class, MstKaisya.class);

    /**
     * 会社の全件リストを取得します
     * @return
     */
    public static List<MstKaisya> GetKaisyaAllList() {

    	List<MstKaisya> kaisyas = MstKaisya.find.where().eq("sakujo_flg", MtmHead.DeleteFlag.NOMAL).orderBy("kaisya_cd").findList();

    	return kaisyas;

    }

}
