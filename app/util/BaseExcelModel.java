package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * 【WeoS】改造依頼書（明細）BEAN
 *
 * @author SunForYou.Ltd
 *
 */
public class BaseExcelModel {

  //******************************************************
  //* フィールド変数
  //******************************************************
  // 対象入出力オブジェクト
  File file = null;
  // 対象入力オブジェクト
  FileInputStream input = null;
  // 対象POIファイルシステム
  POIFSFileSystem fs = null;
  // 対象出力オブジェクト
  FileOutputStream output = null;
  // ワークブックオブジェクト
  HSSFWorkbook book = null;
  // シートオブジェクト
  HSSFSheet sheet = null;
  //******************************************************
  //* コンストラクタ
  //******************************************************
  /**
   * コンストラクタ
   */
  public BaseExcelModel() {
  }
  /**
   * コンストラクタ
   */
  public BaseExcelModel(String path) {
    file = new File(path);
  }
  //******************************************************
  //* ファイル関連処理
  //******************************************************
  /**
   * Excel入力初期処理
   */
  public boolean startInput() throws Exception {
    boolean result = false;

    // 指定したパスが存在しない場合
    if (!file.exists()) {
      return result;
    }
    // 指定したパスがファイル以外の場合
    if (!file.isFile()) {
      return result;
    }
    // 指定したパスが読込み不可の場合
    if (!file.canRead()) {
      return result;
    }

    input = new FileInputStream(file);
    fs = new POIFSFileSystem(input);
    book = new HSSFWorkbook(fs);

    result = true;

    return result;
  }
  /**
   * Excel入力初期処理
   */
  public boolean startInput(InputStream in) throws Exception {
    boolean result = false;

    if (in == null) {
      return result;
    }

    fs = new POIFSFileSystem(in);
    book = new HSSFWorkbook(fs);

    result = true;

    return result;
  }
  /**
   * Excel出力初期処理
   * @return
   * @throws Exception
   */
  public boolean startOutput() throws Exception {
    boolean result = false;

    book = new HSSFWorkbook();

    result = true;

    return result;
  }
  //******************************************************
  //* エクセル関連処理
  //******************************************************
  /**
   * ワークブックの取得
   */
  public HSSFWorkbook getWorkBook() throws Exception {
    return book;
  }

  public void setSheet(HSSFSheet st) {
    this.sheet = st;
  }
  public HSSFSheet getSheet() {
    return this.sheet;
  }
  public void shiftRow(int startRow,int endRow,int shift){
    this.sheet.shiftRows(startRow, endRow, shift);
  }
  public void createRow(int cpyRowNo){
    this.sheet.createRow(cpyRowNo);
  }
  public void copyRow(int orgRowNo, int cpyRowNo, boolean cpst) {
    this.sheet.createRow(cpyRowNo);
    HSSFRow orgRow = this.sheet.getRow(orgRowNo);
    HSSFRow cpyRow = this.sheet.getRow(cpyRowNo);
    cpyRow.setHeight(orgRow.getHeight());
    for (int i=0;i<orgRow.getLastCellNum();i++) {
      createCell(cpyRowNo,(short)i);
    }
  }
  public void createCell(int cpyRowNo,short cpyColNo){
    HSSFRow cpyRow = this.sheet.getRow(cpyRowNo);
    cpyRow.createCell(cpyColNo);
  }
  // 文字データをセットする。
  public void setCellText(short rowIdx,short colIdx, String value) {
    setCellText(rowIdx,colIdx,0,value);
  }
  // 文字データをセットする。(行シフト対応)
  public void setCellText(short rowIdx,short colIdx,int shiftRow, String value) {

    // nullチェックはあえてしない。※その場合、Mappingクラスの指定が間違っているので。
    HSSFRow row = this.sheet.getRow( rowIdx + shiftRow );
    row.getCell(colIdx).setCellValue(new HSSFRichTextString(value));

  }

  // 日付データをセットする。
  public void setCellText(short rowIdx,short colIdx, java.util.Date value) {
    setCellText(rowIdx,colIdx,0,value);
  }
  // 日付データをセットする。
  public void setCellText(short rowIdx,short colIdx,int shiftRow, java.util.Date value) {
    HSSFRow row = this.sheet.getRow(rowIdx + shiftRow );
    row.getCell(colIdx).setCellValue(value);
  }

  // 数値データをセットする。
  public void setCellText(short rowIdx,short colIdx, double value) {
    setCellText(rowIdx,colIdx,0,value);
  }

  // 数値データをセットする。
  public void setCellText(short rowIdx,short colIdx,int shiftRow, double value) {
    HSSFRow row = sheet.getRow( rowIdx + shiftRow );
    row.getCell(colIdx).setCellValue(value);
  }

  // 計算式をセットする。
  public void setCellFormula(short rowIdx,short colIdx,String value, int i) {

    setCellFormula(rowIdx,colIdx,0,value);

  }
  // 計算式をセットする。
  public void setCellFormula(short rowIdx,short colIdx,int shiftRow, String value) {

    HSSFRow row = sheet.getRow( rowIdx + shiftRow );
    row.getCell(colIdx).setCellFormula(value);

  }
  // セルの値を取得する
  @SuppressWarnings("deprecation")
  public String getCellString(short rowIdx,short colIdx,int shiftRow) {
    String result = "";

    HSSFRow row = sheet.getRow( rowIdx + shiftRow );
    if (row != null) {
      HSSFCell cell = row.getCell(colIdx);
      if (cell != null) {
        result = cell.getStringCellValue();
      }
    }
    return result;

  }
  // セルの値を取得する
  @SuppressWarnings("deprecation")
  public double getCellNumeric(short rowIdx,short colIdx,int shiftRow) {
    double result = 0;

    HSSFRow row = sheet.getRow( rowIdx + shiftRow );
    if (row != null) {
      HSSFCell cell = row.getCell(colIdx);
      if (cell != null) {
        result = cell.getNumericCellValue();
      }
    }
    return result;

  }
  // 最終行数を取得する
  @SuppressWarnings("deprecation")
  public int getLastRowNum() {
    return sheet.getLastRowNum();
  }

}