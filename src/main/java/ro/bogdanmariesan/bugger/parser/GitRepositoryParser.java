package ro.bogdanmariesan.bugger.parser;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
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
import java.util.List;
import java.util.Map;

@Service
public final class GitRepositoryParser {

    private static final String GIT_REPOSITORY_SETTINGS = "/.git";
    private static final String PROGRAMMING_LANGUAGE_FILE_EXTENSION = ".java";


    private final GitRepositoryFileManager gitRepositoryFileManager;
    private final HotSpotScoreCalculator hotSpotScoreCalculator;
    private final GitCommitHandler gitCommitHandler;

    @Autowired
    public GitRepositoryParser(GitRepositoryFileManager gitRepositoryFileManager, HotSpotScoreCalculator hotSpotScoreCalculator, GitCommitHandler gitCommitHandler) {
        this.gitRepositoryFileManager = gitRepositoryFileManager;
        this.hotSpotScoreCalculator = hotSpotScoreCalculator;
        this.gitCommitHandler = gitCommitHandler;
    }


    private static Repository openJGitCookbookRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .findGitDir(new File("/Users/bogdan.mariesan/Applications/workspace/b2B"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }

    public void generateHotSpotScore(String repositoryLocation, Double bugScoreBoundary) throws IOException, GitAPIException {
        final List<File> rawFileNamesInGitRepository = new ArrayList<>();
        gitRepositoryFileManager.listFilesRecursively(repositoryLocation, rawFileNamesInGitRepository);

        final Repository repository = openJGitCookbookRepository();

        final Git git = new Git(repository);
        final List<RevCommit> commitsList = new ArrayList<>();

        final Iterable<RevCommit> commits = git.log()
                .add(repository.resolve("remotes/origin/master"))
                .call();

        final RevCommit firstCommit = gitCommitHandler.extractFirstCommit(repository);
        final RevCommit lastCommit = gitCommitHandler.extractLastCommit(repository);

        for (final RevCommit commit : commits) {
            commitsList.add(commit);
        }
        final Map<String, List<CommitModel>> commitsByFileNameMap = gitCommitHandler.produceDefectCommitMap(repository, commitsList);
        final List<HotSpotScore> hotSpotScores = hotSpotScoreCalculator.evaluateHotSpotScoreForBranch(commitsByFileNameMap, firstCommit, lastCommit, bugScoreBoundary);

        hotSpotScores.sort(new HotSpotScoreComparator());

        for (final HotSpotScore hotSpot : hotSpotScores) {
            System.out.println(hotSpot);
        }
    }


}
