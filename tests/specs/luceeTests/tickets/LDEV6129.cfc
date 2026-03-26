component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll(){
		variables.uri = server.helpers.getTestPath( "tickets/LDEV6129/basic" );
		_InternalRequest( template: "#variables.uri#/setup.cfm" );
	}

	function run( testResults, testBox ){
		describe( "LDEV-6129/6156 - ORM connection not released when flushAll() throws", function(){
			it(
				title = "connection returned to pool even when auto-flush throws a constraint violation",
				body  = function( currentSpec ){
					// Trigger the potential leak: unique constraint violation at request end
					try {
						_InternalRequest( template: "#variables.uri#/flush_leak.cfm" );
					} catch ( any e ) {
						// flush error is expected
						systemOutput( "flush_leak threw: #e.message#", true );
					}

					// Now verify the connection is actually usable
					var N = 5;
					for ( var i = 1; i <= N; i++ ) {
						var result = _InternalRequest( template: "#variables.uri#/simple.cfm" );
						systemOutput(
							"simple request #i#: status=#result.status#, content=#trim( result.filecontent )#",
							true
						);
						expect( result.status ).toBe(
							200,
							"simple request #i# failed — connection not available (pool exhausted?)"
						);
						expect( left( trim( result.filecontent ), 2 ) ).toBe(
							"ok",
							"simple request #i# returned unexpected content: #trim( result.filecontent )#"
						);
					}
				}
			);
		} );

		describe( "LDEV-6129/6156 - dead reconnect code throws when session.isConnected() returns false", function(){
			it(
				title = "entityLoad succeeds when session isConnected() is forced false via reflection",
				body  = function( currentSpec ){
					var result = _InternalRequest( template: "#variables.uri#/reconnect_leak.cfm" );
					systemOutput(
						"reconnect_leak result: status=#result.status#, content=#trim( result.filecontent )#",
						true
					);

					expect( result.status ).toBe( 200 );
					expect( left( trim( result.filecontent ), 2 ) ).toBe(
						"ok",
						"entityLoad failed after isConnected()=false — reconnect dead code is broken: #trim( result.filecontent )#"
					);
				}
			);
		} );
	}

}
