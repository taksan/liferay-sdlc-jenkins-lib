#!groovy
import groovy.json.JsonSlurper

import java.util.List;
import java.util.Map;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;

class SDLCPrUtilities {
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
        File failureReasonFile = new File("failureReasonFile");
        if (!failureReasonFile.exists())
            return false;
        
        def reasonText = failureReasonFile.text.trim();
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
            println "Will not close PR because error is not considered to be introduced by new code"
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

    @NonCPS
    static def gradlew(args)
    {
        if (isUnix())
            sh "./gradlew " + args
        else
            bat "gradlew " + args
    }

    @NonCPS
    static def appendAdditionalCommand(fileName, varMap) {
        def url = "https://raw.githubusercontent.com/objective-solutions/liferay-environment-bootstrap/master/custom.gradle";
        def additionalCustomCommands= new URL(url).getText();
		for (e in varMap) 
			additionalCustomCommands = additionalCustomCommands.replace("#{"+e.key+"}", e.value);

        def value = '';
        if (fileExists(fileName)) {
            value = readFile(fileName);
        }
        value += '\n\n'+ additionalCustomCommands;
        writeFile file: fileName, text: value
    }

    @NonCPS
    static def sonarqube(args)
    {
        def SonarHostUrl = global("SonarHostUrl");
        print "Running sonar with arguments : ${args}"
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

}


