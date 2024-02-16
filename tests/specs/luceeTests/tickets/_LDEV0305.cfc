component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function run( testResults, testBox ){
		// TODO: Fix Me!
		// https://luceeserver.atlassian.net/browse/LDEV-0305
		var isResolved = FALSE;

		describe(
			title = "Test suite for LDEV-305",
			body  = function(){
				it( "checking property data type, attribute type='numeric' set with unsavedvalue='0' ", function( currentSpec ){
					var uri    = server.helpers.getTestPath( "tickets/LDEV0305/App1/index.cfm" );
					var result = _InternalRequest( template: uri );
					expect( result.filecontent.trim() ).toBe( "success" );
				} );

				it( "checking property data type, attribute ORMtype='numeric' set with unsavedvalue='0'", function( currentSpec ){
					var uri    = server.helpers.getTestPath( "tickets/LDEV0305/App2/index.cfm" );
					var result = _InternalRequest( template: uri );
					expect( result.filecontent.trim() ).toBe( "success" );
				} );

				it( "checking property data type, attribute type='numeric' ", function( currentSpec ){
					var uri    = server.helpers.getTestPath( "tickets/LDEV0305/App3/index.cfm" );
					var result = _InternalRequest( template: uri );
					expect( result.filecontent.trim() ).toBe( "success" );
				} );
			},
			skip = !isResolved || !server.helpers.canUseDatasource( "mysql" )
		);
	}

}
