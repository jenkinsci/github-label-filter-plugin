package org.jenkinsci.plugins.github.label.filter;

import hudson.model.TaskListener;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)

public class PullRequestLabelsMatchAllFilterTraitTest {
    @Mock
    GitHubSCMSourceRequest gitHubSCMSourceRequest;
    @Mock
    GHPullRequest ghPullRequest;
    @Mock
    PullRequestSCMHead pullRequestSCMHead;
    @Mock
    GHLabel ghLabel1;
    @Mock
    GHLabel ghLabel2;
    @Mock
    TaskListener taskListener;
    @Mock
    PrintStream logger;
    URL ghPullRequestUrl = new URL("http://github.com/own1/repo1/pull/1");

    public PullRequestLabelsMatchAllFilterTraitTest() throws MalformedURLException {
    }

    @Before
    public void setup() {
        when(gitHubSCMSourceRequest.getPullRequests()).thenReturn(Arrays.asList(ghPullRequest));
        when(ghPullRequest.getNumber()).thenReturn(12);
        when(pullRequestSCMHead.getNumber()).thenReturn(12);
        when(ghLabel1.getName()).thenReturn("label1");
        when(ghLabel2.getName()).thenReturn("label2");
        when(gitHubSCMSourceRequest.listener()).thenReturn(taskListener);
        when(taskListener.getLogger()).thenReturn(logger);
        when(ghPullRequest.getHtmlUrl()).thenReturn(ghPullRequestUrl);

    }

    @Test
    public void testMatchAll() throws IOException, InterruptedException {
        when(ghPullRequest.getLabels()).thenReturn(Arrays.asList(ghLabel1, ghLabel2));
        boolean isExcluded = filter("label1,label2").isExcluded(gitHubSCMSourceRequest, pullRequestSCMHead);
        assertThat(isExcluded).isFalse();
        Mockito.verify(logger, times(2)).format(any(),any());
    }

    @Test
    public void testMatchSome() throws IOException, InterruptedException {
        when(ghPullRequest.getLabels()).thenReturn(Arrays.asList(ghLabel1, ghLabel2));
        boolean isExcluded = filter("label1,label4").isExcluded(gitHubSCMSourceRequest, pullRequestSCMHead);
        assertThat(isExcluded).isTrue();
        Mockito.verify(logger, times(2)).format(any(),any());
    }

    @Test
    public void testNoMatch() throws IOException, InterruptedException {
        when(ghPullRequest.getLabels()).thenReturn(Arrays.asList(ghLabel1, ghLabel2));
        boolean isExcluded = filter("label3,label4").isExcluded(gitHubSCMSourceRequest, pullRequestSCMHead);
        assertThat(isExcluded).isTrue();
        Mockito.verify(logger, times(2)).format(any(),any());
    }


    @Test
    public void testPRNoLabel() throws IOException, InterruptedException {
        when(ghPullRequest.getLabels()).thenReturn(Collections.emptyList());
        boolean isExcluded = filter("label3,label4").isExcluded(gitHubSCMSourceRequest, pullRequestSCMHead);
        assertThat(isExcluded).isTrue();
        Mockito.verify(logger, times(2)).format(any(),any());
    }

    private SCMHeadFilter filter(String s) {
        return new PullRequestLabelsMatchAllFilterTrait(s).getScmHeadFilter();
    }
}