/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.sshd.agent.SshAgentFactory;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.channel.RequestHandler;
import org.apache.sshd.common.cipher.Cipher;
import org.apache.sshd.common.compression.Compression;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.forward.TcpipForwarderFactory;
import org.apache.sshd.common.io.IoServiceFactory;
import org.apache.sshd.common.kex.KeyExchange;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.mac.Mac;
import org.apache.sshd.common.random.Random;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.server.forward.ForwardingFilter;

/**
 * This interface allows retrieving all the <code>NamedFactory</code> used
 * in the SSH protocol.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public interface FactoryManager {

    /**
     * Key used to retrieve the value of the window size in the
     * configuration properties map.
     */
    String WINDOW_SIZE = "window-size";

    /**
     * Key used to retrieve timeout (msec.) to wait for data to
     * become available when reading from a channel. If not set
     * or non-positive then infinite value is assumed
     */
    String WINDOW_TIMEOUT = "window-timeout";


    /**
     * Key used to retrieve the value of the maximum packet size
     * in the configuration properties map.
     */
    String MAX_PACKET_SIZE = "packet-size";

    /**
     * Number of NIO worker threads to use.
     */
    String NIO_WORKERS = "nio-workers";

    /**
     * Default number of worker threads to use.
     */
    int DEFAULT_NIO_WORKERS = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * Key used to retrieve the value of the timeout after which
     * it will close the connection if the other side has not been
     * authenticated.
     */
    String AUTH_TIMEOUT = "auth-timeout";

    /**
     * Key used to retrieve the value of idle timeout after which
     * it will close the connection.  In milliseconds.
     */
    String IDLE_TIMEOUT = "idle-timeout";

    /**
     * Key used to retrieve the value of the disconnect timeout which
     * is used when a disconnection is attempted.  If the disconnect
     * message has not been sent before the timeout, the underlying socket
     * will be forcibly closed.
     */
    String DISCONNECT_TIMEOUT = "disconnect-timeout";

    /**
     * Key used to configure the timeout used when writing a close request
     * on a channel. If the message can not be written before the specified
     * timeout elapses, the channel will be immediately closed. In milliseconds.
     */
    String CHANNEL_CLOSE_TIMEOUT = "channel-close-timeout";

    /**
     * Socket backlog.
     * See {@link java.nio.channels.AsynchronousServerSocketChannel#bind(java.net.SocketAddress, int)}
     */
    String SOCKET_BACKLOG = "socket-backlog";

    /**
     * Socket keep-alive.
     * See {@link java.net.StandardSocketOptions#SO_KEEPALIVE}
     */
    String SOCKET_KEEPALIVE = "socket-keepalive";

    /**
     * Socket send buffer size.
     * See {@link java.net.StandardSocketOptions#SO_SNDBUF}
     */
    String SOCKET_SNDBUF = "socket-sndbuf";

    /**
     * Socket receive buffer size.
     * See {@link java.net.StandardSocketOptions#SO_RCVBUF}
     */
    String SOCKET_RCVBUF = "socket-rcvbuf";

    /**
     * Socket reuse address.
     * See {@link java.net.StandardSocketOptions#SO_REUSEADDR}
     */
    String SOCKET_REUSEADDR = "socket-reuseaddr";

    /**
     * Socket linger.
     * See {@link java.net.StandardSocketOptions#SO_LINGER}
     */
    String SOCKET_LINGER = "socket-linger";

    /**
     * Socket tcp no-delay.
     * See {@link java.net.StandardSocketOptions#TCP_NODELAY}
     */
    String TCP_NODELAY = "tcp-nodelay";

    /**
     * Read buffer size for NIO2 sessions
     * See {@link org.apache.sshd.common.io.nio2.Nio2Session}
     */
    String NIO2_READ_BUFFER_SIZE = "nio2-read-buf-size";

    /**
     * The default reported version of {@link #getVersion()} if the built-in
     * version information cannot be accessed
     */
    String DEFAULT_VERSION = "SSHD-UNKNOWN";

    /**
     * <P>A map of properties that can be used to configure the SSH server
     * or client.  This map will never be changed by either the server or
     * client and is not supposed to be changed at runtime (changes are not
     * bound to have any effect on a running client or server), though it may
     * affect the creation of sessions later as these values are usually not
     * cached.</P>
     *
     * <P><B>Note:</B> the <U>type</U> of the mapped property should match the
     * expected configuration value type - {@code Long, Integer, Boolean,
     * String}, etc.... If it doesn't, the {@code toString()} result of the
     * mapped value is used to convert it to the required type. E.g., if
     * the mapped value is the <U>string</U> &quot;1234&quot; and the expected
     * value is a {@code long} then it will be parsed into one. Also, if
     * the mapped value is an {@code Integer} but a {@code long} is expected,
     * then it will be converted into one.</P>
     *
     * @return a valid <code>Map</code> containing configuration values, never {@code null}
     */
    Map<String, Object> getProperties();

    /**
     * An upper case string identifying the version of the software used on
     * client or server side. This version includes the name of the software
     * and usually looks like this: <code>SSHD-1.0</code>
     *
     * @return the version of the software
     */
    String getVersion();

    IoServiceFactory getIoServiceFactory();

    /**
     * Retrieve the list of named factories for <code>KeyExchange</code>.
     *
     * @return a list of named <code>KeyExchange</code> factories, never {@code null}
     */
    List<NamedFactory<KeyExchange>> getKeyExchangeFactories();

    /**
     * Retrieve the list of named factories for <code>Cipher</code>.
     *
     * @return a list of named <code>Cipher</code> factories, never {@code null}
     */
    List<NamedFactory<Cipher>> getCipherFactories();

    /**
     * Retrieve the list of named factories for <code>Compression</code>.
     *
     * @return a list of named <code>Compression</code> factories, never {@code null}
     */
    List<NamedFactory<Compression>> getCompressionFactories();

    /**
     * Retrieve the list of named factories for <code>Mac</code>.
     *
     * @return a list of named <code>Mac</code> factories, never {@code null}
     */
    List<NamedFactory<Mac>> getMacFactories();

    /**
     * Retrieve the list of named factories for <code>Signature</code>.
     *
     * @return a list of named <code>Signature</code> factories, never {@code null}
     */
    List<NamedFactory<Signature>> getSignatureFactories();

    /**
     * Retrieve the <code>KeyPairProvider</code> that will be used to find
     * the host key to use on the server side or the user key on the client side.
     *
     * @return the <code>KeyPairProvider</code>, never {@code null}
     */
    KeyPairProvider getKeyPairProvider();

    /**
     * Retrieve the <code>Random</code> factory to be used.
     *
     * @return the <code>Random</code> factory, never {@code null}
     */
    Factory<Random> getRandomFactory();

    /**
     * Retrieve the list of named factories for <code>Channel</code> objects.
     *
     * @return a list of named <code>Channel</code> factories, never {@code null}
     */
    List<NamedFactory<Channel>> getChannelFactories();

    /**
     * Retrieve the agent factory for creating <code>SshAgent</code> objects.
     *
     * @return the factory
     */
    SshAgentFactory getAgentFactory();

    /**
     * Retrieve the <code>ScheduledExecutorService</code> to be used.
     *
     * @return the <code>ScheduledExecutorService</code>, never {@code null}
     */
    ScheduledExecutorService getScheduledExecutorService();

    /**
     * Retrieve the <code>ForwardingFilter</code> to be used by the SSH server.
     * If no filter has been configured (i.e. this method returns
     * {@code null}), then all forwarding requests will be rejected.
     *
     * @return the <code>ForwardingFilter</code> or {@code null}
     */
    ForwardingFilter getTcpipForwardingFilter();

    /**
     * Retrieve the tcpip forwarder factory used to support tcpip forwarding.
     *
     * @return the <code>TcpipForwarderFactory</code>
     */
    TcpipForwarderFactory getTcpipForwarderFactory();

    /**
     * Retrieve the <code>FileSystemFactory</code> to be used to traverse the file system.
     *
     * @return a valid <code>FileSystemFactory</code> object or {@code null} if file based
     * interactions are not supported on this server
     */
    FileSystemFactory getFileSystemFactory();

    /**
     * Retrieve the list of SSH <code>Service</code> factories.
     *
     * @return a list of named <code>Service</code> factories, never {@code null}
     */
    List<ServiceFactory> getServiceFactories();

    /**
     * Retrieve the list of global request handlers.
     *
     * @return a list of named <code>GlobalRequestHandler</code>
     */
    List<RequestHandler<ConnectionService>> getGlobalRequestHandlers();

}
