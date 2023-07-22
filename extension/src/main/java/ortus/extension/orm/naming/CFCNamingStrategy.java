package ortus.extension.orm.naming;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.naming.NamingStrategy;
import lucee.runtime.type.UDF;

import ortus.extension.orm.runtime.type.KeyImpl;
import ortus.extension.orm.util.CommonUtil;

public class CFCNamingStrategy implements NamingStrategy {

    Component cfc;

    public CFCNamingStrategy( String cfcName ) throws PageException {
        this.cfc = CFMLEngineFactory.getInstance().getThreadPageContext().loadComponent( cfcName );
    }

    public Component getComponent() {
        return cfc;
    }

    @Override
    public String convertTableName( String tableName ) {
        return call( "getTableName", tableName );
    }

    @Override
    public String convertColumnName( String columnName ) {
        return call( "getColumnName", columnName );
    }

    private String call( String functionName, String name ) {
        Object res = cfc.get( new KeyImpl( functionName ), null );
        if ( ! ( res instanceof UDF ) )
            return name;
        CFMLEngine engine = CFMLEngineFactory.getInstance();
        try {
            return engine.getCastUtil()
                    .toString( cfc.call( engine.getThreadPageContext(), functionName, new Object[] { name } ) );
        } catch ( PageException pe ) {
            throw engine.getCastUtil().toPageRuntimeException( pe );
        }
    }

    @Override
    public String getType() {
        return "cfc";
    }

}
