<cfparam name="FORM.scene" default="">
<cfscript>
	try {
		if ( form.scene == 1 ){
			res = isArray( ORMExecuteQuery( "From test where Ant = :ok", { "ok": "lucee" } ) );
			res = isArray( ORMExecuteQuery( hql="From test where Ant = :ok", params={ "ok": "lucee" } ) );
		} else if ( form.scene == 2 ){
			// no param checking at all, coz no params
			res = isArray( ORMExecuteQuery( hql="From test where ant = :ok" ) );
		} else if ( form.scene == 3 ){
			// oh look, ant is lowercase, should be Ant
			res = isArray( ORMExecuteQuery( hql="From test where ant = :ok", params={ "ok": "lucee" } ) );
			res = isArray( ORMExecuteQuery( "From test where ant = :ok", { "ok": "lucee" } ) );
		} else if ( form.scene == 4 ){
			// no param checking at all, coz no params
			res = isArray( ORMExecuteQuery( "From test where ant = 'lucee'" ) );
		}
	} catch ( any e ){
		res = e.stacktrace;
	}
	writeoutput( res );
</cfscript>
