import java.io.File;
import java.net.URL;
import java.util.Base64;
import jenkins.model.*;
import hudson.FilePath;

static def fops = new FileOperations();

def createFilePath(path) {
    if ("master".equals(NODE_NAME))
        return new FilePath(File(path));
    println build;
    return new FilePath(build.workspace.getChannel(), path);
}

def downloadTo(String urlFrom, toPath) {
	createFilePath(toPath).copyFrom(new URL(urlFrom));
}

def mkdir(path) {
	createFilePath(bundle_artifact).mkdirs();
}

def remove(path) {
	createFilePath(path).delete();
}

def copyRecursive(from, to) {
	createFilePath(from).copyRecursiveTo(bundle_artifact);
}

