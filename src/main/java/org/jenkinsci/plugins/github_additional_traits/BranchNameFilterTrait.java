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
import hudson.Extension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.plugins.github_branch_source.BranchSCMHead;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * A {@link Discovery} trait for GitHub that will only select branches with name matching regex.
 *
 */
public class BranchNameFilterTrait extends BaseGithubExtendedFilterTrait {

    /**
     * Constructor for stapler.
     *
     * @param regex Label for filtering
     */
    @DataBoundConstructor
    public BranchNameFilterTrait(String regex) {
        super(regex);
    }

    protected SCMHeadFilter getScmHeadFilter() {
        SCMHeadFilter scmHeadFilter = new SCMHeadFilter() {

            @Override
            public boolean isExcluded(@NonNull SCMSourceRequest request,
                                      @NonNull SCMHead head) throws IOException, InterruptedException {

                if (request instanceof GitHubSCMSourceRequest && head instanceof BranchSCMHead) {
                    BranchSCMHead branchSCMHead = (BranchSCMHead) head;

                    if (!DEFAULT_MATCH_ALL_REGEX.equals(getRegex())) {

                        String branchName = branchSCMHead.getName();
                        boolean found = getPattern().matcher(branchName).matches();

                        if (found) {
                            request.listener().getLogger().format("%n    Will Build branch %s.%n", branchName);
                        } else {
                            request.listener().getLogger().format("%n    Won't build branch %s. name doesn't match filter.", branchName);
                        }
                        return !found;

                    }
                }

                return false;
            }
        };

        return scmHeadFilter;
    }

    @Extension
    @Discovery
    public static class DescriptorImpl extends BaseDescriptorImpl {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Filter only branches by name";
        }

    }

}
