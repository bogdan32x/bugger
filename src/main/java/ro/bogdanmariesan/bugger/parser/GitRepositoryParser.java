package ro.bogdanmariesan.bugger.parser;

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
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class GitRepositoryParser {

    public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {

        Repository repo = new FileRepository("D:\\Programare\\workspace\\spring-framework\\.git");

        Git git = new Git(repo);
        RevWalk walk = new RevWalk(repo);

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

        Map<Integer, Map<String, Integer>> commitFilesMap = new TreeMap<Integer, Map<String, Integer>>();
        for (RevCommit commit : commitsList) {
            List<PathChangeModel> fileListInCommit = JGitUtils.getFilesInCommit(repo, commit);
            for (PathChangeModel file : fileListInCommit) {
                String fileName = file.name;
                Integer time = commit.getCommitTime();
                if (commitFilesMap.containsKey(time)) {
                    Map<String, Integer> scoreMap = commitFilesMap.get(time);
                    if (scoreMap.containsKey(fileName)) {
                        Integer currentScore = scoreMap.get(fileName);
                        currentScore++;
                        scoreMap.put(fileName, currentScore);
                    } else {
                        Integer currentScore = 1;
                        scoreMap.put(fileName, currentScore);
                    }
                } else {
                    Map<String, Integer> scoreMap = new HashMap<String, Integer>();
                    Integer currentScore = 1;
                    scoreMap.put(fileName, currentScore);
                    commitFilesMap.put(time, scoreMap);
                }
            }

        }

        SortedSet<Integer> dateSet = new TreeSet<Integer>(commitFilesMap.keySet());
        double min = dateSet.first();
        double max = dateSet.last();

        for (Integer date : dateSet) {
            System.out.println((date - min) / (max - min));
        }

    }

    private static double extractBugScore(Map<Integer, Integer> map) {

        SortedSet<Integer> dateSet = new TreeSet<Integer>(map.keySet());
        Integer firstDate = dateSet.first();
        Integer lastDate = dateSet.last();

        // (x-min)/(max-min)

        return 0;
    }
}
