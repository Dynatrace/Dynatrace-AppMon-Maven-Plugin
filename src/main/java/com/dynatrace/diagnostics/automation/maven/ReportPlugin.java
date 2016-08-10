package com.dynatrace.diagnostics.automation.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.dynatrace.diagnostics.automation.rest.sdk.RESTEndpoint;
import com.dynatrace.diagnostics.automation.rest.sdk.entity.BaseRecord;
import com.dynatrace.diagnostics.automation.rest.sdk.entity.BusinessTransaction;
import com.dynatrace.diagnostics.automation.rest.sdk.entity.WebRequest;

/**
 *
 * @goal report
 * @phase site
 */
public class ReportPlugin extends AbstractMavenReport {
	private final static String DASHLET_SELECTION_EXPRESSION = "dashboardreport/data/*"; //$NON-NLS-1$
	private final static String XSL_DASHLET = "res/dashlet.xsl"; //$NON-NLS-1$

	/**
	 * The server-side RESTful interface URL.
	 *
	 * @parameter expression="${dynaTrace.server.url}"
	 *            default-value="https://localhost:8021"
	 * @required
	 */
	private URL url;

	/**
	 * The path relative to server URL. Value should be
	 * rest/management/dashboard
	 *
	 * @parameter expression="${dynaTrace.server.dashboardRelativePath}"
	 *            default-value="rest/management/dashboard"
	 * @required
	 */
	private String dashboardRelativePath;

	/**
	 * The username needed to connect to the server-side RESTful interface.
	 *
	 * @parameter expression="${dynaTrace.server.username}"
	 *            default-value="admin"
	 * @required
	 */
	private String username;

	/**
	 * The password needed to connect to the server-side RESTful interface.
	 *
	 * @parameter expression="${dynaTrace.server.password}"
	 *            default-value="admin"
	 * @required
	 */
	private String password;

	/**
	 * Directory where reports will go.
	 *
	 * @parameter expression="${project.reporting.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private String outputDirectory;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * <i>Maven Internal</i>: The Doxia Site Renderer.
	 *
	 * @component
	 */
	private Renderer siteRenderer;

	/**
	 * @parameter expression="${dynaTrace.report.name}"
	 *            default-value="Dynatrace report"
	 */
	private String name;

	/**
	 * @parameter expression="${dynaTrace.report.description}"
	 *            default-value="This is a Dynatrace report"
	 */
	private String description;

	/**
	 * @parameter expression="${dynaTrace.report.outputname}"
	 *            default-value="dtReport"
	 */
	private String outputname;

	/**
	 * The name of the dashboard to use for this report generation.
	 *
	 * @parameter expression="${dynaTrace.report.dashboard}"
	 * @required
	 */
	private String dashboard;

	/**
	 * The source that the dashboard should be using.
	 * If nothing is specified, it will be the latest session recorded by the startRecording goal.
	 *
	 * @parameter expression="${dynaTrace.report.source}"
	 */
	private String source;

	/**
	 * The comparison source to use when generating the report.
	 *
	 * @parameter expression="${dynaTrace.report.comparison}"
	 */
	private String comparison;

	/**
	 * The filter to use when generating the report.
	 *
	 * @parameter expression="${dynaTrace.report.filter}"
	 */
	private String filter;

	public URL getUrl() {
		return url;
	}

	public String getDashboardRelativePath() {
		return dashboardRelativePath;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	public String getDescription(Locale arg0) {
		return getDescription();
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getName(Locale arg0) {
		return getName();
	}

	public String getName() {
		return name;
	}

	@Override
	public String getOutputName() {
		return outputname;
	}

	public String getDashboard() {
		return dashboard;
	}

	public String getSource() {
		return source;
	}

	public String getComparison() {
		return comparison;
	}

	public String getFilter() {
		return filter;
	}

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		Sink sink = getSink();

		sink.head();
		sink.title();
		sink.text(getName());
		sink.title_();
		sink.head_();

		sink.body();

		sink.section1();
		sink.sectionTitle1();
		sink.text(getName());
		sink.sectionTitle1_();
		sink.text(getDescription());
		sink.section1_();


		try {
			RESTEndpoint endpoint = new RESTEndpoint(getUsername(), getPassword(), getUrl().toExternalForm());

			BaseRecord filterObject = null;
			if(filter != null) {
				if(filter.startsWith("BT:")) //$NON-NLS-1$
					filterObject = BusinessTransaction.createForFilter(filter.substring(3), null);
				if(filter.startsWith("WR:")) //$NON-NLS-1$
					filterObject = WebRequest.createForFilter(filter.substring(3));
			}
			InputStream stream = endpoint.getRawDashboardData(dashboard, source, comparison, filterObject);
			Document doc = getDashboardDocument(stream);
			stream.close();
			processDashboardDocument(doc, sink);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new MavenReportException(e.getMessage());
		}

		/*
		sink.rawText("<H1>HELLO</H1>");

		sink.table();

		sink.tableRow();
		sink.tableHeaderCell();
		sink.text(getDescription()); // we use our parameter here
		sink.tableHeaderCell_();
		sink.tableRow_();

		sink.table_();
		*/
		sink.body_();
		sink.flush();
		sink.close();

	}

	private Document getDashboardDocument(InputStream stream) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder builder = docFactory.newDocumentBuilder();
		return builder.parse(stream);
	}

	private void processDashboardDocument(Document doc, Sink sink) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList list = (NodeList) xpath.evaluate(DASHLET_SELECTION_EXPRESSION, doc, XPathConstants.NODESET);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		for (int counter = 0; counter < list.getLength(); counter++) {
			processDashlet(transformerFactory, sink, list.item(counter));
		}
	}

	private void processDashlet(TransformerFactory transformerFactory, Sink sink, Node dashletNode) {
		StringWriter sw = new StringWriter();
		Result result = new StreamResult(sw);

		try {
			Transformer transformer = transformerFactory.newTransformer(getXSLTSource(dashletNode));
			transformer.transform(new DOMSource(dashletNode), result);
			sink.rawText(sw.toString());
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private Source getXSLTSource(Node node) {
		InputStream in = getClass().getClassLoader().getResourceAsStream(XSL_DASHLET);
		return new StreamSource(in);
	}

}
