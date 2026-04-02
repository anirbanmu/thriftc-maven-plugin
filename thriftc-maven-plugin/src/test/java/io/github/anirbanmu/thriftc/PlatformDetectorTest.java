package io.github.anirbanmu.thriftc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformDetectorTest {

    @Test
    void detectsLinuxX86_64() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Linux", "amd64");
        assertEquals(PlatformDetector.Os.LINUX, platform.os());
        assertEquals(PlatformDetector.Arch.X86_64, platform.arch());
        assertEquals("linux-x86_64", platform.classifier());
    }

    @Test
    void detectsLinuxAarch64() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Linux", "aarch64");
        assertEquals(PlatformDetector.Os.LINUX, platform.os());
        assertEquals(PlatformDetector.Arch.AARCH64, platform.arch());
        assertEquals("linux-aarch64", platform.classifier());
    }

    @Test
    void detectsDarwinX86_64() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Mac OS X", "x86_64");
        assertEquals(PlatformDetector.Os.DARWIN, platform.os());
        assertEquals(PlatformDetector.Arch.X86_64, platform.arch());
        assertEquals("darwin-x86_64", platform.classifier());
    }

    @Test
    void detectsDarwinAarch64() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Mac OS X", "aarch64");
        assertEquals(PlatformDetector.Os.DARWIN, platform.os());
        assertEquals(PlatformDetector.Arch.AARCH64, platform.arch());
        assertEquals("darwin-aarch64", platform.classifier());
    }

    @Test
    void detectsWindowsX86_64() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Windows 10", "amd64");
        assertEquals(PlatformDetector.Os.WINDOWS, platform.os());
        assertEquals(PlatformDetector.Arch.X86_64, platform.arch());
        assertEquals("windows-x86_64", platform.classifier());
    }

    // OS name variations

    @Test
    void detectsDarwinFromDarwinOsName() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Darwin", "x86_64");
        assertEquals(PlatformDetector.Os.DARWIN, platform.os());
        assertEquals("darwin-x86_64", platform.classifier());
    }

    @Test
    void detectsWindowsFromWindows11() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Windows 11", "amd64");
        assertEquals(PlatformDetector.Os.WINDOWS, platform.os());
        assertEquals("windows-x86_64", platform.classifier());
    }

    // Arch variations

    @Test
    void detectsX86_64FromX86_64Arch() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Linux", "x86_64");
        assertEquals(PlatformDetector.Arch.X86_64, platform.arch());
        assertEquals("linux-x86_64", platform.classifier());
    }

    @Test
    void detectsAarch64FromArm64Arch() {
        PlatformDetector.Platform platform = PlatformDetector.detect("Linux", "arm64");
        assertEquals(PlatformDetector.Arch.AARCH64, platform.arch());
        assertEquals("linux-aarch64", platform.classifier());
    }

    @Test
    void throwsOnUnsupportedOs() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> PlatformDetector.detect("SunOS", "amd64"));
        assertTrue(ex.getMessage().contains("SunOS"), "message should contain the unsupported OS");
        assertTrue(ex.getMessage().contains("linux-x86_64"), "message should list supported classifiers");
    }

    @Test
    void throwsOnUnsupportedArch() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> PlatformDetector.detect("Linux", "sparc"));
        assertTrue(ex.getMessage().contains("sparc"), "message should contain the unsupported arch");
        assertTrue(ex.getMessage().contains("linux-x86_64"), "message should list supported classifiers");
    }

    @Test
    void throwsOnUnsupportedCombinationWindowsAarch64() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> PlatformDetector.detect("Windows 10", "aarch64"));
        assertTrue(ex.getMessage().contains("windows-aarch64"), "message should contain the unsupported classifier");
        assertTrue(ex.getMessage().contains("linux-x86_64"), "message should list supported classifiers");
    }
}
