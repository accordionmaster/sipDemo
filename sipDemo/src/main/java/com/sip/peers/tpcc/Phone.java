package com.sip.peers.tpcc;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-18 14:13
 */
public class Phone implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    SipProvider sipProvider;

    private static final String myAddress = "127.0.0.1";

    private static int myPort;

    protected ServerTransaction inviteTid;

    private Response okResponse;

    private Request inviteRequest;

    private Dialog dialog;

    private String transport = "udp";

    class MyTimerTask extends TimerTask{
        Phone shootmeA;
        boolean byebye;

        public MyTimerTask(Phone shootmeA, boolean byebye) {
            this.shootmeA = shootmeA;
            this.byebye = byebye;
        }

        @Override
        public void run() {
            if (byebye){
                shootmeA.sendBye(); // create a bye
            } else {
                shootmeA.sendInviteOk();
            }
        }
    }

    protected static final String usageString =  "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);
    }

    public void processRequest(RequestEvent requestEvent){
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)){
            processBye(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)){
            processCancel(requestEvent, serverTransactionId);
        }
    }



    /**
     * Process the ACK request. Send the bye and complete the call flow.
     * @param requestEvent
     * @param serverTransactionId
     */
    private void processAck(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        System.out.println("shootmeA: got an ACK! ");
        System.out.println("Dialog State = " + dialog.getState());
        new Timer().schedule(new MyTimerTask(this,true), 4000);
    }

    /**
     * Process the invite request
     * @param requestEvent
     * @param serverTransactionId
     */
    private void processInvite(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootmeA: got an Invite sending Trying");
            System.out.println("shootmeA: " + request);
            Response response = messageFactory.createResponse(Response.TRYING, request);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            st.sendResponse(response);

            this.okResponse = messageFactory.createResponse(Response.OK, request);

            Address address = addressFactory.createAddress("ShootmeA <sip:"
                    + myAddress + ":" + myPort + ";lr" + ">");
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            if (toHeader.getTag() == null){
                toHeader.setTag(new Integer((int)(Math.random() * 10000)).toString()); // Application is supposed to set.
            } else {
                System.out.println("Re-INVITE processing");
            }
            okResponse.addHeader(contactHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application","sdp");
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();
            okResponse.setContent(contents, contentTypeHeader);

            this.inviteTid = st;
            this.inviteRequest = request;

            new Timer().schedule(new MyTimerTask(this,false), 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void sendBye() {
        try {
            dialog = this.inviteTid.getDialog();
            if (dialog.getState() == DialogState.TERMINATED) {
                System.out.println("Dialog already terminated!");
                return;
            }
            System.out.println("Sending BYE");
            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
            dialog.sendRequest(ct);
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    private void sendInviteOk(){
        try {
            if (inviteTid.getState() != TransactionState.COMPLETED){
                System.out.println("shootmeA: Dialog state before 200: "
                        + inviteTid.getDialog().getState());
                inviteTid.sendResponse(okResponse);
                System.out.println(myPort + " Dialog state after 200: "
                        + inviteTid.getDialog().getState());
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the bye request
     * @param requestEvent
     * @param serverTransactionId
     */
    private void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println(myPort + " got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void processCancel(RequestEvent requestEvent,
                              ServerTransaction serverTransactionId){
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootmeA: got a cancle.");
            if (serverTransactionId == null){
                System.out.println("shootmeA: null tid.");
                return;
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED){
                response = messageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteRequest);
                inviteTid.sendResponse(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    @Override
    public void processResponse(ResponseEvent responseEvent) {

    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()){
            transaction = timeoutEvent.getServerTransaction();
        }else {
            transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = " + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
    }

    public void inti(){
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "TRACE");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "phone" + myPort
                + "debuglog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "phone"
                + myPort + "log.txt");
        properties.setProperty("javax.sip.STACK_NAME", "TextClient");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("sipStack = " + sipStack);
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getCause() != null){
                e.getCause().printStackTrace();
            }
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp = sipStack.createListeningPoint("192.168.200.1",myPort, transport);

            Phone listener = this;

            sipProvider = sipStack.createSipProvider(lp);
            System.out.println("provider " + sipProvider);
            sipProvider.addSipListener(listener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            usage();
        }
    }

    public static void main(String[] args) {
        myPort = 8082;
        new Phone().inti();
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog terminated event recieved");
    }
}
