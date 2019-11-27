package com.sip.peers.sipserver;

import com.sip.peers.sip.SipLayer;
import com.sip.peers.util.MD5Util;
import gov.nist.javax.sip.header.Via;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
 * @create: 2019-11-25 15:27
 */
public class JainSipServer implements SipListener {

    SipStack sipstack = null;
    HeaderFactory hf = null;
    AddressFactory af = null;
    MessageFactory mf = null;
    SipProvider sipProvider = null;

    /**
     * 保存正在注册的用户，注册第一步的
     */
    private static Set<String> registingId = new HashSet<>();

    /**
     * 保存当前注册的用户，注册成功的
     */
    private static Hashtable<String, URI> registedContactURI = new Hashtable<>();

    /**
     * 主叫对话
     */
    private Dialog calleeDialog = null;

    /**
     * 被叫对话
     */
    private Dialog callerDialog = null;

    ClientTransaction clientTransactionId = null;

    /**
     * 服务器侦听IP地址
     */
    private String ipAddr = "192.168.200.1";

    /**
     * 服务器侦听端口
     */
    private int port = 5060;

    public static void main(String[] args) {
        JainSipServer test = new JainSipServer();
        test.init();
    }
    public void init() {
        Properties prop = new Properties();
        prop.setProperty("javax.sip.STACK_NAME", "teststackname");
//		prop.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
//		prop.setProperty("javax.sip.OUTBOUND_PROXY", "127.0.0.1:8888/UDP");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "siptestdebug.txt");
        prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "siptestlog.txt");

        SipFactory sf = SipFactory.getInstance();
        sf.setPathName("gov.nist");
        try {
            sipstack = sf.createSipStack(prop);
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }

        try {
            hf = sf.createHeaderFactory();
            af = sf.createAddressFactory();
            mf = sf.createMessageFactory();
            ListeningPoint listeningPoint = sipstack.createListeningPoint("192.168.200.1", 5060, "udp");

            sipProvider = sipstack.createSipProvider(listeningPoint);
            sipProvider.addSipListener(this);
            System.out.println("服务启动完成。。。");
        } catch (TransportNotSupportedException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (ObjectInUseException e) {
            e.printStackTrace();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }

    }


    //Listener实现
    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();
        if(null == request) {
            System.out.println("收到的requestEvent.getRequest() is null.");
            return ;
        }

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
    }

    /**
     * 处理invite请求
     * @param requestEvent
     * @param serverTransactionId
     */
    private void processInvite(RequestEvent requestEvent, ServerTransaction serverTransactionId) {

        Request request = requestEvent.getRequest();
        if (null == request){
            System.out.println("processInvite request is null.");
            return;
        }

        try {
            // 发送100 Trying
            if (serverTransactionId == null){
                serverTransactionId = sipProvider.getNewServerTransaction(request);
                callerDialog = serverTransactionId.getDialog();
                Response response = mf.createResponse(Response.TRYING, request);
                serverTransactionId.sendResponse(response);
            }
            // 查询目标地址
            URI reqUri = request.getRequestURI();
            URI contactURI = registedContactURI.get("Tom");

            System.out.println("processInvite rqStr=" + reqUri + " contact=" + contactURI);

            // 根据Request uri来路由, 后续的响应消息通过VIA来路由
            Request cliReq = mf.createRequest(request.toString());
            cliReq.setRequestURI(contactURI);

            Via callerVia = (Via) request.getHeader(Via.NAME);
            Via via = (Via) hf.createViaHeader(ipAddr, 5060,"UDP",callerVia.getBranch()+"sipphone");

            // Fixme 需要测试是否能够通过设置VIA头域来修改VIA头域值
            cliReq.removeHeader(Via.NAME);
            cliReq.addHeader(via);

            // 更新contact的地址
            ContactHeader contactHeader = hf.createContactHeader();
            Address address = af.createAddress("sip:sipsoft@" + ipAddr + ":" + port);
            contactHeader.setAddress(address);
            contactHeader.setExpires(3600);
            cliReq.setHeader(contactHeader);

            clientTransactionId = sipProvider.getNewClientTransaction(cliReq);
            clientTransactionId.sendRequest();

            System.out.println("processInvite clientTransactionId=" + clientTransactionId.toString());
            System.out.println("send invite to callee: " + cliReq);
        } catch (TransactionAlreadyExistsException e) {
            e.printStackTrace();
        } catch (TransactionUnavailableException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        if(null == response) {
            System.out.println("response is null.");
            return ;
        }
        System.out.println("收到的Response is :"+response);
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();
        Request request = clientTransaction.getRequest();
        System.out.println("收到的Response for request:"+request);

        if(response.getStatusCode() == Response.TRYING) {
            System.out.println("收到的response is 100 TRYING");
            return ;
        }
        switch(request.getMethod().toUpperCase()) {
            case Request.INVITE:
                System.out.println("收到INVITE的响应");
                break;
            case Request.BYE:
                System.out.println("收到BYE的响应");
                break;
            case Request.CANCEL:
                System.out.println("收到CANCEL的响应");
                break;
            default:
                System.out.println("不处理的response的请求类型："+request.getMethod().toUpperCase());
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub
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
                            response = mf.createResponse(Response.OK, request);
                            DateHeader dateHeader = hf.createDateHeader(Calendar.getInstance());
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
                            response = mf.createResponse(Response.FORBIDDEN, request);
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
                        response = mf.createResponse(Response.UNAUTHORIZED, request);
                        String realm = "zectec";
                        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
                        String callId = callIdHeader.getCallId();
                        String nonce = MD5Util.MD5(callId+toUserId);
                        WWWAuthenticateHeader wwwAuthenticateHeader = hf.createWWWAuthenticateHeader("Digest realm=\""+realm+"\",nonce=\""+nonce+"\"");
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
                response = mf.createResponse(Response.OK, request);
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

}
