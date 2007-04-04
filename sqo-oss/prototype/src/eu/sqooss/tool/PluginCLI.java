package eu.sqooss.tool;

import org.apache.commons.cli.*;

import eu.sqooss.plugin.PluginList;

import eu.sqooss.db.Plugin;

import eu.sqooss.util.ReadOnlyIterator;

/**
 * Command Line handling class for the plugin options
 */
public class PluginCLI extends CLI {
    
    private PluginCLI(String[] args) {
        super(args);        
        options.addOption("l","list",false,"List available plugins");
        options.addOption("h","help",false,"Prints the online help");
        options.addOption("a","add",false,"Add a new module");
    }
    
    public static void parse(String[] args) {
        PluginCLI pcli = new PluginCLI(args);
        CommandLine cmdLine = pcli.parseArgs();
        
        if(cmdLine == null) { return; }
        
        if(cmdLine.hasOption('h')) {
            System.out.println(HEADER);
            pcli.formatter.printHelp( " ", pcli.options);
            return;
        }
                
        if(cmdLine.hasOption('l')) {
            System.out.println("Print all available Modules:\n");
            PluginList pl = PluginList.getInstance();
            ReadOnlyIterator roi = pl.getPlugins();
            while(roi.hasNext()) {
                System.out.println(((Plugin)roi.next()).toString());
            }
            
            return;
        }
    }
}
