package com.dynatrace.diagnostics.automation.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal createReport
 * @phase verify
 */
public class DtCreateReport extends DtServerBase {
	
	/**
	 * The data source
	 * @parameter expression="${dynaTrace.source}"
	 * @required
	 */
	private String source;

	/**
	 * The data source
	 * @parameter expression="${dynaTrace.dashboardName}"
	 * @required
	 */
	private String dashboardName;
				
	/**
	 * Optional comparison data source
	 * @parameter expression="${dynaTrace.comparison}"
	 */
	private String comparison;
	
	/**
	 * Optional iterating Dashboard
	 * @parameter expression="${dynaTrace.iteratorDashboard}"
	 */
	private String iteratorDashboard;
	
	/**
	 * Output xml file for the results
	 * @parameter expression="${dynaTrace.xmlToFile}"
	 */
	private File xmlToFile;
	
	/**
	 * Output directory
	 * @parameter expression="${dynaTrace.reportDir}"
	 */
	private File reportDir;
	
	/**
	 * Output directory
	 * @parameter expression="${dynaTrace.createHtml}"
	 */
	private boolean createHtml;
	
	public void execute() throws MojoExecutionException {
		
		try {
			com.dynatrace.diagnostics.automation.common.Report report = new com.dynatrace.diagnostics.automation.common.Report(getEndpoint());
			report.setComparison(getComparison());
			report.setCreateHtml(isCreateHtml());
			report.setDashboardName(getDashboardName());
			report.setIteratorDashboard(getIteratorDashboard());
			report.setReportDir(getReportDir());
			report.setSource(getSource());
			report.setXmlToFile(getXmlToFile());
			
			getLog().info("Calling report with: " +  //$NON-NLS-1$
					(getDashboardName() == null ? "null" : getDashboardName()) + "/" +  //$NON-NLS-1$ //$NON-NLS-2$
					(getIteratorDashboard() == null ? "null" :getIteratorDashboard()) + "/" +  //$NON-NLS-1$ //$NON-NLS-2$
					(getSource() == null ? "null" : getSource()) + "/" +  //$NON-NLS-1$ //$NON-NLS-2$
					(getComparison() == null ? "null" : getComparison()) + "/" + //$NON-NLS-1$ //$NON-NLS-2$
					isCreateHtml() + "/"); //$NON-NLS-1$
			report.execute();
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}

	public String getComparison() {
		return comparison;
	}

	public void setIteratorDashboard(String iteratorDashboard) {
		this.iteratorDashboard = iteratorDashboard;
	}

	public String getIteratorDashboard() {
		return iteratorDashboard;
	}

	public void setXmlToFile(File xmlToFile) {
		this.xmlToFile = xmlToFile;
	}

	public File getXmlToFile() {
		return xmlToFile;
	}

	public void setReportDir(File reportDir) {
		this.reportDir = reportDir;
	}

	public File getReportDir() {
		return reportDir;
	}

	public void setCreateHtml(boolean createHtml) {
		this.createHtml = createHtml;
	}

	public boolean isCreateHtml() {
		return createHtml;
	}

	public void setDashboardName(String dashboardName) {
		this.dashboardName = dashboardName;
	}

	public String getDashboardName() {
		return dashboardName;
	}
}
