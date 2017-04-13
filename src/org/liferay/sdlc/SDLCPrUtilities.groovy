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
			settingsGradle+="\nrootProject.name=$projectKey";
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

        def args=""
        if (isPullRequest()) {
            println "Sonarqube Pull Request Evaluation"
            args="-Dsonar.analysis.mode=preview -Dsonar.github.pullRequest=${_.CHANGE_ID()} -Dsonar.github.oauth=${_.GithubOauth()} -Dsonar.github.repository=${gitRepository}"
        }
        else {
            args="-Dsonar.analysis.mode=preview"
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
