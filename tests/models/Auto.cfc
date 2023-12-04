component accessors="true" persistent="true" {

	property
		name     ="id"
		type     ="string"
		fieldtype="id"
		ormtype  ="string";
	property name="make"  type="string";
	property name="model" type="string";
	property
		name     ="dealer"
		fieldtype="many-to-one"
		cfc      ="Dealership"
		fkcolumn ="dealerID";
	property
		name   ="inserted"
		type   ="boolean"
		default="false";
	property
		name   ="updated" 
		type   ="boolean"
		default="false";
	property
		name      ="nonPersistentProp"
		type      ="string"
		persistent="false";

	this.name = "Auto";

	/**
	 * Quick test for OOE-2
	 * https://ortussolutions.atlassian.net/browse/OOE-2
	 */
	function preInsert(){
		setInserted( true );
	}

	function preUpdate(){
		setUpdated( true );
	}

}
