from omniORB import CORBA
from threading import Thread

import CosNaming
import Alitheia_idl
import eu
import eu__POA

from eu.sqooss.impl.service.corba.alitheia import StoredProject

class CorbaHandler:
    orb = None
    poa = None
    poaobj = None
    orb_thread = None
    m_instance = None

    class OrbThread(Thread):
        orb = None

        def __init__(self,orb):
            Thread.__init__(self)
            self.orb = orb
            #self.start()

        def run(self):
            self.orb.run()

    def __init__(self):
        self.orb = CORBA.ORB_init(['-ORBInitRef','NameService=corbaloc:iiop:1.2@localhost:2809/NameService'], CORBA.ORB_ID)
        self.poaobj = self.orb.resolve_initial_references('RootPOA')
        poaManager = self.poaobj._get_the_POAManager()
        poaManager.activate()
        self.orb_thread = CorbaHandler.OrbThread(self.orb)

    @staticmethod
    def instance():
        if CorbaHandler.m_instance is None:
            CorbaHandler.m_instance = CorbaHandler()
        return CorbaHandler.m_instance

    def getObject(self,name):
        nameService = self.orb.resolve_initial_references('NameService')
        nameService = nameService._narrow( CosNaming.NamingContext )
        if nameService is None:
            print 'Error: Could not find naming service'
            return None
        cosName = [ CosNaming.NameComponent(name,'')]
        obj = nameService.resolve(cosName)
        return obj

    def exportObject(self,obj,name):
        nameService = self.orb.resolve_initial_references('NameService')
        nameService = nameService._narrow( CosNaming.NamingContext )
        if nameService is None:
            print 'Error: Could not find naming service'
            return None
        cosName = [ CosNaming.NameComponent(name,'')]
        nameService.rebind(cosName, obj._this())

    def shutdown(self):
        self.orb.shutdown(True)

class Scheduler:
    scheduler = None
    
    def __init__(self):
        self.scheduler = CorbaHandler.instance().getObject('AlitheiaScheduler')

    def enqueueJob(self,job):
        if len(job.orbname) == 0:
            self.registerJob(job)
        self.scheduler.enqueueJob(job.orbname)
    
    def registerJob(self,job):
        job.orbname = 'Alitheia_Job_' + str(Core.instance().getUniqueId())
        CorbaHandler.instance().exportObject(job, job.orbname)
        self.scheduler.registerJob(job.orbname)

    def isExecuting(self):
        return self.scheduler.isExecuting()

    def startExecute(self,n):
        self.scheduler.stateExecute(n)

    def stopExecute(self,n):
        self.scheduler.stopExecute(n)

    def unregisterJob(self,job):
        self.scheduler.unregisterJob(job.orbname)
        CorbaHandler.instance().unexportObject(job.orbname)

    def addJobDependency(self,job,dependency):
        if len(job.orbname) == 0:
            self.registerJob(job)
        if len(dependency.orbname) == 0:
            self.registerJob(dependency)
        self.scheduler.addJobDependency(job.orbname, dependency.orbname)

    def waitForJobFinished(self,job):
        if len(job.orbname) == 0:
            self.registerJob(job)
        self.scheduler.waitForJobFinished(job.orbname)

class Job (eu__POA.sqooss.impl.service.corba.alitheia.Job):
    orbname = ''
    state = None
    scheduler = Scheduler()

    def priority(self):
        return 0

    def state(self):
        return self.state

    def stateChanged(self,state):
        return

    def setState(self,state):
        if self.state == state:
            return
        self.state = state
        self.stateChanged(state)

    def addDependency(self,other):
        self.scheduler.addJobDependency(self,other)

    def waitForFinished(self):
        self.scheduler.waitForJobFinished(self)

