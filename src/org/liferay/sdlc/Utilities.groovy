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

def loadLibrary(resource) {
   def contents = libraryResource resource;
   return contents
}

def _readFile(fileName) {
    sh 'pwd'
    return readFile(fileName)
}

def _fileExists(fileName) {
    log "checking file exists $fileName "
    r = fileExists(fileName);

    log "file exists result: $r $fileName "
    return r
}

def _writeFile(fileName, contents) {
    writeFile file: fileName, text: contents
}
