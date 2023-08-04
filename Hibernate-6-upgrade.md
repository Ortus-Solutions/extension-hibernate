# Hibernate 6.x upgrade

* Upgrade to Hibernate 6.x
  * Update schema exports to use [SchemaManagementTool](https://docs.jboss.org/hibernate/orm/6.0/javadocs/org/hibernate/tool/schema/spi/SchemaManagementTool.html) 
  * Update HBMCreator to generate JPA xml format
    * To be fair, this is a MUCH easier path than switching to java annotations.
  * AbstractEntityTuplizer to [EntityRepresentationStrategy](https://docs.jboss.org/hibernate/orm/6.3/javadocs/org/hibernate/metamodel/spi/ManagedTypeRepresentationStrategy.html)
  * Criteria queries to JPA criteria queries OR to JPQL, since that will not enforce typing.
  * HQLQueryPlan to some subinterface of [QueryPlan](https://docs.jboss.org/hibernate/orm/6.3/javadocs/org/hibernate/query/spi/QueryPlan.html)
  * Drop versioned dialects usage, since [Hibernate 6 can introspect whatever version info it needs](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#dialects).

## Resources and Migration Guides

* https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc
* https://in.relation.to/2021/12/08/hibernate-orm-562/
* https://docs.jboss.org/hibernate/orm/5.6/javadocs/deprecated-list.html
* https://hibernate.atlassian.net/browse/HHH-11828