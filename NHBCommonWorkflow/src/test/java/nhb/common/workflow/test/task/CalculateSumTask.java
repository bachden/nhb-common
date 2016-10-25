package nhb.common.workflow.test.task;

import nhb.common.data.PuArray;
import nhb.common.data.PuDataType;
import nhb.common.data.PuObject;
import nhb.common.data.PuValue;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.impl.AbstractTask;
import nhb.common.workflow.impl.GenericJobContext;
import nhb.common.workflow.test.CalculatorState;

public class CalculateSumTask extends AbstractTask {

	public CalculateSumTask() {
		super("calculateSum");
	}

	@Override
	public TaskFuture execute(JobContext context) {
		if ((boolean) context.getEnvironmentVariable("debug", false)) {
			System.out.println(
					"executing " + this.getName() + " task on thread [" + Thread.currentThread().getName() + "]");
		}
		GenericJobContext genericJobContext = castContext(context);
		int value = 0;
		if (genericJobContext.getInput() != null) {
			PuArray elements = genericJobContext.getInput().getPuArray("data");
			for (PuValue puValue : elements) {
				if (puValue.getType() == PuDataType.INTEGER) {
					value += puValue.getInteger();
				}
			}
		}
		if (genericJobContext.getOutput() == null) {
			genericJobContext.setOutput(new PuObject());
		}
		genericJobContext.getOutput().set("sum", value);
		context.getStateMachine().gotoState(CalculatorState.PRINT_RESULT);
		return null;
	}

}