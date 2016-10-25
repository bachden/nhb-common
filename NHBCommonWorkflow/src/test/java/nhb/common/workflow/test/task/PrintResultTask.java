package nhb.common.workflow.test.task;

import nhb.common.async.BaseRPCFuture;
import nhb.common.async.Callback;
import nhb.common.async.RPCFuture;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.CancelDispatcher;
import nhb.common.workflow.async.CancelListener;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.async.impl.BaseTaskFuture;
import nhb.common.workflow.impl.AbstractTask;
import nhb.common.workflow.impl.GenericJobContext;

public class PrintResultTask extends AbstractTask {

	public PrintResultTask() {
		super("printResult");
	}

	@Override
	public TaskFuture execute(JobContext _context) {
		GenericJobContext context = castContext(_context);

		if ((boolean) context.getEnvironmentVariable("debug", false)) {
			System.out.println(
					"executing " + this.getName() + " task on thread [" + Thread.currentThread().getName() + "]");
		}
		context.getStateMachine().end();
		TaskFuture future = new BaseTaskFuture(context);
		future.dispatchSuccess();

		RPCFuture<Object> gaiaIdFuture = new BaseRPCFuture<>();
		RPCFuture<Object> facebookFuture = new BaseRPCFuture<>();

		if (context instanceof CancelDispatcher) {
			((CancelDispatcher) context).addCancelListener(new CancelListener() {

				@Override
				public void cancel() {
					gaiaIdFuture.cancel(false);
					facebookFuture.cancel(false);
				}
			});
		}

		gaiaIdFuture.setCallback(new Callback<Object>() {

			@Override
			public void apply(Object result) {
				context.getOutput().set("facebookId", result);
				if (gaiaIdFuture.isDone() && facebookFuture.isDone()) {
					future.dispatchSuccess();
				}
			}
		});

		facebookFuture.setCallback(new Callback<Object>() {

			@Override
			public void apply(Object result) {
				if (gaiaIdFuture.isDone() && facebookFuture.isDone()) {
					future.dispatchSuccess();
				}
			}
		});

		return future;
		// return null;
	}
}