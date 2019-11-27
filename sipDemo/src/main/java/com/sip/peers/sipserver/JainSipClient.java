package com.sip.peers.sipserver;

import com.sip.peers.bo.RingParam;
import com.sip.peers.bo.RingResponse;
import com.sip.peers.bo.RingResult;
import com.sip.peers.bo.SipRingRecord;
import com.sip.peers.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.*;

public class JainSipClient {

    private SipStack sipStack;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    private Dialog dialog;

    String ip = "192.168.200.1";
    int port = 5061;
    String uname="Tom";

    public static void main(String[] args) {
        JainSipClient client = new JainSipClient();
        client.init();
        client.sendMessage("Tom","192.168.200.1:5061","Tom","192.168.200.1:5061","");
        client.sendRing("Tom","192.168.200.1:5061","some","192.168.200.1:64811","");
    }

    public void init(){
        try {
            Properties prop = new Properties();
            prop.setProperty("javax.sip.STACK_NAME", "teststackname");
            prop.setProperty("javax.sip.IP_ADDRESS", ip);
            prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sipclientdebug.txt");
            prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "sipclientlog.txt");

            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");

            sipStack = sipFactory.createSipStack(prop);

            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();

            ListeningPoint listeningpoint_udp =sipStack.createListeningPoint(port, "udp");
//			ListeningPoint listeningponit_tcp =sipStack.createListeningPoint(port, "tcp");

            sipProvider = sipStack.createSipProvider(listeningpoint_udp);
            ClientListener listener = new ClientListener(addressFactory,headerFactory,messageFactory,sipProvider);
            sipProvider.addSipListener(listener);
//			sipProvider = sipStack.createSipProvider(listeningponit_tcp);
//			sipProvider.addSipListener(listener);
            System.out.println("client init finished.");
        } catch (PeerUnavailableException | TransportNotSupportedException | ObjectInUseException
                | InvalidArgumentException | TooManyListenersException e) {
            e.printStackTrace();
        }

    }
    public void sendMessage(String fromUserName , String fromIpPort , String toUserName,String toIpPort,String message){
        try {
            //requestURI
            SipURI requestSipURI = addressFactory.createSipURI("gov.nist","192.168.200.1:5060");
            requestSipURI.setTransportParam("udp");
            //from
            SipURI fromSipURI = addressFactory.createSipURI(fromUserName, fromIpPort);
            Address fromAddress = addressFactory.createAddress(fromSipURI);
            fromAddress.setDisplayName("Tom");
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress,"mytag");
            //to
            SipURI toSipURI = addressFactory.createSipURI(toUserName, toIpPort);
            Address toAddress = addressFactory.createAddress(toSipURI);
            toAddress.setDisplayName("some");
            ToHeader toHeader = headerFactory.createToHeader(toAddress,null);
            //via
            ViaHeader viaHeader = headerFactory.createViaHeader(ip, port, "udp", "branchingbranching");
            List<ViaHeader> viaHeaderList = new ArrayList<>();
            viaHeaderList.add(viaHeader);
            //callid,cseq,maxforwards
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER);
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
            //
            Request request = messageFactory.createRequest(requestSipURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaderList, maxForwardsHeader);
            //contact
            SipURI contactURI = addressFactory.createSipURI(fromUserName, fromIpPort);
            contactURI.setPort(port);
            Address  contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(uname);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);
            //expires
            ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
            request.addHeader(expiresHeader);

//			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text","plain");
//			request.setContent(message,contentTypeHeader);

            System.out.println(request);
            sipProvider.sendRequest(request);
//			//
//			ClientTransaction trans = sipProvider.getNewClientTransaction(request);
//			dialog = trans.getDialog();
//			trans.sendRequest();
//			//
//			request = dialog.createRequest(Request.MESSAGE);
//			request.setHeader(contactHeader);
//			request.setContent(message, contentTypeHeader);
//			ClientTransaction ctrans = sipProvider.getNewClientTransaction(request);
//			ctrans.sendRequest();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }


    public void sendRing(String fromUserName,String fromIpPort,String toUserName,String toIpPort,String message){
        try {
            //requestURI
            SipURI requestSipURI = addressFactory.createSipURI("gov.nist","192.168.200.1:5060");
            requestSipURI.setTransportParam("udp");
            //from
            SipURI fromSipURI = addressFactory.createSipURI(fromUserName, fromIpPort);
            Address fromAddress = addressFactory.createAddress(fromSipURI);
            fromAddress.setDisplayName("a");
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress,"mytag");
            //to
            SipURI toSipURI = addressFactory.createSipURI(toUserName, toIpPort);
            Address toAddress = addressFactory.createAddress(toSipURI);
            toAddress.setDisplayName("b");
            ToHeader toHeader = headerFactory.createToHeader(toAddress,null);
            //via
            ViaHeader viaHeader = headerFactory.createViaHeader(ip, port, "udp", "branchingbranching");
            List<ViaHeader> viaHeaderList = new ArrayList<>();
            viaHeaderList.add(viaHeader);
            //callid,cseq,maxforwards
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
            //
            Request request = messageFactory.createRequest(requestSipURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaderList, maxForwardsHeader);
            //contact
            SipURI contactURI = addressFactory.createSipURI(fromUserName, fromIpPort);
            contactURI.setPort(port);
            Address  contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(uname);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);
            //expires
            ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
            request.addHeader(expiresHeader);

