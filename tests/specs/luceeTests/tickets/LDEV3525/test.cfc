component
	displayname="test"
	entityname ="test"
	table      ="test"
	persistent =true
	output     =false
	accessors  =true
{

	property
		name        ="ID"
		ormtype     ="string"
		length      ="32"
		fieldtype   ="id"
		generator   ="uuid"
		unsavedvalue=""
		default     ="1";
	property name="activeFlag" ormtype="boolean";
	property name="urlTitle"   ormtype="string";
	property name="Name"       ormtype="string";

}
