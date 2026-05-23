package com.xxxx.ddd.application.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureBoundaryTest {

    @Test
    void applicationModuleDoesNotDependOnInfrastructure() throws IOException {
        Path root = findProjectRoot();
        String applicationPom = Files.readString(root.resolve("xxxx-application/pom.xml"), StandardCharsets.UTF_8);

        assertFalse(
                applicationPom.contains("<artifactId>xxxx-infrastructure</artifactId>"),
                "xxxx-application must not declare a compile dependency on xxxx-infrastructure"
        );
    }

    @Test
    void sourceModulesKeepDeclaredDependencyDirection() throws IOException {
        Path root = findProjectRoot();

        assertNoForbiddenReference(
                root.resolve("xxxx-application/src/main/java"),
                forbiddenReference("infrastructure"),
                "Application source must depend on application/domain contracts, not infrastructure adapters"
        );
        assertNoForbiddenReference(
                root.resolve("xxxx-application/src/test/java"),
                forbiddenReference("infrastructure"),
                "Application tests must mock application/domain contracts, not infrastructure adapters"
        );
        assertNoForbiddenReference(
                root.resolve("xxxx-controller/src/main/java"),
                forbiddenReference("infrastructure"),
                "Controller source must call application services, not infrastructure adapters"
        );
        assertNoForbiddenReference(
                root.resolve("xxxx-infrastructure/src/main/java"),
                forbiddenReference("controller"),
                "Infrastructure source must not depend on controllers"
        );
    }

    private static String forbiddenReference(String packageSegment) {
        return String.join(".", "com", "xxxx", "ddd", packageSegment);
    }

    private static void assertNoForbiddenReference(Path sourceRoot, String forbidden, String message) throws IOException {
        if (!Files.exists(sourceRoot)) {
            return;
        }

        try (Stream<Path> files = Files.walk(sourceRoot)) {
            List<Path> offenders = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("ArchitectureBoundaryTest.java"))
                    .filter(path -> contains(path, forbidden))
                    .toList();

            assertTrue(offenders.isEmpty(), message + ": " + offenders);
        }
    }

    private static boolean contains(Path path, String expected) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).contains(expected);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private static Path findProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("xxxx-application"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root from " + Path.of("").toAbsolutePath());
    }
}
