component {

	this.name = "LDEV-6129-basic";

	this.datasources[ "LDEV6129h2" ] = {
		class            : "org.h2.Driver",
		bundleName       : "org.lucee.h2",
		connectionString : "jdbc:h2:#expandPath( "db/LDEV6129" )#;MODE=MySQL",
		connectionLimit  : 1
	};

	this.ormEnabled  = true;
	this.datasource  = "LDEV6129h2";
	this.ormSettings = {
		dbcreate          : "dropcreate",
		dialect           : "h2",
		flushAtRequestEnd : true,
		autoManageSession : true
	};

	public function onRequestStart(){
		setting requesttimeout=10;
	}

}
