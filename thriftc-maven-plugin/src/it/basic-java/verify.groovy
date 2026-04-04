File genDir = new File(basedir, "target/generated-sources/thrift")
assert genDir.isDirectory() : "generated-sources/thrift directory does not exist"

// thrift generates java files into a gen-java subdirectory, so search recursively
List<File> javaFiles = []
genDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".java")) {
        javaFiles.add(file)
    }
}

assert javaFiles.size() > 0 : "no .java files found under " + genDir
