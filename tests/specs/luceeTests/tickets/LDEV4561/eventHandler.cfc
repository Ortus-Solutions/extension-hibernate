component hint="logs out any orm events" {

	this.name = "global"; // used for logging out events

	function init(){
		return this;
	}

	function preInsert( entity, event ){
		eventLog( arguments );
	}

	private function eventLog( required struct args ){
		var eventName = callStackGet( "array" )[ 2 ].function;
		application.ormEventLog.append( "global_" & eventName );
	}

}
