package com.jdte.test;

import org.junit.jupiter.api.BeforeAll;

/** Loads the data-driven item and fluid components before constructing stacks. */
public interface WorldRegistryTest {
    @BeforeAll
    static void bindWorldComponents() {
        TestWorldRegistries.load();
    }
}
