# thriftc-maven-plugin

[![thriftc-maven-plugin build](https://github.com/anirbanmu/thriftc-maven-plugin/actions/workflows/build-thriftc-maven-plugin.yml/badge.svg)](https://github.com/anirbanmu/thriftc-maven-plugin/actions/workflows/build-thriftc-maven-plugin.yml)
[![thriftc-jar build](https://github.com/anirbanmu/thriftc-maven-plugin/actions/workflows/build-thriftc-jar.yml/badge.svg)](https://github.com/anirbanmu/thriftc-maven-plugin/actions/workflows/build-thriftc-jar.yml)
[![Maven Central: thriftc-maven-plugin](https://img.shields.io/maven-central/v/io.github.anirbanmu/thriftc-maven-plugin?label=thriftc-maven-plugin)](https://central.sonatype.com/artifact/io.github.anirbanmu/thriftc-maven-plugin)
[![Maven Central: thriftc-jar](https://img.shields.io/maven-central/v/io.github.anirbanmu/thriftc-jar?label=thriftc-jar)](https://central.sonatype.com/artifact/io.github.anirbanmu/thriftc-jar)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A self-contained [Apache Thrift](https://thrift.apache.org/) compiler for Maven. Add the plugin to your `pom.xml` and compile `.thrift` files.

The plugin detects your OS and architecture, resolves the correct pre-compiled thrift compiler binary from Maven Central, and invokes it to generate source code. Java 17+.

## Usage

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.anirbanmu</groupId>
            <artifactId>thriftc-maven-plugin</artifactId>
            <version>1.3.0</version>
            <configuration>
                <thriftVersion>0.25.0</thriftVersion>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Place your `.thrift` files in `src/main/thrift/` (and `src/test/thrift/` for test sources). The plugin generates code into `target/generated-sources/thrift/` during `generate-sources` and `target/generated-test-sources/thrift/` during `generate-test-sources`. Subdirectories are scanned recursively.

Generated Java sources are automatically added to the compile (or test-compile) source roots. For non-Java generators, the output is written but not added to Maven's source roots.

### Configuration

`compile` goal defaults shown:

| Parameter | Default | Description |
|---|---|---|
| `thriftVersion` | `0.25.0` | Version of the thrift compiler binary to resolve |
| `generator` | `java` | Thrift language generator (passed to `--gen`) |
| `thriftSourceDir` | `src/main/thrift` | Directory containing `.thrift` files |
| `outputDirectory` | `target/generated-sources/thrift` | Output directory for generated code |
| `includePaths` | *(empty)* | Additional include paths (`-I` flags) |
| `recursive` | `false` | Enable recursive generation for included files (`-r` flag) |
| `timeoutSeconds` | `300` | Process timeout for the thrift compiler |

The `compile-test` goal uses the same parameters but defaults `thriftSourceDir` to `src/test/thrift` and `outputDirectory` to `target/generated-test-sources/thrift`.

### Offline builds

The plugin resolves the thrift compiler binary at execution time, so `mvn dependency:go-offline` alone won't pre-fetch it. Use the `resolve` goal to download the binary into your local Maven repository before going offline:

```bash
mvn thriftc:resolve
mvn -o compile
```

Or bind it to an early phase so it runs automatically:

```xml
<execution>
    <id>resolve</id>
    <phase>initialize</phase>
    <goals>
        <goal>resolve</goal>
    </goals>
</execution>
```

The `resolve` goal accepts the same `thriftVersion` parameter as the compile goals.

## How it works

The repo contains two modules:

**thriftc-maven-plugin** — the Maven plugin described above. It detects your platform, resolves the matching `thriftc-jar` binary via Maven's artifact resolution APIs, and invokes it to compile `.thrift` files.

**thriftc-jar** — pre-compiled thrift compiler binaries published to Maven Central as classified artifacts under `io.github.anirbanmu:thriftc-jar`. The version tracks upstream Apache Thrift (e.g. `0.25.0`).

Supported platforms:
| Classifier | OS | Arch |
|---|---|---|
| `linux-x86_64` | Linux | x86-64 |
| `linux-aarch64` | Linux | ARM64 |
| `darwin-x86_64` | macOS | x86-64 (Intel) |
| `darwin-aarch64` | macOS | ARM64 (Apple Silicon) |
| `windows-x86_64` | Windows | x86-64 |

## Building

Prerequisites: Java 25+, Maven 3+ (see `mise.toml`).

```bash
# Full build: thriftc-jar + plugin verify (requires Docker, Linux only)
mise run verify

# Or manually:
# Build thriftc-jar (requires Docker for Linux targets)
mvn package -f thriftc-jar/pom.xml -Plinux-x86_64

# Build the plugin (requires thriftc-jar installed locally first)
mvn verify -Dgpg.skip=true -f thriftc-maven-plugin/pom.xml
```

## See also

Inspired by:

- [maven-thrift-plugin](https://github.com/dtrott/maven-thrift-plugin) and [thrift-maven-plugin](https://central.sonatype.com/artifact/org.apache.thrift/thrift-maven-plugin)
- [mvn-thrift-compiler](https://github.com/ccascone/mvn-thrift-compiler)
- [protobuf-maven-plugin](https://github.com/xolstice/protobuf-maven-plugin) (xolstice) and [protobuf-maven-plugin](https://github.com/ascopes/protobuf-maven-plugin) (ascopes)
- [protoc-jar](https://github.com/os72/protoc-jar) and [protoc-jar-maven-plugin](https://github.com/os72/protoc-jar-maven-plugin)
- [com.google.protobuf:protoc](https://central.sonatype.com/artifact/com.google.protobuf/protoc)

## License

[MIT](LICENSE)
