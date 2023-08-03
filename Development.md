# Developing the Ortus ORM Extension

You will need to install the following for extension development:

* Java 1.8, 11, or 17 (JRE and JDK)
* [maven](https://linuxize.com/post/how-to-install-apache-maven-on-ubuntu-20-04/) (for other automated tooling)
* Optional: [Docker](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/) (for managing test database servers)

## Getting Started

1. Install the repo - `git clone git@github.com:ortus-solutions/extension-hibernate.git`
2. Set up an environment/secrets file - `cp .env.template .env`
3. Download dependency sources for IDE integration - `mvn dependency:copy-dependencies`
4. [Build extension via `mvn package`](#building)
5. Test via `box run-script deploy.lucee5 && server restart && testbox run` (See [#Testing](#testing) for more info)

**Before you send a PR:**

1. [Run the test suite](#testing)
   1. (if you can't get tests to pass, ask for help!)
2. Add a note to `CHANGELOG.md` under `## Unreleased` with a summary of the change

## Building

* `mvn dependency:copy-dependencies` Download dependency sources for VS code detection and source browsing. This is not needed to build the extension, but it is useful to remove the "Import not found" errors in VS Code.
* `mvn clean` clean build directories
* `mvn package` Package the extension into a Lucee-installable `.lex` extension file
* `mvn test` run java-based (junit) tests
* `mvn javadoc:javadoc` generate java docs

## Testing

To run CFML tests against the extension:

1. Start the test server using `box server start`
   1. For testing on Lucee 6, run `box server start server-lucee6.json`
2. Build and deploy the extension source via `box run-script deploy.lucee5`
   1. For testing on Lucee 6, run `box run-script deploy.lucee6`
3. Restart the server to pick up the new extension - `box server restart`
4. Run tests from the extension root with `box testbox run`

For "full" database suite tests, you'll want to start the test databases using Docker-compose `docker-compose up -d`. This will start up MSSQL, MySQL, and Postgres database instances for database-specific tests.

### Lucee 6 Testing

To build and test on Lucee 6, use the `server-lucee6.json` file and `deploy.lucee6` build script:

* `box server start server-lucee6.json`
* `box run-script deploy.lucee6`
* `box server restart name=lucee6`
* `box testbox run`

## Publishing a Release

Releasing is *mostly* automated. To publish a release:

1. Check out the `master` branch - `git checkout master`
2. Merge in `development` changes - `git merge development`
3. Push to github: `git push`
4. Check out `development` branch for further work - `git checkout development`
5. Bump the major, minor, or patch version so you don't push snapshots on a released version: `box bump --minor`

Once you push, the `release` GitHub workflow will kick in and:

1. compile and test the extension
2. create javadocs
3. push up the built artifact, javadocs, and the logo to S3
4. publish the extension to Forgebox
5. create a git tag
6. create a GitHub release with changelog release notes

If you push to `development`, steps 5 and 6 will be ignored and the release pushed to ForgeBox will be a `-SNAPSHOT` release version.

## Branching Strategy

We use the following branches for managing releases:

* `master` - Each push to this branch will trigger a stable release build.
* `development` - Each push to this branch will trigger a `snapshot` build.
* `release/x.x` - These are occasionally used for work-in-progress on a new version without affecting the main development-to-master workflow. Each push to this branch will trigger a `snapshot` build

General development can be branched at will, but we prefer the following naming structure:

* `feat/the-new-feature` - Contains work in progress on a new feature
* `patch/the-bug-in-question` - Contains work in progress on a patch for a known bug or issue

In general, work on a new feature, bug fix, or improvement should be done on a new branch. When the work is ready for releasing, it should be merged or PR-ed to the `development` branch, where it will be deployed with the next release.

## Java IDE

I highly recommend [VS Code](https://code.visualstudio.com/) for java development. More experienced Java developers may appreciate a java-specific IDE like [Intellij](https://www.jetbrains.com/idea/). For VS Code Java support, check out:

* [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) - a must for java language support, easy refactoring tools, etc.
* [Refactoring Java](https://code.visualstudio.com/docs/java/java-refactoring) documentation - Keyboard shortcuts for moving methods, renaming variables, the works.
