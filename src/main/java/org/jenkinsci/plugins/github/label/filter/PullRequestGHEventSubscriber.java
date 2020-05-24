package org.jenkinsci.plugins.github.label.filter;

import com.cloudbees.jenkins.GitHubRepositoryName;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.security.ACL;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.SCMSourceOwners;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Sets.immutableEnumSet;
import static org.kohsuke.github.GHEvent.PULL_REQUEST;

@Extension
public class PullRequestGHEventSubscriber extends GHEventsSubscriber {
	private static final Logger LOGGER = Logger.getLogger(PullRequestGHEventSubscriber.class.getName());
	private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)");

	@Override
	protected boolean isApplicable(@Nullable Item project) {
		if (project != null) {
			if (project instanceof SCMSourceOwner) {
				SCMSourceOwner owner = (SCMSourceOwner) project;
				for (SCMSource source : owner.getSCMSources()) {
					if (source instanceof GitHubSCMSource) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected Set<GHEvent> events() {
		return immutableEnumSet(PULL_REQUEST);
	}

	@Override
	protected void onEvent(GHSubscriberEvent event) {
		try {
			final GHEventPayload.PullRequest p = GitHub.offline()
					.parseEventPayload(new StringReader(event.getPayload()), GHEventPayload.PullRequest.class);
			String action = p.getAction();
			String repoUrl = p.getRepository().getHtmlUrl().toExternalForm();
			LOGGER.log(Level.FINE, "Received {0} for {1} from {2}",
					new Object[]{event.getGHEvent(), repoUrl, event.getOrigin()}
			);
			Matcher matcher = REPOSITORY_NAME_PATTERN.matcher(repoUrl);
			if (matcher.matches()) {
				final GitHubRepositoryName changedRepository = GitHubRepositoryName.create(repoUrl);
				if (changedRepository == null) {
					LOGGER.log(Level.WARNING, "Malformed repository URL {0}", repoUrl);
					return;
				}
				if ("labeled".equals(action) || "unlabeled".equals(action)) {
					triggerScan(changedRepository);
				}
			}
		} catch (IOException e) {
			LogRecord lr = new LogRecord(Level.WARNING, "Could not parse {0} event from {1} with payload: {2}");
			lr.setParameters(new Object[]{event.getGHEvent(), event.getOrigin(), event.getPayload()});
			lr.setThrown(e);
			LOGGER.log(lr);
		}
	}

	private void triggerScan(GitHubRepositoryName changedRepository) {
		ACL.impersonate(ACL.SYSTEM, () -> {
			Iterable<SCMSourceOwner> scmSourceOwners = SCMSourceOwners.all();
			process(changedRepository, scmSourceOwners);

		});
	}


	void process(GitHubRepositoryName changedRepository, Iterable<SCMSourceOwner> scmSourceOwners) {
		StreamSupport.stream(scmSourceOwners.spliterator(), false)
				.filter(owner -> owner instanceof WorkflowMultiBranchProject)
				.map(owner -> (WorkflowMultiBranchProject) owner)
				.filter(this::hasLabelsFilterTrigger)
				.filter(owner ->
						owner.getSCMSources().stream()
								.filter(source -> source instanceof GitHubSCMSource)
								.map(source -> (GitHubSCMSource) source)
								.filter(isRepoMatch(changedRepository))
								.filter(this::hasLabelsFilterTraits)
								.findFirst()
								.isPresent()
				)
				.forEach(
						owner -> {
							owner.scheduleBuild(new Cause() {
								@Override
								public String getShortDescription() {
									return "Triggered by labels change";
								}
							});
							LOGGER.log(Level.FINE,
									"Repo {1}:{2}/{3} has labels filter and schedule build",
									new Object[]{
											changedRepository.getHost(),
											changedRepository.getUserName(),
											changedRepository.getRepositoryName()
									}
							);
						}
				);
	}

	private boolean hasLabelsFilterTrigger(WorkflowMultiBranchProject project) {
		return project.getTriggers().values().stream()
				.filter(trigger -> trigger instanceof LabeledFilterWebHookTrigger)
				.findFirst()
				.isPresent();
	}

	private Predicate<GitHubSCMSource> isRepoMatch(GitHubRepositoryName changedRepository) {
		return gitHubSCMSource ->
				gitHubSCMSource.getRepoOwner().equalsIgnoreCase(changedRepository.getUserName())
						&& gitHubSCMSource.getRepository().equalsIgnoreCase(changedRepository.getRepositoryName());
	}

	private boolean hasLabelsFilterTraits(GitHubSCMSource gitHubSCMSource) {
		return gitHubSCMSource.getTraits()
				.stream()
				.filter(trait -> trait instanceof LabelsFilter)
				.findFirst().isPresent();
	}
}
