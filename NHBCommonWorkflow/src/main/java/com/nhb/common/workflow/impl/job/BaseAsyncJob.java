package com.nhb.common.workflow.impl.job;

import com.nhb.common.workflow.JobAware;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.async.TaskFuture;
import com.nhb.common.workflow.async.impl.BaseTaskFuture;
import com.nhb.common.workflow.concurrent.impl.DisruptorWorkProcessorGroup;
import com.nhb.common.workflow.concurrent.route.RoutingBus;
import com.nhb.common.workflow.holder.WorkProcessorGroupHolder;
import com.nhb.common.workflow.impl.AbstractJob;
import com.nhb.common.workflow.impl.BaseWorkProcessorGroupHolder;

public class BaseAsyncJob extends AbstractJob {

	private DisruptorWorkProcessorGroup defaultDisruptorWorkProcessorGroup;
	private RoutingBus routingBus;

	public BaseAsyncJob(String name, WorkProcessorGroupHolder workProcessorGroupHolder, RoutingBus routingBus) {
		super(name, workProcessorGroupHolder);
		this.routingBus = routingBus;
		this.routingBus.setTaskHolder(this);
		this.routingBus.setWorkProcessorGroupHolder(this.getWorkProcessorGroupHolder());
		this.routingBus.start();
	}

	public BaseAsyncJob(String name, WorkProcessorGroupHolder workProcessorGroupHolder) {
		super(name, workProcessorGroupHolder);
		this.routingBus = new RoutingBus(2, 2048, name + " router #%d", this, this.getWorkProcessorGroupHolder());
		this.routingBus.start();
	}

	public BaseAsyncJob(String name) {
		this(name, new BaseWorkProcessorGroupHolder());
	}

	public BaseAsyncJob(String name, RoutingBus routingBus) {
		super(name, new BaseWorkProcessorGroupHolder());
		this.routingBus = routingBus;
		this.routingBus.setTaskHolder(this);
		this.routingBus.setWorkProcessorGroupHolder(this.getWorkProcessorGroupHolder());
		this.routingBus.start();
	}

	public void shutdown() {
		if (this.defaultDisruptorWorkProcessorGroup != null) {
			this.defaultDisruptorWorkProcessorGroup.shutdown();
			this.defaultDisruptorWorkProcessorGroup = null;
		}
		this.routingBus.shutdown();
	}

	@Override
	public TaskFuture execute(JobContext context) {
		BaseTaskFuture future = new BaseTaskFuture(context);
		if (context instanceof JobAware) {
			((JobAware) context).setParentJob(this);
		}
		this.routingBus.submit(context, future);
		return future;
	}
}
