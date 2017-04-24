import java.io.File;
import java.net.URL;
import java.util.Base64;
import jenkins.model.*;
import hudson.FilePath;
import hudson.FilePath.FileCallable;


def FileOperations() {
        channel = null;
}

def createFilePath(path) {
    if (!path.startsWith("/"))
        path = "$workspace/$path";
    if ("master".equals(env.NODE_NAME))
        return new FilePath(File(path));

    def channel = Jenkins.instance.getComputer(env.NODE_NAME).channel;
    return new FilePath(channel, path);
}

def downloadTo(String urlFrom, toPath) {
    println "downloadTo $urlFrom -> $toPath"
    FilePath fp = createFilePath(toPath);
    fp.copyFrom(new URL(urlFrom));
    return fp;
}

def mkdir(path) {
    println "mkdir $path"
    createFilePath(path).mkdirs();
}

def remove(path) {
    println "remove $path"
    createFilePath(path).delete();
}

def copyRecursive(from, to) {
    println "copyRecursiveTo $from -> $to"
    createFilePath(from).copyRecursiveTo(createFilePath(to));
}

def unzip(zipFile, targetDir) {
    println "unzip $zipFile -> $targetDir"
    zipFile.unzip(createFilePath(targetDir));
}

def zip(path, zipFileName) {
    println "zip $path -> $zipFileName"
    //createFilePath(path).zip(createFilePath(zipFileName));
    workspace.act(new FileCallable<Void>() {
        @Override public Void invoke(File f, VirtualChannel channel) {
            zip archive: zipFileName, dir: path
            return null;
        }
    });
}
