package ro.bogdanmariesan.bugger.parser;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.bogdanmariesan.bugger.comparator.HotSpotScoreComparator;
import ro.bogdanmariesan.bugger.model.CommitModel;
import ro.bogdanmariesan.bugger.model.HotSpotScore;
import ro.bogdanmariesan.bugger.utils.file.GitRepositoryFileManager;
import ro.bogdanmariesan.bugger.utils.git.GitCommitHandler;
import ro.bogdanmariesan.bugger.utils.score.HotSpotScoreCalculator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public final class GitRepositoryParser {

    private static final String GIT_REPOSITORY_SETTINGS = "\\.git";
    private static final String PROGRAMMING_LANGUAGE_FILE_EXTENSION = ".java";


    @Autowired
    private GitRepositoryFileManager gitRepositoryFileManager;
    @Autowired
    private HotSpotScoreCalculator hotSpotScoreCalculator;
    @Autowired
    private GitCommitHandler gitCommitHandler;

    public void generateHotSpotScore(String repositoryLocation, Double bugScoreBoundary) throws IOException, GitAPIException {
        final List<File> rawFileNamesInGitRepository = new ArrayList<File>();
        gitRepositoryFileManager.listFilesRecursively(repositoryLocation, rawFileNamesInGitRepository);

        final Repository repository = new FileRepository(repositoryLocation + GIT_REPOSITORY_SETTINGS);

        final Git git = new Git(repository);
        git.checkout().setName("master").call();

        final List<Ref> branches = git.branchList().call();
        final List<RevCommit> commitsList = new ArrayList<RevCommit>();

        for (final Ref branch : branches) {
            // System.out.println(firstCommit.getFullMessage());
            // System.out.println(lastCommit.getFullMessage());
            final RevCommit firstCommit = gitCommitHandler.extractFirstCommit(repository);
            final RevCommit lastCommit = gitCommitHandler.extractLastCommit(repository);

            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.checkout().setName(branch.getName()).call();

            System.out.println("Commits of branch: " + branch.getName());
            System.out.println("-------------------------------------");

            final Iterable<RevCommit> commits = git.log().all().call();

            for (final RevCommit commit : commits) {
//                System.out.println(commit.getFullMessage());
                commitsList.add(commit);
            }
            final Map<String, List<CommitModel>> commitsByFileNameMap = gitCommitHandler.produceDefectCommitMap(repository, commitsList);
            final List<HotSpotScore> hotSpotScores = hotSpotScoreCalculator.evaluateHotSpotScoreForBranch(commitsByFileNameMap, firstCommit, lastCommit, bugScoreBoundary);

            Collections.sort(hotSpotScores, new HotSpotScoreComparator());

            for (final HotSpotScore hotSpot : hotSpotScores) {
                System.out.println(hotSpot);
            }
        }
    }


}
