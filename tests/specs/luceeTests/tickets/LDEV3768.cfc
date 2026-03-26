component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll(){
		variables.uri = server.helpers.getTestPath( "tickets/LDEV3768" );
	}

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-3768", function(){
			it( title="ORMExecuteQuery(), HQL with correct column name to declare param", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { Scene = 1 }
				);
				expect( trim( result.filecontent ) ).toBe( "true" );
			});

			it( title="ORMExecuteQuery(), HQL with wrong case column name without param", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { Scene = 2 }
				);
				expect( trim( result.filecontent ) ).toInclude( "Named parameter not bound" ); // it should throw
			});

			it( title="ORMExecuteQuery(), HQL with wrong case column name to declare param", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { Scene = 3 }
				); // used to NPE
				expect( trim( result.filecontent ) ).toInclude( "entity names are case sensitive" );
			});

			it( title="ORMExecuteQuery(), HQL with no params", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { Scene = 4 }
				);
				expect( trim( result.filecontent ) ).toBe( "true" );
			});
		});
	}

}
