package com.jsdroid.proxy;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsdroid.util.LocalSocketUtil;

public class ProxyService implements Runnable {
    LocalServerSocket server;
    String serviceName;

    public ProxyService(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void run() {
        try {
            server = new LocalServerSocket(serviceName);
            for (; ; ) {
                LocalSocket socket = server.accept();
                try {
                    LocalSocketUtil socketUtil = new LocalSocketUtil(socket);
                    String line = socketUtil.readLine();
                    JSONObject json = JSON.parseObject(line);
                    String methodName = json.getString("method");
                    JSONArray params = json.getJSONArray("params");
                    Object result = null;
                    try {
                        if (params != null) {
                            Object[] paramsObject = new Object[params.size()];
                            Class<?>[] types = new Class<?>[params.size()];
                            for (int i = 0; i < params.size(); i++) {
                                JSONObject param = params.getJSONObject(i);
                                String type = param.getString("type");
                                types[i] = Class.forName(type);
                                paramsObject[i] = param.getObject("value",
                                        types[i]);
                            }
                            Method method = getClass().getMethod(methodName,
                                    types);
                            result = method.invoke(this, paramsObject);

                        } else {
                            Method method = getClass().getMethod(methodName);
                            result = method.invoke(this);
                        }
                    } catch (Exception e) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        PrintWriter pw = new PrintWriter(outputStream);
                        e.printStackTrace(pw);
                        pw.close();
                    }
                    JSONObject retJson = new JSONObject();
                    if (result == null) {
                        retJson.put("type", "void");
                    } else {
                        String retType = result.getClass().getName();
                        retJson.put("type", retType);
                        retJson.put("value", result);
                    }
                    socketUtil.sendLine(retJson.toJSONString());
                } catch (Exception e) {
                } finally {
                    try {
                        socket.close();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                server.close();
            } catch (Exception e) {
            }
        }
    }
}
