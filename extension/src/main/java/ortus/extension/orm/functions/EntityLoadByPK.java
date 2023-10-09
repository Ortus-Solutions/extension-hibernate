package ortus.extension.orm.functions;

import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ORMUtil;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.util.Cast;
import lucee.runtime.ext.function.BIF;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;

/**
 * Built-in CFML method to load entity by primary key.
 */
public class EntityLoadByPK extends BIF {
    private static final int MIN_ARGUMENTS = 2;
    private static final int MAX_ARGUMENTS = 2;

    public static Object call( PageContext pc, String name, Object oID ) throws PageException {
        ORMSession session = ORMUtil.getSession( pc );
        String id;
        if ( CommonUtil.isBinary( oID ) )
            id = CommonUtil.toBase64( oID );
        else
            id = CommonUtil.toString( oID );
        return session.load( pc, name, id );
        // FUTURE call instead load(..,..,OBJECT);
    }

    @Override
    public Object invoke( PageContext pc, Object[] args ) throws PageException {
        CFMLEngine engine = CFMLEngineFactory.getInstance();
        Cast cast = engine.getCastUtil();

        if ( args.length == 2 )
            return call( pc, cast.toString( args[ 0 ] ), args[ 1 ] );

        throw engine.getExceptionUtil().createFunctionException( pc, "EntityLoadByPK", MIN_ARGUMENTS, MAX_ARGUMENTS, args.length );
    }
}