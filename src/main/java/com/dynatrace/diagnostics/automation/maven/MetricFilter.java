package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by tomasz.chojnacki on 1/24/2017.
 */
public class MetricFilter {

    @Parameter (property = "dynaTrace.group")
    private String group;
    @Parameter (property = "dynaTrace.metric")
    private String metric;

    public MetricFilter() {
    }

    public MetricFilter(String group, String metric) {
        this.group = group;
        this.metric = metric;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }
}
