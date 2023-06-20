component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm,cache,ehCache" {

	function run( testResults, testBox ){
		describe( "Test suite for LDEV-1741", () => {
			it( "checking ORM secondary ehcache with this.ormsettings.cacheconfig = 'ehcache.xml' ", () => {
				var uri     = server.helpers.getTestPath( "tickets/LDEV1741" );
				var result1 = _InternalRequest( template: "#uri#/App1/index.cfm", urls: { appName : "MyAppOne" } );

				var result2 = _InternalRequest( template: "#uri#/App1/index.cfm", urls: "appName=MyAppTwo" );

				assertEquals( 200, result2.status_code );

				if ( result2.status_code == 200 ) assertEquals( "Bar", result2.filecontent.trim() );
			} );

			it( "checking ORM secondary ehcache without cacheconfig", () => {
				var uri     = server.helpers.getTestPath( "tickets/LDEV1741" );
				var result1 = _InternalRequest( template: "#uri#/App2/index.cfm", urls: { appName : "testOne" } );

				var result2 = _InternalRequest( template: "#uri#/App2/index.cfm", urls: { appName : "testTwo" } );

				assertEquals( 200, result2.status_code );
				if ( result2.status_code == 200 ) assertEquals( "Bar", result2.filecontent.trim() );
			} );
		} );
	}

}
