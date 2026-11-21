package org.chalten.testkit.parity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SUnitSourceParserTest {

    private static final Path FILE = Path.of("source", "Fixture-Tests", "Fixture.st");

    @Test
    void readsAClassDefinitionAndItsUnaryTestSelectors() {
        SUnitTestClass parsed = SUnitSourceParser.parse("""
                "
                A comment mentioning Foo >> testNotAMethod [
                "
                Class {
                \t#name : 'GregorianDateTest',
                \t#superclass : 'ChaltenTest',
                \t#category : 'Chalten-Core-Tests',
                \t#package : 'Chalten-Core-Tests'
                }

                { #category : 'private' }
                GregorianDateTest >> assert: aDate isBefore: anotherDate [
                \tself assert: aDate < anotherDate
                ]

                { #category : 'private' }
                GregorianDateTest >> oneDay [
                \t^ 1 day
                ]

                { #category : 'tests' }
                GregorianDateTest >> testAccessing [
                \tself assert: 1 = 1
                ]

                { #category : 'tests' }
                GregorianDateTest >> testISO8601 [
                \tself assert: [ :x | x ] notNil
                ]

                { #category : 'tests' }
                GregorianDateTest >> testWith: anArgument [
                \t^ anArgument
                ]
                """.replace("\n", "\r\n"), FILE);

        assertEquals("GregorianDateTest", parsed.className());
        assertEquals("ChaltenTest", parsed.superclassName());
        assertEquals("Chalten-Core-Tests", parsed.packageName());
        assertFalse(parsed.extension());
        assertFalse(parsed.isAbstract());
        assertEquals("GregorianDateTest", parsed.key());
        assertEquals(List.of("testAccessing", "testISO8601"), parsed.ownTestSelectors());
    }

    @Test
    void readsAnExtensionAndTakesItsPackageFromTheMethodCategories() {
        SUnitTestClass parsed = SUnitSourceParser.parse("""
                Extension { #name : 'GregorianDateTimeTest' }

                { #category : '*Chalten-Timezones-Tests' }
                GregorianDateTimeTest >> testInTimeZone [
                \tself assert: true
                ]
                """, FILE);

        assertTrue(parsed.extension());
        assertEquals("Chalten-Timezones-Tests", parsed.packageName());
        assertEquals("GregorianDateTimeTest (extension)", parsed.key());
        assertEquals(List.of("testInTimeZone"), parsed.ownTestSelectors());
    }

    @Test
    void classSideIsAbstractMakesTheClassAbstractAndClassSideTestsAreNotTests() {
        SUnitTestClass parsed = SUnitSourceParser.parse("""
                Class {
                \t#name : 'HebrewCalendarTest',
                \t#superclass : 'ChaltenTest',
                \t#category : 'Chalten-Hebrew-Calendar-Tests'
                }

                { #category : 'testing' }
                HebrewCalendarTest class >> isAbstract [
                \t^ self = HebrewCalendarTest
                ]

                { #category : 'testing' }
                HebrewCalendarTest class >> testClassSide [
                \t^ true
                ]
                """, FILE);

        assertTrue(parsed.isAbstract());
        assertEquals("Chalten-Hebrew-Calendar-Tests", parsed.packageName());
        assertEquals(List.of(), parsed.ownTestSelectors());
    }

    @Test
    void rejectsFilesThatAreNotTonelDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> SUnitSourceParser.parse("Object subclass: #Foo", FILE));
        assertThrows(IllegalArgumentException.class, () -> SUnitSourceParser.parse("""
                Extension { #name : 'FooTest' }

                BarTest >> testBar [
                ]
                """, FILE));
    }

    @Test
    void selectorsOfMethodHeaders() {
        assertEquals("testAccessing", SUnitSourceParser.selector("testAccessing"));
        assertEquals("assert:daysAfter:is:", SUnitSourceParser.selector("assert: n daysAfter: date is: other"));
        assertEquals("=", SUnitSourceParser.selector("= anObject"));
    }

    @Test
    void concreteSubclassesInheritTheTestsOfAbstractSuperclasses() {
        SUnitTestClass base = new SUnitTestClass("P", "BaseTest", false, "ChaltenTest", true,
                List.of("testInherited"), FILE);
        SUnitTestClass concrete = new SUnitTestClass("P", "ConcreteTest", false, "BaseTest", false,
                List.of("testOwn"), FILE);
        SUnitTestClass empty = new SUnitTestClass("P", "EmptyTest", false, "TestCase", false, List.of(), FILE);
        SUnitTestClass extension = new SUnitTestClass("Q", "ConcreteTest", true, null, false,
                List.of("testExtension"), FILE);

        Map<String, List<String>> selectors =
                SUnitSourceParser.testSelectorsByKey(List.of(base, concrete, empty, extension));

        assertEquals(Map.of("ConcreteTest", List.of("testOwn", "testInherited"),
                "ConcreteTest (extension)", List.of("testExtension")), selectors);
    }
}
