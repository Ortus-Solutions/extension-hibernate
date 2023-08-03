component persistent="true" {

	property
		name     ="id"
		type     ="string"
		fieldtype="id"
		ormtype  ="string";
	property name="make"     type="string";
	property name="model"    type="string";
	property name="dealerID" type="string";
	property
		name   ="inserted"
		type   ="boolean"
		default="false";
	property
		name   ="updated" 
		type   ="boolean"
		default="false";

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
