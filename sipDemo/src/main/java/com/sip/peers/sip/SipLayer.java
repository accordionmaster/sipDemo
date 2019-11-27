package com.sip.peers.sip;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.gson.Gson;
import com.sip.peers.bo.*;
import com.sip.peers.util.HttpUtil;
import com.sip.peers.util.InjectServiceUtil;
import com.sip.peers.util.MD5Util;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.Expires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.jfunc.json.Json;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.*;

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

    private Dialog dialog;

    private Response okResponse;

    private Request inviteRequest;

    protected ClientTransaction inviteTid;

    private RingResult ringResult = new RingResult();

    private RingResponse ringResponse = new RingResponse();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    String transport = "udp";

    private boolean isTimeout = false;

    private  ClientTransaction clientTransaction = null;

    /**
     * 保存正在注册的用户，注册第一步的
     */
    private static Set<String> registingId = new HashSet<>();

    /**
     * 保存当前注册的用户，注册成功的
     */
    private static Hashtable<String, URI> registedContactURI = new Hashtable<>();

    class MyTimerTask extends TimerTask {
        SipLayer sipLayer;
        boolean byebye;

        public MyTimerTask(SipLayer sipLayer, boolean flag) {
            this.sipLayer = sipLayer;
            this.byebye = flag;

        }

        public void run() {
            if (byebye) {
                sipLayer.sendBye();// create a bye

            } else {
                sipLayer.sendInviteOK();
            }
        }
    }

    private void sendBye() {
        try {
            System.out.println("Sending BYE");
            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction ct = sipProvider
                    .getNewClientTransaction(byeRequest);
            dialog.sendRequest(ct);
        } catch (SipException ex) {
            ex.printStackTrace();
        }
    }

    private void sendAck() {
        try {
            System.out.println("Sending ACK");
            Request ackRequest = dialog.createRequest(Request.ACK);
            ClientTransaction ct = sipProvider
                    .getNewClientTransaction(ackRequest);
            dialog.sendRequest(ct);
        } catch (SipException ex) {
            ex.printStackTrace();
        }
    }

    private void sendInviteOK() {
        try {
            System.out.println("shootmeA: Dialog state before 200: "
                    + inviteTid.getDialog().getState());
            inviteTid.sendRequest();
            System.out.println(getPort() + " Dialog state after 200: "
                    + inviteTid.getDialog().getState());
        } catch (SipException ex) {
            ex.printStackTrace();
        }
    }
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

        ListeningPoint udp = sipStack.createListeningPoint(ip, port, "udp");

        // 目前存在的问题: 不能够重复发起请求
        sipProvider = sipStack.createSipProvider(udp);

        sipProvider.addSipListener(this);
    }

    public void callRing(String callId, RingParam ringParam) throws ParseException, InvalidArgumentException, SipException {
        ringResult.setCallId(callId);
        ringResult.setTimeStamp(new Date());
        ringResponse.setCallId(ringParam.getCalled());
        ringResponse.setAppId(ringParam.getAppId());
        String called = ringParam.getCalled();
        temp = called;
        SipURI from = addressFactory.createSipURI(getUsername(), getHost()
                + ":" + getPort());
        Address fromNameAddress = addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(getUsername());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "callclientv1.0");
//        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
//                "textclientv1.0");

        String username = called.substring(called.indexOf(":") + 1, called.indexOf("@"));
        String address = called.substring(called.indexOf("@") + 1);

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
        callIdHeader.setCallId(callId);
        logger.info("callId : " + callIdHeader.getCallId().toString());
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
        request.setContent("", contentTypeHeader);
        inviteTid = sipProvider.getNewClientTransaction(request);
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
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();
        if(null == request) {
            System.out.println("收到的requestEvent.getRequest() is null.");
            return ;
        }

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);


        System.out.println(">>>>>收到的request内容是\n"+request);

        switch(request.getMethod().toUpperCase()){
            case Request.INVITE:
                System.out.println("收到INVITE的请求");
                processInvite(requestEvent, serverTransactionId);
                break;
            case Request.REGISTER:
                System.out.println("收到REGISTER的请求");
                doRegister(request,requestEvent);
                break;
            case Request.SUBSCRIBE:
                System.out.println("收到SUBSCRIBE的请求");
                break;
            case Request.ACK:
                System.out.println("收到ACK的请求");
                break;
            case Request.BYE:
                System.out.println("收到BYE的请求");
                break;
            case Request.CANCEL:
                System.out.println("收到CANCEL的请求");
                break;
            default:
                System.out.println("不处理的requestMethod："+request.getMethod().toUpperCase());
        }

