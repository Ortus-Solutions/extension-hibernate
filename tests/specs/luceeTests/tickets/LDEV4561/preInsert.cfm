<cfsetting showdebugoutput="false">
<cfscript>
	person = entityNew( "Person" );
	person.setPerson( "ralio" );
	entitySave( person );

	ormFlush();
	ormClearSession();

	result = {
		events : [],
		errors : [],
		person : entityLoadByPK( "Person", 1 ).getPerson()
	};

	loop array=application.ormEventLog item="a" {
		arrayAppend( result.events, a );
	};

	loop array=application.ormEventErrorLog item="a" {
		arrayAppend( result.errors, a );
	};

	echo( result.toJson() );
</cfscript>