//			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text","plain");
//			request.setContent(message,contentTypeHeader);

            System.out.println(request);
            sipProvider.sendRequest(request);
//			//
//			ClientTransaction trans = sipProvider.getNewClientTransaction(request);
//			dialog = trans.getDialog();
//			trans.sendRequest();
//			//
//			request = dialog.createRequest(Request.MESSAGE);
//			request.setHeader(contactHeader);
//			request.setContent(message, contentTypeHeader);
//			ClientTransaction ctrans = sipProvider.getNewClientTransaction(request);
//			ctrans.sendRequest();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

}



//class ClientListener implements SipListener{
//    private AddressFactory addressFactory;
//
//    private HeaderFactory headerFactory;
//
//    private MessageFactory messageFactory;
//
//    private SipProvider sipProvider;
//
//    public ClientListener(AddressFactory addressFactory, HeaderFactory headerFactory, MessageFactory messageFactory,
//                          SipProvider sipProvider) {
//        super();
//        this.addressFactory = addressFactory;
//        this.headerFactory = headerFactory;
//        this.messageFactory = messageFactory;
//        this.sipProvider = sipProvider;
//    }
//
//    @Override
//    public void processRequest(RequestEvent requestEvent) {
//        System.out.println("processRequest执行");
//        Request request = requestEvent.getRequest();
//        if(null == request) {
//            System.out.println("requestEvent.getRequest() is null.");
//            return ;
//        }
//
//        System.out.println("request内容是\n"+request);
//    }
//
//    @Override
//    public void processResponse(ResponseEvent responseEvent) {
//        System.out.println("processResponse执行");
//        Response response = responseEvent.getResponse();
//        if(null == response) {
//            System.out.println("response is null.");
//            return ;
//        }
//        System.out.println("返回码:"+response.getStatusCode());
//        System.out.println("Response is :"+response);
//        WWWAuthenticateHeader wwwHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
//        if(null != wwwHeader) {
//            String realm = wwwHeader.getRealm();
//            String nonce = wwwHeader.getNonce();
//            String A1 = MD5Util.MD5("Tom:"+realm+":12345678");
//            String A2 = MD5Util.MD5("REGISTER:sip:servername@192.168.200.1:5060");
//            String resStr = MD5Util.MD5(A1+":"+nonce+":"+A2);
//
//            try {
//                //requestURI
//                SipURI requestSipURI = addressFactory.createSipURI("gov.nist","192.168.200.1:5060");
//                requestSipURI.setTransportParam("udp");
//                //from
//                SipURI fromSipURI = addressFactory.createSipURI("Tom", "192.168.200.1:5061");
//                Address fromAddress = addressFactory.createAddress(fromSipURI);
//                fromAddress.setDisplayName("a");
//                FromHeader fromHeader = headerFactory.createFromHeader(fromAddress,"mytag2");
//                //to
//                SipURI toSipURI = addressFactory.createSipURI("Tom", "192.168.200.1:5061");
//                Address toAddress = addressFactory.createAddress(toSipURI);
//                toAddress.setDisplayName("b");
//                ToHeader toHeader = headerFactory.createToHeader(toAddress,null);
//                //via
//                ViaHeader viaHeader = headerFactory.createViaHeader("192.168.200.1", 5061, "udp", "branchingbranching");
//                List<ViaHeader> viaHeaderList = new ArrayList<>();
//                viaHeaderList.add(viaHeader);
//                //callid,cseq,maxforwards
//                CallIdHeader callIdHeader = sipProvider.getNewCallId();
//                CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(2L, Request.REGISTER);
//                MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
//                //
//                Request request = messageFactory.createRequest(requestSipURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaderList, maxForwardsHeader);
//                //contant
//                SipURI contantURI = addressFactory.createSipURI("Tom", "192.168.200.1:5061");
//                contantURI.setPort(5061);
//                Address  contantAddress = addressFactory.createAddress(contantURI);
//                contantAddress.setDisplayName("abc");
//                ContactHeader contactHeader = headerFactory.createContactHeader(contantAddress);
//                request.addHeader(contactHeader);
//                //expires
//                ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
//                request.addHeader(expiresHeader);
//
//                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text","plain");
//                request.setContent("",contentTypeHeader);
//
//                AuthorizationHeader aHeader = headerFactory.createAuthorizationHeader("Digest");
//                aHeader.setUsername("Tom");
//                aHeader.setRealm(realm);
//                aHeader.setNonce(nonce);
//                aHeader.setURI(fromSipURI);
//                aHeader.setResponse(resStr);
//                aHeader.setAlgorithm("MD5");
//                request.addHeader(aHeader);
//
//                System.out.println(request);
//                sipProvider.sendRequest(request);
//            } catch (ParseException | InvalidArgumentException | SipException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    @Override
//    public void processTimeout(TimeoutEvent timeoutEvent) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void processIOException(IOExceptionEvent exceptionEvent) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
//        System.out.println("processTransactionTerminated执行");
//    }
//
//    @Override
//    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
//        System.out.println("processDialogTerminated执行");
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//}
