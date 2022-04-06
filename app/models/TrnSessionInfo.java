package models;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
/**
 * 【WAAS】セッションデータ
 *
 * @author SunForYou.Ltd
 *
 */
public class TrnSessionInfo extends Model {

    /**
     * セッションキー
     */
    @Id
    public String sessionKey = "";
    /**
     * 見積No.
     */
    public String mtmNo = "";
    /**
     * 枝番
     */
    public long edaban = 0;
    /**
     * モード
     */
    public String mode = "";
    /**
     * 更新日時
     * @return
     */
    @UpdatedTimestamp
    public Timestamp updateTime;

    public static Finder<Long, TrnSessionInfo> find = new Finder<Long, TrnSessionInfo>(Long.class, TrnSessionInfo.class);

    /**
     * セッションデータを取得します
     * @param sessionKey
     * @return
     */
    public static TrnSessionInfo getSessionData(String sessionKey) {
      return TrnSessionInfo.find.where().eq("session_key", sessionKey).findUnique();
    }

    /**
     * セッションデータを削除します
     * @param sessionKey
     * @return
     */
    public static boolean deleteSessionData(String sessionKey) {
      boolean result = false;
      int     count  = 0;

      count = Ebean.createSqlUpdate("DELETE FROM trn_session_info WHERE session_key = :sessionKey").setParameter("sessionKey", sessionKey).execute();
      if (count > 0) {
        result = true;
      }
      return result;
    }

}
