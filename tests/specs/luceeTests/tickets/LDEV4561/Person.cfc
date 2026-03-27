component persistent="true" cfcName="person" {

	property
		name     ="ID"
		type     ="numeric"
		ormtype  ="int"
		fieldtype="id"
		ormtype  ="long"
		generator="increment";
	property name="Person" type="string";

	public function preInsert(){
		eventLog( arguments );
	}

	private function eventLog( required struct args ){
		var eventName = callStackGet( "array" )[ 2 ].function;
		application.ormEventLog.append( "cfc_" & eventName );
	}

}
