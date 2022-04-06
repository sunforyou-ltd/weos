package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
/**
 * 【WAAS】プロパティマスタ
 *
 * @author SunForYou.Ltd
 *
 */
public class MstProperty extends Model {

    /**
     * コード
     */
    @Id
    public String code;
    /**
     * 属性名称
     */
    @Id
    public String name;
    /**
     * 値
     */
    @Id
    public String value;

    public static Finder<Long, MstProperty> find = new Finder<Long, MstProperty>(Long.class, MstProperty.class);

    public class Code {
    	/**
    	 * 見積郵便番号
    	 */
    	public static final String  MT_YUBIN	= "001";
    	/**
    	 * 見積住所１
    	 */
    	public static final String  MT_ADDR1	= "002";
    	/**
    	 * 見積住所２
    	 */
    	public static final String  MT_ADDR2	= "003";
    	/**
    	 * 見積電話番号
    	 */
    	public static final String  MT_TEL		= "004";
    	/**
    	 * 見積FAX番号
    	 */
    	public static final String  MT_FAX		= "005";
    	/**
    	 * 改造費請求先
    	 */
    	public static final String  MT_KAIZOUHI	= "006";
      /**
       * LE会社コード
       */
      public static final String  LE_KCODE = "007";
      /**
       * メル支店コード
       */
      public static final String  MR_SCODE = "008";
      /**
       * メル会社名
       */
      public static final String  MR_KNAME = "009";
      /**
       * 自社会社名
       */
      public static final String  MY_NAME = "010";
      /**
       * 自社住所１
       */
      public static final String  MY_ADDR1 = "011";
      /**
       * 自社住所２
       */
      public static final String  MY_ADDR2 = "012";
      /**
       * 自社住所３
       */
      public static final String  MY_ADDR3 = "013";
      /**
       * 電話
       */
      public static final String  MY_TEL = "014";
      /**
       * ＦＡＸ
       */
      public static final String  MY_FAX = "015";
      /**
       * 改造センター電話番号
       */
      public static final String  CENTER_TEL = "016";
      /**
       * 改造センターＦＡＸ
       */
      public static final String  CENTER_FAX = "017";
      /**
       * 納品仕様書
       */
      public static final String  NOUHIN_URL = "018";
      /**
       * 施工証明書（住所１）
       */
      public static final String  SEKOUADDR1 = "019";
      /**
       * 施工証明書（住所２）
       */
      public static final String  SEKOUADDR2 = "020";
      /**
       * 施工証明書（住所３）
       */
      public static final String  SEKOUADDR3 = "021";
      /**
       * 施工証明書（住所４）
       */
      public static final String  SEKOUADDR4 = "022";
      /**
       * 施工会社名
       */
      public static final String  SEKOUNAME  = "023";
      /**
       * 施工管理者
       */
      public static final String  SEKOUKANRI = "024";
    }

    /**
     * 指定されたプロパティ値を取得します
     * @param pCode
     * @return
     */
    public static String GetValue(String pCode) {

    	String result = "";

    	MstProperty property = MstProperty.find.where().eq("code", pCode).findUnique();

    	if (property != null) {
    		result = property.value;
    	}

    	return result;

    }
}
