package junit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import junit.rules.JettyServerSetup;
import junit.rules.TestLogger;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import core.Utils;
public class JettyServerTest {
	static final String RESOURCE_BASE = "/tmp/webapp";
	static final Integer PORT = 8080;
	@ClassRule
	public static final TemporaryFolder tempFolder = new TemporaryFolder();
	@ClassRule
	public static final JettyServerSetup server = new JettyServerSetup(RESOURCE_BASE, PORT);
	@ClassRule
	public static final TestLogger logger = new TestLogger();
	public static Logger log;
	@ClassRule
	public static final ExpectedException exception = ExpectedException.none();
	@BeforeClass
	public static void setupClass() {
		log = logger.getLog();
	}
	File tmpdir;
	File htmlfile;
	File javafile;
	@Before
	public void setUp() throws IOException {
		tmpdir = new File(RESOURCE_BASE);
		htmlfile = new File(tmpdir, "index.html");
		PrintWriter htmlout = new PrintWriter(new BufferedWriter(
				new FileWriter(htmlfile)));
		htmlout.println(
				"<html>\n" +
				"<head>\n" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"/>\n" +
				"<title>TestPage</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"<h2>TestPage</h2>\n" +
				"<p>Welcome to the TestPage!</p>\n" +
				"</body>\n" +
				"</html>\n"
		);
		htmlout.close();
		javafile = new File(tmpdir, "TestClass.txt");
		PrintWriter javaout = new PrintWriter(new BufferedWriter(
				new FileWriter(javafile)));
		javaout.println(
			"import java.util.List;\n" +
			"public class TestClass {\n" +
			"    List myList;\n" +
			"    public TestClass() {}\n" +
			"}\n"
		);
		javaout.close();
	}
	@Test
	public void testGET1() throws Exception {
		HttpURLConnection http = (HttpURLConnection) new URL(String.format("http://localhost:%d/index.html", PORT)).openConnection();
		http.connect();
		assertThat("Response Code", http.getResponseCode(), is(HttpStatus.OK_200));
		String responseContent = Utils.http2text(http);
		assertThat("Response Content", responseContent, is(Utils.file2text(htmlfile)));
	}
	@Test
	public void testGET2() throws Exception {
		HttpURLConnection http = (HttpURLConnection) new URL(String.format("http://localhost:%d/TestClass.txt", PORT)).openConnection();
		http.connect();
		assertThat("Response Code", http.getResponseCode(), is(HttpStatus.OK_200));
		String responseContent = Utils.http2text(http);
		assertThat("Response Content", responseContent, is(Utils.file2text(javafile)));
	}
}