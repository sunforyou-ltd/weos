package parameter;

import models.MtmHead;

/**
 * 見積検索条件
 * @author kimura
 *
 */
public class MtmRetrievalMatch {

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
     * 権限
     */
    public String kengen = "";
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
    public boolean order = false;
    /**
     * 受注確定
     */
    public boolean commit = false;
    /**
     * 施工完了
     */
    public boolean sekou = false;

    /**
     * 見積状態が全てONかチェックします
     * @return
     */
    public boolean StatusAllOnCheck() {
    	return input & anser & order & commit & sekou;
    }
    /**
     * 見積状態が全てOFFかチェックします
     * @return
     */
    public boolean StatusAllOffCheck() {
    	return !(input || anser || order || commit || sekou);
    }
    /**
     * 見積依頼の状態がONの場合、ステータス番号を返します
     * @return
     */
    public String ChangeSttsInput() {
    	String result = "0";
    	if (input) {
    		result = MtmHead.Stts.MT_INPUT;
    	}
    	return result;
    }
    /**
     * 見積回答済の状態がONの場合、ステータス番号を返します
     * @return
     */
    public String ChangeSttsAnser() {
    	String result = "0";
    	if (anser) {
    		result = MtmHead.Stts.MT_ANSER;
    	}
    	return result;
    }
    /**
     * 発注の状態がONの場合、ステータス番号を返します
     * @return
     */
    public String ChangeSttsOrder() {
    	String result = "0";
    	if (order) {
    		result = MtmHead.Stts.MT_ORDER;
    	}
    	return result;
    }
    /**
     * 受注確定の状態がONの場合、ステータス番号を返します
     * @return
     */
    public String ChangeSttsCommit() {
    	String result = "0";
    	if (commit) {
    		result = MtmHead.Stts.MT_COMMIT;
    	}
    	return result;
    }
    /**
     * 施工完了の状態がONの場合、ステータス番号を返します
     * @return
     */
    public String ChangeSttsSekou() {
    	String result = "0";
    	if (sekou) {
    		result = MtmHead.Stts.MT_SEKOU;
    	}
    	return result;
    }
}