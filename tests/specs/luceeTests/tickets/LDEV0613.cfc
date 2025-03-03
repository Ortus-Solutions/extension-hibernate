component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll(){
	}
	function run( testResults, testBox ){
		describe( "Running hql query with script", function(){
			it( "With OrmExecuteQuery", function( currentSpec ){
				uri          = server.helpers.getTestPath( "tickets/LDEV0613/index.cfm" );
				local.result = _InternalRequest( template: uri, forms: { Scene : 1 } );
				assertEquals( "", result.filecontent.trim() );
			} );
		} );

		describe( "Running hql query with tag", function(){
			it( "With dbtype hql", function( currentSpec ){
				uri          = server.helpers.getTestPath( "tickets/LDEV0613/index.cfm" );
				local.result = _InternalRequest( template: uri, forms: { Scene : 2 } );
				assertEquals( "", left( result.filecontent.trim(), 100 ) );
			} );
		} );
	}

}
