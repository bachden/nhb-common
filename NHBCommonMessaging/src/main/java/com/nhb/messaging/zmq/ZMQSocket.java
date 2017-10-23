package com.nhb.messaging.zmq;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import lombok.Getter;

public class ZMQSocket {

	@Getter
	private final int port;
	@Getter
	private final String address;

	private final ZMQ.Socket socket;

	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final Runnable onCloseCallback;

	ZMQSocket(ZMQ.Socket socket, int port, String address, Runnable onCloseCallback) {
		if (socket == null) {
			throw new NullPointerException("ZMQ.Socket cannot be null");
		}
		this.socket = socket;
		this.port = port;
		this.address = address;
		this.onCloseCallback = onCloseCallback;
	}

	public void forwardTo(ZMQSocket backend) {
		ZMQ.proxy(this.socket, backend.socket, null);
	}

	public void proxy(ZMQSocket backend, ZMQSocket capture) {
		ZMQ.proxy(this.socket, backend.socket, capture.socket);
	}

	public ZMQSocketType getSocketType() {
		return ZMQSocketType.fromFlag(this.getType());
	}

	/**
	 * 
	 * @see org.zeromq.ZMQ.Socket#close()
	 */
	public void close() {
		if (this.closed.compareAndSet(false, true)) {
			socket.close();
			if (this.onCloseCallback != null) {
				this.onCloseCallback.run();
			}
		}
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getType()
	 */
	public int getType() {
		return socket.getType();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getLinger()
	 */
	public long getLinger() {
		return socket.getLinger();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getReconnectIVL()
	 */
	public long getReconnectIVL() {
		return socket.getReconnectIVL();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getBacklog()
	 */
	public long getBacklog() {
		return socket.getBacklog();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getReconnectIVLMax()
	 */
	public long getReconnectIVLMax() {
		return socket.getReconnectIVLMax();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getMaxMsgSize()
	 */
	public long getMaxMsgSize() {
		return socket.getMaxMsgSize();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getSndHWM()
	 */
	public long getSndHWM() {
		return socket.getSndHWM();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getRcvHWM()
	 */
	public long getRcvHWM() {
		return socket.getRcvHWM();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getHWM()
	 */
	public long getHWM() {
		return socket.getHWM();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getSwap()
	 */
	public long getSwap() {
		return socket.getSwap();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getAffinity()
	 */
	public long getAffinity() {
		return socket.getAffinity();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getTCPKeepAliveSetting()
	 */
	public long getTCPKeepAliveSetting() {
		return socket.getTCPKeepAliveSetting();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getTCPKeepAliveIdle()
	 */
	public long getTCPKeepAliveIdle() {
		return socket.getTCPKeepAliveIdle();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getTCPKeepAliveInterval()
	 */
	public long getTCPKeepAliveInterval() {
		return socket.getTCPKeepAliveInterval();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getTCPKeepAliveCount()
	 */
	public long getTCPKeepAliveCount() {
		return socket.getTCPKeepAliveCount();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getIdentity()
	 */
	public byte[] getIdentity() {
		return socket.getIdentity();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getLastEndpoint()
	 */
	public byte[] getLastEndpoint() {
		return socket.getLastEndpoint();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getRate()
	 */
	public long getRate() {
		return socket.getRate();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getRecoveryInterval()
	 */
	public long getRecoveryInterval() {
		return socket.getRecoveryInterval();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#hasMulticastLoop()
	 */
	public boolean hasMulticastLoop() {
		return socket.hasMulticastLoop();
	}

	/**
	 * @param mcast_hops
	 * @see org.zeromq.ZMQ.Socket#setMulticastHops(long)
	 */
	public void setMulticastHops(long mcast_hops) {
		socket.setMulticastHops(mcast_hops);
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getMulticastHops()
	 */
	public long getMulticastHops() {
		return socket.getMulticastHops();
	}

	/**
	 * @param timeout
	 * @see org.zeromq.ZMQ.Socket#setReceiveTimeOut(int)
	 */
	public void setReceiveTimeOut(int timeout) {
		socket.setReceiveTimeOut(timeout);
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getReceiveTimeOut()
	 */
	public int getReceiveTimeOut() {
		return socket.getReceiveTimeOut();
	}

	/**
	 * @param timeout
	 * @see org.zeromq.ZMQ.Socket#setSendTimeOut(int)
	 */
	public void setSendTimeOut(int timeout) {
		socket.setSendTimeOut(timeout);
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getSendTimeOut()
	 */
	public int getSendTimeOut() {
		return socket.getSendTimeOut();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getSendBufferSize()
	 */
	public long getSendBufferSize() {
		return socket.getSendBufferSize();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getReceiveBufferSize()
	 */
	public long getReceiveBufferSize() {
		return socket.getReceiveBufferSize();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getIPv4Only()
	 */
	public boolean getIPv4Only() {
		return socket.getIPv4Only();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getPlainServer()
	 */
	public boolean getPlainServer() {
		return socket.getPlainServer();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getPlainUsername()
	 */
	public byte[] getPlainUsername() {
		return socket.getPlainUsername();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getPlainPassword()
	 */
	public byte[] getPlainPassword() {
		return socket.getPlainPassword();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#hasReceiveMore()
	 */
	public boolean hasReceiveMore() {
		return socket.hasReceiveMore();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getFD()
	 */
	public long getFD() {
		return socket.getFD();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getEvents()
	 */
	public long getEvents() {
		return socket.getEvents();
	}

	/**
	 * @param linger
	 * @see org.zeromq.ZMQ.Socket#setLinger(long)
	 */
	public void setLinger(long linger) {
		socket.setLinger(linger);
	}

	/**
	 * @param reconnectIVL
	 * @see org.zeromq.ZMQ.Socket#setReconnectIVL(long)
	 */
	public void setReconnectIVL(long reconnectIVL) {
		socket.setReconnectIVL(reconnectIVL);
	}

	/**
	 * @param backlog
	 * @see org.zeromq.ZMQ.Socket#setBacklog(long)
	 */
	public void setBacklog(long backlog) {
		socket.setBacklog(backlog);
	}

	/**
	 * @param reconnectIVLMax
	 * @see org.zeromq.ZMQ.Socket#setReconnectIVLMax(long)
	 */
	public void setReconnectIVLMax(long reconnectIVLMax) {
		socket.setReconnectIVLMax(reconnectIVLMax);
	}

	/**
	 * @param maxMsgSize
	 * @see org.zeromq.ZMQ.Socket#setMaxMsgSize(long)
	 */
	public void setMaxMsgSize(long maxMsgSize) {
		socket.setMaxMsgSize(maxMsgSize);
	}

	/**
	 * @param sndHWM
	 * @see org.zeromq.ZMQ.Socket#setSndHWM(long)
	 */
	public void setSndHWM(long sndHWM) {
		socket.setSndHWM(sndHWM);
	}

	/**
	 * @param rcvHWM
	 * @see org.zeromq.ZMQ.Socket#setRcvHWM(long)
	 */
	public void setRcvHWM(long rcvHWM) {
		socket.setRcvHWM(rcvHWM);
	}

	/**
	 * @param hwm
	 * @see org.zeromq.ZMQ.Socket#setHWM(long)
	 */
	public void setHWM(long hwm) {
		socket.setHWM(hwm);
	}

	/**
	 * @param swap
	 * @see org.zeromq.ZMQ.Socket#setSwap(long)
	 */
	public void setSwap(long swap) {
		socket.setSwap(swap);
	}

	/**
	 * @param affinity
	 * @see org.zeromq.ZMQ.Socket#setAffinity(long)
	 */
	public void setAffinity(long affinity) {
		socket.setAffinity(affinity);
	}

	/**
	 * @param optVal
	 * @see org.zeromq.ZMQ.Socket#setTCPKeepAlive(long)
	 */
	public void setTCPKeepAlive(long optVal) {
		socket.setTCPKeepAlive(optVal);
	}

	/**
	 * @param optVal
	 * @see org.zeromq.ZMQ.Socket#setTCPKeepAliveCount(long)
	 */
	public void setTCPKeepAliveCount(long optVal) {
		socket.setTCPKeepAliveCount(optVal);
	}

	/**
	 * @param optVal
	 * @see org.zeromq.ZMQ.Socket#setTCPKeepAliveInterval(long)
	 */
	public void setTCPKeepAliveInterval(long optVal) {
		socket.setTCPKeepAliveInterval(optVal);
	}

	/**
	 * @param optVal
	 * @see org.zeromq.ZMQ.Socket#setTCPKeepAliveIdle(long)
	 */
	public void setTCPKeepAliveIdle(long optVal) {
		socket.setTCPKeepAliveIdle(optVal);
	}

	/**
	 * @param identity
	 * @see org.zeromq.ZMQ.Socket#setIdentity(byte[])
	 */
	public void setIdentity(byte[] identity) {
		socket.setIdentity(identity);
	}

	/**
	 * @param topic
	 * @see org.zeromq.ZMQ.Socket#subscribe(byte[])
	 */
	public void subscribe(byte[] topic) {
		socket.subscribe(topic);
	}

	/**
	 * @param topic
	 * @see org.zeromq.ZMQ.Socket#unsubscribe(byte[])
	 */
	public void unsubscribe(byte[] topic) {
		socket.unsubscribe(topic);
	}

	/**
	 * @param rate
	 * @see org.zeromq.ZMQ.Socket#setRate(long)
	 */
	public void setRate(long rate) {
		socket.setRate(rate);
	}

	/**
	 * @param recovery_ivl
	 * @see org.zeromq.ZMQ.Socket#setRecoveryInterval(long)
	 */
	public void setRecoveryInterval(long recovery_ivl) {
		socket.setRecoveryInterval(recovery_ivl);
	}

	/**
	 * @param mcast_loop
	 * @see org.zeromq.ZMQ.Socket#setMulticastLoop(boolean)
	 */
	public void setMulticastLoop(boolean mcast_loop) {
		socket.setMulticastLoop(mcast_loop);
	}

	/**
	 * @param sndbuf
	 * @see org.zeromq.ZMQ.Socket#setSendBufferSize(long)
	 */
	public void setSendBufferSize(long sndbuf) {
		socket.setSendBufferSize(sndbuf);
	}

	/**
	 * @param rcvbuf
	 * @see org.zeromq.ZMQ.Socket#setReceiveBufferSize(long)
	 */
	public void setReceiveBufferSize(long rcvbuf) {
		socket.setReceiveBufferSize(rcvbuf);
	}

	/**
	 * @param v4only
	 * @see org.zeromq.ZMQ.Socket#setIPv4Only(boolean)
	 */
	public void setIPv4Only(boolean v4only) {
		socket.setIPv4Only(v4only);
	}

	/**
	 * @param mandatory
	 * @see org.zeromq.ZMQ.Socket#setRouterMandatory(boolean)
	 */
	public void setRouterMandatory(boolean mandatory) {
		socket.setRouterMandatory(mandatory);
	}

	/**
	 * @param verbose
	 * @see org.zeromq.ZMQ.Socket#setXpubVerbose(boolean)
	 */
	public void setXpubVerbose(boolean verbose) {
		socket.setXpubVerbose(verbose);
	}

	/**
	 * @param plain
	 * @see org.zeromq.ZMQ.Socket#setPlainServer(boolean)
	 */
	public void setPlainServer(boolean plain) {
		socket.setPlainServer(plain);
	}

	/**
	 * @param username
	 * @see org.zeromq.ZMQ.Socket#setPlainUsername(byte[])
	 */
	public void setPlainUsername(byte[] username) {
		socket.setPlainUsername(username);
	}

	/**
	 * @param password
	 * @see org.zeromq.ZMQ.Socket#setPlainPassword(byte[])
	 */
	public void setPlainPassword(byte[] password) {
		socket.setPlainPassword(password);
	}

	/**
	 * @param domain
	 * @see org.zeromq.ZMQ.Socket#setZAPDomain(byte[])
	 */
	public void setZAPDomain(byte[] domain) {
		socket.setZAPDomain(domain);
	}

	/**
	 * @param isServer
	 * @see org.zeromq.ZMQ.Socket#setGSSAPIServer(boolean)
	 */
	public void setGSSAPIServer(boolean isServer) {
		socket.setGSSAPIServer(isServer);
	}

	/**
	 * @param principal
	 * @see org.zeromq.ZMQ.Socket#setGSSAPIPrincipal(byte[])
	 */
	public void setGSSAPIPrincipal(byte[] principal) {
		socket.setGSSAPIPrincipal(principal);
	}

	/**
	 * @param principal
	 * @see org.zeromq.ZMQ.Socket#setGSSAPIServicePrincipal(byte[])
	 */
	public void setGSSAPIServicePrincipal(byte[] principal) {
		socket.setGSSAPIServicePrincipal(principal);
	}

	/**
	 * @param isPlaintext
	 * @see org.zeromq.ZMQ.Socket#setGSSAPIPlainText(boolean)
	 */
	public void setGSSAPIPlainText(boolean isPlaintext) {
		socket.setGSSAPIPlainText(isPlaintext);
	}

	/**
	 * @param isServer
	 * @see org.zeromq.ZMQ.Socket#setCurveServer(boolean)
	 */
	public void setCurveServer(boolean isServer) {
		socket.setCurveServer(isServer);
	}

	/**
	 * @param key
	 * @see org.zeromq.ZMQ.Socket#setCurvePublicKey(byte[])
	 */
	public void setCurvePublicKey(byte[] key) {
		socket.setCurvePublicKey(key);
	}

	/**
	 * @param key
	 * @see org.zeromq.ZMQ.Socket#setCurveSecretKey(byte[])
	 */
	public void setCurveSecretKey(byte[] key) {
		socket.setCurveSecretKey(key);
	}

	/**
	 * @param key
	 * @see org.zeromq.ZMQ.Socket#setCurveServerKey(byte[])
	 */
	public void setCurveServerKey(byte[] key) {
		socket.setCurveServerKey(key);
	}

	/**
	 * @param conflate
	 * @see org.zeromq.ZMQ.Socket#setConflate(boolean)
	 */
	public void setConflate(boolean conflate) {
		socket.setConflate(conflate);
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getConflate()
	 */
	public boolean getConflate() {
		return socket.getConflate();
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getImmediate()
	 */
	public boolean getImmediate() {
		return socket.getImmediate();
	}

	/**
	 * @param immediate
	 * @see org.zeromq.ZMQ.Socket#setImmediate(boolean)
	 */
	public void setImmediate(boolean immediate) {
		socket.setImmediate(immediate);
	}

	/**
	 * @param isRelaxed
	 * @see org.zeromq.ZMQ.Socket#setReqRelaxed(boolean)
	 */
	public void setReqRelaxed(boolean isRelaxed) {
		socket.setReqRelaxed(isRelaxed);
	}

	/**
	 * @param isCorrelate
	 * @see org.zeromq.ZMQ.Socket#setReqCorrelate(boolean)
	 */
	public void setReqCorrelate(boolean isCorrelate) {
		socket.setReqCorrelate(isCorrelate);
	}

	/**
	 * @param isProbeRouter
	 * @see org.zeromq.ZMQ.Socket#setProbeRouter(boolean)
	 */
	public void setProbeRouter(boolean isProbeRouter) {
		socket.setProbeRouter(isProbeRouter);
	}

	/**
	 * @param addr
	 * @see org.zeromq.ZMQ.Socket#bind(java.lang.String)
	 */
	public void bind(String addr) {
		socket.bind(addr);
	}

	/**
	 * @param addr
	 * @return
	 * @see org.zeromq.ZMQ.Socket#bindToRandomPort(java.lang.String)
	 */
	public int bindToRandomPort(String addr) {
		return socket.bindToRandomPort(addr);
	}

	/**
	 * @param addr
	 * @param min_port
	 * @return
	 * @see org.zeromq.ZMQ.Socket#bindToRandomPort(java.lang.String, int)
	 */
	public int bindToRandomPort(String addr, int min_port) {
		return socket.bindToRandomPort(addr, min_port);
	}

	/**
	 * @param addr
	 * @param min_port
	 * @param max_port
	 * @return
	 * @see org.zeromq.ZMQ.Socket#bindToRandomPort(java.lang.String, int, int)
	 */
	public int bindToRandomPort(String addr, int min_port, int max_port) {
		return socket.bindToRandomPort(addr, min_port, max_port);
	}

	/**
	 * @param addr
	 * @param min_port
	 * @param max_port
	 * @param max_tries
	 * @return
	 * @see org.zeromq.ZMQ.Socket#bindToRandomPort(java.lang.String, int, int, int)
	 */
	public int bindToRandomPort(String addr, int min_port, int max_port, int max_tries) {
		return socket.bindToRandomPort(addr, min_port, max_port, max_tries);
	}

	/**
	 * @param addr
	 * @return
	 * @see org.zeromq.ZMQ.Socket#bindToSystemRandomPort(java.lang.String)
	 */
	public String bindToSystemRandomPort(String addr) {
		return socket.bindToSystemRandomPort(addr);
	}

	/**
	 * @param addr
	 * @see org.zeromq.ZMQ.Socket#unbind(java.lang.String)
	 */
	public void unbind(String addr) {
		socket.unbind(addr);
	}

	/**
	 * @param addr
	 * @see org.zeromq.ZMQ.Socket#connect(java.lang.String)
	 */
	public void connect(String addr) {
		socket.connect(addr);
	}

	/**
	 * @param addr
	 * @see org.zeromq.ZMQ.Socket#disconnect(java.lang.String)
	 */
	public void disconnect(String addr) {
		socket.disconnect(addr);
	}

	/**
	 * @param addr
	 * @param events
	 * @return
	 * @throws ZMQException
	 * @see org.zeromq.ZMQ.Socket#monitor(java.lang.String, int)
	 */
	public boolean monitor(String addr, int events) throws ZMQException {
		return socket.monitor(addr, events);
	}

	/**
	 * @param msg
	 * @param offset
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#send(byte[], int, int)
	 */
	public boolean send(byte[] msg, int offset, int flags) {
		return socket.send(msg, offset, flags);
	}

	/**
	 * @param msg
	 * @param offset
	 * @param len
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#send(byte[], int, int, int)
	 */
	public boolean send(byte[] msg, int offset, int len, int flags) {
		return socket.send(msg, offset, len, flags);
	}

	/**
	 * @param buffer
	 * @param len
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#sendZeroCopy(java.nio.ByteBuffer, int, int)
	 */
	public boolean sendZeroCopy(ByteBuffer buffer, int len, int flags) {
		return socket.sendZeroCopy(buffer, len, flags);
	}

	/**
	 * @param msg
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#send(byte[], int)
	 */
	public boolean send(byte[] msg, int flags) {
		return socket.send(msg, flags);
	}

	/**
	 * @param msg
	 * @return
	 * @see org.zeromq.ZMQ.Socket#send(java.lang.String)
	 */
	public boolean send(String msg) {
		return socket.send(msg);
	}

	/**
	 * @param msg
	 * @return
	 * @see org.zeromq.ZMQ.Socket#sendMore(java.lang.String)
	 */
	public boolean sendMore(String msg) {
		return socket.sendMore(msg);
	}

	/**
	 * @param msg
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#send(java.lang.String, int)
	 */
	public boolean send(String msg, int flags) {
		return socket.send(msg, flags);
	}

	/**
	 * @param bb
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#sendByteBuffer(java.nio.ByteBuffer, int)
	 */
	public int sendByteBuffer(ByteBuffer bb, int flags) {
		return socket.sendByteBuffer(bb, flags);
	}

	/**
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recv(int)
	 */
	public byte[] recv(int flags) {
		return socket.recv(flags);
	}

	/**
	 * @param buffer
	 * @param offset
	 * @param len
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recv(byte[], int, int, int)
	 */
	public int recv(byte[] buffer, int offset, int len, int flags) {
		return socket.recv(buffer, offset, len, flags);
	}

	/**
	 * @param buffer
	 * @param len
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recvZeroCopy(java.nio.ByteBuffer, int, int)
	 */
	public int recvZeroCopy(ByteBuffer buffer, int len, int flags) {
		return socket.recvZeroCopy(buffer, len, flags);
	}

	/**
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recv()
	 */
	public byte[] recv() {
		return socket.recv();
	}

	/**
	 * @return
	 * @deprecated
	 * @see org.zeromq.ZMQ.Socket#recvStr()
	 */
	public String recvStr() {
		return socket.recvStr();
	}

	/**
	 * @param charset
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recvStr(java.nio.charset.Charset)
	 */
	public String recvStr(Charset charset) {
		return socket.recvStr(charset);
	}

	/**
	 * @param flags
	 * @return
	 * @deprecated
	 * @see org.zeromq.ZMQ.Socket#recvStr(int)
	 */
	public String recvStr(int flags) {
		return socket.recvStr(flags);
	}

	/**
	 * @param flags
	 * @param charset
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recvStr(int, java.nio.charset.Charset)
	 */
	public String recvStr(int flags, Charset charset) {
		return socket.recvStr(flags, charset);
	}

	/**
	 * @param buffer
	 * @param flags
	 * @return
	 * @see org.zeromq.ZMQ.Socket#recvByteBuffer(java.nio.ByteBuffer, int)
	 */
	public int recvByteBuffer(ByteBuffer buffer, int flags) {
		return socket.recvByteBuffer(buffer, flags);
	}

	/**
	 * @param option
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getLongSockopt(int)
	 */
	public long getLongSockopt(int option) {
		return socket.getLongSockopt(option);
	}

	/**
	 * @param option
	 * @return
	 * @see org.zeromq.ZMQ.Socket#getBytesSockopt(int)
	 */
	public byte[] getBytesSockopt(int option) {
		return socket.getBytesSockopt(option);
	}

	/**
	 * @param option
	 * @param optval
	 * @see org.zeromq.ZMQ.Socket#setLongSockopt(int, long)
	 */
	public void setLongSockopt(int option, long optval) {
		socket.setLongSockopt(option, optval);
	}

	/**
	 * @param option
	 * @param optval
	 * @see org.zeromq.ZMQ.Socket#setBytesSockopt(int, byte[])
	 */
	public void setBytesSockopt(int option, byte[] optval) {
		socket.setBytesSockopt(option, optval);
	}
}
