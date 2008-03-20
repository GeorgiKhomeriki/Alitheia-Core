#include "dbobject.h"

#include "core.h"

#include "Alitheia.h"

#include "CORBA.h"

#include <sstream>

using namespace Alitheia;

using std::string;
using std::istream;
using std::stringbuf;

using std::cout;
using std::endl;

DAObject::DAObject( int id )
    : id( id )
{
}

DAObject::~DAObject()
{
}

StoredProject::StoredProject( const alitheia::StoredProject& project )
    : DAObject( project.id ),
      name( project.name ),
      website( project.website ),
      contact( project.contact ),
      bugs( project.bugs ),
      repository( project.repository ),
      mail( project.mail )
{
}

alitheia::StoredProject StoredProject::toCorba() const
{
    alitheia::StoredProject result;
    result.id = id;
    result.name = CORBA::string_dup( name.c_str() );
    result.website = CORBA::string_dup( website.c_str() );
    result.contact = CORBA::string_dup( contact.c_str() );
    result.bugs = CORBA::string_dup( bugs.c_str() );
    result.repository = CORBA::string_dup( repository.c_str() );
    return result;
}

StoredProject::operator CORBA::Any() const
{
    CORBA::Any any;
    any <<= toCorba();
    return any;
}

ProjectVersion::ProjectVersion( const alitheia::ProjectVersion& version )
    : DAObject( version.id ),
      project( version.project ),
      version( version.version ),
      timeStamp( version.timeStamp )
{
}

alitheia::ProjectVersion ProjectVersion::toCorba() const
{
    alitheia::ProjectVersion result;
    result.id = id;
    result.project = project.toCorba();
    result.version = version;
    result.timeStamp = timeStamp;
    return result;
}

ProjectVersion::operator CORBA::Any() const
{
    CORBA::Any any;
    any <<= toCorba();
    return any;
}


class ProjectFileBuffer : public stringbuf
{
public:
    ProjectFileBuffer( const ProjectFile* file )
        : file( file ),
          read( false )
    {
    }

protected:
    int underflow()
    {
        if( !read )
        {
            string data = Core::instance()->getFileContents( *file );
            sputn( data.c_str(), data.size() );
            read = true;
        }
        return stringbuf::underflow();
    }

private:
    const ProjectFile* const file;
    bool read;
};

ProjectFile::ProjectFile()
    : istream( new ProjectFileBuffer( this ) ),
      DAObject( 0 )
{
}

ProjectFile::ProjectFile( const alitheia::ProjectFile& file )
    : istream( new ProjectFileBuffer( this ) ),
      DAObject( file.id ),
      name( file.name ),
      projectVersion( file.projectVersion ),
      status( file.status )
{
}

ProjectFile::ProjectFile( const ProjectFile& other )
    : istream( other.rdbuf() ),
      DAObject( other.id ),
      name( other.name ),
      projectVersion( other.projectVersion ),
      status( other.status )
{
}

alitheia::ProjectFile ProjectFile::toCorba() const
{
    alitheia::ProjectFile result;
    result.id = id;
    result.name = CORBA::string_dup( name.c_str() );
    result.projectVersion = projectVersion.toCorba();
    result.status = CORBA::string_dup( status.c_str() );
    return result;
}

ProjectFile::operator CORBA::Any() const
{
    CORBA::Any any;
    any <<= toCorba();
    return any;
}

ProjectFile::~ProjectFile()
{
    delete rdbuf();
}

FileGroup::FileGroup( const alitheia::FileGroup& group )
    : DAObject( group.id ),
      name( group.name ),
      subPath( group.subPath ),
      regex( group.regex ),
      recalcFreq( group.recalcFreq ),
      lastUsed( group.lastUsed ),
      projectVersion( group.projectVersion )
{
}

alitheia::FileGroup FileGroup::toCorba() const
{
    alitheia::FileGroup result;
    result.id = id;
    result.name = CORBA::string_dup( name.c_str() );
    result.subPath = CORBA::string_dup( subPath.c_str() );
    result.regex = CORBA::string_dup( regex.c_str() );
    result.recalcFreq = recalcFreq;
    result.lastUsed = CORBA::string_dup( lastUsed.c_str() );
    result.projectVersion = projectVersion.toCorba();
    return result;
}

FileGroup::operator CORBA::Any() const
{
    CORBA::Any any;
    any <<= toCorba();
    return any;
}
