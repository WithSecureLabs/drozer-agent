package com.WithSecure.jsolar.api.links;

import com.WithSecure.jsolar.api.DeviceInfo;
import com.WithSecure.jsolar.api.connectors.Connector;
import com.WithSecure.jsolar.api.connectors.Endpoint;
import com.WithSecure.jsolar.api.connectors.EndpointSocketFactory;
import com.WithSecure.jsolar.api.transport.SocketTransport;
import com.WithSecure.jsolar.connection.SecureConnection;
import com.WithSecure.jsolar.logger.LogMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;

public class Client extends Link{

    public static final int RESET_TIMEOUT = 5000;

    public Client(Endpoint endpoint, DeviceInfo deviceInfo) {
        super(endpoint, deviceInfo);
    }

    public String getHostCertificateFingerprint() {
        return ((SecureConnection) this.connection).getHostCertificateFingerprint();
    }

    public String getPeerCertificateFingerprint() {
        return ((SecureConnection) this.connection).getPeerCertificateFingerprint();
    }

    @Override
    public void resetConnection() {
        this.parameters.setStatus(Endpoint.Status.CONNECTING);

        try {
            Thread.sleep(RESET_TIMEOUT);
        } catch (InterruptedException e) {
            this.log(LogMessage.DEBUG, "Issue resetting client connection");
        }

        super.resetConnection();
    }

    @Override
    public void run() {
        Endpoint endpoint = (Endpoint) this.parameters;

        this.log(LogMessage.INFO, "Starting...");
        this.running = true;

        while (this.running) {
            try {
                if (this.connection == null) {
                    this.parameters.setStatus(Endpoint.Status.CONNECTING);

                    this.log(LogMessage.INFO, "Attempting connection to " + endpoint.toConnectionString() + "...");
                    Socket socket = new EndpointSocketFactory().createSocket(endpoint);

                    if (socket != null) {
                        this.log(LogMessage.INFO, "Socket connected.");

                        this.log(LogMessage.INFO, "Attempting to start drozer thread...");
                        this.createConnection(new SocketTransport(socket));
                    }
                } else {
                    synchronized (this.connection) {
                        try {
                            this.connection.wait();
                        } catch (InterruptedException e) {
                            this.log(LogMessage.DEBUG, "Issue waiting on thread in client : " + e.getMessage());
                        }  catch (IllegalMonitorStateException e) {
                            this.log(LogMessage.DEBUG, "Issue waiting on thread in client : " + e.getMessage());
                        }
                    }

                    if (this.connection.started && !this.connection.running) {
                        this.log(LogMessage.INFO, "Connection was reset.");

                        this.resetConnection();
                    }
                }
            } catch (UnknownHostException e) {
                this.log(LogMessage.ERROR, "Unknown Host: " + endpoint.getHost());

                this.stopConnector();
            }
            catch(IOException e) {
                this.log(LogMessage.ERROR, "IO Error. Resetting connection.");
                this.log(LogMessage.DEBUG, e.getMessage());

                this.resetConnection();
            }
            catch(KeyManagementException e) {
                this.log(LogMessage.ERROR, "Error loading key material for SSL.");

                this.stopConnector();
            }
        }
        this.log(LogMessage.INFO, "Stopped.");
    }

    @Override
    public void setStatus(Connector.Status status) {
        this.parameters.setStatus(status);
    }
}
