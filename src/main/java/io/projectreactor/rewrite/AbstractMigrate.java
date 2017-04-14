package io.projectreactor.rewrite;

import java.io.File;

import com.netflix.rewrite.parse.Parser;

/**
 * Base class for Reactor migration implementations with utility method to apply
 * a code migration to a source directory after sanity checks on said directory.
 *
 * @author Simon Baslé
 */
//TODO tests
public class AbstractMigrate {

	public static boolean hasGitDir(File dir) {
		return dir.listFiles(f -> f.isDirectory() && f.getName().equals(".git"))
				.length == 1;
	}

	public static boolean hasGitDirSibling(File dir) {
		return dir.getParentFile().listFiles(f -> f.isDirectory() && f.getName().equals(".git"))
				.length == 1;
	}

	public static void checkRecoverable(File rootSourceDirectory) {
		if (!rootSourceDirectory.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + rootSourceDirectory);
		}

		if (!hasGitDir(rootSourceDirectory) && !hasGitDirSibling(rootSourceDirectory))
			throw new IllegalArgumentException("Neither root directory nor its parent has a .git directory");
	}

	/**
	 *
	 * @param rootSourceDirectory the root directory where to find sources to migrate
	 * @param jdkParser a {@link Parser} used for refactoring that has the correct classpath set up
	 * @param checkRecoverable set to true to perform sanity checks on the root directory, like does it or its
	 * parent have a ".git" directory (ensuring the refactored files can be overwritten with a way to revert that)
	 */
	public void migrateAll(File rootSourceDirectory, Parser jdkParser, boolean checkRecoverable) {
		if (checkRecoverable) {
			checkRecoverable(rootSourceDirectory);
		}

		//TODO parse all java classes in the source hierarchy, apply the refactors
	}

}
