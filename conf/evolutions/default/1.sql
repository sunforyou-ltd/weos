# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table msi_kaizo_kanryo (
  mitumori_no               varchar(255) not null,
  edaban                    bigint not null,
  gyo_no                    bigint not null,
  taisyo_kataban            varchar(255),
  taisyo_naiyo              varchar(255),
  seizo_no                  varchar(255),
  sekou_name                varchar(255),
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               timestamp)
;

create table mst_bumon (
  kaisya_cd                 varchar(255) not null,
  shiten_cd                 varchar(255) not null,
  bumon_cd                  varchar(255) not null,
  bumon_name                varchar(255),
  bumon_ryaku               varchar(255),
  sakujo_flg                varchar(255),
  koshin_date               timestamp)
;

create table mst_common (
  bunruicd                  varchar(255) not null,
  infocd                    varchar(255) not null,
  detailinfo1               varchar(255),
  detailinfo2               varchar(255),
  seq                       bigint)
;

create table mst_daibunrui (
  daibunrui_cd              varchar(255) not null,
  daibunrui_name            varchar(255),
  constraint pk_mst_daibunrui primary key (daibunrui_cd))
;

create table mst_kaisya (
  kaisya_cd                 varchar(255) not null,
  kaisya_name               varchar(255),
  kaisya_ryaku              varchar(255),
  kengen_flg                varchar(255),
  sakujo_flg                varchar(255),
  koshin_date               timestamp,
  constraint pk_mst_kaisya primary key (kaisya_cd))
;

create table mst_kataban (
  seihin_kataban            varchar(255) not null,
  bousyoku_tanka            bigint,
  jubousyoku_tanka          bigint,
  taiengai_tanka            bigint,
  taijuengai_tanka          bigint,
  jubousei_tanka            bigint,
  sakujo_flg                varchar(255),
  koshin_date               timestamp,
  constraint pk_mst_kataban primary key (seihin_kataban))
;

create table mst_menu (
  kengen                    varchar(255) not null,
  daibunrui_cd              varchar(255) not null,
  gyomu_id                  varchar(255) not null,
  hyouji                    integer,
  gyomu_name                varchar(255),
  link_pas                  varchar(255))
;

create table mst_option_buhin (
  option_cd                 varchar(255) not null,
  option_name               varchar(255),
  option_tanka              bigint,
  sakujo_flg                varchar(255),
  koshin_date               timestamp,
  constraint pk_mst_option_buhin primary key (option_cd))
;

create table mst_shiten (
  kaisya_cd                 varchar(255) not null,
  shiten_cd                 varchar(255) not null,
  shiten_name               varchar(255),
  shiten_ryaku              varchar(255),
  sakujo_flg                varchar(255),
  koshin_date               timestamp,
  kennaigai_flg             varchar(255),
  tokuisaki_name            varchar(255))
;

create table mst_user (
  user_id                   varchar(255) not null,
  shimei_kanji              varchar(255),
  shimei_kana               varchar(255),
  kaisya_cd                 varchar(255),
  shiten_cd                 varchar(255),
  bumon_cd                  varchar(255),
  sakujo_flg                varchar(255),
  koshin_date               timestamp,
  kengen                    varchar(255),
  password                  varchar(255),
  constraint pk_mst_user primary key (user_id))
;

create table mtm_head (
  mitumori_no               varchar(255) not null,
  edaban                    bigint not null,
  order_no                  varchar(255),
  kanri_no                  varchar(255),
  koji_kenmei               varchar(255),
  irai_noki                 varchar(255),
  kaito_noki                varchar(255),
  mitumori_iraibi           varchar(255),
  mitumori_kaitoubi         varchar(255),
  tokuisaki_name            varchar(255),
  nohinsaki_yubin           varchar(255),
  nohinsaki_add1            varchar(255),
  nohinsaki_add2            varchar(255),
  nohinsaki_tel             varchar(255),
  nohinsaki_fax             varchar(255),
  saisyu_mitumori_gokei     bigint,
  mitumori_gokei            bigint,
  nebiki_gaku               bigint,
  nebiki_ritu               bigint,
  kaizo_syonin_no           varchar(255),
  koubai_kanri_no           varchar(255),
  untin_seikyu              varchar(255),
  kaizouhi_seikyu           varchar(255),
  paneru_color              varchar(255),
  setti_basyo               varchar(255),
  bikou_1                   varchar(255),
  bikou_2                   varchar(255),
  bikou_3                   varchar(255),
  mitumori_irai_kaisya      varchar(255),
  mitumori_irai_siten       varchar(255),
  sakusei_uid               varchar(255),
  kaitou_uid                varchar(255),
  kakutei_flg               varchar(255),
  mitumori_jotai            varchar(255),
  sakujo_flg                varchar(255),
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               timestamp,
  mitumori_irai_bumon       varchar(255),
  hatsureimoto_code         varchar(255),
  tokuisaki_code            varchar(255),
  renkeiput                 varchar(255),
  refnebikigaku             bigint,
  nebikitanka               bigint,
  pmitumori_gokei           bigint,
  siten_name                varchar(255),
  tantou_name               varchar(255),
  koutei_nouki1             varchar(255),
  koutei_nouki2             varchar(255),
  koutei_nouki3             varchar(255))
