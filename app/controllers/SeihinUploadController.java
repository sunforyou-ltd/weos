package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import models.MstKataban;
import models.MtmHead;
import parameter.MtmRetrievalMatch;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import util.BaseExcelModel;
import auth.WeosAuthenticator;
import bean.CMstKataban;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 製品単価取り込みコントローラー
 * @author kimura
 *
 */
public class SeihinUploadController extends BaseController {

  private static final String INPUT_EXCEL__FOLDA = "C:\\temp\\excel\\upload";
  private static final String OUTPUT_EXCEL_PATH = "C:\\temp\\excel\\download\\kakaku.xls";


	/**
	 * 製品単価取り込み画面遷移
	 * @return メニュー画面レンダー
	 */
	@Authenticated(WeosAuthenticator.class)
  public static Result move() {
      return ok(views.html.seihinupload.render());
  }
  /**
   * 製品単価ダウンロード
   * @return
   */
  @Authenticated(WeosAuthenticator.class)
  public static Result upload() {

    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson   = Json.newObject();
    Logger.info(">>> SEIHIN TANKA UPLOAD START. ");
    //------------------------------------------------------------------------------------
    //- 製品マスタExcelの内容をチェックし、正しければ製品マスタ単価の内容を更新する
    //------------------------------------------------------------------------------------
    try {

      //------------------------------------------------------------------------------------
      //- ファイルをサーバ上に格納する
      //------------------------------------------------------------------------------------
      //----- アップロード用フォルダの存在チェック -----
      File fUpFolda = new File(INPUT_EXCEL__FOLDA);
      if (!fUpFolda.exists()) { //アップロードフォルダが存在しない場合
        resultJson.put("result"   , "nonfoldaerror");
        Logger.error(">>> SEIHIN TANKA NO FOLDA ERROR. ");
      }
      else {
        Http.MultipartFormData multipartFormData  = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart excel     = multipartFormData.getFile("excel");
        String fileName   = excel.getFilename();
        File file         = excel.getFile();
        String filePath   = INPUT_EXCEL__FOLDA + "\\" + fileName;
        File tmpfile      = new File(filePath);
        //----- アップロード用ファイルの存在チェック -----
        if (tmpfile.exists()) { //アップロードファイルが存在する場合
          Logger.info(">>> SEIHIN TANKA FILE EXISTS >>> FILE DELETE. ");
          tmpfile.delete();
        }
        file.renameTo(tmpfile);

        //----- アップロード用ファイルの読み込み開始 -----
        Logger.info(">>> SEIHIN TANKA FILE READ START. ");
        BaseExcelModel bem = new BaseExcelModel(filePath);
        bem.startInput();
        bem.setSheet(bem.getWorkBook().getSheetAt(0));

        //----- 各オブジェクトの定義 -----
        List<MstKataban> katabans   = new ArrayList<MstKataban>();
        List<CMstKataban> ckatabans = new ArrayList<CMstKataban>();
        Hashtable<String, CMstKataban> checks = new Hashtable<String, CMstKataban>();

        //----- Excelの内容をチェックしながらマスタの生成を行う -----
        for (int i=0;i<bem.getLastRowNum();i++) {
          if (!"".equals(bem.getCellString((short)i,(short)1, 2))) {
            String sSeihin    = bem.getCellString((short)i,(short)1, 2);
            CMstKataban check = checks.get(sSeihin);
            CMstKataban error = null;
            MstKataban data   = null;
            //----- チェック開始 -----
            if (check != null) { //既に同じ製品型番が存在している場合
              error = new CMstKataban();
              error.sSeihinKataban  = sSeihin;
              error.bSeihinKataban  = true;
              error.iLineNo         = i + 2 + 1;
              error.iMacheLineNo    = check.iLineNo;
            }
            else { //重複型番が存在しない場合
              data = new MstKataban();
              data.seihinKataban = sSeihin;
              for (int col = 0;col < 5;col++) {
                try {
                  long tanka = (long)bem.getCellNumeric((short)i,(short)(2 + col), 2);
                  switch (col) {
                  case 1: //重防食単価
                    data.jubousyokuTanka = tanka;
                    break;

                  case 2: //耐塩害単価
                    data.taiengaiTanka = tanka;
                    break;

                  case 3: //耐重塩害単価
                    data.taijuengaiTanka = tanka;
                    break;

                  case 4: //重防錆単価
                    data.jubouseiTanka = tanka;
                    break;

                  default: //防食単価
                    data.bousyokuTanka = tanka;
                    break;
                  }
                }
                catch (Exception e) {
                  Logger.error(e.getMessage(), e);
                  if (error == null) {
                    error                 = new CMstKataban();
                    error.sSeihinKataban  = sSeihin;
                    error.iLineNo         = i + 2 + 1;
                  }
                  switch (col) {
                  case 1: //重防食単価
                    error.bJubousyokuTanka  = true;
                    break;

                  case 2: //耐塩害単価
                    error.bTaiengaiTanka    = true;
                    break;

                  case 3: //耐重塩害単価
                    error.bTaijuengaiTanka  = true;
                    break;

                  case 4: //重防錆単価
                    error.bJubouseiTanka    = true;
                    break;

                  default: //防食単価
                    error.bBousyokuTanka    = true;
                    break;
                  }
                }
              }
            }
            //エラーチェックの結果を確認する
            if (error != null) { //エラーが存在する場合
              ckatabans.add(error); //エラーリストに格納する
            }
            else {
              katabans.add(data);
              //二重チェック用に登録する
              check = new CMstKataban();
              check.sSeihinKataban  = sSeihin;
              check.iLineNo         = i + 2 + 1;
              check.iMacheLineNo    = 0;
              checks.put(check.sSeihinKataban, check);
            }
          }
        }
        //----- チェック結果によって処理を分ける -----
        if (ckatabans.size() > 0) { //エラーが存在する場合
          //----- エラーメッセージを生成する -----
          for (CMstKataban ckataban : ckatabans) {
            ObjectNode dataJson   = Json.newObject();
            dataJson.put("kataban", ckataban.sSeihinKataban);
            if (ckataban.bSeihinKataban) { //製品型番二重登録の場合
              dataJson.put("message", "「" + ckataban.sSeihinKataban + "」の型番は" + ckataban.iMacheLineNo + "行目の型番と同じです。(" + ckataban.iLineNo + "行目のデータ)");
            }
            else { //単価エラーの場合
              StringBuffer sb = new StringBuffer();
              sb.append("「" + ckataban.sSeihinKataban + "」単価が数値に変換できませんでした。\r\n");
              if (ckataban.bBousyokuTanka) {
                sb.append("【防食単価】");
              }
              if (ckataban.bJubousyokuTanka) {
                sb.append("【重防食単価】");
              }
              if (ckataban.bTaiengaiTanka) {
                sb.append("【耐塩害単価】");
              }
              if (ckataban.bTaijuengaiTanka) {
                sb.append("【耐重塩害単価】");
              }
              if (ckataban.bJubouseiTanka) {
                sb.append("【重防錆単価】");
              }
              sb.append("(" + ckataban.iLineNo + "行目のデータ)");
              dataJson.put("message", sb.toString());
            }
            listJson.put(String.valueOf(ckataban.iLineNo) , dataJson);
          }
          resultJson.put("result"     , "dataerror");
          resultJson.put("error"      , listJson);
        }
        else { //エラーが存在しない場合
          //----- 製品マスタを更新する -----
          try {

            Ebean.beginTransaction();
            Ebean.createSqlUpdate("TRUNCATE TABLE mst_kataban;").execute();
            for (MstKataban kataban : katabans) {
              kataban.sakujoFlg = MtmHead.DeleteFlag.NOMAL;
              kataban.save();
            }
          } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            resultJson.put("result"   , "uperror");
            Ebean.rollbackTransaction();
          }
          finally {
              Ebean.commitTransaction();
            Ebean.endTransaction();
          }
          resultJson.put("result"       , "success");
        }
      }
    } catch (Exception e) {
      Logger.error(e.getMessage(), e);
      resultJson.put("result"   , "uperror");
    }
    finally {
    }

