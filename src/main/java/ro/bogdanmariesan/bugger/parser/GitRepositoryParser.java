package ro.bogdanmariesan.bugger.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class GitRepositoryParser {

    public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {

        List<File> files = new ArrayList<File>();
        listf("C:\\workspace\\gateway", files);

        List<String> actualFileNames = new ArrayList<String>();
        for (File f : files) {
            String fileRaw = f.getAbsolutePath();
            if (fileRaw.contains(".java")) {
                String fileWithoutHeader = fileRaw.replace("C:\\workspace\\gateway\\", "");
                String fileFinal = fileWithoutHeader.replace("\\", "/");
                actualFileNames.add(fileFinal);
            }
        }

        Repository repo = new FileRepository("C:\\workspace\\gateway\\.git");

        Git git = new Git(repo);
        RevWalk walk = new RevWalk(repo);

        RevCommit firstCommit = extractFirstCommit(walk, repo);
        RevCommit lastCommit = extractLastCommit(walk, repo);

        System.out.println(firstCommit.getFullMessage());
        System.out.println(lastCommit.getFullMessage());

        List<Ref> branches = git.branchList().call();
        List<RevCommit> commitsList = new ArrayList<RevCommit>();
        for (Ref branch : branches) {
            String branchName = branch.getName();

            System.out.println("Commits of branch: " + branch.getName());
            System.out.println("-------------------------------------");

            Iterable<RevCommit> commits = git.log().all().call();

            for (RevCommit commit : commits) {
                boolean foundInThisBranch = false;

                RevCommit targetCommit = walk.parseCommit(repo.resolve(commit.getName()));
                for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
                    if (e.getKey().startsWith(Constants.R_HEADS)) {
                        if (walk.isMergedInto(targetCommit, walk.parseCommit(e.getValue().getObjectId()))) {
                            String foundInBranch = e.getValue().getName();
                            if (branchName.equals(foundInBranch)) {
                                foundInThisBranch = true;
                                break;
                            }
                        }
                    }
                }

                if (foundInThisBranch) {
                    commitsList.add(commit);
                }
            }
        }

        Map<String, Map<Integer, Integer>> commitFilesMap = new TreeMap<String, Map<Integer, Integer>>();
        for (RevCommit commit : commitsList) {
            List<PathChangeModel> fileListInCommit = JGitUtils.getFilesInCommit(repo, commit);
            for (PathChangeModel file : fileListInCommit) {
                String fileName = file.name;
                // System.out.println(fileName);
                if (actualFileNames.contains(fileName)) {
                    Integer time = commit.getCommitTime();
                    if (commitFilesMap.containsKey(fileName)) {
                        Map<Integer, Integer> scoreMap = commitFilesMap.get(fileName);
                        if (scoreMap.containsKey(time)) {
                            Integer currentScore = scoreMap.get(time);
                            currentScore++;
                            scoreMap.put(time, currentScore);
                        } else {
                            Integer currentScore = 1;
                            scoreMap.put(time, currentScore);
                        }
                    } else {
                        Map<Integer, Integer> scoreMap = new HashMap<Integer, Integer>();
                        Integer currentScore = 1;
                        scoreMap.put(time, currentScore);
                        commitFilesMap.put(fileName, scoreMap);
                    }
                }
            }

        }

        double min = firstCommit.getCommitTime();
        double max = lastCommit.getCommitTime();

        for (String file : commitFilesMap.keySet()) {
            System.out.println(file);
        }

        // System.out.println((date - min) / (max - min));

    }

    private static RevCommit extractFirstCommit(RevWalk rw, Repository repo) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
        RevCommit c = null;
        AnyObjectId headId = repo.resolve(Constants.HEAD);
        RevCommit root = rw.parseCommit(headId);
        rw.sort(RevSort.REVERSE);
        rw.markStart(root);
        c = rw.next();
        return c;
    }

    private static RevCommit extractLastCommit(RevWalk rw, Repository repo) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
        AnyObjectId headId = repo.resolve(Constants.HEAD);
        RevCommit root = rw.parseCommit(headId);
        return root;
    }

    private static double extractBugScore(Map<Integer, Integer> map) {

        SortedSet<Integer> dateSet = new TreeSet<Integer>(map.keySet());
        Integer firstDate = dateSet.first();
        Integer lastDate = dateSet.last();

        // (x-min)/(max-min)

        return 0;
    }

    public static void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }
}
