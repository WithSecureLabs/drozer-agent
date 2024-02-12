package com.WithSecure.jsolar.api.connectors;

import com.WithSecure.jsolar.api.DeviceInfo;
import com.WithSecure.jsolar.api.Protobuf.Message;
import com.WithSecure.jsolar.api.builders.MessageFactory;
import com.WithSecure.jsolar.api.builders.SystemRequestFactory;
import com.WithSecure.jsolar.api.handlers.MessageHandler;
import com.WithSecure.jsolar.api.handlers.SystemMessageHandler;
import com.WithSecure.jsolar.api.links.Link;
import com.WithSecure.jsolar.api.sessions.Session;
import com.WithSecure.jsolar.api.transport.SecureTransport;
import com.WithSecure.jsolar.api.transport.Transport;
import com.WithSecure.jsolar.connection.AbstractConnection;
import com.WithSecure.jsolar.connection.AbstractLink;
import com.WithSecure.jsolar.connection.SecureConnection;
import com.WithSecure.jsolar.logger.LogMessage;

public class Connection extends AbstractConnection implements SecureConnection {

    private MessageHandler system_message_handler;

    public Connection(AbstractLink connector, DeviceInfo device_info, Transport transport) {
        super(connector, device_info, transport);

        this.system_message_handler = new SystemMessageHandler(this, device_info);
    }

    @Override
    /**
     * Attempt to handshake with a Server to bind this device, sharing the device id, manufacturer,
     * model and software version.
     *
     * Note: this is only used if we are operating as a Client (see {@link #mustBind()}).
     */
    protected boolean bindToServer(DeviceInfo device) {
        if(this.mustBind()) {
            this.log(LogMessage.DEBUG, "Sending BIND_DEVICE to drozer server...");

            this.send(new MessageFactory(SystemRequestFactory.bind().setDevice(
                    device.getAndroid_id(),
                    device.getManufacturer(),
                    device.getModel(),
                    device.getSoftware())).setId(1).build());

            Message message = this.receive();

            if(message != null &&
                    message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
                    message.hasSystemResponse() &&
                    message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
                    message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.BOUND) {
                this.getConnector().setStatus(Connector.Status.ONLINE);

                return true;
            }

            return false;
        }
        else
            return true;
    }

    @Override
    protected Link getConnector() {
        return (Link)super.getConnector();
    }

    @Override
    /**
     * Calculates the fingerprint of the host's SSL Certificate.
     */
    public String getHostCertificateFingerprint() {
        return ((SecureTransport)this.getTransport()).getHostCertificateFingerprint();
    }

    @Override
    /**
     * Calculates the fingerprint of the peer's SSL Certificate.
     */
    public String getPeerCertificateFingerprint() {
        return ((SecureTransport)this.getTransport()).getPeerCertificateFingerprint();
    }

    @Override
    protected Message handleReflectionRequest(Message message) {
        Session session = this.getConnector().getSession(message.getReflectionRequest().getSessionId());

        if(session != null)
            session.deliverMessage(message);

        return null;
    }

    @Override
    protected Message handleReflectionResponse(Message message) {
        return null;
    }

    @Override
    protected Message handleSystemRequest(Message message) {
        return this.system_message_handler.handle(message);
    }

    @Override
    protected Message handleSystemResponse(Message message) {
        return null;
    }

    /**
     * Send a log message, with a custom log level.
     */
    public void log(int level, String message) {
        this.getConnector().log(level, message);
    }

    @Override
    /**
     * Attempt to disconnect from the server, indicating that our device id is not longer
     * available.
     */
    protected void unbindFromServer(DeviceInfo device) {
        if(this.mustBind()) {
            this.log(LogMessage.DEBUG, "Sending UNBIND_DEVICE to drozer server...");

            this.send(new MessageFactory(SystemRequestFactory.unbind().setDevice(
                    device.getAndroid_id(),
                    device.getManufacturer(),
                    device.getModel(),
                    device.getSoftware())).setId(1).build());

            Message message = this.receive();

            if(message != null &&
                    message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
                    message.hasSystemResponse() &&
                    message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
                    message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.UNBOUND) {
                this.getConnector().setStatus(Connector.Status.OFFLINE);
            }
        }
    }

}
