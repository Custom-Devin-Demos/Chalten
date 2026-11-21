package org.chalten.testkit.parity;

import java.nio.file.Path;
import java.util.List;

/**
 * An SUnit test class (or a test-class extension) read from a Tonel {@code .st} file.
 *
 * @param packageName the Tonel package ({@code #package}, or the {@code *Package} category of an extension)
 * @param className the Smalltalk class name
 * @param extension whether the file is an {@code Extension} of a class defined in another package
 * @param superclassName the superclass ({@code null} for extensions)
 * @param isAbstract whether the class answers {@code true} to {@code isAbstract} (it defines {@code class >> isAbstract})
 * @param ownTestSelectors the unary {@code test*} selectors defined in the file, in file order
 * @param file the parsed file
 */
public record SUnitTestClass(String packageName, String className, boolean extension, String superclassName,
        boolean isAbstract, List<String> ownTestSelectors, Path file) {

    public SUnitTestClass {
        ownTestSelectors = List.copyOf(ownTestSelectors);
    }

    /** The key used by the parity matrix in {@code PLAN.md} §8: the class name, plus {@code " (extension)"}. */
    public String key() {
        return extension ? className + " (extension)" : className;
    }
}
