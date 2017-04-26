import jenkins.model.*;
import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy
import static com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy.PROJECT;


static def createProjectFromXML(jobName, jobXml)
{
    Jenkins.instance.createProjectFromXML(jobName, new ByteArrayInputStream(jobXml.getBytes()))
}

static def checkViewExists(viewName)
{
    return Jenkins.instance.getView(viewName) != null;
}

static def createView(viewName)
{
    Jenkins.instance.addView(new ListView(viewName, Jenkins.instance))
}

static def addJobToView(jobName, viewName)
{
    Jenkins.instance.getView(viewName).add(Jenkins.instance.getItem(jobName))
}

static def assignSidToRole(sid, roleName) {
    def projectRoleMap = Jenkins.instance.authorizationStrategy.roleMaps.get(PROJECT);

    def role = projectRoleMap.getRole(roleName);

    try {
        if (role == null) {
            println "It's not possible to team role because the required role doesn't exist"
            return;
        }
        println "Assign $sid to role $roleName"
        projectRoleMap.assignRole(role, sid);
    }finally {
        projectRoleMap = null;
        role = null;
    }
}

static def getJobParameterNames(jobName) {
	def parametersNames= []
    for (e in Jenkins.instance.getItem(jobName).properties) {
        if (e.key instanceof hudson.model.ParametersDefinitionProperty.DescriptorImpl) {
            parametersNames = e.value.parameterDefinitionNames
        }
    }
	return parametersNames;
}
