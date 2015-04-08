package is.hello.shiba.util;

import android.support.annotation.NonNull;

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicBoolean;

import is.hello.shiba.ui.util.Optional;

public class OptionalTests extends TestCase {
    public void testEmpty() throws Exception {
        Optional<?> absent = Optional.empty();
        assertFalse(absent.isPresent());
        assertThrows(absent::get);
        assertSame(absent, Optional.empty());
    }

    public void testOf() throws Exception {
        Optional<String> present = Optional.of("Test string");
        assertTrue(present.isPresent());
        assertNotNull(present.get());
        assertEquals("Test string", present.get());

        //noinspection ConstantConditions
        assertThrows(() -> Optional.of(null));
    }

    public void testOfNullable() throws Exception {
        Optional<String> present = Optional.ofNullable("Test string");
        assertTrue(present.isPresent());
        assertNotNull(present.get());
        assertEquals("Test string", present.get());


        Optional<String> absent = Optional.ofNullable(null);
        assertFalse(absent.isPresent());
        assertThrows(absent::get);
    }


    public void testIsPresent() throws Exception {
        Optional<String> present = Optional.of("Test string");
        assertTrue(present.isPresent());

        Optional<?> absent = Optional.empty();
        assertFalse(absent.isPresent());
    }

    public void testGet() throws Exception {
        Optional<String> present = Optional.of("Test string");
        assertNotNull(present.get());
        assertEquals("Test string", present.get());


        Optional<String> absent = Optional.empty();
        assertThrows(absent::get);
    }


    public void testEquals() throws Exception {
        Optional<String> present1 = Optional.of("Test string");
        Optional<String> present2 = Optional.of("Test string");
        Optional<String> present3 = Optional.of("Test string #2");

        assertEquals(present1, present1);
        assertEquals(present1, present2);
        assertEquals(present2, present1);
        assertFalse(present1.equals(present3));

        Optional<String> absent = Optional.empty();
        assertFalse(absent.equals(present1));
        assertFalse(present1.equals(absent));
        assertEquals(absent, absent);
    }

    public void testHashCode() throws Exception {
        Optional<String> absent = Optional.empty();
        assertEquals(0, absent.hashCode());

        Optional<String> present = Optional.of("Test string");
        assertEquals(present.get().hashCode(), present.hashCode());
    }


    public void testFilter() throws Exception {
        Optional<String> absent = Optional.empty();
        assertEquals(Optional.<String>empty(), absent.filter(s -> (s.length() % 2) == 0));

        Optional<String> present1 = Optional.of("Odd");
        assertEquals(Optional.<String>empty(), present1.filter(s -> (s.length() % 2) == 0));

        Optional<String> present2 = Optional.of("Even");
        assertEquals(present2, present2.filter(s -> (s.length() % 2) == 0));
    }

    public void testMap() throws Exception {
        Optional<String> present1 = Optional.of("Test");
        Optional<String> present2 = present1.map(s -> s + " string");
        assertTrue(present2.isPresent());
        assertEquals("Test string", present2.get());

        Optional<String> absent1 = Optional.empty();
        Optional<String> absent2 = absent1.map(s -> s + " string");
        assertFalse(absent2.isPresent());
    }

    public void testFlatMap() throws Exception {
        Optional<String> present1 = Optional.of("Test");
        Optional<String> present2 = present1.flatMap(s -> Optional.of(s + " string"));
        assertTrue(present2.isPresent());
        assertEquals("Test string", present2.get());

        Optional<String> absent1 = Optional.empty();
        Optional<String> absent2 = absent1.flatMap(s -> Optional.of(s + " string"));
        assertFalse(absent2.isPresent());

        Optional<String> absent3 = present1.flatMap(s -> Optional.empty());
        assertFalse(absent3.isPresent());
    }

    public void testIfPresent() throws Exception {
        Optional<String> present = Optional.of("Test");
        AtomicBoolean called1 = new AtomicBoolean(false);
        present.ifPresent(value -> {
            assertEquals("Test", value);
            called1.set(true);
        });
        assertTrue(called1.get());

        Optional<String> absent = Optional.empty();
        AtomicBoolean called2 = new AtomicBoolean(false);
        absent.ifPresent(value -> called2.set(true));
        assertFalse(called2.get());
    }


    public void testOrElse() throws Exception {
        assertEquals("Test", Optional.<String>empty().orElse("Test"));
        assertEquals("Shoes", Optional.of("Shoes").orElse("Test"));
    }

    public void testOrElseGet() throws Exception {
        assertEquals("Test", Optional.<String>empty().orElseGet(() -> "Test"));
        assertEquals("Shoes", Optional.of("Shoes").orElseGet(() -> "Test"));
    }

    public void testOrElseThrow() throws Exception {
        assertEquals("Test", Optional.of("Test").orElseThrow(() -> new RuntimeException("Never thrown")));
        assertThrows(() -> Optional.empty().orElseThrow(() -> new RuntimeException("Definitely thrown")));
    }


    private static void assertThrows(@NonNull ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            return;
        }

        fail("Runnable did not throw");
    }

    private static interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
