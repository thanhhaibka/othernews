package vcc.optimization.othernews;

import java.util.HashMap;

public class Name {
	public static String table = "user_log";
	public static String keyspace = "retargetlog";
	public static int portConn = 9042;
	public static String node = "192.168.23.60";
	public static String hostName = "192.168.3.67";
	public static String lastTime = "2016-05-24";
	public static String dbName = "news";
	public static String userName = "chienpq";
//	public static String password = "7qCN7fWp7eUJB4j";
	public static String password = "JnRKay6GpO1ftnf";
	public static String domain_soha = "soha.vn";
	public static String domain_kenh14 = "kenh14.vn";
	public static String domain_afamily = "afamily.vn";
	public static String domain_gamek = "gamek.vn";
	
	public static String[] topics = {"hocduong","dulich","game","cudanmang","kinhdoanh","cuocsong","tvshow","quansu","anninh","doisong","congnghe","khampha","suckhoe","sanphamcongnghe","batdongsan","giaitri","xevacongnghe","anngonkheotay","quocte","tamsu","thethao","chungkhoan","phim","xahoi","star","esport","thoitrang","songkhoe","phapluat","handmade"};
	public static String SqlTop200Soha = "SELECT  `newsId`,`publishDate` FROM `news`.`news_resource`WHERE `sourceNews`= 'Soha' AND `publishDate` >= ? AND `publishDate` <= ?  ORDER BY newsid;";
//	public static double similar[] = {0.0,8961.04386207963,0.0,637.0577224194702,0.0,40057.32218685362,0.0,0.0,477.52100959462854,254.61163985024905,1303.6012558717966,1099.2266078574144,1790.4826504114285,0.0,1214.1557725641308,0.0,0.0,3202.831945795826,0.0,0.0,0.0,0.0,2553.954237036942,0.0};
	public static double similar[] = {0.0,7082.43296990882,0.0,480.19711387301413,0.0,31254.185471342174,0.0,0.0,388.1646738426825,196.1882224564908,996.0613197950767,805.462547855518,1362.440002589284,0.0,918.9384397714032,0.0,0.0,2398.6699356377962,0.0,0.0,0.0,0.0,1993.835492611293,0.0};
	
	public static String query = "SELECT A.`newsId`, IF(B.`parent_id` =0, B.`id`, B.`parent_id`) catid,A.`insertDate` FROM `news_resource` A INNER JOIN `category` B ON A.`catId` = B.`id` WHERE A.`publishDate` between ? AND ? AND A.`sourceNews` = 'Soha' ;";
	public static String table_raw = "cat_raw";
	public static String table_cat = "cat";
	
	public static String table_gamek_raw = "gamek_cat_raw";
	public static String table_rows[] = {"congnghe","cudanmang","danong360","doisong","fun","giaitri","hinhsu","infographic","khampha","kinhdoanh","phapluat","quansu","quocte","seagames27","songkhoe","tetnguyendan","thegioidoday","thethao","truyen","tuvanphapluat","vanhoa","worldcup2014","xahoi","yeu"};
	public static HashMap<String,Integer> hmCatFrequence;
	public static String alter_table = "ALTER TABLE addamsFamily ADD gravesite varchar;";
	public static String query_select_cat_to_alter = "SELECT  `id`,  `href` FROM `news`.`category` WHERE `parent_id` = 0 AND domain = 'soha.vn';";
	public static String query_getCatid_Gamek = "SELECT  `catId`,  `sourceNews`FROM `news`.`news_resource` WHERE sourceNews = 'gamek' GROUP BY catId ORDER BY catId;";
	public static String query_creat_table = "CREATE TABLE IF NOT EXISTS cat_raw(guid bigint,date_insert timestamp,domain varchar,PRIMARY KEY(guid)) WITH caching = {'keys': 'ALL', 'rows_per_partition': 'ALL'};";
	public static String date = "2016-03-07";
	public static String query_getDataToTrain = "SELECT  `newsId`,  `title`,  `sapo`,`content` FROM  `news`.`news_resource`  LIMIT 0, 150000 ;";
	public static String query_getContentFromNewsID = "SELECT  `newsId`,  `title`,  `sapo`,`content` FROM  `news`.`news_resource` WHERE newsId = ";
	
	public static String[] file = new String[] { "parquet_logfile_at_00h_00.snap", "parquet_logfile_at_00h_30.snap",
			"parquet_logfile_at_01h_00.snap", "parquet_logfile_at_01h_30.snap", "parquet_logfile_at_02h_00.snap",
			"parquet_logfile_at_02h_30.snap", "parquet_logfile_at_03h_00.snap", "parquet_logfile_at_03h_30.snap",
			"parquet_logfile_at_04h_00.snap", "parquet_logfile_at_04h_30.snap", "parquet_logfile_at_05h_00.snap",
			"parquet_logfile_at_05h_30.snap", "parquet_logfile_at_06h_00.snap", "parquet_logfile_at_06h_30.snap",
			"parquet_logfile_at_07h_00.snap", "parquet_logfile_at_07h_30.snap", "parquet_logfile_at_08h_00.snap",
			"parquet_logfile_at_08h_30.snap", "parquet_logfile_at_09h_00.snap", "parquet_logfile_at_09h_30.snap",
			"parquet_logfile_at_10h_00.snap", "parquet_logfile_at_10h_30.snap", "parquet_logfile_at_11h_00.snap",
			"parquet_logfile_at_11h_30.snap", "parquet_logfile_at_12h_00.snap", "parquet_logfile_at_12h_30.snap",
			"parquet_logfile_at_13h_00.snap", "parquet_logfile_at_13h_30.snap", "parquet_logfile_at_14h_00.snap",
			"parquet_logfile_at_14h_30.snap", "parquet_logfile_at_15h_00.snap", "parquet_logfile_at_15h_30.snap",
			"parquet_logfile_at_16h_00.snap", "parquet_logfile_at_16h_30.snap", "parquet_logfile_at_17h_00.snap",
			"parquet_logfile_at_17h_30.snap", "parquet_logfile_at_18h_00.snap", "parquet_logfile_at_18h_30.snap",
			"parquet_logfile_at_19h_00.snap", "parquet_logfile_at_19h_30.snap", "parquet_logfile_at_20h_00.snap",
			"parquet_logfile_at_20h_30.snap", "parquet_logfile_at_21h_00.snap", "parquet_logfile_at_21h_30.snap",
			"parquet_logfile_at_22h_00.snap", "parquet_logfile_at_22h_30.snap", "parquet_logfile_at_23h_00.snap",
			"parquet_logfile_at_23h_30.snap" };
}