package org.liferay.sdlc;

@NonCPS
def _gradlew(args)
{
    if (isUnix())
        sh "./gradlew " + args
    else
        bat "gradlew " + args
}

def log(args) {
    println args
}

def getWorkspace() {
    return workspace
}

def getLibraryResource(resource) {
    s = libraryResource resource;
    return s;
}
