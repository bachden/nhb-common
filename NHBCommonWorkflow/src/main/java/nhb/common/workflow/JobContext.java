package nhb.common.workflow;

import nhb.common.workflow.statemachine.StateMachine;

public interface JobContext {

	int getId();

	Job getParentJob();

	/**
	 * Short hand for getParentJob().getName();
	 * 
	 * @return the parent job's name
	 */
	default String getJobName() {
		return this.getParentJob().getName();
	}

	StateMachine getStateMachine();

	/**
	 * Short hand for getParentJob().getEnvironmentVariable(varName)
	 * 
	 * @param varName
	 *            variable name to get
	 * @return variable value
	 */
	default <T> T getEnvironmentVariable(String varName) {
		return this.getParentJob().getEnvironmentVariable(varName);
	}

	default <T> T getEnvironmentVariable(String varName, T defaultValue) {
		if (this.getParentJob().containsEnvironmentVariable(varName)) {
			return this.getParentJob().getEnvironmentVariable(varName);
		}
		return defaultValue;
	}

	/**
	 * clear all listener, use before execute new task
	 */
	void clear();
}