//        if (request.getMethod().equals(Request.INVITE)) {
//            processInvite(requestEvent, serverTransactionId);
//        } else if (request.getMethod().equals(Request.ACK)) {
//            processAck(requestEvent, serverTransactionId);
//        } else if (request.getMethod().equals(Request.BYE)) {
//            processBye(requestEvent, serverTransactionId);
//        } else if (request.getMethod().equals(Request.CANCEL)) {
//            processCancel(requestEvent, serverTransactionId);
//        }
    }

    private void doRegister(Request request, RequestEvent requestEvent) {
        if(null == request || null == requestEvent) {
            System.out.println("无法处理REGISTER请求，request="+request+",event="+requestEvent);
            return ;
        }
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
        try {
            Response response = null;

            ToHeader toHead = (ToHeader) request.getHeader(ToHeader.NAME);
            Address toAddress = toHead.getAddress();
            URI toURI = toAddress.getURI();
            SipURI sipURI_to = (SipURI) toURI;
            String toUserId = sipURI_to.getUser();
            System.out.println("注册的toUserId是"+toUserId);

            ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
            Address contactAddr = contactHeader.getAddress();
            URI contactURI = contactAddr.getURI();

            System.out.println("注册 from: " + toURI + " request str: " + contactURI);
            if(null == toUserId || "".equals(toUserId)) {
                System.out.println("无法识别的userId，不处理。");
                return ;
            }
            int expires = request.getExpires().getExpires();
            // 如果expires不等于0,则为注册，否则为注销。
            if (expires != 0 || contactHeader.getExpires() != 0) {//注册
                if(registedContactURI.containsKey(toUserId)) {//已经注册了
                    System.out.println("已经注册过了 user=" + toURI);
                }else {//不是注册成功状态
                    if(registingId.contains(toUserId)) {//是第二次注册
                        System.out.println("第二次注册 register user=" + toURI);
                        // 验证AuthorizationHeader摘要认证信息
                        AuthorizationHeader authorizationHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
                        boolean authorizationResult = false;
                        if(null != authorizationHeader) {//验证
                            String username = authorizationHeader.getUsername();
                            String realm = authorizationHeader.getRealm();
                            String nonce = authorizationHeader.getNonce();
                            URI uri = authorizationHeader.getURI();
                            String res = authorizationHeader.getResponse();
                            String algorithm = authorizationHeader.getAlgorithm();
                            System.out.println("Authorization信息：username="+username+",realm="+realm+",nonce="+nonce+",uri="+uri+",response="+res+",algorithm="+algorithm);
                            if(null==username || null==realm || null==nonce || null==uri || null==res || null==algorithm) {
                                System.out.println("Authorization信息不全，无法认证。");
                            }else {
                                // 比较Authorization信息正确性
                                String A1 = MD5Util.MD5(username+":"+realm+":12345678");
                                String A2 = MD5Util.MD5("REGISTER:sip:servername@192.168.200.1:5060");
                                String resStr = MD5Util.MD5(A1+":"+nonce+":"+A2);
                                if(resStr.equals(res)) {
                                    //注册成功，标记true
                                    authorizationResult = true;
                                }
                            }
                        }
                        registingId.remove(toUserId);//不管第二次是否成功都移除，失败要从头再来
                        // 验证成功加入成功注册列表，失败不加入
                        if(authorizationResult) {//注册成功
                            System.out.println("注册成功？");
                            registedContactURI.put(toUserId, contactURI);
                            //返回成功
                            response = messageFactory.createResponse(Response.OK, request);
                            DateHeader dateHeader = headerFactory.createDateHeader(Calendar.getInstance());
                            response.addHeader(dateHeader);
                            System.out.println("返回注册结果 response是\n" + response.toString());

                            if (serverTransactionId == null) {
                                serverTransactionId = sipProvider.getNewServerTransaction(request);
                                serverTransactionId.sendResponse(response);
                                // serverTransactionId.terminate();
//								System.out.println("register serverTransaction: " + serverTransactionId);
                            } else {
                                System.out.println("processRequest serverTransactionId is null.");
                            }
                        }else {//注册失败
                            System.out.println("注册失败？");
                            //返回失败
                            response = messageFactory.createResponse(Response.FORBIDDEN, request);
                            System.out.println("返回注册结果 response是\n" + response.toString());

                            if (serverTransactionId == null) {
                                serverTransactionId = sipProvider.getNewServerTransaction(request);
                                serverTransactionId.sendResponse(response);
                            } else {
                                System.out.println("processRequest serverTransactionId is null.");
                            }
                        }
                    }else {//是第一次注册
                        System.out.println("首次注册 user=" + toURI);
                        // 加入registing列表
                        registingId.add(toUserId);
                        //发送响应
                        response = messageFactory.createResponse(Response.UNAUTHORIZED, request);
                        String realm = "zectec";
                        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
                        String callId = callIdHeader.getCallId();
                        String nonce = MD5Util.MD5(callId+toUserId);
                        WWWAuthenticateHeader wwwAuthenticateHeader = headerFactory.createWWWAuthenticateHeader("Digest realm=\""+realm+"\",nonce=\""+nonce+"\"");
                        response.setHeader(wwwAuthenticateHeader);
                        System.out.println("返回注册结果 response是\n" + response.toString());

                        if (serverTransactionId == null) {
                            serverTransactionId = sipProvider.getNewServerTransaction(request);
                            serverTransactionId.sendResponse(response);
                            // serverTransactionId.terminate();
//							System.out.println("register serverTransaction: " + serverTransactionId);
                        } else {
                            System.out.println("processRequest serverTransactionId is null.");
                        }

                    }
                }
            } else {//注销
                System.out.println("注销 user=" + toURI);
                //发送ok响应
                response = messageFactory.createResponse(Response.OK, request);
                System.out.println("返回注销结果 response  : " + response.toString());
                if (serverTransactionId == null) {
                    serverTransactionId = sipProvider.getNewServerTransaction(request);
                    serverTransactionId.sendResponse(response);
                    // serverTransactionId.terminate();
                    System.out.println("register serverTransaction: " + serverTransactionId);
                } else {
                    System.out.println("processRequest serverTransactionId is null.");
                }
                //移除
                registingId.remove(toUserId);
                registedContactURI.remove(toUserId);
            }


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
                              ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootmeA: got an Invite sending Trying");
            // System.out.println("shootmeA: " + request);
            Response response = messageFactory.createResponse(Response.TRYING,
                    request);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            st.sendResponse(response);

            this.okResponse = messageFactory.createResponse(Response.OK,
                    request);
            Address address = addressFactory.createAddress("ShootmeA <sip:"
                    + getHost() + ":" + getPort() + ";lr" + ">");
            ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            if (toHeader.getTag() == null ) {
                toHeader.setTag(new Integer((int) ( Math.random()  * 10000) ).toString()); // Application is supposed to set.
            } else {
                System.out.println("Re-INVITE processing");
            }
            okResponse.addHeader(contactHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();
            okResponse.setContent(contents, contentTypeHeader);

//            this.inviteTid = st;
            this.inviteRequest = request;

            new Timer().schedule(new MyTimerTask(this,false), 10000);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent,
                           ServerTransaction serverTransaction) {
        System.out.println("shootmeA: got an ACK! ");
        System.out.println("Dialog State = " + dialog.getState());
        new Timer().schedule(new MyTimerTask(this,true), 4000);
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
                           ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println(getPort() + "  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent,
                              ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootmeA:  got a cancel.");
            if (serverTransactionId == null) {
                System.out.println("shootmeA:  null tid.");
                return;
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED) {
                response = messageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteRequest);
//                inviteTid.sendResponse(response);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        ClientTransaction clientTransactionTmp = responseEvent.getClientTransaction();
        System.out.println("responseEvent.getResponse()" + responseEvent.getResponse()+"\n\n");
        Response response = responseEvent.getResponse();

        int  statusCode = response.getStatusCode();
        if (clientTransactionTmp == null){
            System.out.println("clientTransactionTmp is null");
            return;
        }
        try {
            if(statusCode == Response.BUSY_HERE){
                Dialog dialog = clientTransactionTmp.getDialog();
                Request request = dialog.createRequest(Request.ACK);
                ClientTransaction newClientTransaction = sipProvider.getNewClientTransaction(request);
                newClientTransaction.sendRequest();
            }
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = "
                + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
        isTimeout = true;
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        messageProcessor.processError("Previous message not sent: "
                + "I/O Exception");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("TransactionTerminated......事务终止");
        if (isTimeout){
            ringResult.setAnsCode(AnsCodeEnum.CALL_TIMEOUT.getCode());
        }else{
            ringResult.setAnsCode(AnsCodeEnum.CALLED_REFUSE.getCode());
        }

        // 将呼叫结果保存到数据库(写数据库)
        InjectServiceUtil.getInstance().getSipRingService().saveSipRingRecord(ringResult);

        // 发送Http请求,将数据发送到广汽接口(异步返回呼叫结果)
        Gson gson = new Gson();
        String json = gson.toJson(ringResult);
        System.out.println(json);
        JSONObject jsonObject = JSONObject.parseObject(json);
        HttpUtil.sendPost("Http://localhost:8089/callback", jsonObject);
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("DialogTerminated......对话终止");
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
