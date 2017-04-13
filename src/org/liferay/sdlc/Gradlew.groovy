package org.liferay.sdlc;

class Gradlew {
}

@NonCPS
def _gradlew(args)
{
    if (isUnix())
        sh "gradlew " + args
    else
        bat "gradlew " + args
}

