#ifndef DBOBJECT_H
#define DBOBJECT_H

#include <iostream>
#include <string>    

#include "Alitheia.h"

namespace Alitheia
{
    class DAObject
    {
    protected:
        DAObject() : id( 0 ) {}
        DAObject( int id );

    public:
        virtual ~DAObject();

        virtual operator CORBA::Any() const = 0;

        int id;
    };

    class StoredProject : public DAObject
    {
    public:
        StoredProject() {}
        explicit StoredProject( const eu::sqooss::impl::service::corba::alitheia::StoredProject& project );

        eu::sqooss::impl::service::corba::alitheia::StoredProject toCorba() const;
        operator CORBA::Any() const;
       
        std::string name;
        std::string website;
        std::string contact;
        std::string bugs;
        std::string repository;
        std::string mail;
    };

    class Developer : public DAObject
    {
    public:
        Developer() {}
        explicit Developer( const eu::sqooss::impl::service::corba::alitheia::Developer& developer );

        eu::sqooss::impl::service::corba::alitheia::Developer toCorba() const;
        operator CORBA::Any() const;

        std::string name;
        std::string email;
        std::string username;
        StoredProject storedProject;
    };

    class Directory : public DAObject
    {
    public:
        Directory() {}
        explicit Directory( const eu::sqooss::impl::service::corba::alitheia::Directory& directory );

        eu::sqooss::impl::service::corba::alitheia::Directory toCorba() const;
        operator CORBA::Any() const;

        std::string path;
    };

    class ProjectFile;
    class ProjectVersion : public DAObject
    {
    public:
        ProjectVersion() : version(0), timeStamp(0){}
        explicit ProjectVersion( const eu::sqooss::impl::service::corba::alitheia::ProjectVersion& version );
        
        eu::sqooss::impl::service::corba::alitheia::ProjectVersion toCorba() const;
        operator CORBA::Any() const;

        std::vector< ProjectFile > getVersionFiles() const;

        StoredProject project;
        int version;
        int timeStamp;
        Developer committer;
        std::string commitMsg;
        std::string properties;
    };

    class ProjectFile : public std::istream, public DAObject
    {
    public:
        ProjectFile();
        explicit ProjectFile( const eu::sqooss::impl::service::corba::alitheia::ProjectFile& file );
        ProjectFile( const ProjectFile& other );
        ~ProjectFile();

        eu::sqooss::impl::service::corba::alitheia::ProjectFile toCorba() const;
        operator CORBA::Any() const;

        ProjectFile& operator=( const ProjectFile& other );

        void save( std::ostream& stream ) const;
        void save( const std::string& filename ) const;

        std::string name;
        ProjectVersion projectVersion;
        std::string status;
        bool isDirectory;
        Directory directory;
    };

    class FileGroup : public DAObject
    {
    public:
        explicit FileGroup( const eu::sqooss::impl::service::corba::alitheia::FileGroup& group );

        eu::sqooss::impl::service::corba::alitheia::FileGroup toCorba() const;
        operator CORBA::Any() const;

        std::string name;
        std::string subPath;
        std::string regex;
        int recalcFreq;
        std::string lastUsed;
        ProjectVersion projectVersion;
    };

    class MetricType : public DAObject
    {
    public:
        enum Type
        {
            SourceCode = ::eu::sqooss::impl::service::corba::alitheia::SourceCode,
            MailingList = ::eu::sqooss::impl::service::corba::alitheia::MailingList,
            BugDatabase = ::eu::sqooss::impl::service::corba::alitheia::BugDatabase
        };

        MetricType() : type( SourceCode ) {}
        explicit MetricType( const eu::sqooss::impl::service::corba::alitheia::MetricType& metrictype );

        eu::sqooss::impl::service::corba::alitheia::MetricType toCorba() const;
        operator CORBA::Any() const;

        Type type;
    };

    class Plugin : public DAObject
    {
    public:
        Plugin() {}
        explicit Plugin( const eu::sqooss::impl::service::corba::alitheia::Plugin& plugin );

        eu::sqooss::impl::service::corba::alitheia::Plugin toCorba() const;
        operator CORBA::Any() const;

        std::string name;
        std::string installdate;
    };

    class Metric : public DAObject
    {
    public:
        Metric() {}
        explicit Metric( const eu::sqooss::impl::service::corba::alitheia::Metric& metric );

        eu::sqooss::impl::service::corba::alitheia::Metric toCorba() const;
        operator CORBA::Any() const;

        Plugin plugin;
        MetricType metricType;
        std::string mnemonic;
        std::string description;
    };

    class ProjectFileMeasurement : public DAObject
    {
    public:
        ProjectFileMeasurement() {}
        explicit ProjectFileMeasurement( const eu::sqooss::impl::service::corba::alitheia::ProjectFileMeasurement& measurement );

        eu::sqooss::impl::service::corba::alitheia::ProjectFileMeasurement toCorba() const;
        operator CORBA::Any() const;

        Metric metric;
        ProjectFile projectFile;
        std::string whenRun;
        std::string result;
    };

    class ProjectVersionMeasurement : public DAObject
    {
    public:
        ProjectVersionMeasurement() {}
        explicit ProjectVersionMeasurement( const eu::sqooss::impl::service::corba::alitheia::ProjectVersionMeasurement& measurement );

        eu::sqooss::impl::service::corba::alitheia::ProjectVersionMeasurement toCorba() const;
        operator CORBA::Any() const;

        Metric metric;
        ProjectVersion projectVersion;
        std::string whenRun;
        std::string result;
    };
}

#endif
