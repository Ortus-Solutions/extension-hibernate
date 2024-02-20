# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### 🐛 Fixed

- Fixes empty string values coercing to `NULL` when no property type is declared. - Resolves [OOE-25](https://ortussolutions.atlassian.net/browse/OOE-25), introduced in 6.5.0.

## [6.5.0] - 2024-02-16

### 🐛 Fixed

-   Fixes an incorrect property name lookup for the `unsavedValue` persistent property attribute.
-   Fixes the pre-event listeners to ignore empty strings in entity state properties if the field type is one of `string`, `character`, or `text`. This resolves issues where a `preInsert()` or `preUpdate()` throws a "can't cast \[] to date value" when processing event listeners if a date field (for example) is unpopulated or has an empty `default` attribute.

### ♻️ Changed

Add the entity name to the exception message when attempting to persist changes from `preInsert` or `preUpdate` event listeners. The updated exception message is now:

> Error populating event state for persistance in [&lt;entity name&gt;] entity pre-event listener method: &lt;error message from Hibernate>

### 🔐 Security

Bump Lucee build dependency to `5.4.4.38` to avoid [vulnerable dependencies in the build process](https://mvnrepository.com/artifact/org.lucee/lucee/5.4.1.8).

## [6.4.0] - 2023-12-05

### 🔐 Security

Resolve an [Uncontrolled Resource Consumption](https://security.snyk.io/vuln/SNYK-JAVA-CHQOSLOGBACK-6097493) vulnerability disclosed on 12/4/2023 by upgrading `logback-core` to `1.3.14`. [See vulnerability details](https://security.snyk.io/vuln/SNYK-JAVA-CHQOSLOGBACK-6097493). 

### ⭐ Added

New `ORMQueryExecute()` alias for the `ORMExecuteQuery`. This new alias behaves identically to the `ORMExecuteQuery()` method, but is named consistently with the `queryExecute()` method.

### 🐛 Fixed

-   Fixes custom configuration support via `this.ormSettings.ormConfig`.
-   Fixes named argument support for `entityLoad()` - [LDEV-4285](https://luceeserver.atlassian.net/browse/LDEV-4285)
-   Fixes named argument support for `entityLoadByPK()` - [LDEV-4461](https://luceeserver.atlassian.net/browse/LDEV-4461)

### ♻️ Changed

While not technically a change in ORM functionality, the `useDBforMapping` implementation has been greatly improved "under the hood", with tests to boot.

## [6.3.2] - 2023-09-29

### 🐛 Fixed

Fixed pre-event listeners to include parent component properties when checking for entity mutations to persist back to the event entity state. This resolves issues with changes made in `preInsert()`/`preUpdate()` not persisting if the changes are made on a persistent property from a parent component. Resolves [OOE-14](https://ortussolutions.atlassian.net/browse/OOE-14).

## [6.3.1] - 2023-09-26

### 🐛 Fixed

Refactored nullability checks to occur _after_ pre-event listener methods fire. Resolves [OOE-12](https://ortussolutions.atlassian.net/browse/OOE-12)

### ⭐ Added

Added context to the error message in `CFCGetter`, which handles retrieving entity values from Hibernate code. This improves odd error messages in some edge cases with the Hibernate tuplizer.

## [6.3.0] - 2023-08-18

### 🔐 Security

Switched the [EHCache](https://mvnrepository.com/artifact/net.sf.ehcache/ehcache/2.10.6) library to use [net.sf.ehcache.internal:ehcache-core](https://mvnrepository.com/artifact/net.sf.ehcache.internal/ehcache-core/2.10.9.2).

-   Upgrades EHCache version from `2.10.6` to `2.10.9.2`.
-   Drops an embedded `rest-management-private-classpath` directory
-   Drops a number of (unused) vulnerable jackson and jetty libraries such as jackson-core.
-   As an added bonus, this reduces the final `.lex` extension file size by over 6 MB. 🎉

**Note:** While it is not 100% clear, [some of these CVEs may have been false positives](https://github.com/jeremylong/DependencyCheck/issues/517).

## [6.2.0] - 2023-08-03

### ♻️ Changed

#### Hibernate Upgraded from 5.4 to 5.6

This brings the Hibernate dependencies up to date (released Feb. 2023), and should not change any CFML-facing features for _most_ users. (See [CLOB columns in Postgres81](#clob-columns-in-postgres81))

See the migration guides for more info:

-   [Hibernate 5.4 -> 5.4 migration guide](https://github.com/hibernate/hibernate-orm/blob/5.5/migration-guide.adoc)
-   [Hibernate 5.5 -> 5.6 migration guide](https://github.com/hibernate/hibernate-orm/blob/5.6/migration-guide.adoc)

#### CLOB columns in Postgres81

Due to the Hibernate 5.6 upgrade, if you are using the `PostgreSQL81` dialect and have `CLOB` columns in your database, [it is recommended you migrate existing text columns for LOBs to `oid`](https://github.com/hibernate/hibernate-orm/blob/5.6/migration-guide.adoc#changes-to-the-ddl-type-for-clob-in-postgresql81dialect-and-its-subclasses).

#### Default EHCache Configuration

The default `ehcache.xml` for EHCache changed to include [`clearOnFlush="true"`](https://www.ehcache.org/apidocs/2.10.1/net/sf/ehcache/config/CacheConfiguration.html#clearOnFlush) and [`diskSpoolBufferSizeMB="30MB"`](https://www.ehcache.org/apidocs/2.10.1/net/sf/ehcache/config/CacheConfiguration.html#diskSpoolBufferSizeMB) properties to match [Adobe ColdFusion 9's  default `ehCache.xml` config](https://helpx.adobe.com/coldfusion/developing-applications/coldfusion-orm/performance-optimization/caching.html). Both these values represent default settings in EHCache itself.

### 🐛 Fixed

-   Fixes handling of `"timezone"`-typed column values. Previously, fields defined with `ormtype="timezone"` would neither use the `default` value nor allow new values to be set. [OOE-10](https://ortussolutions.atlassian.net/browse/OOE-10)
-   Fixes entity state changes in `preInsert()`/`preUpdate()` listeners for properties with no `default` defined. [OOE-9](https://ortussolutions.atlassian.net/browse/OOE-9)

## [6.1.0] - 2023-07-14

### ♻️ Changed

-   Lots of java source code cleanup that won't affect the CFML experience, but will aid in faster development and fewer bugs. 

### 🐛 Fixed

-   Any hibernate exceptions returned during schema generation are once again logged to the Lucee ORM log file.

### 💥 Removed

-   Dropped the public `getDialectNames()` method from the Dialect class. This method was unused (to my knowledge) and unnecessary.

### 🔐 Security

-   Switched to [Snyk vulnerability scanner](https://github.com/snyk/actions/tree/master/maven-3-jdk-11) to limit false positives. Security vulnerabilities will now be published on the [GitHub repository's Security Advisories page](https://github.com/Ortus-Solutions/extension-hibernate/security/advisories).
-   Bumped Lucee dependency to `5.40.80` to remove vulnerability notices on [org.apache.tika:tika-core](https://security.snyk.io/vuln/SNYK-JAVA-ORGAPACHETIKA-2936441) and [commons-net:commons-net](https://security.snyk.io/vuln/SNYK-JAVA-COMMONSNET-3153503). These vulnerabilities are only theoretical, since Lucee is a `provided` dependency and not bundled with the extension.

## [6.0.0] - 2023-07-01

### ⭐ Added

#### Second-Level Caching

The extension will now throw an error if you try to configure an unsupported cache provider like `"jbosscache"`, `"swarmcache"`, etc. Previously, the extension would silently switch to ehcache if any cache provider besides EHCache was configured.

#### Hibernate Logging

This version re-enables Hibernate logging via SLF4j and LogBack. Hibernate root and cache loggers are defaulted to `ERROR` level, while SQL logging is set to `DEBUG` if `this.ormSettings.logSQL` is enabled. (Set to `true`.)

#### OWASP Dependency CVE Scans

The extension [GitHub Release page](https://github.com/Ortus-Solutions/extension-hibernate/releases/latest) now generates a dependency CVE report via [Jeremy Long's OWASP dependency-check maven plugin](https://jeremylong.github.io/DependencyCheck/index.html). Any known CVEs contained in dependencies ( excluding `test` and `provided`-scoped dependencies) will be noted in [each release's CVE report artifact](https://github.com/Ortus-Solutions/extension-hibernate/releases/download/latest/owasp-cve-report.html).

### ♻️ Changed

#### New Repo Layout

-   Java source moved to `extension/src/main/java`
-   All java classes are now under the `ortus.extension.orm` package
-   Dropped the java source format-on-push in favor of format-on-save IDE tooling

#### New Test Layout

-   Internal tests rewritten to native Testbox specs
-   Cloned all ORM tests from the Lucee repository
-   Updated to TestBox 5.0

#### New Build (and .jar file) Layout

We re-architected the build to inline most dependencies. I.e. we no longer copy in extension dependencies as (custom-built) OSGI bundles, but instead as compiled classes.

-   This resolves intermittent issues with bundle resolution and/or duplicate bundle collision upon installing the ORM extension into a Lucee server prior to uninstalling the Lucee Hibernate extension.
-   This also removes a number of direct dependencies on custom OSGI bundles, thus it is more reliable and will offer easier dependency upgrades with less pain.

#### Other

-   The `"node"` attribute is deprecated in Hibernate 5.x, and is no longer generated on HBM/XML mapping files to avoid Hibernate warning that "Use of DOM4J entity-mode is considered deprecated".

### 🐛Fixed

-   The `.fld` definition file for all built-ins was missed during the conversion to a Maven build. (Since [v5.4.29.25](https://github.com/Ortus-Solutions/extension-hibernate/releases/tag/v5.4.29.25)). This caused the `orm*()` and `entity*()` built-in method calls to be picked up by Lucee core before being routed to this extension. No known errors resulted from this mistake, but we feel embarrassed anyway. 😅
-   Clear ORM context data once per ORM reload, not once per ORM entity parsing. This should improve ORM startup/reload time and avoid difficult session or cache manager lifecycle issues.

## [5.4.29.28] - 2023-06-07

### 🐛 Fixed

We now set the JAXB `ContextFactory` system property based on the JRE version. If less than JRE 11, we set `javax.xml.bind.context.factory=com.sun.xml.bind.v2.ContextFactory`. If JRE 11 or greater, we set `javax.xml.bind.JAXBContextFactory=com.sun.xml.bind.v2.ContextFactory`.

This prevents the following warning from being logged on each ORM method call:

    WARNING: Using non-standard property: javax.xml.bind.context.factory. Property javax.xml.bind.JAXBContextFactory should be used instead.

See [OOE-3](https://ortussolutions.atlassian.net/browse/OOE-3).

## [5.4.29.27] - 2023-05-29

### 🐛 Fixed

-   We now set a `javax.xml.bind.context.factory=com.sun.xml.bind.v2.ContextFactory` System property to ensure the JAXB API can find its implementation in CommandBox environments. This may trigger a log message, but shouldn't cause any concern. Vanilla Tomcat installations _may_ need to overwrite or clear this property. [LDEV-4276](https://luceeserver.atlassian.net/browse/)

## [5.4.29.26] - 2023-05-24

### ♻️ Changed

-   Improved logo for Lucee admin 🤩

### 🐛 Fixed

-   Entity changes made in `onPreInsert()` and `onPreUpdate()` do not persist [OOE-2](https://ortussolutions.atlassian.net/browse/OOE-2)

## [5.4.29.25] - 2023-05-23

### ♻️ Changed

-   Switched to Maven for a faster, more stable build process
-   Improved entity event listeners for a much speedier ORM startup ([8924b58a9058d296e2a783ccfabbf90e26dc9c1b](https://github.com/Ortus-Solutions/extension-hibernate/commit/8924b58a9058d296e2a783ccfabbf90e26dc9c1b))
-   New and Improved logo for Lucee admin visibility ([10bdf56a7a78f0221ab1a6e66a5512a92819e5b7](https://github.com/Ortus-Solutions/extension-hibernate/commit/10bdf56a7a78f0221ab1a6e66a5512a92819e5b7))

### 🐛 Fixed

-   Entity has no state when listener method (`onPreInsert`, for example) is fired ([014814263b5d31b8bac4c17479c2ca731ceb4e7c](https://github.com/Ortus-Solutions/extension-hibernate/commit/014814263b5d31b8bac4c17479c2ca731ceb4e7c), [OOE-1](https://ortussolutions.atlassian.net/browse/OOE-1))

## [5.4.29.24] - 2023-05-17

### 🔐 Security

-   Upgraded dom4j library from 1.6.1 to 2.1.4. This removes [two potential vulnerabilities](https://mvnrepository.com/artifact/dom4j/dom4j/1.6.1) in dom4j's XML parsing capabilities.

## [5.4.29.23] - 2023-05-15

### 🐛 Fixed

-   ORMExecuteQuery ignores `"unique"` argument if `options` struct is passed

## [5.4.29.22] - 2023-05-11

### ⭐ Added

-   Adds support for `autoGenMap=false` - [LDEV-3525](https://luceeserver.atlassian.net/browse/LDEV-3525)
-   Adds javadocs auto-published to [apidocs.ortussolutions.com](https://apidocs.ortussolutions.com/#/lucee/hibernate-extension/)

### 🐛 Fixed

-   ORM events not firing ([LDEV-4308](https://luceeserver.atlassian.net/browse/LDEV-4308))
-   Session close on transaction end ([LDEV-4017](https://luceeserver.atlassian.net/browse/LDEV-4017))
-   "length" not used on varchar fields ([LDEV-4150](https://luceeserver.atlassian.net/browse/LDEV-4150))

### ♻️ Changed

-   Dramatic improvements in initialization performance
-   Cuts ORM reload time by 60%
-   Better build/test documentation
-   Improved maintenance and build docs

[Unreleased]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.5.0...HEAD

[6.5.0]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.4.0...v6.5.0

[6.4.0]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.3.2...v6.4.0

[6.3.2]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.3.1...v6.3.2

[6.3.1]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.3.0...v6.3.1

[6.3.0]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.2.0...v6.3.0

[6.2.0]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.1.0...v6.2.0

[6.1.0]: https://github.com/Ortus-Solutions/extension-hibernate/compare/v6.0.0...v6.1.0
