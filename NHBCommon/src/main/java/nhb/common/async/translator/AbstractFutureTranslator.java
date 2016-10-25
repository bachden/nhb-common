package nhb.common.async.translator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nhb.common.BaseLoggable;
import nhb.common.annotations.ThreadSafe;
import nhb.common.async.RPCFuture;

public abstract class AbstractFutureTranslator<FromType, ToType> extends BaseLoggable
		implements FutureTranslator<FromType, ToType> {

	private final Future<FromType> sourceFuture;
	private volatile ToType lastResult;

	private volatile boolean lastResultIsNull = false;
	private boolean allowNullResult = false;
	private Throwable failedCause;

	public AbstractFutureTranslator(RPCFuture<FromType> future) {
		assert future != null;
		this.sourceFuture = future;
	}

	public AbstractFutureTranslator(RPCFuture<FromType> future, boolean allowNullResult) {
		this(future);
		this.allowNullResult = allowNullResult;
	}

	protected Future<FromType> getSourceFuture() {
		return this.sourceFuture;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.sourceFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.sourceFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.sourceFuture.isDone();
	}

	/**
	 * Attempt to parse and save the result returned by source future, using
	 * translate(result) method. <br>
	 * </br>
	 * Any <b>exception</b> thrown by translate() will be caught automatically
	 * and prevent the translate method to be re-executed
	 * 
	 * 
	 * @param result
	 *            the result from source future have to be translated
	 * @return translated result
	 */
	protected final ToType parseAndSaveResult(FromType result) {
		if (this.lastResult == null && this.getFailedCause() == null && !this.lastResultIsNull) {
			synchronized (this) {
				if (this.lastResult == null && this.getFailedCause() == null && !this.lastResultIsNull) {
					if (result != null || this.isAllowNullResult()) {
						try {
							this.lastResult = this.translate(cloneBeforeTranslate(result));
							if (lastResult == null) {
								this.lastResultIsNull = true;
							}
						} catch (Exception e) {
							this.setFailedCause(e);
						}
					} else {
						Throwable failedCause = null;
						if (this.sourceFuture instanceof RPCFuture) {
							failedCause = ((RPCFuture<FromType>) this.sourceFuture).getFailedCause();
						}
						if (failedCause == null) {
							failedCause = new NullPointerException("Result from source future is null");
						}
						this.setFailedCause(failedCause);
					}
				}
			}
		}
		return this.lastResult;
	}

	/**
	 * Translate response in FromType to ToType <br>
	 * </br>
	 * Any exception thrown will be caught by parseAndSaveResult() to prevent
	 * this method to be re-executed
	 * 
	 * @see AbstractFutureTranslator.parseAndSaveResponse
	 * @param sourceResult
	 *            the source result have to be translated
	 * @return the translated result
	 */
	@ThreadSafe
	protected abstract ToType translate(FromType sourceResult) throws Exception;

	/**
	 * By default, return <b>sourceResult</b> parameter by itself. <br/>
	 * Override this if the translate method make any change on the sourceResult
	 * value
	 * 
	 * @param sourceResult
	 * @return cloned value
	 */
	protected FromType cloneBeforeTranslate(FromType sourceResult) {
		return sourceResult;
	}

	@Override
	public ToType get() throws InterruptedException, ExecutionException {
		return this.parseAndSaveResult(this.sourceFuture.get());
	}

	@Override
	public ToType get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.parseAndSaveResult(this.sourceFuture.get(timeout, unit));
	}

	@Override
	public Throwable getFailedCause() {
		return failedCause;
	}

	protected void setFailedCause(Throwable failedCause) {
		this.failedCause = failedCause;
	}

	protected boolean isAllowNullResult() {
		return allowNullResult;
	}

	protected boolean isLastResultIsNull() {
		return this.lastResultIsNull;
	}

}
