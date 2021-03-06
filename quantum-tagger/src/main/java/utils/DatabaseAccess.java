package utils;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.collections4.map.HashedMap;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import annotations.Annotation;
import knowledgebase.Candidate;
import knowledgebase.Unit;
import knowledgebase.Units_Measures;
import knowledgebase.WeightsCalculator;
import loader.DocumentLoader;
import data.Table_;
import edu.stanford.nlp.util.Pair;
import resources.Resources;

public class DatabaseAccess {

	/**
	 * Replace all spaces with _, for concepts and categories For entities
	 * coming from AIDA replace all YAGO: with "" For all Categories, add
	 * Category: to the start units should be fine, already assigned wikipedia
	 * id if its there
	 */
	private static Connection connection = null;
	private static DatabaseAccess db_acess = null;
	private static Database db_load = null;
	private int max = 0;
	private int max_mentions_count;
	private int max_mention_candidate_count;

	public int getMax_mention_candidate_count() {
		return max_mention_candidate_count;
	}

	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);

	public int getMaxCount() {
		return max;
	}

	private static final String COLUMN_PAIR_SELECT = "SELECT wiki1.page_title as s1, wiki2.page_title as s2 , count  FROM mst.column_pair_count "
			+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =column1"
			+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column2"
			+ " where (wiki1.page_title in (#LIST1@#) and wiki2.page_title in (#LIST2@#));";
	private static final String ROW_PAIR_SELECT = "SELECT wiki1.page_title as s1, wiki2.page_title as s2 , count  FROM mst.row_pair_count "
			+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =column1"
			+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column2"
			+ " where (wiki1.page_title in (#LIST1@#) and wiki2.page_title in (#LIST2@#));";
	private static final String HEADER_CELL_SELECT = "SELECT wiki1.page_title as s1, wiki2.page_title as s2 , count FROM mst.header_columns "
			+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =header_"
			+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column_"
			+ " where (wiki1.page_title in (#LIST1@#) and wiki2.page_title in (#LIST2@#));";
	private static final String HEADER_HEADER_SELECT = "SELECT wiki1.page_title as s1, wiki2.page_title as s2 , count  FROM mst.header_header "
			+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =header1"
			+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =header2"
			+ " where (wiki1.page_title in (#LIST1@#) and wiki2.page_title in (#LIST2@#));";

	// private static final String MENTION_COUNT_SELECT = "SELECT
	// wiki1.page_title as s1, mention as s2 , count from mst.mentions_count "
	// + " inner join mst.wiki_page as wiki1 on wiki1.page_id =
	// mst.mentions_count.page_id"
	// + " where (wiki1.page_title in (#LIST1@#) and mention in (#LIST2@#)) ;";

	// private static final String GENERAL_REALTEDNESS_ = " select count(*) as
	// count, link1.target_title as s1 , link1.target_id as id1 ,
	// link2.target_title as s2, link2.target_id as id2 "
	// + " from wikitables.table_links_per_table as link1 "
	// + "inner join wikitables.table_links_per_table as link2 "
	// + " on link1.table_id = link2.table_id and link1.page_id = link2.page_id
	// and link1.target_id != link2.target_id "
	// + " where link1.target_title in (#LIST1@#) "
	// + " and link2.target_title in (#LIST2@#) group by s1,s2, id1, id2";

	///////////////////////////// The used ones ///////////////////
	private static final String GENERAL_REALTEDNESS = "SELECT count, s1, id1, s2, id2"
			+ "  FROM wikitables.table_candidate_candidate_unique" + "  where s1 in (#LIST1@#) and s2 in (#LIST1@#);";
	// private static final String GENERAL_REALTEDNESS ="select count(*) as
	// count, first_.target_title as s1 , first_.target_id as id1 ,
	// second_.target_title as s2, second_.target_id as id2"
	// + " from wikitables.table_link_unique_page_target as first_ inner join
	// wikitables.table_link_unique_page_target as second_"
	// + " on first_.table_id = second_.table_id and first_.page_id =
	// second_.page_id and first_.target_id != second_.target_id"
	// + " where first_.target_title in (#LIST1@#) and second_.target_title in
	// (#LIST2@#) group by s1,s2, id1, id2";
	private static final String HEADER_MENTION_SELECT = " select \"header\" as mention1, mention as mention2 , count "
			+ " from wikitables.mentions_header_cell_count where \"header\" in(#LIST1@#) and mention in (#LIST2@#);";
	// private static final String SAME_COLUMN_MENTION_SELECT = " select
	// first_.surface_form as mention1 , second_.surface_form as mention2,
	// count(*) as count "
	// + " from wikitables.table_link_unique_page_target as first_ inner join
	// wikitables.table_link_unique_page_target as second_"
	// + " on first_.table_id = second_.table_id"
	// + " and first_.page_id = second_.page_id"
	// + " and first_.column = second_.column"
	// + " and first_.row <> second_.row"
	// + " where first_.surface_form in (#LIST1@#) and second_.surface_form in
	// (#LIST2@#)"
	// + " group by first_.surface_form ,second_.surface_form;";

	// private static final String SAME_ROW_MENTION_SELECT = " select
	// first_.surface_form as mention1 , second_.surface_form as mention2,
	// count(*) as count"
	// + " from wikitables.table_link_unique_page_target as first_ inner join
	// wikitables.table_link_unique_page_target as second_"
	// + " on first_.table_id = second_.table_id"
	// + " and first_.page_id = second_.page_id"
	// + " and first_.row = second_.row and first_.column <> second_.column"
	// + " where first_.surface_form in (#LIST1@#) and second_.surface_form in
	// (#LIST1@#)"
	// + " group by first_.surface_form ,second_.surface_form;";
	//
	private static final String SAME_COLUMN_MENTION_SELECT = "select mention1, mention2, count "
			+ "from wikitables.table_mention_mention_column where mention1 in (#LIST1@#)  and mention2 in (#LIST1@#); ";
	private static final String SAME_ROW_MENTION_SELECT = "select mention1, mention2, count "
			+ "from wikitables.table_mention_mention_row where mention1 in (#LIST1@#)  and mention2 in (#LIST1@#) ";
	private static final String MENTION_CANDIDATES_SELECT = "select surface_form as mention, target_title as candidate, count "
			+ " from wikitables.surfaceform_target_count " + " where surface_form in (#LIST1@#);";

	private static final String MENTION_CANDIDATES_SELECT_2 = "select surface_form as mention, target_title as candidate, count "
			+ " from wikitables.surfaceform_target_count "
			+ "where surface_form in (#LIST1@#) and candidate in (#LIST2@#);";

	private DatabaseAccess() throws SQLException {
		db_load = new Database();
	}

	public static DatabaseAccess getDatabaseAccess() throws SQLException {
		if (db_acess == null) {
			db_acess = new DatabaseAccess();
		}
		return db_acess;
	}

	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	private Connection getConnection() throws SQLException {
		Connection connection = null;
		try {
			InitialContext cxt = new InitialContext();
			DataSource ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/postgres");
			if (ds != null)
				connection = ds.getConnection();
			else {
				connection = DriverManager.getConnection(Resources.getResources().getDb_connectin_url(),
						Resources.getResources().getDb_user(), Resources.getResources().getDb_password());
				
			}

		} catch (NamingException exc) {
			connection = DriverManager.getConnection(Resources.getResources().getDb_connectin_url(),
					Resources.getResources().getDb_user(), Resources.getResources().getDb_password());
			
		}
		return connection;

	}
	public int getRowPairCount(String cand1, String cand2) {
		if (db_load != null) {
			return db_load.getRowPairCount(cand1, cand2);
		}
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = "SELECT count  FROM mst.row_pair_count "
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =column1"
					+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column2" + " where (wiki1.page_title=E'"
					+ cand1 + "' and wiki2.page_title =E'" + cand2 + "')" + " OR   (wiki2.page_title=E'" + cand1
					+ "' and wiki1.page_title =E'" + cand2 + "');";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return count;
	}

	public int getColumnPairCount(String cand1, String cand2) {
		if (db_load != null) {
			return db_load.getColumnPairCount(cand1, cand2);
		}
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = "SELECT count  FROM mst.column_pair_count "
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =column1"
					+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column2" + " where (wiki1.page_title=E'"
					+ cand1 + "' and wiki2.page_title =E'" + cand2 + "')" + " OR   (wiki2.page_title=E'" + cand1
					+ "' and wiki1.page_title =E'" + cand2 + "');";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return count;
	}

	public int getHeaderCellCount(String cand1, String cand2) {
		if (db_load != null) {
			return db_load.getHeaderCellCount(cand1, cand2);
		}
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			connection =  getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = "SELECT count  FROM mst.header_columns "
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =header_"
					+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =column_" + " where (wiki1.page_title=E'"
					+ cand1 + "' and wiki2.page_title =E'" + cand2 + "')" + " OR   (wiki2.page_title=E'" + cand1
					+ "' and wiki1.page_title =E'" + cand2 + "');";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				count = rs.getInt("count");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return count;
	}

	public int getGeneralRelatednessCount(String cand1, String cand2) {
		int count = 0;
		if (db_load != null) {
			count = db_load.getGenralRelatedness(cand1, cand2);
		} else {
			Statement stmt = null;
			ResultSet rs = null;
			count = 0;
			try {
				connection = getConnection();
				connection.setAutoCommit(false);
				stmt = connection.createStatement();
				stmt.setFetchSize(10);
				String sql = "select count(*) as count, link1.target_title as s1 , link1.target_id as id1 , link2.target_title as s2, link2.target_id as id2 "
						+ " from wikitables.table_links_per_table as link1 "
						+ "inner join wikitables.table_links_per_table as link2    "
						+ " on link1.table_id = link2.table_id and link1.page_id = link2.page_id  and link1.target_id != link2.target_id "
						+ "   where (link1.target_title = E'" + cand1 + "' and link2.target_title = E'" + cand2 + "')"
						+ " OR ( link1.target_title = E'" + cand2 + "' and link2.target_title = E'" + cand1 + "')"
						+ " group by s1,s2, id1, id2";
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					count = rs.getInt("count");
				}
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (Exception e) { /* ignored */
				}
				try {
					stmt.close();
				} catch (Exception e) { /* ignored */
				}
				try {
					connection.close();
				} catch (Exception e) { /* ignored */
				}
			}
		}
		return count;
	}

	public int getMentionCount(String mention, String cand) {
		if (db_load != null) {
			return db_load.getMentionCount(mention, cand);
		}
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = "SELECT count from mst.mentions_count "
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id = mst.mentions_count.page_id"
					+ " where (wiki1.page_title=E'" + cand + "' and mention= E'" + mention + "') ;";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return count;
	}

	public int getHeaderHeaderCount(String cand1, String cand2) {
		if (db_load != null) {
			return db_load.getHeaderHeaderCount(cand1, cand2);
		}
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = "SELECT count  FROM mst.header_header "
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id =header1"
					+ " inner join  mst.wiki_page as wiki2 on  wiki2.page_id =header2" + " where (wiki1.page_title=E'"
					+ cand1 + "' and wiki2.page_title =E'" + cand2 + "')" + " OR   (wiki2.page_title=E'" + cand1
					+ "' and wiki1.page_title =E'" + cand2 + "');";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return count;
	}

	public double[] getHeaderValues(String cand) {
		double[] values = new double[2];
		Statement stmt = null;
		ResultSet rs = null;

		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(10);
			String sql = " SELECT min , max  from mst.header_value"
					+ " inner join mst.wiki_page as wiki1 on  wiki1.page_id = header_"
					+ " where (wiki1.page_title in(E'" + cand + "')) ;";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				values[0] = rs.getInt("min");
				values[1] = rs.getInt("max");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return values;
	}

	public void loadDBFor(Table_ table, Multimap<String, Candidate> all_candidates, boolean general) {

		String db_id;
		StringBuilder cand_list = new StringBuilder();
		Set<String> memory = new HashSet<String>();

		for (Entry<String, Candidate> entry : all_candidates.entries()) {
			// prepare candidates
			Candidate cand = entry.getValue();
			db_id = WeightsCalculator.getID(cand);
			if (!memory.contains(db_id)) {
				cand_list.append("E'" + db_id + "',");
				memory.add(db_id);
			}

		}

		// mentions list
		memory.clear();
		StringBuilder mentions_list = new StringBuilder();
		Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> annotations_map = table.getInverted_annotations();
		Set<Pair<String, Annotation>> cell_annotations;
		if (annotations_map != null) {
			for (int i = 0; i < table.getNrow(); i++) {
				for (int j = 0; j < table.getNcol(); j++) {
					cell_annotations = (Set<Pair<String, Annotation>>) annotations_map
							.get(new Pair<Integer, Integer>(i, j));
					for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
						if (!memory.contains(cell_annotaion.first)) {
							mentions_list.append("E'" + cell_annotaion.first.replace("'", "''") + "',");
							memory.add(cell_annotaion.first);
						}
					}

				}
			}
		}
		memory.clear();

		if (cand_list.length() <= 0 || mentions_list.length() <= 0)
			return;
		// add only for the evaluator
		loadCandidatesFor(mentions_list);
		String list1 = cand_list.substring(0, cand_list.length() - 1);
		cand_list = null;
		String list2 = mentions_list.substring(0, mentions_list.length() - 1);
		mentions_list = null;
		Map<String, Integer> colum_pairs_map = null;
		Map<String, Integer> row_pairs_map = null;
		Map<String, Integer> header_cell_map = null;
		Map<String, Integer> header_header_map = null;
		Map<String, Integer> mention_count_map = null;
		Map<String, Integer> general_relatedness = null;
		if (general) {
			general_relatedness = loadFromQuery(GENERAL_REALTEDNESS, list1, list1);
		} else {
			colum_pairs_map = loadFromQuery(COLUMN_PAIR_SELECT, list1, list1);
			row_pairs_map = loadFromQuery(ROW_PAIR_SELECT, list1, list1);
			header_cell_map = loadFromQuery(HEADER_CELL_SELECT, list1, list1);
			header_header_map = loadFromQuery(HEADER_HEADER_SELECT, list1, list1);
			mention_count_map = loadFromQuery(MENTION_CANDIDATES_SELECT_2, list1, list2);
		}
		db_load.init(colum_pairs_map, row_pairs_map, header_cell_map, header_header_map, mention_count_map,
				general_relatedness, max);

		Map<Pair<String, String>, Integer> mention_mention_count;

		mention_mention_count = loadMentionsCountFromQuery(HEADER_MENTION_SELECT, list2);
		db_load.setHeader_mention_count(mention_mention_count);
		db_load.setMax_header_mention_count(max_mentions_count);

		mention_mention_count = loadMentionsCountFromQuery(SAME_COLUMN_MENTION_SELECT, list2);
		db_load.setMention_mention_column_count(mention_mention_count);
		db_load.setMax_mention_mention_column_count(max_mentions_count);

		mention_mention_count = loadMentionsCountFromQuery(SAME_ROW_MENTION_SELECT, list2);
		db_load.setMention_mention_row_count(mention_mention_count);
		db_load.setMax_mention_mention_row_count(max_mentions_count);

	}

	private Map<Pair<String, String>, Integer> loadMentionsCountFromQuery(String query, String list) {
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		String s1, s2;
		max_mentions_count = 0;
		Pair<String, String> key = null;
		Map<Pair<String, String>, Integer> result = new HashMap<Pair<String, String>, Integer>();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = query.replace("#LIST1@#", list).replace("#LIST2@#", list);
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
				s1 = rs.getString("mention1");
				s2 = rs.getString("mention2");
				key = new Pair<String, String>(s2, s1);
				if (result.containsKey(key)) {// this as in the database the
												// stats are collected for
												// row>row2,
					// thus it differes by which mention appears first
					count = result.get(key) + count;
					result.put(key, count);// readd the new count
				} else {
					key = new Pair<String, String>(s1, s2);
					result.put(key, count);
				}
				if (count > max_mentions_count) {
					max_mentions_count = count;
				}
				// slogger_.info("count: " + count +"s1: " + s1 +"s2: "
				// +s2);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return result;
	}

	public void test() {
		String list = "U&'Anarchism', U&'John_Wyndham', U&'George_Balanchine', U&'La_Valse',U&'000_Bon_Jovi_Fans_Can''t_Be_Wrong'"
				+ ",U&'Kilometre,50510,1,000',U&'A',U&'D',U&'Carmine Coppola', U&'Carmine_Coppola'";
		slogger_.info("COLUMN_PAIR_SELECT");
		loadFromQuery(COLUMN_PAIR_SELECT, list, list);
		slogger_.info("ROW_PAIR_SELECT");
		loadFromQuery(ROW_PAIR_SELECT, list, list);
		slogger_.info("HEADER_CELL_SELECT");
		loadFromQuery(HEADER_CELL_SELECT, list, list);
		slogger_.info("HEADER_HEADER_SELECT");
		loadFromQuery(HEADER_HEADER_SELECT, list, list);
		slogger_.info("MENTION_COUNT_SELECT");
		loadFromQuery(MENTION_CANDIDATES_SELECT_2, list, list);
	}

	private Map<String, Integer> loadFromQuery(String query, String list1, String list2) {
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		String s1, s2;
		max = 0;
		Map<String, Integer> result = new HashedMap<String, Integer>();
		try {
			connection = getConnection();
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = query.replace("#LIST1@#", list1).replace("#LIST2@#", list2);
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
				if (count > max) {
					max = count;
				}
				s1 = rs.getString("s1");
				s2 = rs.getString("s2");
				// TODO
				result.put(s1 + s2, count);
				// slogger_.info("count: " + count +"s1: " + s1 +"s2: "
				// +s2);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return result;
	}

	/*
	 * private Map<String, Set<String>> load_abbreviations() { Statement stmt =
	 * null; ResultSet rs = null; String unit_key, abbreviation; Set<String>
	 * units; Map<String, Set<String>> units_abbreviations = new HashMap<String,
	 * Set<String>>(); try { connection =
	 * DriverManager.getConnection(Resources.getResources()
	 * .getDb_connectin_url(), Resources.getResources() .getDb_user(),
	 * Resources.getResources().getDb_password());
	 * connection.setAutoCommit(false); stmt = connection.createStatement();
	 * stmt.setFetchSize(1000000); String sql =
	 * " select mst.abbreviation.name as abbreviation, mst.unit.key as unit_key"
	 * +
	 * "  from mst.abbreviation inner join mst.unit_abbreviation on mst.unit_abbreviation.abbreviation_id = mst.abbreviation.id"
	 * + "  inner join mst.unit on unit.id = mst.unit_abbreviation.unit_id"; rs
	 * = stmt.executeQuery(sql);
	 * 
	 * while (rs.next()) { abbreviation =
	 * rs.getString("abbreviation").toLowerCase(); unit_key =
	 * rs.getString("unit_key"); if(unit_key.isEmpty()) continue; if
	 * (units_abbreviations.containsKey(abbreviation)) {
	 * units_abbreviations.get(abbreviation).add(unit_key); } else { units = new
	 * LinkedHashSet<String>(); units.add(unit_key);
	 * units_abbreviations.put(abbreviation, units); } }
	 * connection.setAutoCommit(true); } catch (SQLException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } finally { try {
	 * rs.close(); } catch (Exception e) { ignored } try { stmt.close(); } catch
	 * (Exception e) { ignored } try { connection.close(); } catch (Exception e)
	 * { ignored } } return units_abbreviations; }
	 */
	private Multimap<String, String> load_aliases() {
		Statement stmt = null;
		ResultSet rs = null;
		String unit_key, alias;

		Multimap<String, String> units_aliases = HashMultimap.create();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = " select lower(alias.name) as alias, unit.key as unit_key"
					+ "  from qkb.alias inner join qkb.unit_alias on unit_alias.alias_id = alias.id"
					+ "  inner join qkb.unit on unit.id = qkb.unit_alias.unit_id union "
					+ " select lower(name) as alias, key from qkb.unit;";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				unit_key = rs.getString("unit_key");
				alias = rs.getString("alias").replace('_', ' '); // .toLowerCase()
				if (unit_key.isEmpty())
					continue;
				units_aliases.put(alias, unit_key);
				
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return units_aliases;
	}

	public void load_units() {
		Statement stmt = null;
		ResultSet rs = null;
		String name, key, wiki_title, class_, dimension, dimension_wiki_title;
		// Map<String, Set<String>> units_abbreviations = load_abbreviations();
		Multimap<String, String> units_aliases = load_aliases();
		Unit unit = null;
		Units_Measures units = Units_Measures.getUnits_Measures();
		// units.setAbbreviations(units_abbreviations);
		units.setAliases(units_aliases);
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = " select unit.name as name, unit.wikipedia_title as wiki_title, qkb.unit.key, "
					+ " qkb.class.name as \"class\", qkb.dimension.name as dimension, qkb.dimension.wiki_page_title as dimension_wiki_title "
					+ " from qkb.class inner join qkb.dimension on  qkb.dimension.class = qkb.class.id  "
					+ " full join qkb.unit on qkb.unit.dimension = qkb.dimension.freebase_id;";
			// check null values here for unit and measure
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				name = rs.getString("name");
				key = rs.getString("key");
				class_ = rs.getString("class");
				wiki_title = rs.getString("wiki_title");
				dimension = rs.getString("dimension");
				dimension_wiki_title = rs.getString("dimension_wiki_title");
				unit = new Unit(name, key, wiki_title, class_, dimension, dimension_wiki_title);
				units.addUnit(unit);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return;
	}

	public Map<String, String> load_statistical_modifiers() {
		Statement stmt = null;
		ResultSet rs = null;
		String modifier, alias;
		Map<String, String> modifiers = new HashedMap<String, String>();
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = "select mst.alias.name as alias,  mst.statistical_modifier.name as modifier from mst.statistical_modifier inner join mst.statistical_modifier_alias on mst.statistical_modifier.id = statistical_modifier_id "
					+ " inner join mst.alias on alias_id = mst.alias.id "
					+ " order by character_length(mst.alias.name) desc";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				alias = rs.getString("alias");
				modifier = rs.getString("modifier");
				modifiers.put(alias, modifier);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		units_measures.setStatisticalModifiers(modifiers);
		return modifiers;
	}

	public Map<String, Pair<String, String>> load_dimensions() {
		Statement stmt = null;
		ResultSet rs = null;
		String dimension, wiki_title, alias;
		Map<String, Pair<String, String>> dimensions = new HashedMap<String, Pair<String, String>>();
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = "select qkb.dimension.name as dimension , qkb.dimension.wiki_page_title as wiki_page, lower(qkb.alias.name) as alias"
					+ " from qkb.dimension inner join qkb.dimension_alias on  qkb.dimension.id = qkb.dimension_alias.dimension_id"
					+ " inner join qkb.alias on qkb.alias.id = qkb.dimension_alias.alias_id union "
					+ " select qkb.dimension.name as dimension , qkb.dimension.wiki_page_title as wiki_page, lower(qkb.dimension.name) as alias "
					+ " from qkb.dimension";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				dimension = rs.getString("dimension");
				wiki_title = rs.getString("wiki_page");
				alias = rs.getString("alias");
				dimensions.put(alias, new Pair<String, String>(dimension, wiki_title));
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		units_measures.setDimensions(dimensions);
		return dimensions;
	}

	public Map<String, String> load_classes() {
		Statement stmt = null;
		ResultSet rs = null;
		String class_, alias;
		Map<String, String> classes = new HashedMap<String, String>();
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = "select qkb.class.name as class, lower(qkb.alias.name ) as alias "
					+ " from qkb.class inner join qkb.class_alias on  qkb.class.id = qkb.class_alias.class_id "
					+ " inner join qkb.alias on qkb.alias.id = qkb.class_alias.alias_id "
					+ " union select qkb.class.name as class, lower(qkb.class.name) as alias from qkb.class;";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				class_ = rs.getString("class");
				alias = rs.getString("alias");
				classes.put(alias, class_);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		units_measures.setClasses(classes);
		return classes;
	}

	public Set<String> load_dimensionless_classes() {
		String query = "Select name from qkb.class where id not in (select class from qkb.dimension);";
		Set<String> dimensionless_classes = new HashSet<String>();
		Statement stmt = null;
		ResultSet rs = null;
		String class_name;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				class_name = rs.getString("name");
				dimensionless_classes.add(class_name);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		Units_Measures.setDimensionless_classes(dimensionless_classes);

		return dimensionless_classes;
	}

	// public Map<String, String> load_dimensionless_measures() {
	// Statement stmt = null;
	// ResultSet rs = null;
	// String measure, alias;
	// Map<String, String> dimensionsless_measures = new HashedMap<String,
	// String>();
	// Units_Measures units_measures = Units_Measures.getUnits_Measures();
	//
	// try {
	// connection = DriverManager.getConnection(Resources.getResources()
	// .getDb_connectin_url(), Resources.getResources()
	// .getDb_user(), Resources.getResources().getDb_password());
	// connection.setAutoCommit(false);
	// stmt = connection.createStatement();
	// stmt.setFetchSize(1000000);
	// String sql = "select mst.dimensionless_measures.name as
	// dimenstionless_measure , mst.alias.name as alias "
	// + " from mst.dimensionless_measures inner join
	// mst.dimensionless_measures_alias on mst.dimensionless_measures.id =
	// mst.dimensionless_measures_alias.dimensionless_measure_id"
	// + " inner join mst.alias on mst.alias.id =
	// mst.dimensionless_measures_alias.alias_id";
	// rs = stmt.executeQuery(sql);
	//
	// while (rs.next()) {
	// measure = rs.getString("dimenstionless_measure");
	// alias = rs.getString("alias");
	// dimensionsless_measures.put(alias, measure);
	// }
	// connection.setAutoCommit(true);
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } finally {
	// try {
	// rs.close();
	// } catch (Exception e) { /* ignored */
	// }
	// try {
	// stmt.close();
	// } catch (Exception e) { /* ignored */
	// }
	// try {
	// connection.close();
	// } catch (Exception e) { /* ignored */
	// }
	// }
	// units_measures.set_dimensionless_measures(dimensionsless_measures);
	// return dimensionsless_measures;
	// }

	public void load_units_mentions() {
		Statement stmt = null;
		ResultSet rs = null;
		String unit_key, mention;
		int count, max = 0;
		Map<Pair<String, String>, Integer> mention_unit_count = new HashedMap<Pair<String, String>, Integer>();

		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = " select surface_form as mention, target_title as candidate, target_id as candidate_id, count, key  "
					+ " from qkb.unit inner join wikitables.surfaceform_target_count on target_id = wikipedia_id ;";
			// "SELECT distinct surface_form, count, key"
			// + " FROM qkb.unit inner join wikitables.mention_count on
			// target_id = wikipedia_id";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				mention = rs.getString("mention");
				count = rs.getInt("count");
				if (count > max)
					max = count;
				unit_key = rs.getString("key");
				mention_unit_count.put(new Pair<String, String>(mention, unit_key), count);
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		db_load.setMentionUnitCount(mention_unit_count);
		db_load.setMaxMentionUnitCount(max);
		return;
	}

	public int getCountUnitMention(String mention, String unit_key) {

		return db_load.getCountUnitMention(mention, unit_key);
	}

	public int getMaxMentionUnitCount() {
		return db_load.getMaxMentionUnitCount();

	}

	public void copyToDB(String table_name, StringBuilder sb, int nvalues) {
		if (sb.length() <= 0)
			return;
		CopyManager cpManager;
		PushbackReader reader = null;
		try {
			connection = getConnection();
			cpManager = ((PGConnection) connection).getCopyAPI();
			reader = new PushbackReader(new StringReader(sb.toString()), nvalues);
			cpManager.copyIn("COPY " + table_name + " FROM STDIN WITH CSV", reader);
		} catch (IOException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
	}

	public void insertDocumentData(String document_id, String document_title, String url, String content) {
		/*
		 * 
		 */
		PreparedStatement pstmt = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			pstmt = connection.prepareStatement(
					"INSERT INTO evaluation.document_data(document_id, title, url, content)" + " VALUES (?, ?, ?, ?);");
			pstmt.setString(1, document_id);
			pstmt.setString(2, document_title);
			pstmt.setString(3, url);
			pstmt.setString(4, content);
			pstmt.executeUpdate();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return;

	}

	public void insertTableData(int id, String document_id, int nrows, int ncolumns, String content) {
		/*
		 * 
		 */
		PreparedStatement pstmt = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			pstmt = connection.prepareStatement(
					"INSERT INTO evaluation.table_data(table_id, ncolumns, nrows, document_id, content)"
							+ "  VALUES (?, ?, ?, ?, ?);");
			pstmt.setInt(1, id);
			pstmt.setInt(2, ncolumns);
			pstmt.setInt(3, nrows);
			pstmt.setString(4, document_id.trim());
			pstmt.setString(5, content);
			pstmt.executeUpdate();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return;

	}

	public void insertTableHTMLResults(int id, String document_id, String experiement_id, String html_content) {
		PreparedStatement pstmt = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			pstmt = connection.prepareStatement(
					"INSERT INTO evaluation.table_html_results(document_id, table_id, experiement_id, html_content)"
							+ "    VALUES (?, ?, ?, ?);");
			pstmt.setString(1, document_id);
			pstmt.setInt(2, id);
			pstmt.setString(3, experiement_id);
			pstmt.setString(4, html_content);
			pstmt.executeUpdate();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return;

	}

	public void insertDocumentHTMLResults(String document_id, String experiement_id, String html_content) {
		PreparedStatement pstmt = null;
		try {
			connection= getConnection();
			connection.setAutoCommit(false);
			pstmt = connection.prepareStatement(
					"INSERT INTO evaluation.document_html_results(document_id, experiement_id, html_content) "
							+ "VALUES (?, ?, ?);");
			pstmt.setString(1, document_id);
			pstmt.setString(2, experiement_id);
			pstmt.setString(3, html_content);
			pstmt.executeUpdate();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return;

	}

	public int getSameColumnMentionCount(String mention1, String mention2) {
		if (db_load != null) {
			return db_load.getSameColumnMentionCount(mention1, mention2);
		} else
			return 0;
	}

	public int getSameRowMentionsCount(String mention1, String mention2) {
		if (db_load != null) {
			return db_load.getSameRowMentionCount(mention1, mention2);
		} else
			return 0;
	}

	public double getSameRowMentionsMaxCount() {
		if (db_load != null) {
			return db_load.getMax_mention_mention_row_count();
		} else
			return 0;
	}

	public double getSameColumnMentionsMaxCount() {
		if (db_load != null) {
			return db_load.getMax_mention_mention_column_count();
		} else
			return 0;
	}

	public int getHeaderMentionCount(String header, String mention) {
		if (db_load != null) {
			return db_load.getHeaderMentionCount(header, mention);
		} else
			return 0;
	}

	public double getHeaderMentionMaxCount() {
		if (db_load != null) {
			return db_load.getMax_header_mention_count();
		} else
			return 0;
	}

	public void loadCandidatesFor(StringBuilder mentions) {
		Map<String, List<Pair<String, Integer>>> candidates = new HashMap<String, List<Pair<String, Integer>>>();
		candidates = loadCandsforMentions(MENTION_CANDIDATES_SELECT, mentions);
		db_load.setMention_candidates(candidates);
		db_load.setMax_mention_candidate_count(max_mention_candidate_count);

	}

	public List<Pair<String, Integer>> getCandidatesForMention(String mention) {
		if (db_load != null) {
			return db_load.getCandidatesForMention(mention);
		} else
			return null;
	}

	private Map<String, List<Pair<String, Integer>>> loadCandsforMentions(String query, StringBuilder mentions) {
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		String mention, candidate;
		max_mention_candidate_count = 0;
		List<Pair<String, Integer>> candidates = null;
		Map<String, List<Pair<String, Integer>>> result = new HashMap<String, List<Pair<String, Integer>>>();
		if (mentions.length() == 0)
			return result;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = query.replace("#LIST1@#", mentions.substring(0, mentions.length() - 1));
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				count = rs.getInt("count");
				if (count > max_mention_candidate_count) {
					max_mention_candidate_count = count;
				}
				mention = rs.getString("mention");
				candidate = rs.getString("candidate");
				if (candidate == null || mention == null)
					continue;
				if (result.containsKey(mention)) {
					result.get(mention).add(new Pair<String, Integer>(candidate, count));
				} else {
					candidates = new LinkedList<Pair<String, Integer>>();
					candidates.add(new Pair<String, Integer>(candidate, count));
					result.put(mention, candidates);
				}

			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return result;
	}

	public Map<String, Map<String, String>> load_term_expansion() {
		Statement stmt = null;
		ResultSet rs = null;
		String domain, term, expansion;
		Map<String, String> term_expansion;
		Map<String, Map<String, String>> domain_terms_expantion = new HashMap<String, Map<String, String>>();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			String sql = " SELECT term, expansion, domain" + " FROM qkb.terms;";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				domain = rs.getString("domain");
				term = rs.getString("term");
				expansion = rs.getString("expansion");
				if (domain_terms_expantion.containsKey(domain)) {
					domain_terms_expantion.get(domain).put(term, expansion);
				} else {
					term_expansion = new HashMap<String, String>();
					term_expansion.put(term, expansion);
					domain_terms_expantion.put(domain, term_expansion);
				}
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		Units_Measures.getUnits_Measures().setDomain_terms_expansion(domain_terms_expantion);
		return domain_terms_expantion;

	}

}
