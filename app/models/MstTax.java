package models;

import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】消費税マスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstTax extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 4822146201576935559L;
    /**
     * 消費税開始
     */
    @Id
    public java.sql.Date taxfrom;
    /**
     * 消費税終了
     */
    @Id
    public java.sql.Date taxto;
    /**
     * 消費税
     */
    public long tax;
    /**
     * 消費税区分
     */
    public String taxkind;

    public static Finder<Long, MstTax> find = new Finder<Long, MstTax>(Long.class, MstTax.class);

    /**
     * 指定された日付文字列(8桁)から消費税データを取得します
     * @return
     */
    public static MstTax getTaxData(String pDate) {

      if (pDate == null || pDate.length() != 8) { //不正な日付文字列の場合
        return null;
      }

      Calendar cal = Calendar.getInstance();
      cal.set(Integer.valueOf(pDate.substring(0, 4)), (Integer.valueOf(pDate.substring(4, 6)) - 1), Integer.valueOf(pDate.substring(6)));

    	return MstTax.find.where().le("taxfrom", new java.sql.Date(cal.getTime().getTime())).ge("taxto", new java.sql.Date(cal.getTime().getTime())).findUnique();

    }
}
