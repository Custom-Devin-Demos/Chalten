package org.chalten.testkit.parity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Looks up compiled JUnit test classes. */
@FunctionalInterface
public interface JavaTestClasses {

    /**
     * A compiled test class.
     *
     * @param name the fully qualified class name
     * @param location where the class was loaded from (a class directory or jar), or {@code null} if unknown
     * @param testMethods the no-argument methods JUnit runs (annotated, directly or through a meta-annotation, with
     *        {@link Testable}: {@code @Test}, {@code @ParameterizedTest}, jqwik {@code @Property}, ...), including
     *        inherited ones
     * @param disabledMethods the test methods that are {@link Disabled} (all of them when the class is)
     */
    record JavaTestClass(String name, Path location, Set<String> testMethods, Set<String> disabledMethods) {

        public JavaTestClass {
            testMethods = Set.copyOf(testMethods);
            disabledMethods = Set.copyOf(disabledMethods);
        }

        /** Whether {@code selector} is a test method JUnit runs. */
        public boolean runs(String selector) {
            return testMethods.contains(selector) && !disabledMethods.contains(selector);
        }
    }

    Optional<JavaTestClass> find(String className);

    /** Finds classes through {@code classLoader} by reflection. Abstract classes are not test classes. */
    static JavaTestClasses reflective(ClassLoader classLoader) {
        return className -> {
            Class<?> type;
            try {
                type = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }
            if (Modifier.isAbstract(type.getModifiers())) {
                return Optional.empty();
            }
            boolean classDisabled = AnnotationSupport.isAnnotated(type, Disabled.class);
            Set<String> tests = new LinkedHashSet<>();
            Set<String> disabled = new LinkedHashSet<>();
            for (Method method : ReflectionSupport.findMethods(type,
                    each -> each.getParameterCount() == 0 && !Modifier.isStatic(each.getModifiers())
                            && AnnotationSupport.isAnnotated(each, Testable.class),
                    HierarchyTraversalMode.TOP_DOWN)) {
                tests.add(method.getName());
                if (classDisabled || AnnotationSupport.isAnnotated(method, Disabled.class)) {
                    disabled.add(method.getName());
                }
            }
            return Optional.of(new JavaTestClass(className, location(type), tests, disabled));
        };
    }

    private static Path location(Class<?> type) {
        CodeSource source = type.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) {
            return null;
        }
        try {
            return Path.of(source.getLocation().toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }
}
