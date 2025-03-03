component {

	this.name = "LDEV1214" & hash( getCurrentTemplatePath() );


	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasource                  = server.helpers.getDatasource( "h2", expandPath( "./db/LDEV1214" ) );

	this.ormEnabled  = true;
	this.ormSettings = {
		dbcreate              : "update",
		secondarycacheenabled : false,
		flushAtRequestEnd     : false,
		autoManageSession     : false,
		secondaryCacheEnabled : false,
		eventhandling         : true
	};

	public function onRequestStart(){
		setting requesttimeout=10;
	}

}
