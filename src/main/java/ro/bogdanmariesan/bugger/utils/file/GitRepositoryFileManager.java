package ro.bogdanmariesan.bugger.utils.file;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class GitRepositoryFileManager {

    public void listFilesRecursively(final String gitLocation, List<File> files) {
        final File directory = new File(gitLocation);

        // get all the files from a directory
        for (final File file : directory.listFiles()) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFilesRecursively(file.getAbsolutePath(), files);
            }
        }
    }
}