class Logger:
    logger = None
    name = None

    def __init__( self, name ):
        self.logger = CorbaHandler.instance().getObject('AlitheiaLogger')
        self.name = name

    def debug( self, message ):
        self.logger.debug( self.name, message )

    def info( self, message ):
        self.logger.info( self.name, message )

    def warn( self, message ):
        self.logger.warn( self.name, message )

    def error( self, message ):
        self.logger.error( self.name, message )

class Core:
    core = None
    m_instance = None

    def __init__(self):
        self.core = CorbaHandler.instance().getObject('AlitheiaCore')

    @staticmethod
    def instance():
        if Core.m_instance is None:
            Core.m_instance = Core()
        return Core.m_instance

    @staticmethod
    def shutdown():
        CorbaHandler.instance().shutdown();

    def getUniqueId(self):
        return self.core.getUniqueId()

    def registerMetric(self,metric):
        metric.orbname = 'Alitheia_Metric_' + str(Core.instance().getUniqueId())
        CorbaHandler.instance().exportObject(metric, metric.orbname)
        metric.id = self.core.registerMetric(metric.orbname)

    def unregisterMetric(self,metric):
        self.core.unregisterMetric(metric.id)

class Database (eu__POA.sqooss.impl.service.corba.alitheia.Database):
    db = None

    def __init__(self):
        self.db = CorbaHandler.instance().getObject('AlitheiaDatabase')

    def addRecord(self,object):
        return self.db.addRecord(obect)

    def deleteRecord(self,object):
        return self.db.deleteRecord(object)

    def updateRecord(self,object):
        return self.db.updateRecord(object)

    def findObjectById(self,type,id):
        any = CORBA.Any(CORBA.TypeCode('IDL:eu/sqooss/impl/service/corba/alitheia/' + type.__name__ + ':1.0'), type())
        return self.db.findObjectById(any,long(id)).value()
      
    def findObjectsByProperties(self,type,properties):
        any = CORBA.Any(CORBA.TypeCode('IDL:eu/sqooss/impl/service/corba/alitheia/' + type.__name__ + ':1.0'), type())
        map = []
        
        for k, v in properties.iteritems():
            if v.__class__ == long or v.__class__ == int:
                value = CORBA.Any(CORBA.TC_long, long(v))
            elif v.__class__ == str:
                value = CORBA.Any(CORBA.TC_string, v)
            else:
                value = CORBA.Any(CORBA.TypeCode('IDL:eu/sqooss/impl/service/corba/alitheia/' + v.__class__.__name__ + ':1.0'), v)
            map.append(eu.sqooss.impl.service.corba.alitheia.map_entry(k,value))

        resultAny = self.db.findObjectsByProperties(any,map)
        result = []

        for i in resultAny:
            result.append(i.value())

        return result

    def doHQL(self,hql,params={}):
        map = []

        for k, v in params.iteritems():
            if v.__class__ == long or v.__class__ == int:
                value = CORBA.Any(CORBA.TC_long, long(v))
            elif v.__class__ == str:
                value = CORBA.Any(CORBA.TC_string, v)
            else:
                value = CORBA.Any(CORBA.TypeCode('IDL:eu/sqooss/impl/service/corba/alitheia/' + v.__class__.__name__ + ':1.0'), v)
            map.append(eu.sqooss.impl.service.corba.alitheia.map_entry(k,value))

        resultAny = self.db.doHQL(hql,map)
        result = []

        for i in resultAny:
            result.append(i.value())

        return result

    def doSQL(self,sql,params={}):
        map = []

        for k, v in params.iteritems():
            if v.__class__ == long or v.__class__ == int:
                value = CORBA.Any(CORBA.TC_long, long(v))
            elif v.__class__ == str:
                value = CORBA.Any(CORBA.TC_string, v)
            else:
                value = CORBA.Any(CORBA.TypeCode('IDL:eu/sqooss/impl/service/corba/alitheia/' + v.__class__.__name__ + ':1.0'), v)
            map.append(eu.sqooss.impl.service.corba.alitheia.map_entry(k,value))

        resultAny = self.db.doSQL(sql,map)
        result = []

        for i in resultAny:
            result.append(i.value())

        return result

