import java.io.File;
import java.net.URL;
import java.util.Base64;
import jenkins.model.*;
import hudson.FilePath;


def FileOperations() {
        channel = null;
}

def createFilePath(path) {
    if ("master".equals(env.NODE_NAME))
        return new FilePath(File(path));

    def channel = Jenkins.instance.getComputer(env.NODE_NAME).channel;
    return new FilePath(channel, path);
}

def downloadTo(String urlFrom, toPath) {
    FilePath fp = createFilePath(toPath);
    fp.copyFrom(new URL(urlFrom));
    return fp;
}

def mkdir(path) {
    createFilePath(path).mkdirs();
}

def remove(path) {
    createFilePath(path).delete();
}

def copyRecursive(from, to) {
    createFilePath(from).copyRecursiveTo(to);
}
