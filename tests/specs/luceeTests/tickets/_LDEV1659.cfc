component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function run( testResults , testBox ) {
		// 😭😭😭😭
		// https://luceeserver.atlassian.net/browse/LDEV-0305
		var isResolved = FALSE;

		describe( title="Test suite for LDEV-1659", body=function() {
			it( 'Initialize ORM Secondary cache', function( currentSpec ) {
				var result = _InternalRequest(
					template: server.helpers.getTestPath('tickets/LDEV1659/index.cfm'),
					urls: { AppName="myAppTwo" }
				);
				var result2 = _InternalRequest(
					template: server.helpers.getTestPath('tickets/LDEV1659/index.cfm'),
					urls: { AppName="myAppOne" }
				);
			});

			it( 'Checking ORM cache connection', function( currentSpec ) {
				var result3 = _InternalRequest(
					template: server.helpers.getTestPath('tickets/LDEV1659/index.cfm'),
					urls: { AppName="myAppTwo" }
				);
				var result4 = _InternalRequest(
					template: server.helpers.getTestPath('tickets/LDEV1659/index.cfm'),
					urls: { AppName="myAppOne" }
				);
				assertEquals("1234", result4.filecontent.trim());
			});
		}, skip = !isResolved );
	}
}