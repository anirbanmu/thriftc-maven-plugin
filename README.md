# thriftc-maven-plugin

A zero-install [Apache Thrift](https://thrift.apache.org/) compiler experience for Maven. Add the plugin to your `pom.xml` and compile `.thrift` files — no manual thrift installation required.

## Modules

### thriftc-jar

Pre-compiled thrift compiler binaries published to Maven Central as classified artifacts under `io.github.anirbanmu:thriftc-jar`.

Supported platforms:
| Classifier | OS | Arch |
|---|---|---|
| `linux-x86_64` | Linux | x86-64 |
| `linux-aarch64` | Linux | ARM64 |
| `darwin-x86_64` | macOS | x86-64 (Intel) |
| `darwin-aarch64` | macOS | ARM64 (Apple Silicon) |
| `windows-x86_64` | Windows | x86-64 |

Each binary is attached with a classifier and `.exe` type. The version tracks upstream Apache Thrift (e.g. `0.22.0`).

### thriftc-maven-plugin *(planned)*

A Maven plugin that automatically detects your OS/arch, resolves the correct `thriftc-jar` binary from Maven Central, and invokes it to compile `.thrift` files into generated source code. Requires Java 17+.

## Usage

### thriftc-jar (direct artifact resolution)

```xml
<dependency>
    <groupId>io.github.anirbanmu</groupId>
    <artifactId>thriftc-jar</artifactId>
    <version>0.22.0</version>
    <classifier>linux-x86_64</classifier>
    <type>exe</type>
</dependency>
```

### thriftc-maven-plugin (coming soon)

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.anirbanmu</groupId>
            <artifactId>thriftc-maven-plugin</artifactId>
            <version>1.0.0</version>
            <configuration>
                <thriftVersion>0.22.0</thriftVersion>
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

#### Configuration (`compile` goal defaults shown)

| Parameter | Default | Description |
|---|---|---|
| `thriftVersion` | `0.22.0` | Version of the thrift compiler binary to resolve |
| `generator` | `java` | Thrift language generator (passed to `--gen`) |
| `thriftSourceDir` | `src/main/thrift` | Directory containing `.thrift` files |
| `outputDirectory` | `target/generated-sources/thrift` | Output directory for generated code |
| `includePaths` | *(empty)* | Additional include paths (`-I` flags) |
| `recursive` | `false` | Enable recursive generation for included files (`-r` flag) |
| `timeoutSeconds` | `300` | Process timeout for the thrift compiler |

The `compile-test` goal uses the same parameters but defaults `thriftSourceDir` to `src/test/thrift` and `outputDirectory` to `target/generated-test-sources/thrift`.

Generated Java sources are automatically added to the compile (or test-compile) source roots. For non-Java generators, the output is written but not added to Maven's source roots.

## Building

Prerequisites: Java 25+, Maven 3+ (see `.tool-versions`).

```bash
# Build thriftc-jar (requires Docker for Linux targets)
mvn package -f thriftc-jar/pom.xml -Plinux-x86_64

# Build the plugin (requires thriftc-jar installed locally first)
mvn verify -f thriftc-maven-plugin/pom.xml
```

## License

[MIT](LICENSE)
