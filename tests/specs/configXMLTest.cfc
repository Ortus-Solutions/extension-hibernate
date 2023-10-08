component extends="testbox.system.BaseSpec" {

	public void function run(){
		/**
		 * TODO: Get these tests to pass!
		 */
		describe( "ormConfig.xml", () => {
			beforeEach( () => {
				var result            = _internalRequest( "/tests/testApp/index.cfm?reinitApp=true" );
				variables.ormSettings = {
					dbcreate  : "dropcreate",
					ormConfig : "/tests/resources/hibernate.cfg.xml"
				};
			} );
			it( "can set ad hoc config properties in Hibernate SessionFactory", () => {
				var xml = "
<!DOCTYPE hibernate-configuration PUBLIC 
    ""-//Hibernate/Hibernate Configuration DTD 3.0//EN"" 
    ""http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd"">
<hibernate-configuration>
    <session-factory>
		<property name=""hibernate.session_factory_name"">OrtusForTheWin</property>
		<property name=""hibernate.globally_quoted_identifiers"">true</property>
    </session-factory>
</hibernate-configuration>";
				fileWrite( "/tests/resources/hibernate.cfg.xml", xml );

				var theTest = () => {
					ormReload();
					ormGetSession();
					var factoryName = ormGetSessionFactory().getSessionFactoryOptions().getSessionFactoryName();
					if ( factoryName != "OrtusForTheWin" ) {
						throw( "expected factoryName '#factoryName#' to be 'bla-bla.test'" );
					}
				};
				var result = _internalRequest(
					template: "/tests/testApp/index.cfm",
					forms   : {
						ormSettings : serializeJSON( variables.ormSettings ),
						closure     : serialize( theTest )
					}
				);
			} );

			/**
			 * We ensure invalid XML files do not throw, for backwards compatibility.
			 *
			 * TODO: Next major bump should flip this to a .toThrow() test.
			 */
			it( "DOES NOT throw on invalid XML", () => {
				var xml = "
<?xml version = ""1.0"" encoding = ""utf-8""?>
<!DOCTYPE hibernate-configuration SYSTEM 
""http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd"">
<hibernate-configuration>
    <ba ba black sheep...
</hibernate-configuration>
";
				fileWrite( "/tests/resources/hibernate.cfg.xml", xml );

				ormGetSession();
				var theTest = () => {
				};

				expect( () => {
					_internalRequest(
						template: "/tests/testApp/index.cfm",
						url     : { "ormReload" : true },
						forms   : {
							ormSettings : serializeJSON( variables.ormSettings ),
							closure     : serialize( theTest )
						}
					);
				} ).notToThrow();
			} );
		} )
	}

}
