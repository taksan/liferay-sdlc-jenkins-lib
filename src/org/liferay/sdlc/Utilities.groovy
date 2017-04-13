package org.liferay.sdlc;

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

@NonCPS
def isSonarVerificationEnabled() {
    return env.ENABLE_SONAR == "true";
}

@NonCPS
def SonarHostUrl() {
    return env.SonarHostUrl;
}

@NonCPS
def ChangeId() {
    return env.CHANGE_ID;
}

@NonCPS
def GithubOauth() {
    return env.GithubOauth;
}
