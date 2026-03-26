component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	public void function testEventsTriggerOrder(){
		local.uri = server.helpers.getTestPath( "tickets/LDEV4561/preInsert.cfm" );
		local.result = _InternalRequest( uri );
		expect( result.status ).toBe( 200 );
		local.res = deserializeJson( result.fileContent );

		expect( res.errors.len() ).toBe( 0, "errors #res.errors.toJson()#" );
		expect( res.events.len() ).toBe( 2 );
		expect( res.events ).toBe( [ "cfc_preInsert", "global_preInsert" ] );
	}

}
