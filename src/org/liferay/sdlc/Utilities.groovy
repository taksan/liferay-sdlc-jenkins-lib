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
def workspaceDir() {
    return workspace
}
