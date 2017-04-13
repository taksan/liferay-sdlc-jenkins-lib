package org.liferay.sdlc;

@NonCPS
def _gradlew(args)
{
    if (isUnix())
        sh "./gradlew " + args
    else
        bat "gradlew " + args
}

@NonCPS
def log(args) {
    println args
}

@NonCPS
def loadLibrary(resource) {
   def contents = libraryResource resource;
   return contents
}

@NonCPS
def _readFile(fileName) {
    sh 'pwd'
    return readFile(fileName)
}

@NonCPS
def _fileExists(fileName) {
    log "checking file exists $fileName "
    return fileExists(fileName);
}

@NonCPS
def _writeFile(fileName, contents) {
    writeFile file: fileName, text: contents
}
