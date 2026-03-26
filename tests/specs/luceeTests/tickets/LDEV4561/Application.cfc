component {

	this.name = "orm-events-order-LDEV4561";

	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasource                  = server.helpers.getDatasource( "h2", expandPath( "db" ) );

	this.ormEnabled  = true;
	this.ormSettings = {
		dbcreate          : "dropcreate",
		eventHandling     : true,
		eventHandler      : "eventHandler",
		autoManageSession : false,
		flushAtRequestEnd : false,
		useDBForMapping   : false,
		dialect           : "h2"
	};

	function onApplicationStart(){
		application.ormEventLog = [];
	}

	public function onRequestStart(){
		setting requesttimeout      =10;
		application.ormEventLog     = [];
		application.ormEventErrorLog= [];
	}

}
