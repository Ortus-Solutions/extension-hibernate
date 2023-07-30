/**
 * This entity is intended to contain a property for each supported field type.
 *
 * See HibernateCaster for a definitive list of supported types.
 */
component persistent="true" {

	property
		name     ="id"
		type     ="string"
		fieldtype="id"
		ormtype  ="string";

	property
		name   ="timezone"
		ormtype="timezone"
		default="America/Los_Angelos";

	property
		name   ="string"
		ormtype="string"
		default="johnwhish"
		length ="9";

	property
		name   ="integer"
		ormtype="integer"
		default="12303";

	property
		name   ="int"
		ormtype="integer"
		default="12404";

	property
		name   ="boolean"
		ormtype="boolean"
		default="false";

	property
		name   ="date"
		ormtype="date"
		default="2023-07-29";

	property
		name   ="datetime"
		ormtype="datetime"
		default="2023-07-29T04:56";

	property
		name   ="noinsert"
		ormtype="string"
		default="thedefault"
		insert =false;

	property
		name   ="noupdate"
		ormtype="string"
		default="thedefault"
		update =false;
	this.name = "KitchenSink";

}
