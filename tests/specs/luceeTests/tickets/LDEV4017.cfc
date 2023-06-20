component
	extends="org.lucee.cfml.test.LuceeTestCase"
	labels ="ORM"
	skip   =true
{

	function beforeAll(){
		variables.uri = server.helpers.getTestPath( "tickets/LDEV4017" );
	}

	function run( testResults, testBox ){
		describe( "Testcase for LDEV4017", function(){
			it(
				title = "Access the lazy-loaded ORM entity after the transaction ends",
				skip  = !!server.helpers.isValidDatasource( "h2" ),
				body  = function( currentSpec ){
					var result = _InternalRequest(
						template: "#variables.uri#/LDEV4017.cfm",
						forms   : { uuid : createUUID(), dbfile : variables.dbfile }
					);
					expect( trim( result.filecontent ) ).toBe(
						"person.hasthoughts: true & lazy-loaded works outside of transcation"
					);
				}
			);
		} );
	}

}
