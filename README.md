# Lucee Hibernate Extension (ORM)

![the Ortus ORM Extension logo](https://raw.githubusercontent.com/Ortus-Solutions/extension-hibernate/e82ed175c9b19528b130b079c942f7f654bc6147/logo.png)

[![Java CI](https://github.com/ortus-solutions/extension-hibernate/actions/workflows/ci.yml/badge.svg)](https://github.com/ortus-solutions/extension-hibernate/actions/workflows/ci.yml) Latest

* A [Hibernate ORM](https://hibernate.org/orm/) wrapper for the [Lucee CFML language](https://www.lucee.org/)

* Documentation: https://orm-extension.ortusbooks.com
* Issues: https://ortussolutions.atlassian.net/jira/software/c/projects/OOE/issues
* Javadocs: https://apidocs.ortussolutions.com/#/lucee/hibernate-extension/

## Requirements

Lucee 5.3.9.73 or above.

## Installation

You can install this extension into a preconfigured Lucee server via Commandbox:

```bash
box install D062D72F-F8A2-46F0-8CBC91325B2F067B
```

This will not work unless `box server start` has been run first to set up the Lucee engine directories. Use `--dryRun` to set up the Lucee server without actually starting the server process. This will prevent ORM from attempting to initialize before the extension is installed:

```bash
box
box> server start --dryRun
box> install D062D72F-F8A2-46F0-8CBC91325B2F067B
box> server start
```

## THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
