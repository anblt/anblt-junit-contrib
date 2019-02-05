package anblt.junit;
import static java.lang.System.err;
import static java.lang.System.out;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import anblt.core.Utils;
import anblt.junit.rules.HSQLDBSetup;
import anblt.junit.rules.TestLogger;
public class HSQLDBTest {
	private static String DBURL = null;
	static {
			DBURL = "jdbc:hsqldb:file:/tmp/tempdb/tempdb;shutdown=true;hsqldb.write_delay=false";
	}
	@ClassRule
	public static final HSQLDBSetup server = new HSQLDBSetup(DBURL, null, null);
	@ClassRule
	public static final TestLogger logger = new TestLogger();
	private static Logger log;
	@ClassRule
	public static final ExpectedException exception = ExpectedException.none();
	@BeforeClass
	public static void setupClass() {
		log = logger.getLog();
	}
	@Test
	public void testCreateTable() throws Exception {
		Connection conn = server.getConnection();
		boolean autoCommitDefault = conn.getAutoCommit();
		try (Statement stmt = conn.createStatement()) {
			log.info("Creating table:Product ...");
			stmt.execute("DROP TABLE Product IF EXISTS");
			stmt.execute("CREATE TABLE Product(ID INTEGER PRIMARY KEY,Name VARCHAR(20),Price DECIMAL(10,2))");
			log.info("Creating table:Product ... done");
			log.info("Populating table:Product ...");
			stmt.execute("INSERT INTO PRODUCT VALUES(0,'Iron Iron',54.00)");
			stmt.execute("INSERT INTO PRODUCT VALUES(1,'Chair Iron',178.00)");
			stmt.execute("INSERT INTO PRODUCT VALUES(2,'Chair Iron',84.00)");
			stmt.execute("INSERT INTO PRODUCT VALUES(3,'Clock Ice Tea',72.00)");
			stmt.execute("INSERT INTO PRODUCT VALUES(4,'Clock Telephone',216.00)");
			log.info("Populating table:Product ... done");
			conn.commit();
		} catch (Throwable e) {
			try {
				conn.rollback();
			} catch (Throwable f) {
				err.println("Could not rollback transaction");
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(autoCommitDefault);
			} catch (Throwable e) {
				err.println("Could not restore AutoCommit setting");
			}
		}
	}
	@Test
	public void testSelect() throws Exception {
		Connection conn = server.getConnection();
		try (PreparedStatement ps = conn
				.prepareStatement("SELECT p.* FROM Product p WHERE p.price >= ?")) {
			ps.setInt(1, 80);
			log.debug(String.format("[HSQLDBTest.testSelect] %s", ps.toString()));
			try (ResultSet rs = ps.executeQuery()) {
				List<String> header = Utils.resultset2headerlines(rs);
				List<String> body = Utils.resultset2bodylines(rs);
				assertThat(""+body.size(), equalTo("3"));
				List<String> lines = new ArrayList<>();
				lines.addAll(header);
				lines.addAll(body);
				out.println(Utils.join("\n", lines));
			} 
		}
	}
}