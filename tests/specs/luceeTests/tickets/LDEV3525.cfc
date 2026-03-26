component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll(){
		variables.uri = server.helpers.getTestPath( "tickets/LDEV3525" );
		cleanup();
	}

	function afterAll(){
		cleanup();
	}

	private function cleanup(){
		var hbmFile = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3525/test.cfc.hbm.xml";
		if ( fileExists( hbmFile ) ){
			fileDelete( hbmFile );
		}
	}

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-3525", function(){

			it( "test autogenmap=false and missing xml mapping file", function(){
				expect( function(){
					local.result = _InternalRequest(
						template : "#uri#/index.cfm",
						url : {
							autogenmap : false
						}
					);
				}).toThrow( regex="Hibernate mapping not found for entity" );
			});

			it( "test autogenmap=true and missing xml mapping file", function(){
				cleanup();
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					url : {
						autogenmap : true
					}
				);
				expect( result.fileContent.trim() ).toBe( "testing" );
			});

		});
	}

}
