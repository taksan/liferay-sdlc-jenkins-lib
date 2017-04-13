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

// Must not be NonCps
def _readFile(fileName) {
    sh 'pwd'
    return readFile(fileName)
}

// Must not be NonCps
def _fileExists(fileName) {
    return fileExists(fileName);
}

// Must not be NonCps
def _writeFile(fileName, contents) {
    writeFile file: fileName, text: contents
}
