package org.liferay.sdlc;

import groovy.json.JsonSlurper

import java.util.List;
import java.util.Map;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;
import org.liferay.sdlc.Utilities;

class SDLCPrUtilities {
    static def _ = new Utilities();

    // This method can't be "NonCPS"
    static def prInit(projectKey, projectName) {
        def settingsGradle = _._readFile("settings.gradle")
        if (!settingsGradle.contains("rootProject.name")) {
            settingsGradle+="\nrootProject.name=\"$projectKey\"";
            _._writeFile("settings.gradle", settingsGradle);
        }

        appendAdditionalCommand("build.gradle", [
            "_SONAR_PROJECT_NAME_" : projectName,
            "_SONAR_PROJECT_KEY_"  : projectKey
        ]);
    
    }

    @NonCPS
    static def getLogin(String json) {
        return new JsonSlurper().parseText(json).user.login
    }

    @NonCPS
    static def getEmail(String json) {
        return new JsonSlurper().parseText(json).email
    }

    @NonCPS
    static def isPullRequest()
    {
        return _.ChangeId() != null
    }

    @NonCPS
    static def shouldClosePullRequest()
    {
        if (!isFileExists("failureReasonFile"))
            return false;
        
        def reasonText = _._readFile("failureReasonFile").trim();
        if (reasonText.matches(".*Task: compileJava.*org.gradle.api.internal.tasks.compile.CompilationFailedException.*"))
            return true;

        if (reasonText.matches(".*Task: compileTestJava.*org.gradle.api.internal.tasks.compile.CompilationFailedException.*"))
            return true;

        if (reasonText.matches(".*Task: test.*There were failing tests.*"))
            return true;

        return false;
    }

    @NonCPS
    static def handleError(gitRepository, emailLeader, gitAuthentication) {
        if (!isPullRequest())
            return;

        if (!shouldClosePullRequest()) {
            log "Will not close PR because error is not considered to be introduced by new code"
            return;
        }

        _.closePullRequest(gitRepository, emailLeader, gitAuthentication);
    }

    // This method can't be "NonCPS"
    static def appendAdditionalCommand(fileName, varMap) {
        def additionalCustomCommands = _.loadLibrary("org/liferay/sdlc/custom.gradle")

        for (e in varMap) 
            additionalCustomCommands = additionalCustomCommands.replace("#{"+e.key+"}", e.value);

        if (!_._fileExists(fileName)) {
            log "file $fileName not found"
            return;
        }    

        def contents = _._readFile(fileName);
        contents += '\n\n'+ additionalCustomCommands;
           
        _._writeFile(fileName, contents);
    }


    static def sonarqube(gitRepository)
    {
        if (!_.isSonarVerificationEnabled()) {
            log "Sonar verification is disabled."
            return;
        }

        def credentials = _._getUserPassCredentials('sonar_analyser');
        def args = "-Dsonar.login=${credentials.username} -Dsonar.password=${credentials.password}"

        if (isPullRequest()) {
            println "Sonarqube Pull Request Evaluation"

            try {
                args += " -Dsonar.analysis.mode=preview -Dsonar.github.pullRequest=${_.ChangeId()} -Dsonar.github.oauth=${_.GithubOauth()} -Dsonar.github.repository=${gitRepository}"
            }catch(Exception e) {
                if (!isFileExists("failureReasonFile")) throw e;
                def reasonText = _._readFile("failureReasonFile").trim();
                if (reasonText.matches(".*sonarqube:.*Unable to perform GitHub WS operation:")) {
                    throw new Exception("Sonarqube Failed to access github repository to write sonarqube analysis.");
                }
            }
        }

        gradlew "sonarqube -Dsonar.buildbreaker.queryMaxAttempts=90 -Dsonar.buildbreaker.skip=true -Dsonar.host.url=${_.SonarHostUrl()} ${args}"
    }

    @NonCPS
    static def isFileExists(fileName) {
        return new File(fileName).exists();
    }


    static def gradlew(args) {
        _._gradlew(args)
    }

    @NonCPS
    static def log(args) {
        _.log(args)
    }
}
