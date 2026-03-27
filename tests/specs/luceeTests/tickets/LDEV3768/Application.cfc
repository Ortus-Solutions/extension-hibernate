component {

	this.name = "LDEV3768";

	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasources[ "testH2" ]     = server.helpers.getDatasource( "h2", expandPath( "db" ) );

	this.ORMenabled  = true;
	this.ormSettings = {
		datasource      : "testH2",
		dbCreate        : "dropcreate",
		useDBForMapping : false,
		dialect         : "h2"
	};

	public function onRequestStart(){
		setting requesttimeout=10;
	}

}
