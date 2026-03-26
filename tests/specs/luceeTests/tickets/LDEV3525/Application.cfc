component displayname="Application" output="false" {

	param name="url.autogenmap" default="true";
	this.name = "autogenmap-missing-LDEV-3525-#url.autogenmap#";

	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasource                  = server.helpers.getDatasource( "h2", expandPath( "db" ) );

	this.sessionManagement  = true;
	this.setClientCookies   = true;
	this.setDomainCookies   = false;
	this.sessionTimeOut     = createTimeSpan( 0, 1, 0, 0 );
	this.applicationTimeOut = createTimeSpan( 1, 0, 0, 0 );

	this.ormenabled            = true;
	this.ormSettings.savemapping = true;
	this.ormSettings.autogenmap = url.autogenmap;

	if ( url.autogenmap )
		this.ormSettings.dbcreate = "dropcreate";

}