    Logger.info(">>> SEIHIN TANKA UPLOAD END. ");

    return ok(resultJson);
  }
  /**
   * 製品単価ダウンロード
   * @return
   */
	@Authenticated(WeosAuthenticator.class)
  public static Result download() {

    ObjectNode resultJson = Json.newObject();
    boolean filedownload = false;
    File objFile = new File(OUTPUT_EXCEL_PATH);
    Logger.info(">>> SEIHIN TANKA DOWNLOAD START. ");
  	//------------------------------------------------------------------------------------
  	//- 製品単価の内容をEXCEL形式で編集し、ダウンロードさせる
  	//------------------------------------------------------------------------------------
    try {
      //----- 製品単価データの取得 -----
      List<MstKataban> objKatabans = MstKataban.find.orderBy("seihin_kataban asc").findList();

      if (objKatabans.size() == 0) {
        resultJson.put("result"   , "nondata");
      }
      else {
        //----- Excelデータの生成 -----
        BaseExcelModel bem = new BaseExcelModel();
        bem.startOutput();
        bem.setSheet(bem.getWorkBook().createSheet());
        bem.getWorkBook().setSheetName(0, "価格情報");
        /* ヘッダ情報の作成 */
        bem.createRow(0);
        bem.createCell(0, (short)0);
        bem.createCell(0, (short)1);
        bem.createCell(0, (short)2);
        bem.createCell(0, (short)3);
        bem.createCell(0, (short)4);
        bem.createCell(0, (short)5);
        bem.createCell(0, (short)6);
        bem.createCell(0, (short)7);
        bem.setCellText((short)0,(short)0,0,"");
        bem.setCellText((short)0,(short)1,0,"製品型番");
        bem.setCellText((short)0,(short)2,0,"防　　　蝕");
        bem.setCellText((short)0,(short)3,0,"重　防　蝕");
        bem.setCellText((short)0,(short)4,0,"耐　塩　害");
        bem.setCellText((short)0,(short)5,0,"耐重塩害");
        bem.setCellText((short)0,(short)6,0,"重　防　錆");
        bem.setCellText((short)0,(short)7,0,"備　　　考");
        /* 空白行の作成 */
        bem.createRow(1);
        bem.createCell(1, (short)0);
        bem.createCell(1, (short)1);
        bem.createCell(1, (short)2);
        bem.createCell(1, (short)3);
        bem.createCell(1, (short)4);
        bem.createCell(1, (short)5);
        bem.createCell(1, (short)6);
        bem.createCell(1, (short)7);

        int iRow = 1;
        for (MstKataban kataban : objKatabans) {
          iRow++;
          bem.createRow(iRow);
          bem.createCell(iRow, (short)0);
          bem.createCell(iRow, (short)1);
          bem.createCell(iRow, (short)2);
          bem.createCell(iRow, (short)3);
          bem.createCell(iRow, (short)4);
          bem.createCell(iRow, (short)5);
          bem.createCell(iRow, (short)6);
          bem.createCell(iRow, (short)7);

          bem.setCellText((short)iRow,(short)1, kataban.seihinKataban);
          bem.setCellText((short)iRow,(short)2, kataban.bousyokuTanka);
          bem.setCellText((short)iRow,(short)3, kataban.jubousyokuTanka);
          bem.setCellText((short)iRow,(short)4, kataban.taiengaiTanka);
          bem.setCellText((short)iRow,(short)5, kataban.taijuengaiTanka);
          bem.setCellText((short)iRow,(short)6, kataban.jubouseiTanka);
        }

        //----- Excelデータをファイルに出力する -----
        FileOutputStream objOutput = new FileOutputStream(objFile);
        bem.getWorkBook().write(objOutput);
        objOutput.close();
        filedownload = true;
      }

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		  resultJson.put("result"   , "outputerror");
		}
    finally {
    }

    resultJson.put("result"       , "success");
    resultJson.put("filedownload" , filedownload);

    Logger.info(">>> SEIHIN TANKA DOWNLOAD END. ");

    return ok(resultJson);
  }
  /**
   * Excelファイルダウンロード
   * @return
   */
  @Authenticated(WeosAuthenticator.class)
  public static Result exceldownload() {

    File objFile = new File(OUTPUT_EXCEL_PATH);
    Logger.info(">>> EXCEL DOWNLOAD START. ");
    //------------------------------------------------------------------------------------
    //- 作成済みのEXCELファイルをダウンロードする
    //------------------------------------------------------------------------------------
    Logger.info(">>> EXCEL DOWNLOAD END. ");
    return ok(objFile);
  }
}