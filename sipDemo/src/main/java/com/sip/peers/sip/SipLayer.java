package com.sip.peers.sip;

import com.sip.peers.bo.SipRingRecord;
import com.sip.peers.util.InjectServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-13 12:56
 */
public class SipLayer implements SipListener, MessageProcessor {

    private static long sequenceNum = 1;

    private SipRingRecord ringRecord = new SipRingRecord();

    private MessageProcessor messageProcessor;

    private StringBuffer receivedMessages;

    private String username = "";

    private String temp = "";

    private SipStack sipStack;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SipLayer(String username, String ip, int port) throws PeerUnavailableException, InvalidArgumentException, TransportNotSupportedException, ObjectInUseException, TooManyListenersException {
        setUsername(username);

        sipFactory = SipFactory.getInstance();

        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();

        properties.setProperty("javax.sip.STACK_NAME", "TextClient");

        properties.setProperty("javax.sip.IP_ADDRESS", ip);

        sipStack = sipFactory.createSipStack(properties);

        headerFactory = sipFactory.createHeaderFactory();

        addressFactory = sipFactory.createAddressFactory();

        messageFactory = sipFactory.createMessageFactory();


        ListeningPoint tcp = sipStack.createListeningPoint(port, "tcp");

        ListeningPoint udp = sipStack.createListeningPoint(port, "udp");

        sipProvider = sipStack.createSipProvider(tcp);

        sipProvider.addSipListener(this);

        sipProvider = sipStack.createSipProvider(udp);

        sipProvider.addSipListener(this);
    }


    public void sendMessage(String to, String message) throws ParseException,
            InvalidArgumentException, SipException {
        SipURI from = addressFactory.createSipURI(getUsername(), getHost()
                + ":" + getPort());
        Address fromNameAddress = addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(getUsername());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                "textclientv1.0");

        String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
        String address = to.substring(to.indexOf("@") + 1);

        SipURI toAddress = addressFactory.createSipURI(username, address);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(username);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(username, address);
        requestURI.setTransportParam("udp");

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
                getPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        SipURI contactURI = addressFactory.createSipURI(getUsername(),
                getHost());
        contactURI.setPort(getPort());
        Address contactAddress = addressFactory.createAddress(contactURI);
        contactAddress.setDisplayName(getUsername());
        ContactHeader contactHeader = headerFactory
                .createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        ContentTypeHeader contentTypeHeader = headerFactory
                .createContentTypeHeader("text", "plain");
        request.setContent(message, contentTypeHeader);

        sipProvider.sendRequest(request);
    }

    public void callRing(String to, String message) throws ParseException, InvalidArgumentException, SipException {
        temp = to;
        SipURI from = addressFactory.createSipURI(getUsername(), getHost()
                + ":" + getPort());
        Address fromNameAddress = addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(getUsername());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "callclientv1.0");
