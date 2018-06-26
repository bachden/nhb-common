package com.nhb.common.flag;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import lombok.Getter;

/**
 * Triple states flag for anti race condition issue
 * <p>
 * There're 3 states:
 * <ol>
 * <li><b>NONE</b> mean this flag is not started, can only change to
 * <b>PROCESSING</b> state by invoking <b>{@link #start()}</b></li>
 * <li><b>PROCESSING</b> mean this flag is marked as in processing state, can
 * only change to <b>DONE</b> state by invoking <b>{@link #done()}</b></li>
 * <li><b>DONE</b> mean this flag was done its process and didn't got reset, can
 * only change to <b>NONE</b> state by invoking <b>{@link #reset()}</b></li>
 * </ol>
 * </p>
 * 
 * @author bachden
 *
 */
public class MutexFlag {

	public static enum ProcessingState {
		NONE(0), PROCESSING(1), DONE(2);

		@Getter
		private final int code;

		private ProcessingState(int code) {
			this.code = code;
		}

		public static ProcessingState fromCode(int code) {
			for (ProcessingState state : values()) {
				if (state.getCode() == code) {
					return state;
				}
			}
			return null;
		}
	}

	private final AtomicReference<ProcessingState> state = new AtomicReference<>(ProcessingState.NONE);

	public ProcessingState getState() {
		return this.state.get();
	}

	/**
	 * change state from NONE to PROCESSING
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean start() {
		return this.state.compareAndSet(ProcessingState.NONE, ProcessingState.PROCESSING);
	}

	/**
	 * reset this flag's state to NONE
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean reset() {
		return this.state.compareAndSet(ProcessingState.DONE, ProcessingState.NONE);
	}

	/**
	 * change state from PROCESSING to DONE
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean done() {
		return this.state.compareAndSet(ProcessingState.PROCESSING, ProcessingState.DONE);
	}

	/**
	 * change state from PROCESSING to DONE
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean doneAndReset() {
		return this.state.compareAndSet(ProcessingState.PROCESSING, ProcessingState.NONE);
	}

	/**
	 * <p>
	 * if this flag is in PROCESSING, invoking thread will be locked by a busy spin
	 * to wait until DONE
	 * </p>
	 * <p>
	 * if this flag was DONE, return true immediately
	 * </p>
	 * <p>
	 * if this flag is NONE, return false
	 * </p>
	 * 
	 * @return true if processing is done, false otherwise (flag is not started)
	 */
	public boolean waitForProcessingToDone(long nanoToPack, AtomicBoolean spinBreaker) {
		waitForProcessing(nanoToPack, spinBreaker);
		return this.isDone();
	}

	/**
	 * <p>
	 * if this flag is in PROCESSING, invoking thread will be locked by a busy spin
	 * to wait until DONE
	 * </p>
	 * <p>
	 * if this flag was DONE, return true immediately
	 * </p>
	 * <p>
	 * if this flag is NONE, return false
	 * </p>
	 * 
	 * @return true if processing is done, false otherwise (flag is not started)
	 */
	public boolean waitForProcessingToDone(AtomicBoolean spinBreaker) {
		return waitForProcessingToDone(10, spinBreaker);
	}

	/**
	 * <p>
	 * if this flag is in PROCESSING, invoking thread will be locked by a busy spin
	 * to wait until DONE
	 * </p>
	 * <p>
	 * if this flag is DONE, return false immediately
	 * </p>
	 * <p>
	 * if this flag is NONE, return true
	 * </p>
	 * 
	 * @return true if processing is done, false otherwise (flag is not started)
	 */
	public boolean waitForProcessingToReset(AtomicBoolean spinBreaker) {
		return this.waitForProcessingToReset(10, spinBreaker);
	}

	/**
	 * <p>
	 * if this flag is in PROCESSING, invoking thread will be locked by a busy spin
	 * to wait until NONE
	 * </p>
	 * <p>
	 * if this flag is DONE, return false immediately
	 * </p>
	 * <p>
	 * if this flag is NONE, return true
	 * </p>
	 * 
	 * @return true if processing is done, false otherwise (flag is not started)
	 */
	public boolean waitForProcessingToReset(long nanoToPack, AtomicBoolean spinBreaker) {
		waitForProcessing(nanoToPack, spinBreaker);
		return this.isNone();
	}

	/**
	 * busy spin
	 * 
	 * @param nanoToPack
	 */
	private void waitForProcessing(long nanoToPack, AtomicBoolean breakSpinLoop) {
		while (this.isProcessing()) {
			if (breakSpinLoop == null || !breakSpinLoop.get()) {
				LockSupport.parkNanos(nanoToPack);
			}
		}
	}

	/**
	 * check if flag is in NONE state
	 */
	public boolean isNone() {
		return this.getState() == ProcessingState.NONE;
	}

	/**
	 * check if flag is in PROCESSING state
	 */
	public boolean isProcessing() {
		return this.getState() == ProcessingState.PROCESSING;
	}

	/**
	 * check if flag is in DONE state
	 */
	public boolean isDone() {
		return this.getState() == ProcessingState.DONE;
	}

	/**
	 * check if this flag's status is NONE, apply input runnable then set DONE
	 * 
	 * @param runnable
	 *            work to be executed
	 * @return true if runnable got executed, false otherwise
	 */
	public boolean applyThenDone(Runnable runnable) {
		// this flag's state should be NONE
		if (this.start()) {
			// this flag's state should be PROCESSING
			runnable.run();
			// end PROCESSING
			this.done();
			// this flag's state should be DONE
			return true;
		}
		return false;
	}

	/**
	 * check if this flag is NONE, apply input runnable then reset to NONE
	 * 
	 * @param runnable
	 *            work to be executed
	 * @return true if runnable got executed, false otherwise
	 */
	public boolean applyThenReset(Runnable runnable) {
		// this flag's state should be NONE
		if (this.start()) {
			// this flag's state should be PROCESSING
			runnable.run();
			// end PROCESSING
			this.doneAndReset();
			// this flag's state should be NONE
			return true;
		}
		return false;
	}

	/**
	 * Wait for processing to be reset to NONE then try to apply runnable, pack 10
	 * nanoseconds on each spin loop
	 * 
	 * @param runnable
	 */
	public boolean waitToApplyThenReset(Runnable runnable, AtomicBoolean spinBreaker) {
		return this.waitToApplyThenReset(runnable, 10, spinBreaker);
	}

	/**
	 * Wait for processing to be reset to NONE then try to apply runnable
	 * 
	 * @param runnable
	 *            work to be executed
	 * @param nanoToPack
	 *            time in nano to pack on each spin loop
	 */
	public boolean waitToApplyThenReset(Runnable runnable, long nanoToPack, AtomicBoolean spinBreaker) {
		while (true) {
			if (this.waitForProcessingToReset(nanoToPack, spinBreaker)) {
				if (this.applyThenReset(runnable)) {
					return true;
				}
			} else {
				return false;
			}
		}
	}
}
