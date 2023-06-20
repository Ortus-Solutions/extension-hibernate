component {

	// ROOT_PATH = getDirectoryFromPath( getCurrentTemplatePath() ) );
	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "models" ]        = "/tests/models";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	// writeOutput( this.mappings[ "models" ] );abort;

	this.datasources[ "h2_HRdb" ] = {
		class            : "org.h2.Driver",
		bundleName       : "org.lucee.h2",
		bundleVersion    : "2.1.214.0001L",
		connectionString : "jdbc:h2:./tests/db/HR;MODE=MySQL",
		username         : "",
		password         : "",
		// optional settings
		connectionLimit  : -1, // default:-1
		liveTimeout      : 15, // default: -1; unit: minutes
		validate         : false // default: false
	};
	server.helpers  = new tests.specs.luceeTests.TestHelper();
	this.datasource = "h2";

	param form.ormSettings = "";

	this.ormenabled  = true;
	this.ormSettings = {
		dbcreate          : "update",
		flushatrequestend : false,
		automanagesession : false,
		datasource        : "h2",
		useDBForMapping   : false,
		cfclocation       : this.mappings[ "models" ]
	};
	if ( len( form.ormSettings ) ) {
		deserializeJSON( form.ormSettings ).each( ( key, value ) => this.ormSettings[ key ] = value );
	}
	// writeDump( this.ormSettings );abort;

	public function onRequestStart(){
		setting requesttimeout=10;
		if ( url.keyExists( "reinitApp" ) ) {
			applicationStop();
		}
		if ( url.keyExists( "ormReload" ) ) {
			ormReload();
		}
	}

	public function onRequest(){
		if ( form.keyExists( "closure" ) ) {
			// writeDump( form );abort;
			runner = evaluate( form.closure );
			runner();
		}
	}

}