//        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
//                "textclientv1.0");

        String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
        String address = to.substring(to.indexOf("@") + 1);

        SipURI toAddress = addressFactory.createSipURI(username, address);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(username);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(username, address);
        requestURI.setTransportParam("udp");

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
                getPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        logger.info("callIdHeader : " + callIdHeader.getCallId().toString());
        ringRecord.setCallId(callIdHeader.getCallId().toString());
        ringRecord.setRequestTime(new Date());
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, Request.INVITE);

        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestURI,
                Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        logger.info("requestInfo : " + request.toString());
        ringRecord.getRequestInfoList().add(request.toString());
        SipURI contactURI = addressFactory.createSipURI(getUsername(),
                getHost());
        contactURI.setPort(getPort());
        Address contactAddress = addressFactory.createAddress(contactURI);
        contactAddress.setDisplayName(getUsername());
        ContactHeader contactHeader = headerFactory
                .createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        ContentTypeHeader contentTypeHeader = headerFactory
                .createContentTypeHeader("text", "plain");
        request.setContent(message, contentTypeHeader);

        sipProvider.sendRequest(request);
    }


    public void ack(String to, String message) throws ParseException, InvalidArgumentException, SipException {
        to = temp;
        SipURI from = addressFactory.createSipURI(getUsername(), getHost()
                + ":" + getPort());
        Address fromNameAddress = addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(getUsername());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "callclientv1.0");

        String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
        String address = to.substring(to.indexOf("@") + 1);

        SipURI toAddress = addressFactory.createSipURI(username, address);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(username);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(username, address);
        requestURI.setTransportParam("udp");

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
                getPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        logger.info("callIdHeader : " + callIdHeader.getCallId().toString());
        ringRecord.setCallId(callIdHeader.getCallId().toString());
        ringRecord.setRequestTime(new Date());
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, Request.BYE);

        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestURI,
                Request.BYE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        logger.info("requestInfo : " + request.toString());
        ringRecord.getRequestInfoList().add(request.toString());
        SipURI contactURI = addressFactory.createSipURI(getUsername(),
                getHost());
        contactURI.setPort(getPort());
        Address contactAddress = addressFactory.createAddress(contactURI);
        contactAddress.setDisplayName(getUsername());
        ContactHeader contactHeader = headerFactory
                .createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        ContentTypeHeader contentTypeHeader = headerFactory
                .createContentTypeHeader("text", "plain");
        request.setContent(message, contentTypeHeader);

        sipProvider.sendRequest(request);
    }

    public int getPort() {
        int port = sipProvider.getListeningPoint().getPort();
        return port;
    }

    public String getHost() {
        int port = sipProvider.getListeningPoint().getPort();
        String host = sipStack.getIPAddress();
        return host;
    }



    @Override
    public void processRequest(RequestEvent evt) {
        Request req = evt.getRequest();

        String method = req.getMethod();
        FromHeader from = (FromHeader) req.getHeader("From");
        messageProcessor.processMessage(from.getAddress().toString(),
                new String(req.getRawContent()));
        Response response = null;
        try { //Reply with OK
            response = messageFactory.createResponse(200, req);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("888"); //This is mandatory as per the spec.
            ServerTransaction st = sipProvider.getNewServerTransaction(req);
            st.sendResponse(response);
        } catch (Throwable e) {
            e.printStackTrace();
            messageProcessor.processError("Can't send OK reply.");
        }
    }

    @Override
    public void processResponse(ResponseEvent evt) {
        Response response = evt.getResponse();
        int status = response.getStatusCode();

        if ((status >= 200) && (status < 300)) { //Success!
//            messageProcessor.processInfo("--Sent");
            logger.info("xxxx response is :" + response.toString());
            logger.info("xxxx responseStatusCode is :" + response.getStatusCode() +", and requestResult is Success");
            ringRecord.getResponseInfoList().add(response.toString());
            ringRecord.setResponseTime(new Date());
            return;
        }
        if (status == 486){     // busy
            try {
                this.ack(temp, "");
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
        logger.info("xxxx Sip Response Info :"  + response.toString());
        ringRecord.getResponseInfoList().add(response.toString());
        ringRecord.setResponseTime(new Date());
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        messageProcessor
                .processError("Previous message not sent: " + "timeout");
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        messageProcessor.processError("Previous message not sent: "
                + "I/O Exception");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("TransactionTerminated......事务终止");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("DialogTerminated......对话终止");
        InjectServiceUtil.getInstance().getSipRingService().saveSipRingRecord(ringRecord);
    }


    public MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public void setMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SipStack getSipStack() {
        return sipStack;
    }

    public void setSipStack(SipStack sipStack) {
        this.sipStack = sipStack;
    }

    public SipFactory getSipFactory() {
        return sipFactory;
    }

    public void setSipFactory(SipFactory sipFactory) {
        this.sipFactory = sipFactory;
    }

    public AddressFactory getAddressFactory() {
        return addressFactory;
    }

    public void setAddressFactory(AddressFactory addressFactory) {
        this.addressFactory = addressFactory;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public void setHeaderFactory(HeaderFactory headerFactory) {
        this.headerFactory = headerFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }

    public void setSipProvider(SipProvider sipProvider) {
        this.sipProvider = sipProvider;
    }

    public SipRingRecord getRingRecord() {
        return ringRecord;
    }

    public void setRingRecord(SipRingRecord ringRecord) {
        this.ringRecord = ringRecord;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void processMessage(String sender, String message)
    {
        this.receivedMessages.append("From " +
                sender + ": " + message + "\n");
    }

    public void processError(String errorMessage)
    {
        this.receivedMessages.append("ERROR: " +
                errorMessage + "\n");
    }

    public void processInfo(String infoMessage)
    {
        this.receivedMessages.append(
                infoMessage + "\n");
    }
}
