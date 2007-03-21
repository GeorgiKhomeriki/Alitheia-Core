/**
 * 
 */
package eu.sqooss.simple;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.sqooss.vcs.RepositoryFactory;
import eu.sqooss.vcs.RepositoryType;
import eu.sqooss.vcs.SvnRepository;

/**
 * @author circular
 *
 */
public class SimpleTest {

    /**
     * @param args
     */
	private SvnRepository repository;
	private RepositoryType type;
	private String serverPath;
	private String localPath;
	private String username;
	private String password;
	
	
	public SimpleTest() throws Exception {
		serverPath = "https://svn.sqo-oss.eu/";
    	localPath = "./svntest";
    	username = "svnviewer";
    	password = "Sq0V13weR";
    	type = RepositoryType.SVN;
    	repository = (SvnRepository)RepositoryFactory.getRepository(localPath, serverPath, username, password, type);
	}
	
    public static void main(String[] args) {
        // TODO Auto-generated method stub 	
    }

    private void storeProjectInfo() {
        //url, name, website, contactPoint, srcPath, mailPath
    }
    
    private void checkOut(long revision) {
    	
    }
    
    private void storeProjectFiles(Vector<String> files) {
        
    }
    
    private double runWCTool(String file, long revision) {
        double result = 0.0;
        
        String [] args = new String[2];
        args[0] = "wc -l";
        args[1] = file;
        
        String output = "";
        
        Process p;
        try {
            p = Runtime.getRuntime().exec(args);
            if(p != null) {
                BufferedReader b = 
                	new BufferedReader(new InputStreamReader(p.getInputStream()));
                output = b.readLine();
                b.close();
            }
        } catch (IOException e) {
            return 0.0;
        }
        
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
        	result = new Double(matcher.group(0));
        } else {
        	result = 0.0;
        }
        
        return result;
        
    }
    
    private void storeMetrics(int projectID, long revision, String file) {
        //run CW tool, get the metric value, store it in db
    }
}
