package org.liferay.sdlc;

import jenkins.model.*;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.DirectEntryPrivateKeySource;

def credentialsExists(credentialsId)
{
    domains=SystemCredentialsProvider.instance.getDomainCredentials().domain;
    for (d in domains) {
        for (it in SystemCredentialsProvider.getInstance().getStore().getCredentials(d)) {
            if (it.id == credentialsId)
                return true;
        }   
    }
    return false;
}

def createSshPrivateKey(credentialsId, description, username, privateKey, passphrase)
{
    d = SystemCredentialsProvider.instance.getDomainCredentials().domain.get(0);
    c=new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, credentialsId, username, new DirectEntryPrivateKeySource(privateKey), passphrase, description)
    SystemCredentialsProvider.getInstance().getStore().addCredentials(d, c)
}

def getLoginFor(credentialsId)
{
    domains=SystemCredentialsProvider.instance.getDomainCredentials().domain;
    for (d in domains) {
        for (it in SystemCredentialsProvider.getInstance().getStore().getCredentials(d)) {
            if (it.id == credentialsId)
                return it.username
        }   
    }
    return null;
}

def createSshPrivateKeyIfNeeded(credId, missingMessage, description) 
{
    if (!credentialsExists(credId)) {
        println missingMessage
        def serverData = input(
            id: 'serverData', 
            message: 'Provide ssh credentials to access the server', 
            requestTimeout: 300,
            parameters: [
                [$class: 'StringParameterDefinition', description: 'username', name: 'username'],
                [$class: 'TextParameterDefinition', description: 'private key', name: 'privateKey'],
                [$class: 'PasswordParameterDefinition', description: 'passphrase', name: 'passphrase']
                ]
        )
        
        createSshPrivateKey(credId, description, serverData['username'], serverData['privateKey'], serverData['passphrase'].plainText)
    }
}
