package org.chalten.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.InstanceCreationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChronologyConventionsTest {

    private LocaleConventions conventions;

    @BeforeEach
    void setUp() {
        conventions = new LocaleConventions();
    }

    private void load(String json) {
        conventions.loadJSONChronologyConventionsFromString(json);
    }

    private void loadSpanishConventions() {
        load("""
                {
                	"*": {"shortDateFormat": "y-MM-dd", "mediumDateFormat": "y MMM d", "shortTimeFormat": "HH:mm", "amMark": "AM"},
                	"es": {"shortDateFormat": "d/M/yy", "mediumDateFormat": "d MMM y", "shortTimeFormat": "H:mm", "amMark": "a. m."},
                	"es-AR": {"shortTimeFormat": "h:mm\u202Fa"}
                }""");
    }

    private String propertyNamed(String name, String languageTag) {
        return conventions.chronologyPropertyNamed(name, languageTag);
    }

    @Test
    void testChronologyCategory() {
        loadSpanishConventions();
        assertEquals("d/M/yy", conventions.propertyNamed("shortDateFormat", "chronology", "es-AR"));
        assertEquals("d/M/yy", propertyNamed("shortDateFormat", "es-AR"));
    }

    @Test
    void testLaterLoadsOverridePropertyByProperty() {
        loadSpanishConventions();
        load("{\"es\": {\"mediumDateFormat\": \"d 'de' MMM y\"}, \"fr\": {\"shortDateFormat\": \"dd/MM/y\"}}");
        assertEquals("d 'de' MMM y", propertyNamed("mediumDateFormat", "es"));
        assertEquals("d/M/yy", propertyNamed("shortDateFormat", "es"));
        assertEquals("dd/MM/y", propertyNamed("shortDateFormat", "fr-CA"));
        assertEquals("y MMM d", propertyNamed("mediumDateFormat", "fr-CA"));
    }

    @Test
    void testLoadingFromAFile(@TempDir Path location) throws IOException {
        Path file = location.resolve("chronology.json");
        Files.writeString(file, "{\"es\": {\"shortDateFormat\": \"d/M/yy\"}}");
        conventions.loadJSONChronologyConventionsFrom(file);
        assertEquals("d/M/yy", propertyNamed("shortDateFormat", "es-AR"));
    }

    @Test
    void testMarksAreNotPatterns() {
        load("{\"es\": {\"amMark\": \"a. m.\", \"pmMark\": \"p. m.\"}}");
        assertEquals("a. m.", propertyNamed("amMark", "es"));
        assertEquals("p. m.", propertyNamed("pmMark", "es"));
    }

    @Test
    void testPropertyNames() {
        load("{}");
        for (String name : LocaleConventions.chronologyPropertyNames()) {
            assertEquals("There's no " + name + " convention for es",
                    assertThrows(NoSuchElementException.class, () -> propertyNamed(name, "es")).getMessage());
        }
    }

    @Test
    void testPropertyNotDefined() {
        load("{\"es\": {\"shortDateFormat\": \"d/M/yy\"}}");
        assertEquals("There's no longDateFormat convention for es-AR", assertThrows(NoSuchElementException.class,
                () -> propertyNamed("longDateFormat", "es-AR")).getMessage());
    }

    @Test
    void testPropertyResolvedFromTheMostSpecificRange() {
        loadSpanishConventions();
        assertEquals("h:mm\u202Fa", propertyNamed("shortTimeFormat", "es-AR"));
        assertEquals("d/M/yy", propertyNamed("shortDateFormat", "es-AR"));
        assertEquals("H:mm", propertyNamed("shortTimeFormat", "es-ES"));
        assertEquals("a. m.", propertyNamed("amMark", "es-AR"));
        assertEquals("y-MM-dd", propertyNamed("shortDateFormat", "en"));
        assertEquals("AM", propertyNamed("amMark", "en-GB"));
    }

    @Test
    void testUnknownProperty() {
        assertEquals("shortDate is not a convention of the chronology category",
                assertThrows(ChaltenAssertionException.class,
                        () -> load("{\"es\": {\"shortDate\": \"d/M/yy\"}}")).getMessage());
    }

    @Test
    void testUnsupportedFieldsAreRejectedBeforeLoading() {
        load("{\"es\": {\"shortDateFormat\": \"d/M/yy\"}}");
        assertEquals("The field G is not supported in date and time patterns",
                assertThrows(InstanceCreationFailedException.class, () -> load(
                        "{\"es\": {\"shortDateFormat\": \"d/M/y G\"}, \"fr\": {\"shortDateFormat\": \"dd/MM/y\"}}"))
                        .getMessage());
        assertEquals("d/M/yy", propertyNamed("shortDateFormat", "es"));
        assertThrows(NoSuchElementException.class, () -> propertyNamed("shortDateFormat", "fr"));
    }
}