;

create table mtm_option_kaizo (
  mitumori_no               varchar(255) not null,
  edaban                    bigint not null,
  option_cd                 varchar(255) not null,
  gyo_no                    bigint not null,
  seihin_kataban            varchar(255),
  option_name               varchar(255),
  option_tanka              bigint,
  suryo                     bigint,
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               timestamp,
  mashin_no                 varchar(255),
  nyuka_yotei               varchar(255),
  nyuka_kibou               varchar(255))
;

create table mtm_taisyo_seihin (
  mitumori_no               varchar(255) not null,
  edaban                    bigint not null,
  gyo_no                    bigint not null,
  seihin_kataban            varchar(255),
  bosyoku_kaizo             varchar(255),
  bo_kai_tanka              bigint,
  jubosyoku_kaizo           varchar(255),
  ju_kai_tanka              bigint,
  taiengai_kaizo            varchar(255),
  tai_kai_tanka             bigint,
  taijuengai_kaizo          varchar(255),
  taiju_kai_tanka           bigint,
  jubousei_kaizo            varchar(255),
  jubou_kai_tanka           bigint,
  yobi1kaizo                varchar(255),
  yobi1kaizo_tanka          bigint,
  yobi2kaizo                varchar(255),
  yobi2kaizo_tanka          bigint,
  suryo                     bigint,
  mashin_no                 varchar(255),
  nyuka_yotei               varchar(255),
  haise_reinyu_day          varchar(255),
  seizou_kanryou_day        varchar(255),
  senta_keijou_day          varchar(255),
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               timestamp,
  nyuka_kibou               varchar(255),
  bunno_kubun               varchar(255),
  gokei_suryo               bigint,
  hikitori_kibou            varchar(255),
  pbo_kai_tanka             bigint,
  pju_kai_tanka             bigint,
  ptai_kai_tanka            bigint,
  ptaiju_kai_tanka          bigint,
  pjubou_kai_tanka          bigint,
  p_yobi1kai_tanka          bigint,
  p_yobi2kai_tanka          bigint)
;

create table mtm_tokusyu_kaizo (
  mitumori_no               varchar(255) not null,
  edaban                    bigint not null,
  gyo_no                    bigint not null,
  tokusyu_kataban           varchar(255),
  tokusyu_naiyo             varchar(255),
  tokusyu_kingaku           bigint,
  suryo                     bigint,
  mashin_no                 varchar(255),
  nyuka_yotei               varchar(255),
  haise_reinyu_day          varchar(255),
  seizou_kanryou_day        varchar(255),
  senta_keijou_day          varchar(255),
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               timestamp,
  nyuka_kibou               varchar(255),
  bunno_kubun               varchar(255),
  gokei_suryo               bigint,
  hikitori_kibou            varchar(255),
  ptokusyu_kingaku          bigint)
;

create table tyohyou_rireki (
  tyouhyou_id               varchar(255) not null,
  mitumori_no               varchar(255) not null,
  edaban                    varchar(255) not null,
  hakkou_date               varchar(255),
  koshin_uid                varchar(255),
  koshin_pid                varchar(255),
  koshin_date               varchar(255))
;

create sequence msi_kaizo_kanryo_seq;

create sequence mst_bumon_seq;

create sequence mst_common_seq;

create sequence mst_daibunrui_seq;

create sequence mst_kaisya_seq;

create sequence mst_kataban_seq;

create sequence mst_menu_seq;

create sequence mst_option_buhin_seq;

create sequence mst_shiten_seq;

create sequence mst_user_seq;

create sequence mtm_head_seq;

create sequence mtm_option_kaizo_seq;

create sequence mtm_taisyo_seihin_seq;

create sequence mtm_tokusyu_kaizo_seq;

create sequence tyohyou_rireki_seq;




# --- !Downs

drop table if exists msi_kaizo_kanryo cascade;

drop table if exists mst_bumon cascade;

drop table if exists mst_common cascade;

drop table if exists mst_daibunrui cascade;

drop table if exists mst_kaisya cascade;

drop table if exists mst_kataban cascade;

drop table if exists mst_menu cascade;

drop table if exists mst_option_buhin cascade;

drop table if exists mst_shiten cascade;

drop table if exists mst_user cascade;

drop table if exists mtm_head cascade;

drop table if exists mtm_option_kaizo cascade;

drop table if exists mtm_taisyo_seihin cascade;

drop table if exists mtm_tokusyu_kaizo cascade;

drop table if exists tyohyou_rireki cascade;

drop sequence if exists msi_kaizo_kanryo_seq;

drop sequence if exists mst_bumon_seq;

drop sequence if exists mst_common_seq;

drop sequence if exists mst_daibunrui_seq;

drop sequence if exists mst_kaisya_seq;

drop sequence if exists mst_kataban_seq;

drop sequence if exists mst_menu_seq;

drop sequence if exists mst_option_buhin_seq;

drop sequence if exists mst_shiten_seq;

drop sequence if exists mst_user_seq;

drop sequence if exists mtm_head_seq;

drop sequence if exists mtm_option_kaizo_seq;

drop sequence if exists mtm_taisyo_seihin_seq;

drop sequence if exists mtm_tokusyu_kaizo_seq;

drop sequence if exists tyohyou_rireki_seq;

