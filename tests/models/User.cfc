component persistent="true" cacheUse="read-write" {

	property
		name     ="id"
		type     ="string"
		fieldtype="id"
		ormtype  ="string";
	property name="name" type="string";
	property
		name   ="username"
		type   ="string"
		notnull="true";
	property
		name   ="password"
		type   ="string"
		notnull="true";
	property
		name     ="createdOn"
		ormtype  ="datetime"
		dbdefault="2016-10-10";

	property name="dateCreated" ormType="timestamp";
	property name="dateUpdated" ormType="timestamp";

	this.name = "User";

	function preInsert(){
		setDateCreated( now() );
		if ( isNull( getPassword() ) ) {
			setPassword( createUUID() );
		}
	}
	function preUpdate(){
		setDateUpdated( now() );
		if ( isNull( getPassword() ) ) {
			setPassword( createUUID() );
		}
	}

}
