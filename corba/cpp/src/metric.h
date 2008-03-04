#ifndef METRIC_H
#define METRIC_H

#include "Alitheia.h"

namespace Alitheia
{
    class Core;

    class Metric : virtual public POA_alitheia::Metric
    {
        friend class ::Alitheia::Core;
    protected:
        Metric();
    public:
        ~Metric();

        virtual char* getAuthor();
        virtual char* getDescription();
        virtual char* getName();
        virtual char* getVersion();
        virtual char* getResult();
        virtual char* getDateInstalled();

        virtual std::string author() const = 0;
        virtual std::string description() const = 0;
        virtual std::string name() const = 0;
        virtual std::string version() const = 0;
        virtual std::string result() const = 0;
        virtual std::string dateInstalled() const = 0;

    protected:
        /**
         * @return The name of the metric as it was exported in the ORB.
         * This has nothing to do with getName()
         */
        const std::string& orbName() const;
        /**
         * Sets the name of the object as it is exported in the ORB to \a name.
         * This is set by the core.
         */
        void setOrbName( const std::string& orbName );

        int id() const;
        void setId( int id );

    private:
        class Private;
        Private* d;
    };
}

#endif
