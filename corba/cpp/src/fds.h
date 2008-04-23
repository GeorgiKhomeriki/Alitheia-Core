#ifndef FDS_H
#define FDS_H

#include <string>
#include <vector>

#include "dbobject.h"

namespace Alitheia
{
    class Checkout
    {
    public:
        Checkout() {}
        explicit Checkout( const eu::sqooss::impl::service::corba::alitheia::Checkout& checkout );
        virtual ~Checkout();

        ProjectVersion version;
        std::vector< ProjectFile > files;
    };

    class FDS
    {

    public:
        /**
         * Constructor.
         */
        FDS();

        /**
         * Destructor.
         */
        virtual ~FDS();

        std::string getFileContents( const ProjectFile& file ) const;

        Checkout getCheckout( const ProjectVersion& version ) const;

    private:
        class Private;
        Private* d;
    };
}

#endif
