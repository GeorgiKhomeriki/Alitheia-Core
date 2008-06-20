package eu.sqooss.impl.service.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import eu.sqooss.impl.service.SpecsActivator;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Group;
import eu.sqooss.service.db.User;
import eu.sqooss.service.security.UserManager;

public class SpUser implements SpEntity {
    private DBService db = SpecsActivator.alitheiaCore.getDBService();
    private UserManager um = SpecsActivator.alitheiaCore.getSecurityManager().getUserManager();

    long id = -1;
    boolean persistent = false;
    
    public String name;
    public String password;
    public String email;
    public String groupList = "";
    
    public static ArrayList<SpUser> allUsers() {
        DBService db = SpecsActivator.alitheiaCore.getDBService();
        ArrayList<SpUser> result = new ArrayList<SpUser>();
        
        db.startDBSession();
        List<User> users = db.findObjectsByProperties(User.class, new HashMap<String,Object>());
        
        TreeSet<String> userNames = new TreeSet<String>();
        HashMap<String, SpUser> instances = new HashMap<String, SpUser>();
        for (User user : users) {
            TreeSet<String> groupNames = new TreeSet<String>();
            for (Object obj : user.getGroups())
            {
                Group g = (Group)obj;
                groupNames.add(g.getDescription());
            }
            
            boolean first = true;
            String groupList = "";
            for (String groupName : groupNames)
            {
                if (!first) groupList+=", ";
                groupList+=groupName;
                first = false;
            }
            
            userNames.add(user.getName());
            instances.put(user.getName(),
                          new SpUser(user.getName(),
                                     user.getPassword(),
                                     user.getEmail(),
                                     groupList,
                                     user.getId()));
        }
        for (String userName : userNames) {
            result.add(instances.get(userName));
        }
        
        db.commitDBSession();
        
        return result;
    }
    
    public SpUser(String n) {
        name = n;
        load();
    }

    public SpUser(String n, String p, String e) {
        name = n;
        password = p;
        email = e;
        load();
    }
    
    private SpUser(String n, String p, String e, String gl, long i) {
        name = n;
        password = p;
        email = e;
        groupList = gl;
        id = i;
        persistent = true;
    }
    
    public void load() {
        db.startDBSession();
        User user = um.getUser(name);
        
        if (user==null) {
            persistent = false;
            return;
        }
        
        id = user.getId();
        name = user.getName();
        password = user.getPassword();
        email = user.getEmail();

        TreeSet<String> groupNames = new TreeSet<String>();
        for (Object obj : user.getGroups())
        {
            Group g = (Group)obj;
            groupNames.add(g.getDescription());
        }
        
        boolean first = true;
        for (String groupName : groupNames)
        {
            if (!first) groupList+=", ";
            groupList+=groupName;
            first = false;
        }
        db.commitDBSession();
    }

    public void create() {
        db.startDBSession();
        um.createUser(name, password, email);
        db.commitDBSession();
        persistent = true;
    }

    public void delete() {
        db.startDBSession();
        um.deleteUser(id);
        db.commitDBSession();
        persistent = false;
    }

    public boolean isMemberOf(String groupName) {
       db.startDBSession();
       User user = um.getUser(name);

        boolean result = false;
        TreeSet<String> groupNames = new TreeSet<String>();
        for (Object obj : user.getGroups())
        {
            Group g = (Group)obj;
            if (groupName.compareTo(g.getDescription()) == 0)
            {
                result = true;
            }
        }
        db.commitDBSession();
        
       return result;
    }

    public void changePassword(String newPassword) {
       db.startDBSession();
       User user = um.getUser(name);
  
       user.setPassword(um.getHash(newPassword));      
       db.commitDBSession();    
       password = newPassword;  
    }

    public void changeEmail(String newEmail) {
       db.startDBSession();
       User user = um.getUser(name);
  
       user.setEmail(newEmail);      
       db.commitDBSession();    
       email = newEmail;  
    }
}
