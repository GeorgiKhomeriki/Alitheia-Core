package eu.sqooss.impl.metrics.corba;

import org.osgi.framework.BundleContext;

import eu.sqooss.service.abstractmetric.AbstractMetric;
import eu.sqooss.service.db.MetricType;

abstract public class CorbaMetricImpl extends AbstractMetric {

    eu.sqooss.impl.service.corba.alitheia.AbstractMetric m;

    public CorbaMetricImpl(BundleContext bc, eu.sqooss.impl.service.corba.alitheia.AbstractMetric m) {
        super(bc);
        this.m = m;
    }

    public boolean doAddSupportedMetrics(String desc, String mnemonic, MetricType.Type type) {
        return addSupportedMetrics(desc, mnemonic, type);
    }

    public String getAuthor() {
        return m.getAuthor();
    }

    public String getDescription() {
        return m.getDescription();
    }

    public String getName() {
        return m.getName();
    }

    public String getVersion() {
        return m.getVersion();
    }

    public boolean install() {
        return super.install() && m.doInstall();
    }

    public boolean remove() {
        return m.doRemove();
    }

    public boolean update() {
        return m.doUpdate();
    }
}
