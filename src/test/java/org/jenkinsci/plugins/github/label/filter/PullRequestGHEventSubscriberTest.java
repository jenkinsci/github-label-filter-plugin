package org.jenkinsci.plugins.github.label.filter;

import com.cloudbees.jenkins.GitHubRepositoryName;
import hudson.model.Cause;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestGHEventSubscriberTest {
	@Mock
	private GHSubscriberEvent ghSubscriberEvent;

	@Spy
	PullRequestGHEventSubscriber subscriber = new PullRequestGHEventSubscriber();

	@Mock
	private GitHubRepositoryName gitHubRepositoryName;

	@Mock
	private WorkflowMultiBranchProject scmSourceOwner;

	@Mock
	private LabeledFilterWebHookTrigger trigger;

	@Mock
	private GitHubSCMSource gitHubSCMSource;

	@Mock(extraInterfaces = LabelsFilter.class)
	private SCMSourceTrait trait;


	@Before
	public void setUp(){
		when(gitHubRepositoryName.getUserName()).thenReturn("user1");
		when(gitHubRepositoryName.getRepositoryName()).thenReturn("repo1");
		Map triggers= new HashMap();
		triggers.put("any",trigger);
		when(scmSourceOwner.getTriggers()).thenReturn(triggers);
		when(scmSourceOwner.getSCMSources()).thenReturn(Arrays.asList(gitHubSCMSource));
		when(gitHubSCMSource.getRepoOwner()).thenReturn("user1");
		when(gitHubSCMSource.getRepository()).thenReturn("repo1");
		when(gitHubSCMSource.getTraits()).thenReturn(Arrays.asList(trait));
	}


	@Test
	public void testIsApplicable(){
		assertThat(subscriber.isApplicable(scmSourceOwner)).isTrue();
		assertThat(subscriber.isApplicable(null)).isFalse();
	}

	@Test
	public void testOnEvent() throws IOException {
		InputStream inputStream = PullRequestGHEventSubscriberTest.class.getResourceAsStream("pullRequestEvent.json");
		String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		when(ghSubscriberEvent.getPayload()).thenReturn(text);
		subscriber.onEvent(ghSubscriberEvent);
		Mockito.verify(subscriber,times(1)).process(any(),any());
	}

	@Test
	public void process (){


		subscriber.process(gitHubRepositoryName, Arrays.asList(scmSourceOwner));

		Mockito.verify(scmSourceOwner,times(1)).scheduleBuild(any(Cause.class));
	}
}