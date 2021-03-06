package ro.bogdanmariesan.bugger.utils.git;

import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Component;
import ro.bogdanmariesan.bugger.model.CommitModel;

import java.io.IOException;
import java.util.*;

@Component
public class GitCommitHandler {

    private static final List<String> HOT_SPOT_DICTIONARY = Arrays.asList("bug", "defect", "issue", "fix");

    public RevCommit extractFirstCommit(Repository repository) throws RevisionSyntaxException, IOException {
        final RevWalk revisionWalk = new RevWalk(repository);
        final AnyObjectId headId = repository.resolve(Constants.HEAD);
        final RevCommit root = revisionWalk.parseCommit(headId);
        revisionWalk.sort(RevSort.REVERSE);
        revisionWalk.markStart(root);
        final RevCommit commit = revisionWalk.next();
        revisionWalk.reset();
        return commit;
    }

    public RevCommit extractLastCommit(final Repository repository) throws RevisionSyntaxException, IOException {
        final RevWalk revisionWalk = new RevWalk(repository);
        final AnyObjectId headId = repository.resolve(Constants.HEAD);
        return revisionWalk.parseCommit(headId);
    }

    public Map<String, List<CommitModel>> produceDefectCommitMap(Repository repository, Iterable<RevCommit> commitsList) {
        final Map<String, List<CommitModel>> commitFilesMap = new TreeMap<>();
        commitsList.forEach(commit -> {
            if (isCommitADefect(commit)) {
                final List<PathModel.PathChangeModel> fileListInCommit = JGitUtils.getFilesInCommit(repository, commit);
                fileListInCommit.stream()
                        .filter(file -> file.name.contains(".java"))
                        .forEach(file -> {
                            final String fileName = file.name;
                            //     System.out.println(fileName);
                            final Double date = (double) commit.getCommitTime();
                            if (commitFilesMap.containsKey(fileName)) {
                                final List<CommitModel> commitModelList = commitFilesMap.get(fileName);
                                commitModelList.add(new CommitModel(date));
                                commitFilesMap.put(fileName, commitModelList);
                            } else {
                                final List<CommitModel> commitModelList = new ArrayList<>();
                                commitModelList.add(new CommitModel(date));
                                commitFilesMap.put(fileName, commitModelList);
                            }
                        });
            }
        });
        return commitFilesMap;
    }

    private boolean isCommitADefect(RevCommit commit) {
        boolean result = false;
        for (final String dictionaryKey : HOT_SPOT_DICTIONARY) {
            if (commit.getFullMessage().contains(dictionaryKey)) {
                result = true;
                break;
            }
        }
        return result;
    }
}