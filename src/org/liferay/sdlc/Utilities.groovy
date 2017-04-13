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
    println "loading resource $resource"
    s = libraryResource resource;
    println "resource $resource loaded:"
    println s
    return s;
}
