File buildLog = new File(basedir, "build.log")
String log = buildLog.text

// verify the resolve goal ran and pre-fetched the binary
assert log.contains("pre-fetched thriftc") : "build log should contain resolve goal output"

// verify the second invocation actually ran in offline mode
// maven logs "in offline mode" when it skips remote repository access
assert log.contains("in offline mode") : "build log should indicate offline mode was active"

// verify the offline compile produced generated sources
File genDir = new File(basedir, "target/generated-sources/thrift")
assert genDir.isDirectory() : "generated-sources/thrift directory does not exist"

List<File> javaFiles = []
genDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".java")) {
        javaFiles.add(file)
    }
}

assert javaFiles.size() > 0 : "no .java files found under " + genDir
