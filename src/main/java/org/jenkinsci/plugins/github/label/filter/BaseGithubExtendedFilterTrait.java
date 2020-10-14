/*
 * The MIT License
 *
 * Copyright (c) 2017, Shantur Rathore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.github.label.filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.HyperlinkNote;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class BaseGithubExtendedFilterTrait extends SCMSourceTrait {

	/**
	 * The labels for filtering.
	 */
	private String labels;

	/**
	 * The label lists from from supplied labels string
	 */
	private transient List<String> labelsAsList;


	public BaseGithubExtendedFilterTrait(String labels) {
		this.labels = labels;
	}

	/**
	 * Gets the labels
	 *
	 * @return the labels
	 */
	public String getLabels() {
		return labels;
	}

	protected List<String> getLabelsAsList() {
		if (labelsAsList == null) {
			labelsAsList = Optional.ofNullable(getLabels())
					.filter(StringUtils::isNoneEmpty)
					.map(labels -> labels.split("\\s*,\\s*"))
					.map(Arrays::asList)
					.orElse(Collections.emptyList());
		}
		return labelsAsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void decorateContext(SCMSourceContext<?, ?> context) {
		GitHubSCMSourceContext ctx = (GitHubSCMSourceContext) context;
		ctx.withFilter(getScmHeadFilter());
	}

	protected abstract SCMHeadFilter getScmHeadFilter();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean includeCategory(@NonNull SCMHeadCategory category) {
		return category instanceof ChangeRequestSCMHeadCategory;
	}

	protected List<String> getPullRequestLabels(@NonNull GitHubSCMSourceRequest githubRequest, @NonNull PullRequestSCMHead pullRequestSCMHead) throws IOException {
        Optional<GHPullRequest> pr = StreamSupport.stream(githubRequest.getPullRequests().spliterator(), false)
                .filter(ghPullRequest -> ghPullRequest.getNumber() == pullRequestSCMHead.getNumber())
                .findFirst();
        if (pr.isPresent()) {
            GHPullRequest ghPullRequest = pr.get();
            List<String> labels = ghPullRequest.getLabels().stream().map(GHLabel::getName)
                    .collect(Collectors.toList());
            if (labels.isEmpty()) {
                githubRequest.listener().getLogger().format("%n  Found %s. has no labels %n", HyperlinkNote.encodeTo(ghPullRequest.getHtmlUrl().toString(), "#" + ghPullRequest.getNumber()));
            } else {
                githubRequest.listener().getLogger().format("%n  Found %s. has labels \"%s\" %n", HyperlinkNote.encodeTo(ghPullRequest.getHtmlUrl().toString(), "#" + ghPullRequest.getNumber()), String.join(",", labels));
            }
            return labels;
        } else {
            return Collections.emptyList();
        }
	}

	public static abstract class BaseDescriptorImpl extends SCMSourceTraitDescriptor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public abstract String getDisplayName();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends SCMSourceContext> getContextClass() {
			return GitHubSCMSourceContext.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends SCMSource> getSourceClass() {
			return GitHubSCMSource.class;
		}

		@Restricted(NoExternalUse.class)
		public FormValidation doCheckLabels(@QueryParameter String labels) {
			FormValidation formValidation;
			if (labels.trim().isEmpty()) {
				formValidation = FormValidation.error("Cannot have empty or blank regex.");
			} else {
				formValidation = FormValidation.ok();
			}
			return formValidation;
		}
	}

}
