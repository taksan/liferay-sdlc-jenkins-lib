package org.liferay.sdlc;

import groovy.json.JsonSlurper

import java.util.List;
import java.util.Map;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;
import org.liferay.sdlc.Utilities;

class SDLCPrUtilities {
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
        return global("CHANGE_ID") != null
    }

    @NonCPS
    static def shouldClosePullRequest()
    {
        if (!isFileExists("failureReasonFile"))
            return false;
        
        def reasonText = new File("failureReasonFile").text.trim();
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

        def CHANGE_ID = env("CHANGE_ID")

        def emailText = 'Your Pull Request PR-${CHANGE_ID} broke the build and will be removed. Please fix it at your earliest convenience and re-submit. ${JOB_URL}'
        def emailSubject = "Validate PR-${CHANGE_ID}"

        def body = """
        {"state": "closed"}
        """
        httpRequest acceptType: 'APPLICATION_JSON', authentication: "${gitAuthentication}", contentType: 'APPLICATION_JSON', httpMode: 'PATCH', requestBody: body, url: "https://api.github.com/repos/${gitRepository}/pulls/${CHANGE_ID}"			

        def response = httpRequest acceptType: 'APPLICATION_JSON', authentication: "${gitAuthentication}", contentType: 'APPLICATION_JSON', httpMode: 'GET', url: "https://api.github.com/repos/${gitRepository}/pulls/${CHANGE_ID}"
        def login = getLogin(response.content)

        def respUser = httpRequest acceptType: 'APPLICATION_JSON', authentication: "${gitAuthentication}", contentType: 'APPLICATION_JSON', httpMode: 'GET', url: "https://api.github.com/users/${login}"
        def email = getEmail(respUser.content)
        
        emailext body: "${emailText}", subject: "${emailSubject}", to: "${email}"
        emailext body: "${emailText}", subject: "${emailSubject}", to: "${emailLeader}"
    }

    static def appendAdditionalCommand(fileName, varMap) {
        log "appendAdditionalCommand $fileName $varMap"
        def additionalCustomCommands = new Utilities().getLibraryResource("org/liferay/sdlc/custom.gradle")

		for (e in varMap) 
			additionalCustomCommands = additionalCustomCommands.replace("#{"+e.key+"}", e.value);

        log "Will append the following contents in build.gradle:"
        log additionalCustomCommands

        def value = '';
        if (isFileExists(fileName)) 
            value = new File(workspace(), fileName).text;
        else
            throw new IllegalArgumentException("File ${fileName} not found");
        
        value += '\n\n'+ additionalCustomCommands;
        log "Contents to write"
        log value
        new File(workspace(), fileName).write value
    }


    @NonCPS
    static def sonarqube(args)
    {
        def SonarHostUrl = global("SonarHostUrl");
        log "Running sonar with arguments : ${args}"
        gradlew "sonarqube -Dsonar.buildbreaker.queryMaxAttempts=90 -Dsonar.buildbreaker.skip=true -Dsonar.host.url=${SonarHostUrl} ${args}"
    }

    @NonCPS
    static def global(name) {
       if (System.getenv(name) != null)
            return System.getenv(name);

       List<EnvironmentVariablesNodeProperty> all = Jenkins.instance.getGlobalNodeProperties().getAll(EnvironmentVariablesNodeProperty.class);
        for (EnvironmentVariablesNodeProperty environmentVariablesNodeProperty : all) {
            def value = environmentVariablesNodeProperty.getEnvVars().get(name);
            if (value != null) 
                return value;
        }	
        return null;
    }

    @NonCPS
    static def isFileExists(fileName) {
        return new File(fileName).exists();
    }

    @NonCPS
    static def gradlew(args) {
        new Utilities()._gradlew(args)
    }

    @NonCPS
    static def log(args) {
        new Utilities().log(args)
    }

    @NonCPS
    static def workspace() {
        new Utilities().getWorkspace()
    }

    @NonCPS
    static def getLibraryResource(n) {
        log "Loading library resource $n"
        return new Utilities().getLibraryResource(n);
    }
}


