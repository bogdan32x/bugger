package ro.bogdanmariesan.bugger.parser;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ro.bogdanmariesan.bugger.BuggerConfiguration;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BuggerConfiguration.class)
public class GitRepositoryParserTest {

    @Autowired
    private GitRepositoryParser gitRepositoryParser;
    private static final String GIT_REPOSITORY_LOCATION = "C:\\workspace\\Platform";
    private static final double BUG_SCORE_BOUNDARY = 2d;

    @Test
    public void gitRepositorySanityCheck() throws IOException, GitAPIException {
        gitRepositoryParser.generateHotSpotScore(GIT_REPOSITORY_LOCATION, BUG_SCORE_BOUNDARY);
    }


}