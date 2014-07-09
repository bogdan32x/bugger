package ro.bogdanmariesan.bugger.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import ro.bogdanmariesan.bugger.comparator.CommitModelComparator;
import ro.bogdanmariesan.bugger.comparator.HotSpotScoreComparator;
import ro.bogdanmariesan.bugger.model.CommitModel;
import ro.bogdanmariesan.bugger.model.HotSpotScore;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class GitRepositoryParser {

    public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {

        List<File> files = new ArrayList<File>();
        listf("C:\\workspace\\bugger", files);

        List<String> actualFileNames = new ArrayList<String>();
        for (File f : files) {
            String fileRaw = f.getAbsolutePath();
            if (fileRaw.contains(".java")) {
                String fileWithoutHeader = fileRaw.replace("C:\\workspace\\bugger\\", "");
                String fileFinal = fileWithoutHeader.replace("\\", "/");
                actualFileNames.add(fileFinal);
            }
        }

        Repository repo = new FileRepository("C:\\workspace\\bugger\\.git");

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

        Map<String, List<CommitModel>> commitFilesMap = new TreeMap<String, List<CommitModel>>();
        for (RevCommit commit : commitsList) {
            if (commit.getFullMessage().contains("bug") || commit.getFullMessage().contains("issue") || commit.getFullMessage().contains("fix")) {
                List<PathChangeModel> fileListInCommit = JGitUtils.getFilesInCommit(repo, commit);
                for (PathChangeModel file : fileListInCommit) {
                    String fileName = file.name;
                    // System.out.println(fileName);
                    if (actualFileNames.contains(fileName)) {
                        Integer date = commit.getCommitTime();
                        if (commitFilesMap.containsKey(fileName)) {
                            List<CommitModel> commitModelList = commitFilesMap.get(fileName);
                            commitModelList.add(new CommitModel(date));
                            commitFilesMap.put(fileName, commitModelList);
                        } else {
                            List<CommitModel> commitModelList = new ArrayList<CommitModel>();
                            commitModelList.add(new CommitModel(date));
                            commitFilesMap.put(fileName, commitModelList);
                        }
                    }
                }

            }
        }

        double min = firstCommit.getCommitTime();
        double max = lastCommit.getCommitTime();

        List<HotSpotScore> hotspotList = new ArrayList<HotSpotScore>();

        for (String file : commitFilesMap.keySet()) {
            List<CommitModel> commitList = commitFilesMap.get(file);
            Collections.sort(commitList, new CommitModelComparator());
            double score = extractBugScore(commitList, min, max);
            if (score >= 5) {
                hotspotList.add(new HotSpotScore(score, file));
            }
        }

        Collections.sort(hotspotList, new HotSpotScoreComparator());

        for (HotSpotScore hotSpot : hotspotList) {
            System.out.println(hotSpot);
        }
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

    private static double extractBugScore(List<CommitModel> list, double min, double max) {
        double score = 0;
        for (int k = 0; k < list.size(); k++) {
            score += computeSimpleScore(list.get(k), k, min, max);
        }
        return score;
    }

    private static double computeSimpleScore(CommitModel commitModel, int i, double min, double max) {
        // (x-min)/(max-min) - normalization
        double t = (commitModel.getDate() - min) / (max - min);
        double exp = Math.exp(-12 * t * i + 12);
        double denominator = 1 + exp;
        double numerator = 1;
        return numerator / denominator;
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
