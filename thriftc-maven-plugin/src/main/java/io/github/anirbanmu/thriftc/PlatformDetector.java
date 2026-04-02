package io.github.anirbanmu.thriftc;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class PlatformDetector {

    public enum Os {
        LINUX("linux"),
        DARWIN("darwin"),
        WINDOWS("windows");

        private final String label;

        Os(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        // os.name is freeform ("Mac OS X", "Windows 10") so we match by substring
        static Os fromSystemProperty(String osName) {
            String lower = osName.toLowerCase(Locale.ROOT);
            if (lower.contains("linux")) return LINUX;
            if (lower.contains("mac") || lower.contains("darwin")) return DARWIN;
            if (lower.contains("windows")) return WINDOWS;
            throw new IllegalStateException(
                "unsupported os.name: " + osName +
                " — supported classifiers: " + SUPPORTED_CLASSIFIERS_DISPLAY);
        }
    }

    public enum Arch {
        X86_64("x86_64"),
        AARCH64("aarch64");

        private final String label;

        Arch(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        // os.arch values are well-defined identifiers so we match exactly
        static Arch fromSystemProperty(String osArch) {
            return switch (osArch) {
                case "amd64", "x86_64" -> X86_64;
                case "aarch64", "arm64" -> AARCH64;
                default -> throw new IllegalStateException(
                    "unsupported os.arch: " + osArch +
                    " — supported classifiers: " + SUPPORTED_CLASSIFIERS_DISPLAY);
            };
        }
    }

    public record Platform(Os os, Arch arch) {
        public String classifier() {
            return os.label() + "-" + arch.label();
        }
    }

    // the canonical set of platforms we ship binaries for
    private static final List<Platform> SUPPORTED_PLATFORMS = List.of(
        new Platform(Os.LINUX, Arch.X86_64),
        new Platform(Os.LINUX, Arch.AARCH64),
        new Platform(Os.DARWIN, Arch.X86_64),
        new Platform(Os.DARWIN, Arch.AARCH64),
        new Platform(Os.WINDOWS, Arch.X86_64)
    );

    // derived from SUPPORTED_PLATFORMS so the two can never drift apart
    private static final Set<String> SUPPORTED_CLASSIFIERS = SUPPORTED_PLATFORMS.stream()
        .map(Platform::classifier)
        .collect(Collectors.toUnmodifiableSet());

    private static final String SUPPORTED_CLASSIFIERS_DISPLAY = SUPPORTED_PLATFORMS.stream()
        .map(Platform::classifier)
        .sorted()
        .collect(Collectors.joining(", "));

    public static Platform detect() {
        return detect(System.getProperty("os.name"), System.getProperty("os.arch"));
    }

    // package-private for testing
    static Platform detect(String osName, String osArch) {
        Os os = Os.fromSystemProperty(osName);
        Arch arch = Arch.fromSystemProperty(osArch);
        Platform platform = new Platform(os, arch);

        if (!SUPPORTED_CLASSIFIERS.contains(platform.classifier())) {
            throw new IllegalStateException(
                "unsupported platform: " + osName + "/" + osArch +
                " (mapped to " + platform.classifier() + ")" +
                " — supported classifiers: " + SUPPORTED_CLASSIFIERS_DISPLAY);
        }

        return platform;
    }

    private PlatformDetector() {}
}
