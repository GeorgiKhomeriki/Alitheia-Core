#include <Logger>
#include <Core>
#include <Metric>
#include <DBObject>
#include <Database>

#include <sstream>
#include <ostream>
#include <vector>

using namespace std;
using namespace Alitheia;

class MyMetric : virtual public ProjectFileMetric
{
public:
    bool install()
    {
        return addSupportedMetrics( description(), "Test", MetricType::SourceCode );
    }

    string name() const
    {
        return "Example CORBA metric";
    }

    string author() const
    {
        return "Max Mustermann";
    }

    string description() const
    {
        return "This is just an example about how to put CORBA metrics into alitheia.";
    }

    string version() const
    {
        return "1.0.0.0";
    }

    string result() const
    {
        return string();
    }

    string dateInstalled() const
    {
        return string();
    }

    string getResult( const ProjectFile& ) const
    {
        return "getResult";
    }

    string getResult( const ProjectVersion& ) const
    {
        return "getResult";
    }

    string getResult( const StoredProject& ) const
    {
        return "getResult";
    }

    string getResult( const FileGroup& ) const
    {
        return "getResult";
    }

    void run( ProjectFile& file )
    {
        Logger logger( Logger::NameSqoOssMetric );
        logger.setTeeStream( cout );
        logger << "MyMetric: Measuring " << file.name << endl;
        string line;
        int count = -1;
        do
        {
            ++count;
            std::getline( file, line );
        } while( !file.eof() );

        vector<Metric> metrics = getSupportedMetrics();
        if( metrics.empty() )
            return;

        // add the result
        ProjectFileMeasurement m;
        m.metric = metrics.front();
        m.projectFile = file;
        m.whenRun = m.metric.plugin.installdate;
        stringstream ss;
        ss << count;
        m.result = ss.str();
        db.addRecord( m );
    }

private:
    Database db;
};

#include <vector>

int main( int argc, char **argv)
{
    Core& c = *Core::instance();
   
    Database db;
    Database::property_map properties;
    properties[ "name" ] = std::string( "svn" );
    const std::vector<StoredProject> projects = db.findObjectsByProperties< StoredProject >( properties );
    cout << projects.size() << endl;
    const StoredProject& p = projects.front();
    //const StoredProject p = db.findObjectById< StoredProject >( 34578 );
    cout << p.id << " " << p.name << endl;
/*    p.name = "PDFCreator";
    db.updateRecord( p );
   
    p = db.findObjectById< StoredProject >( p.id );
    cout << p.name << endl;*/

    const Developer dev = Developer::byUsername( "christoph", p );
    cout << dev.id << endl;

    return 0;

    Logger logger( Logger::NameSqoOssMetric );
    logger.setTeeStream( cout );
    
    MyMetric* m = new MyMetric;
    logger << "Registering C++ client metric..." << endl;
    int id = c.registerMetric( m );
    logger << "C++ client metric registered, id is " << id << "." << endl;

    logger << "Metric waiting for orders..." << endl;
    
    c.run();
}