class AbstractMetric (eu__POA.sqooss.impl.service.corba.alitheia.AbstractMetric):
    orbname = ''
    id = 0

    def doInstall(self):
        return self.install()

    def doRemove(self):
        return self.remove()

    def doUpdate(self):
        return self.update()

    def getAuthor(self):
        return self.author()

    def getDescription(self):
        return self.description()

    def getName(self):
        return self.name()

    def getVersion(self):
        return self.version()

    def getDateInstalled(self):
        return self.dateInstalled()

    def install(self):
        return False

    def remove(self):
        return False

    def update(self):
        return False

    def author(self):
        return ''

    def description(self):
        return ''

    def name(self):
        return ''

    def version(self):
        return ''

    def dateInstalled(self):
        return ''

class ProjectVersionMetric (eu__POA.sqooss.impl.service.corba.alitheia.ProjectVersionMetric,AbstractMetric):
    def doRun(self,projectVersion):
        self.run(projectVersion)

    def doGetResult(self,projectVersion):
        return self.getResult(projectVersion)

    def run(self,projectVersion):
        print 'run: Nothing to do'

    def getResult(self,projectVersion):
        print 'getResult: Nothing to do'
        
class ProjectFileMetric (eu__POA.sqooss.impl.service.corba.alitheia.ProjectFileMetric,AbstractMetric):
    def doRun(self,projectFile):
        self.run(projectFile)

    def doGetResult(self,projectFile):
        return self.getResult(projectFile)

    def run(self,projectFile):
        print 'run: Nothing to do'

    def getResult(self,projectFile):
        print 'getResult: Nothing to do'

class StoredProjectMetric (eu__POA.sqooss.impl.service.corba.alitheia.StoredProjectMetric,AbstractMetric):
    def doRun(self,storedProject):
        self.run(storedProject)

    def doGetResult(self,storedProject):
        return self.getResult(storedProject)

    def run(self,storedProject):
        print 'run: Nothing to do'

    def getResult(self,storedProject):
        print 'getResult: Nothing to do'

class FileGroupMetric (eu__POA.sqooss.impl.service.corba.alitheia.FileGroupMetric,AbstractMetric):
    def doRun(self,fileGroup):
        self.run(fileGroup)

    def doGetResult(self,fileGroup):
        return self.getResult(fileGroup)

    def run(self,fileGroup):
        print 'run: Nothing to do'

    def getResult(self,fileGroup):
        print 'getResult: Nothing to do'

OldStoredProjectInit = StoredProject.__init__
def StoredProjectInit(self, id=0, name='', website='', contact='', bugs='', repository='', mail=''):
    return OldStoredProjectInit(self, id, name, website, contact, bugs, repository, mail)

@staticmethod
def StoredProjectGetProjectByName(name):
    db = Database()
    properties = { 'name': name }
    objects = db.findObjectsByProperties(StoredProject,properties)
    if len(objects) == 0:
        return None

    return objects[0]

@staticmethod
def StoredProjectGetLastProjectVersion(project):
    db = Database()
    properties = { 'sp': project }
    objects = db.doHQL('from ProjectVersion pv where pv.project=:sp and pv.version = ( select max( pv2.version ) from ProjectVersion pv2 where pv2.project=:sp)', properties)

    if len(objects) == 0:
        return None

    return objects[0]

setattr(eu.sqooss.impl.service.corba.alitheia.StoredProject,'getProjectByName',StoredProjectGetProjectByName)
setattr(eu.sqooss.impl.service.corba.alitheia.StoredProject,'getLastProjectVersion',StoredProjectGetLastProjectVersion)
setattr(eu.sqooss.impl.service.corba.alitheia.StoredProject,'__init__',StoredProjectInit)
