package org.jenkinsci.plugins.github.label.filter;

import hudson.Extension;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;

public class LabeledFilterWebHookTrigger extends Trigger {


	@DataBoundConstructor
	public LabeledFilterWebHookTrigger() {
	}

	/**
	 * Our {@link hudson.model.Descriptor}
	 */
	@Extension
	@SuppressWarnings("unused") // instantiated by Jenkins
	public static class DescriptorImpl extends TriggerDescriptor {
		/**
		 * {@inheritDoc}
		 */
		public boolean isApplicable(Item item) {
			return item instanceof WorkflowMultiBranchProject;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getDisplayName() {
			return "Scan by labeled/unlabeled github webhook events";
		}
	}
}
