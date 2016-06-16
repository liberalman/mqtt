package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.cloud.HttpClientConfig;
import com.seaofheart.app.util.CryptoUtils;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class j {
    private static final String TAG = j.class.getSimpleName(); // a
    private static final String file_version = "file_version"; // b
    private static final String app_key = "app_key"; // c
    private static final String sdk_version = "sdk_version"; // d
    private static final int e = 2;
    private String f = "easemob";
    private String g = "server.xml";
    private int h = 2;
    private j.Jsalahe i = null;
    private static final String j = "http://www.easemob.com/easemob/server.xml";
    private static final String k = "com.seaofheart.app.config.xml";
    private static final String l = "com.seaofheart.app.config.ky.xml";
    private static long m = 259200000L;
    private static j instance = new j(); // n
    private HostResolverStrategy hostResolverStrategy = null; // o
    private boolean ppp = false; // p
    private Object q = new Object();

    j() {
        this.hostResolverStrategy = new HostResolverStrategy();
    }

    public static j getInstance() { // a()
        return instance;
    }

    public void a(Context var1) {
    }

    public j.Address b() {
        return this.hostResolverStrategy.b();
    }

    public j.Address c() {
        return this.hostResolverStrategy.c();
    }

    public j.Address d() {
        return this.hostResolverStrategy.d();
    }

    public j.Address e() {
        return this.hostResolverStrategy.h();
    }

    public j.Address f() {
        return this.hostResolverStrategy.i();
    }

    public j.Address g() {
        return this.hostResolverStrategy.j();
    }

    public boolean h() {
        return this.i != null;
    }

    public synchronized Jsalahe i() {
        if(this.i != null) {
            return this.i;
        } else {
            if(p.getInstance().I() == -1L) {
                Jsalahe var1 = this.n();
                if(var1 != null) {
                    this.i = var1;
                }
            } else {
                CryptoUtils var4 = new CryptoUtils();
                var4.initAES();

                try {
                    String var2 = var4.decryptBase64String(p.getInstance().H());
                    this.i = this.a(var2);
                    this.r();
                } catch (Exception var3) {
                    EMLog.e(TAG, "parse dns xml from our store is failed with error : " + var3.getMessage());
                }

                if(System.currentTimeMillis() - p.getInstance().F() > 0L) {
                    this.i = this.n();
                }
            }

            return this.i;
        }
    }

    public List<j.Address> j() {
        return this.i == null?null:this.a(this.i.g);
    }

    public List<j.Address> k() {
        return this.i == null?null:this.a(this.i.h);
    }

    private List<j.Address> a(List<j.DnsHost> var1) {
        if(this.i != null && var1 != null) {
            ArrayList var2 = new ArrayList();
            Iterator var4 = var1.iterator();

            while(var4.hasNext()) {
                j.DnsHost var3 = (j.DnsHost)var4.next();
                if(var3 != null && !TextUtils.isEmpty(var3.domain)) {
                    j.Address var5 = new j.Address();
                    var5.host = var3.domain;
                    var5.port = var3.port;
                    var5.protocol = var3.protocol;
                    var5.dnsHost = var3;
                    var2.add(var5);
                }
            }

            return var2;
        } else {
            return null;
        }
    }

    public static boolean a(j.Address var0) {
        return var0 != null && var0.dnsHost != null && var0.dnsHost.ip != null?!var0.dnsHost.ip.equalsIgnoreCase(var0.host):false;
    }

    public synchronized void l() {
        try {
            p var1 = p.getInstance();
            var1.a(-1L);
            var1.b(-1L);
            var1.h("");
            var1.g("");
            this.hostResolverStrategy.k();
            this.i = null;
        } catch (Exception var2) {
            ;
        }

    }

    public synchronized void m() {
        this.hostResolverStrategy.k();
        this.i = null;
    }

    public Jsalahe n() {
        Object var1;
        if(this.ppp) {
            var1 = this.q;
            synchronized(this.q) {
                try {
                    this.q.wait();
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }
            }

            if(this.i != null) {
                return this.i;
            }
        }

        var1 = this.q;
        synchronized(this.q) {
            this.ppp = true;
            Jsalahe var2 = null;

            for(int var3 = 0; var3 < this.h; ++var3) {
                EMLog.d(TAG, "try to retrieve dns config! with retries number : " + var3);
                var2 = this.p();
                if(var2 != null) {
                    this.i = var2;
                    break;
                }

                if(!NetUtils.hasDataConnection(Chat.getInstance().getAppContext())) {
                    break;
                }
            }

            if(var2 == null) {
                String var7 = null;
                if(p.getInstance().n()) {
                    var7 = p.getInstance().f("com.seaofheart.app.config.ky.xml");
                } else {
                    var7 = p.getInstance().f("com.seaofheart.app.config.xml");
                }

                if(var7 != null && !var7.equals("")) {
                    var2 = this.a(var7);
                }
            }

            if(var2 != null) {
                this.r();
            }

            this.ppp = false;
            this.q.notifyAll();
            return var2;
        }
    }

    public void o() {
        for(int var1 = 0; var1 < 2; ++var1) {
            EMLog.d(TAG, "try to retrieve dns config! with retries number : " + var1);
            Jsalahe var2 = this.p();
            if(var2 != null || !NetUtils.hasDataConnection(Chat.getInstance().getAppContext())) {
                break;
            }
        }

    }

    private Jsalahe p() {
        Jsalahe var1 = null;

        String var3;
        try {
            HashMap var2 = new HashMap();
            var2.put(HttpClientConfig.EM_TIME_OUT_KEY, String.valueOf(20000));
            var3 = this.q();
            EMLog.d(TAG, "config server url : " + var3);
            HttpResponse var4 = HttpClient.getInstance().httpExecute(var3, var2, (String)null, HttpClient.GET);
            int var5 = var4.getStatusLine().getStatusCode();
            if(var5 == 200) {
                HttpEntity var6 = var4.getEntity();
                String var7 = EntityUtils.toString(var6);
                EMLog.d(TAG, "returned config content : " + var7);
                var1 = this.a(var7);
                if(var1 != null) {
                    this.a(var1, var7);
                }
            }
        } catch (Exception var8) {
            var3 = "error to retrieve dns config";
            if(var8 != null && var8.getMessage() != null) {
                var3 = var8.getMessage();
            }

            EMLog.e(TAG, "retrieveDNSConfigWithCountDown error:" + var3);
            if(this.i != null && var3.contains("refused")) {
                this.hostResolverStrategy.h();
            }
        }

        return var1;
    }

    private String q() throws UnsupportedEncodingException {
        String var1 = "http://www.easemob.com/easemob/server.xml";
        if(this.i != null) {
            j.Address var2 = this.hostResolverStrategy.d();
            StringBuilder var3 = new StringBuilder();
            if(var2.protocol != null && var2.protocol.contains("http")) {
                var3.append(var2.protocol);
            } else {
                var3.append("http");
            }

            var3.append("://");
            var3.append(var2.host + "/" + this.f + "/" + this.g);
            var1 = var3.toString();
        }

        var1 = var1 + "?" + "sdk_version" + "=" + URLEncoder.encode(p.getInstance().g(), "UTF-8") + "&" + "app_key" + "=" + URLEncoder.encode(ChatConfig.getInstance().APPKEY, "UTF-8") + "&" + "file_version" + "=" + URLEncoder.encode(p.getInstance().G(), "UTF-8");
        return var1;
    }

    private void a(Jsalahe var1, String var2) throws Exception {
        p var3 = p.getInstance();
        String var4 = var3.G();
        if(var1 != null) {
            if(TextUtils.isEmpty(var4) || !var4.equals(var1.b)) {
                CryptoUtils var5 = new CryptoUtils();
                var5.initAES();
                var3.h(var5.encryptBase64String(var2));
                var3.g(var1.b);
            }

            var3.a(System.currentTimeMillis());
            if(System.currentTimeMillis() >= var1.c) {
                var3.b(System.currentTimeMillis() + m);
            } else {
                var3.b(var1.c);
            }
        }

    }

    public Jsalahe a(String var1) {
        return var1 != null && !var1.equals("")?this.a((InputStream)(new ByteArrayInputStream(var1.getBytes()))):null;
    }

    synchronized Jsalahe a(InputStream var1) {
        if(var1 == null) {
            return null;
        } else {
            Jsalahe var2 = null;

            try {
                XmlPullParser var3 = Xml.newPullParser();
                var3.setInput(var1, "UTF-8");

                for(int var4 = var3.getEventType(); var4 != 1; var4 = var3.next()) {
                    if(var4 == 2) {
                        String var5 = var3.getName();
                        if("ebs".equals(var5)) {
                            var2 = new Jsalahe();
                        } else if("deploy_name".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.a = var3.getText();
                            }
                        } else if("file_version".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.b = var3.getText();
                            }
                        } else if("valid_before".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                try {
                                    var2.c = (long)Integer.parseInt(var3.getText());
                                    if(var2.c <= 0L) {
                                        var2.c = System.currentTimeMillis() + m;
                                    } else {
                                        var2.c *= 1000L;
                                    }
                                } catch (Exception var7) {
                                    EMLog.d(TAG, var7.getMessage());
                                    var2.c = System.currentTimeMillis() + m;
                                }
                            }
                        } else if("gcm_enabled".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.d = Boolean.parseBoolean(var3.getText());
                            }
                        } else if("im".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.g = new ArrayList();
                                this.a(var3, var2.g);
                            }
                        } else if("rest".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.h = new ArrayList();
                                this.a(var3, var2.h);
                            }
                        } else if("resolver".equals(var5)) {
                            var4 = var3.next();
                            if(var2 != null) {
                                var2.f = new ArrayList();
                                this.a(var3, var2.f);
                            }
                        }
                    }
                }
            } catch (XmlPullParserException var8) {
                var8.printStackTrace();
                var2 = null;
            } catch (IOException var9) {
                var9.printStackTrace();
                var2 = null;
            }

            return var2;
        }
    }

    private void a(XmlPullParser var1, List<j.DnsHost> var2) throws XmlPullParserException, IOException {
        if(var2 != null) {
            int var3 = var1.getEventType();
            j.DnsHost var4 = null;

            while(true) {
                String var5;
                if(var3 == 2) {
                    var5 = var1.getName();
                    if(var5.equals("host")) {
                        var4 = new j.DnsHost();
                    } else if(var5.equals("domain")) {
                        var1.next();
                        if(var4 != null) {
                            var4.domain = var1.getText();
                        }
                    } else if(var5.equals("ip")) {
                        var1.next();
                        if(var4 != null) {
                            var4.ip = var1.getText();
                        }
                    } else if(var5.equals("port")) {
                        var1.next();
                        if(var4 != null) {
                            try {
                                var4.port = Integer.parseInt(var1.getText());
                            } catch (Exception var7) {
                                EMLog.d(TAG, var7.getMessage());
                                var4.port = -1;
                            }
                        }
                    } else if(var5.equals("protocol")) {
                        var1.next();
                        if(var4 != null) {
                            var4.protocol = var1.getText();
                        }
                    }
                } else if(var3 == 3) {
                    var5 = var1.getName();
                    if(var5.equals("host")) {
                        if(var4 != null) {
                            var2.add(var4);
                        }
                    } else if(var5.equals("hosts")) {
                        break;
                    }
                } else if(var3 == 1) {
                    EMLog.w(TAG, "we reached end of document, but not end of hosts!");
                    break;
                }

                var3 = var1.next();
            }

        }
    }

    private void r() {
        if(this.i != null) {
            if(this.i.g != null) {
                this.b(this.i.g);
            }

            if(this.i.h != null) {
                this.b(this.i.h);
            }

            if(this.i.f != null) {
                this.b(this.i.f);
            }
        }

    }

    private void b(List<j.DnsHost> var1) {
        if(var1 != null && var1.size() > 1) {
            ArrayList var2 = new ArrayList();
            var2.addAll(var1);
            int var3 = var1.size();
            var1.clear();

            for(int var4 = 0; var4 < var3; ++var4) {
                int var5 = 0;
                if(var4 < var3 - 1) {
                    var5 = (new Random()).nextInt(var3 - 1 - var4);
                }

                j.DnsHost var6 = (j.DnsHost)var2.remove(var5);
                var1.add(var6);
            }
        }

    }

    public static class Jsalahe { // class a
        public String a = "";
        public String b = "";
        public long c = -1L;
        public boolean d = false;
        public boolean e = true;
        public List<j.DnsHost> f;
        public List<j.DnsHost> g;
        public List<j.DnsHost> h;

        public Jsalahe() {
        }

        public String a() {
            StringBuilder var1 = new StringBuilder();
            if(this.a != null) {
                var1.append("name : " + this.a + "\n");
            }

            if(this.b != null) {
                var1.append("version : " + this.b + "\n");
            }

            var1.append("valid_before : " + this.c + "\n");
            if(this.f != null) {
                var1.append(this.f.toString());
            }

            if(this.d) {
                var1.append("gcm_enabled : " + this.d + "\n");
            }

            if(this.g != null) {
                var1.append(this.g.toString());
            }

            if(this.h != null) {
                var1.append(this.h.toString());
            }

            return var1.toString();
        }

        public boolean a(Object var1) {
            if(var1 == this) {
                return true;
            } else if(var1 == null) {
                return false;
            } else if(!(var1 instanceof Jsalahe)) {
                return false;
            } else {
                Jsalahe var2 = (Jsalahe)var1;
                return var2.a.equals(this.a) && var2.b.equals(this.b) && var2.c == this.c?((this.g != null || var2.g == null) && (this.g == null || var2.g != null)?((this.h != null || var2.h == null) && (this.h == null || var2.h != null)?(this.g != null && !this.g.equals(var2.g)?false:this.h == null || this.h.equals(var2.h)):false):false):false;
            }
        }
    }

    /**
     * dnsHost
     */
    public static class DnsHost { // class b
        public String domain = ""; // a
        public String ip = ""; // b
        public int port = 0; // c
        public String protocol = ""; // d

        public DnsHost() {
        }

        public String a() {
            StringBuilder var1 = new StringBuilder();
            var1.append("domain : " + this.domain + "\n");
            var1.append("ip : " + this.ip + "\n");
            var1.append("port : " + this.port + "\n");
            var1.append("protocol : " + this.protocol + "\n");
            return var1.toString();
        }

        public boolean a(Object var1) {
            if(var1 == this) {
                return true;
            } else if(var1 == null) {
                return false;
            } else if(var1 instanceof j.DnsHost) {
                j.DnsHost var2 = (j.DnsHost)var1;
                return var2.domain.equals(this.domain) && var2.ip.equals(this.ip) && var2.port == this.port && var2.protocol.equals(this.protocol);
            } else {
                return false;
            }
        }
    }

    public static class Address { // class c
        public String host = null; // a
        public int port = -1; // b
        public String protocol = ""; // c
        public j.DnsHost dnsHost = null;

        public Address() {
        }

        public String a() {
            StringBuilder var1 = new StringBuilder();
            var1.append(" host : " + this.host);
            var1.append(" port : " + this.port);
            var1.append(" protocol : " + this.protocol);
            if(this.dnsHost != null) {
                var1.append("dnsHost : [");
                var1.append(this.dnsHost.a());
                var1.append("]");
            }

            return var1.toString();
        }
    }
}

