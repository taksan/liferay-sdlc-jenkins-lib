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
def getWorkspace() {
    return workspace
}

@NonCPS
def getLibraryResource(resource) {
    return libraryResource(resource);
}

