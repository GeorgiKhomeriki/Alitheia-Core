package eu.sqooss.impl.service.corba.alitheia.core;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;

import eu.sqooss.impl.service.CorbaActivator;
import eu.sqooss.impl.service.corba.alitheia.CorePOA;
import eu.sqooss.impl.service.corba.alitheia.Job;
import eu.sqooss.impl.service.corba.alitheia.JobHelper;
import eu.sqooss.impl.service.corba.alitheia.AbstractMetric;
import eu.sqooss.impl.service.corba.alitheia.AbstractMetricHelper;
import eu.sqooss.impl.service.corba.alitheia.ProjectFileMetric;
import eu.sqooss.impl.service.corba.alitheia.ProjectFileMetricHelper;
import eu.sqooss.impl.service.corba.alitheia.ProjectVersionMetric;
import eu.sqooss.impl.service.corba.alitheia.ProjectVersionMetricHelper;
import eu.sqooss.impl.service.corba.alitheia.StoredProjectMetric;
import eu.sqooss.impl.service.corba.alitheia.StoredProjectMetricHelper;
import eu.sqooss.impl.service.corba.alitheia.FileGroupMetric;
import eu.sqooss.impl.service.corba.alitheia.FileGroupMetricHelper;
import eu.sqooss.impl.service.corba.alitheia.job.CorbaJobImpl;

import eu.sqooss.impl.metrics.corba.CorbaMetricImpl;
import eu.sqooss.impl.metrics.corba.CorbaProjectFileMetricImpl;
import eu.sqooss.impl.metrics.corba.CorbaProjectVersionMetricImpl;
import eu.sqooss.impl.metrics.corba.CorbaStoredProjectMetricImpl;
import eu.sqooss.impl.metrics.corba.CorbaFileGroupMetricImpl;


public class CoreImpl extends CorePOA {

	BundleContext bc;
	
	Map< String, CorbaJobImpl > registeredJobs;
	
	public CoreImpl(BundleContext bc) {
		this.bc = bc;
		registeredJobs = new HashMap< String, CorbaJobImpl >();
	}
	
	public int registerMetric(String name) {
		org.omg.CORBA.Object o;
		try {
			o = CorbaActivator.instance().getExternalCorbaObject(name);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		CorbaMetricImpl wrapper = null;
		if (o._is_a(ProjectVersionMetricHelper.id()))
		{
			ProjectVersionMetric m = ProjectVersionMetricHelper.narrow(o);
			wrapper = new CorbaProjectVersionMetricImpl(bc,m);
		}
		else if (o._is_a(ProjectFileMetricHelper.id()))
		{
			ProjectFileMetric m = ProjectFileMetricHelper.narrow(o);
			wrapper = new CorbaProjectFileMetricImpl(bc,m);
		}
		else if (o._is_a(StoredProjectMetricHelper.id()))
		{
			StoredProjectMetric m = StoredProjectMetricHelper.narrow(o);
			wrapper = new CorbaStoredProjectMetricImpl(bc,m);
		}
		else if (o._is_a(FileGroupMetricHelper.id()))
		{
			FileGroupMetric m = FileGroupMetricHelper.narrow(o);
			wrapper = new CorbaFileGroupMetricImpl(bc,m);
		}
		
		return CorbaActivator.instance().registerExternalCorbaObject(CorbaMetricImpl.class.getName(), wrapper);
	}

	public void unregisterMetric(int id) {
		CorbaActivator.instance().unregisterExternalCorbaObject(id);
	}

	public int registerJob(String name) {
		org.omg.CORBA.Object o;
		try {
			o = CorbaActivator.instance().getExternalCorbaObject(name);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		Job j = JobHelper.narrow(o);
		CorbaJobImpl impl = new CorbaJobImpl(bc,j);
		registeredJobs.put(name, impl);
		return impl.hashCode();
	}

	public void unregisterJob(int id) {
		// TODO Auto-generated method stub
		
	}

	public void enqueueJob(String name) {
		registeredJobs.get(name).enqueue();
	}

	public void addJobDependency(String job, String dependency) {
		try {
			registeredJobs.get(job).addDependency(registeredJobs.get(dependency));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void waitForJobFinished(String job) {
		try {
			registeredJobs.get(job).waitForFinished();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
