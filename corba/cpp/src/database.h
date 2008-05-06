#ifndef DATABASE_H
#define DATABASE_H

namespace CORBA
{
    class Any;
}

namespace Alitheia
{
    class DAObject;

    class Database
    {
    public:
        Database();
        virtual ~Database();

        template< class T >
        bool addRecord( T& object )
        {
            CORBA::Any corbaObject = object;
            const bool result = addCorbaRecord( corbaObject );
            object = T::fromCorba( corbaObject );
            return result;
        }

        bool deleteRecord( const DAObject& object );
        
        template< class T >
        bool updateRecord( T& object )
        {
            CORBA::Any corbaObject = object;
            const bool result = updateCorbaRecord( corbaObject );
            object = T::fromCorba( corbaObject );
            return result;
        }
        
        template< class T >
        T findObjectById( int id )
        {
            return T::fromCorba( *findCorbaObjectById( T(), id ) );
        }

    private:
        CORBA::Any* findCorbaObjectById( const CORBA::Any& type, int id );
        bool addCorbaRecord( CORBA::Any& record );
        bool updateCorbaRecord( CORBA::Any& record );

        class Private;
        Private* const d;
    };
}

#endif
