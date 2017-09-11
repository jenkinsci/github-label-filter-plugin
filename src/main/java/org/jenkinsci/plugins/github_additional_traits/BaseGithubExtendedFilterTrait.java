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
package org.jenkinsci.plugins.github_additional_traits;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BaseGithubExtendedFilterTrait extends SCMSourceTrait {

    protected static final String DEFAULT_MATCH_ALL_REGEX = ".*";

    /**
     * The regex for filtering.
     */
    private String regex;

    /**
     * The pattern compiled from supplied regex
     */
    private transient Pattern pattern;

    public BaseGithubExtendedFilterTrait(String regex) {
        this.regex = regex;
        pattern = Pattern.compile(regex);
    }

    /**
     * Gets the regex
     *
     * @return the regex
     */
    public String getRegex() {
        return regex;
    }

    protected Pattern getPattern() {
        return pattern;
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
        public FormValidation doCheckRegex(@QueryParameter String value) {

            FormValidation formValidation;
            try {
                if (value.trim().isEmpty()) {
                    formValidation = FormValidation.error("Cannot have empty or blank regex.");
                } else {
                    Pattern.compile(value);
                    formValidation = FormValidation.ok();
                }

                if (DEFAULT_MATCH_ALL_REGEX.equals(value)) {
                    formValidation = FormValidation.warning("You should delete this trait instead of matching all");
                }

            } catch (PatternSyntaxException e) {
                formValidation = FormValidation.error("Invalid Regex : " + e.getMessage());
            }

            return formValidation;
        }
    }

}
