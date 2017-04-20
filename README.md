# Refactor Reactor

This project is a demonstration of how we could use [Netflix Rewrite](https://github.com/Netflix-Skunkworks/rewrite)
to auto-remediate API deprecations for users of Project Reactor. Rewrite makes it possible to automatically fix code
in a style-preserving way.

To test-drive:

1. Run `./gradlew pTML` from the project root. This will publish a snapshot jar containing the
`@AutoRewrite` rules to your maven local repository.

2. `cd example`.

3. Run `./gradlew lintSource`. This will present the user with a report indicating that there are
changes that need to be made:

![lintSource](https://github.com/jkschneider/refactor-reactor/raw/master/example/screenshots/lintSource.png)

4. Run `./gradlew fixSourceLint`. This will present a similar report as `lintSource`, but also make the changes
to the underlying source files.

![fixSourceLint](https://github.com/jkschneider/refactor-reactor/raw/master/example/screenshots/fixSourceLint.png)

5. Run `git diff` to examine the changes that were made.