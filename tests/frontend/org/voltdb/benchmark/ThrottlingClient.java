/**
 * 
 */
package org.voltdb.benchmark;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ClientStatusListener;
import org.voltdb.client.NoConnectionsException;
import org.voltdb.client.ProcCallException;
import org.voltdb.client.ProcedureCallback;

import edu.brown.utils.LoggerUtil;

/**
 * @author pavlo
 *
 */
public class ThrottlingClient extends Semaphore implements Client {
    static final Logger LOG = Logger.getLogger(ThrottlingClient.class);
    
    private static final long serialVersionUID = 1L;
    private final Client inner;
    private final AtomicBoolean throttle = new AtomicBoolean(false);
    
    
    private class ThrottleCallback implements ProcedureCallback {
        private final ProcedureCallback inner_callback;
        private final String proc_name;
        
        public ThrottleCallback(String proc_name, ProcedureCallback inner_callback) {
            final boolean debug = LOG.isDebugEnabled(); 
            assert(inner_callback != null);
            this.inner_callback = inner_callback;
            this.proc_name = proc_name;
            
            if (debug) LOG.debug("Created a new ThrottlingCallback around " + inner_callback.getClass().getSimpleName() + " for '" + proc_name + "'");
            try {
                if (debug) LOG.debug("Trying to acquire procedure invocation lock");
                if (throttle.get()) ThrottlingClient.this.tryAcquire(1000, TimeUnit.MILLISECONDS); // To prevent starvation
                if (debug) LOG.debug("We got it! Let's get it on! [proc_name=" + this.proc_name + "]");
            } catch (InterruptedException ex) {
                LOG.fatal("Got interrupted while waiting for lock", ex);
                System.exit(1);
            }
            
        }
        
        @Override
        public void clientCallback(ClientResponse clientResponse) {
            final boolean debug = LOG.isDebugEnabled();
            boolean should_throttle = clientResponse.getThrottle();
            if (debug) LOG.debug(String.format("ThrottlingCallback is forwarding the client callback for to inner callback [txn=%d, proc=%s, throttle=%s]",
                                               clientResponse.getTransactionId(), this.proc_name, should_throttle));
            this.inner_callback.clientCallback(clientResponse);
             
            if (throttle.compareAndSet(true, should_throttle) && should_throttle == false) {
                ThrottlingClient.this.release();
            }
            
        }
    }
    
    /**
     * 
     */
    public ThrottlingClient(Client inner) {
        super(1);
        this.inner = inner;
        LoggerUtil.setupLogging();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#addClientStatusListener(org.voltdb.client.ClientStatusListener)
     */
    @Override
    public void addClientStatusListener(ClientStatusListener listener) {
        this.inner.addClientStatusListener(listener);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#backpressureBarrier()
     */
    @Override
    public void backpressureBarrier() throws InterruptedException {
        this.inner.backpressureBarrier();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#blocking()
     */
    @Override
    public boolean blocking() {
        return this.inner.blocking();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#calculateInvocationSerializedSize(java.lang.String, java.lang.Object[])
     */
    @Override
    public int calculateInvocationSerializedSize(String procName, Object... parameters) {
        return this.inner.calculateInvocationSerializedSize(procName, parameters);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#callProcedure(java.lang.String, java.lang.Object[])
     */
    @Override
    public ClientResponse callProcedure(String procName, Object... parameters) throws IOException,
            NoConnectionsException, ProcCallException {
        return this.inner.callProcedure(procName, parameters);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#callProcedure(org.voltdb.client.ProcedureCallback, java.lang.String, java.lang.Object[])
     */
    @Override
    public boolean callProcedure(ProcedureCallback callback, String procName, Object... parameters) throws IOException,
            NoConnectionsException {
        return this.inner.callProcedure(new ThrottleCallback(procName, callback), procName, parameters);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#callProcedure(org.voltdb.client.ProcedureCallback, int, java.lang.String, java.lang.Object[])
     */
    @Override
    public boolean callProcedure(ProcedureCallback callback, int expectedSerializedSize, String procName,
            Object... parameters) throws IOException, NoConnectionsException {
        return this.inner.callProcedure(callback, expectedSerializedSize, procName, parameters);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#close()
     */
    @Override
    public void close() throws InterruptedException {
        this.inner.close();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#configureBlocking(boolean)
     */
    @Override
    public void configureBlocking(boolean blocking) {
        this.inner.configureBlocking(blocking);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#createConnection(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void createConnection(String host, int port, String username, String password) throws UnknownHostException,
            IOException {
        this.inner.createConnection(host, port, username, password);
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#drain()
     */
    @Override
    public void drain() throws NoConnectionsException {
        this.inner.drain();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getBuildString()
     */
    @Override
    public String getBuildString() {
        return this.inner.getBuildString();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getIOStats()
     */
    @Override
    public VoltTable getIOStats() {
        return this.inner.getIOStats();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getIOStatsInterval()
     */
    @Override
    public VoltTable getIOStatsInterval() {
        return this.inner.getIOStatsInterval();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getInstanceId()
     */
    @Override
    public Object[] getInstanceId() {
        return this.inner.getInstanceId();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getProcedureStats()
     */
    @Override
    public VoltTable getProcedureStats() {
        return this.inner.getProcedureStats();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#getProcedureStatsInterval()
     */
    @Override
    public VoltTable getProcedureStatsInterval() {
        return this.inner.getProcedureStatsInterval();
    }

    /* (non-Javadoc)
     * @see org.voltdb.client.Client#removeClientStatusListener(org.voltdb.client.ClientStatusListener)
     */
    @Override
    public boolean removeClientStatusListener(ClientStatusListener listener) {
        return this.inner.removeClientStatusListener(listener);
    }

}
