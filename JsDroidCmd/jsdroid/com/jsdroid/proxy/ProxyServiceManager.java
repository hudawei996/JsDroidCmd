package com.jsdroid.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsdroid.util.LocalSocketUtil;

public class ProxyServiceManager implements InvocationHandler {

    private static ExecutorService servicePool = Executors.newCachedThreadPool();

    protected String serviceName;

    private ProxyServiceManager(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LocalSocketUtil localSocket = new LocalSocketUtil(serviceName);
        JSONObject json = new JSONObject();
        json.put("method", method.getName());
        if (args != null) {
            JSONArray params = new JSONArray();
            for (int i = 0; i < args.length; i++) {
                JSONObject param = new JSONObject();
                param.put("type", args[i].getClass().getName());
                param.put("value", args[i]);
                params.add(param);
            }
            json.put("params", params);
        }
        localSocket.sendLine(json.toJSONString());
        String ret = localSocket.readLine();
        localSocket.close();
        JSONObject retJson = JSON.parseObject(ret);
        String type = retJson.getString("type");
        if (type == null || type.equals("void")) {
            return null;
        }
        return retJson.getObject("value", Class.forName(type));
    }

    public static <T> T getService(String serviceName, Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        ProxyServiceManager proxyFactory = new ProxyServiceManager(serviceName);
        return (T) Proxy.newProxyInstance(classLoader, interfaces,
                proxyFactory);
    }

    public static void addService(ProxyService service) {
        servicePool.execute(service);
    }
}
