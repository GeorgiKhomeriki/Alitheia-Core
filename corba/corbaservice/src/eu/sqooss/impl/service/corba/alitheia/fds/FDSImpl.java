package eu.sqooss.impl.service.corba.alitheia.fds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.StringHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.corba.alitheia.Checkout;
import eu.sqooss.impl.service.corba.alitheia.FDSPOA;
import eu.sqooss.impl.service.corba.alitheia.ProjectFile;
import eu.sqooss.impl.service.corba.alitheia.ProjectVersion;
import eu.sqooss.impl.service.corba.alitheia.db.DAObject;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.fds.InMemoryCheckout;
import eu.sqooss.service.fds.InMemoryDirectory;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.InvalidRepositoryException;
import eu.sqooss.service.tds.ProjectRevision;

/**
 * Wrapper class provided to export the FDS into the Corba ORB.
 * @author Christoph Schleifenbaum, KDAB
 */
public class FDSImpl extends FDSPOA {

	protected FDSService fds = null;
	
	protected Map<ProjectVersion, Checkout> checkouts = new HashMap< ProjectVersion, Checkout>(); 
	
	public FDSImpl(BundleContext bc) {
        ServiceReference serviceRef = bc.getServiceReference(AlitheiaCore.class.getName());
        AlitheiaCore core = (AlitheiaCore) bc.getService(serviceRef);
        if (core == null) {
            System.out.println("CORBA database could not get the Alitheia core");
            return;
        }
        fds = core.getFDSService();
	
	}

	/**
	 * Gets the contents of \a file.
	 * @param contents Corba string holder to put the content int.
	 * @return The size of the file. 
	 */
	public int getFileContents(ProjectFile file, StringHolder contents) {
        byte[] content = null;
        try {
            content = fds.getFileContents(DAObject.fromCorbaObject(file));
        } catch ( Exception e ) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        if( content == null ) {
            // return an empty string
            contents.value = new String();
            return 0;
        }
        contents.value = new String(content);
        return content.length;
	}

	/**
	 * Get all files within \a dir.
	 * Works recursively.
	 */
	protected List<eu.sqooss.service.db.ProjectFile> getFiles(InMemoryDirectory dir) {
		List<eu.sqooss.service.db.ProjectFile> files = dir.getFiles();
		for (InMemoryDirectory subdir : dir.getSubDirectories()) {
			files.addAll(getFiles(subdir));
		}
		return files;
	}
	
	/**
	 * Creates a new checkout.
	 * @param version The ProjectVersion to create the checkout for.
	 * @return A reference to a Corba style checkout.
	 */
	protected Checkout createCheckout(eu.sqooss.service.db.ProjectVersion version) throws InvalidRepositoryException, InvalidProjectRevisionException {
		ProjectRevision rev = new ProjectRevision(version.getVersion());
		InMemoryCheckout co = fds.getInMemoryCheckout(version.getProject().getId(), rev);
		
		List<eu.sqooss.service.db.ProjectFile> files = getFiles(co.getRoot());
		
		Checkout result = new Checkout();
		result.version = DAObject.toCorbaObject(version);
		result.files = new ProjectFile[files.size()];
		for (int i = 0; i < files.size(); ++i ) {
			result.files[ i ] = DAObject.toCorbaObject(files.get(i));
		}

		return result;
	}

	/**
	 * Gets a checkout.
	 * @param version The ProjectVersion to create the checkout for.
	 */
	public Checkout getCheckout(ProjectVersion version) {
		synchronized( checkouts ) {
			if (!checkouts.containsKey(version)) {
				try {
					checkouts.put(version, createCheckout(DAObject.fromCorbaObject(version)));
				} catch (Exception e) {
					return null;
				}
			}
			return checkouts.get(version);
		}
	}

	/**
	 * Releases a checkout. I.e. delete it from the cache.
	 * @param version The ProjectVersion to release the checkout for.
	 */
	public void releaseCheckout(ProjectVersion version) {
		checkouts.remove(version);
	}
}
