package ro.bogdanmariesan.bugger.utils.file;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class GitRepositoryFileManager {

    public void listFilesRecursively(final String gitLocation, List<File> files) {
        final File directory = new File(gitLocation);
        // get all the files from a directory
        File[] listedFiles = directory.listFiles();
        if (listedFiles != null) {
            Arrays.stream(listedFiles).forEach(file -> {
                        if (file.isFile()) {
                            files.add(file);
                        } else if (file.isDirectory()) {
                            listFilesRecursively(file.getAbsolutePath(), files);
                        }
                    }
            );
        }
    }
}