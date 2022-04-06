package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import parameter.MtmRetrievalMatch;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

@Entity
/**
 * 【WAAS】見積検索条件
 *
 * @author SunForYou.Ltd
 *
 */
public class MtmRetrievalWhere extends Model {

    /**
     * セッションキー
     */
    @Id
    public String sessionKey = "";
    /**
     * 見積番号(自)
     */
    public String mtmnof = "";
    /**
     * 見積番号(至)
     */
    public String mtmnot = "";
    /**
     * 管理番号
     */
    public String kanri = "";
    /**
     * 依頼納期(自)
     */
    public String requef = "";
    /**
     * 依頼納期(至)
     */
    public String requet = "";
    /**
     * 回答納期(自)
     */
    public String anserf = "";
    /**
     * 回答納期(至)
     */
    public String ansert = "";
    /**
     * 作業件名
     */
    public String kenmei = "";
    /**
     * 得意先番号
     */
    public String tokuno = "";
    /**
     * 得意先名称
     */
    public String tokunm = "";
    /**
     * 会社コード
     */
    public String kaisya = "";
    /**
     * 支店コード
     */
    public String shiten = "";
    /**
     * 部門コード
     */
    public String bumon = "";
    /**
     * 見積依頼
     */
    public boolean input = false;
    /**
     * 見積回答済
     */
    public boolean anser = false;
    /**
     * 発注
     */
    public boolean torder = false;
    /**
     * 受注確定
     */
    public boolean tcommit = false;
    /**
     * 施工完了
     */
    public boolean sekou = false;

    public static Finder<Long, MtmRetrievalWhere> find = new Finder<Long, MtmRetrievalWhere>(Long.class, MtmRetrievalWhere.class);

    public void set(String sessionkey, MtmRetrievalMatch match) {
    	//----- 見積検索条件を格納する -----
    	this.sessionKey 	= sessionkey;
    	this.mtmnof 		= match.mtmnof;
    	this.mtmnot 		= match.mtmnot;
    	this.kanri 			= match.kanri;
    	this.requef 		= match.requef;
    	this.anserf 		= match.anserf;
    	this.ansert 		= match.ansert;
    	this.kenmei 		= match.kenmei;
    	this.tokuno 		= match.tokuno;
    	this.tokunm 		= match.tokunm;
    	this.kaisya 		= match.kaisya;
    	this.shiten 		= match.shiten;
    	this.bumon 			= match.bumon;
    	this.input 			= match.input;
    	this.anser 			= match.anser;
    	this.torder 		= match.order;
    	this.tcommit 		= match.commit;
    	this.sekou 			= match.sekou;

    	//----- 自分自身の保存情報を削除 -----
    	Ebean.createNamedSqlUpdate("DELETE FROM mtm_retrieval_where WHERE session_key = :sessionKey")
    	     .setParameter("sessionKey", this.sessionKey)
    	     .execute();
    	//----- 自分自身の保存情報を作成 -----
    	this.save();

    }

}

