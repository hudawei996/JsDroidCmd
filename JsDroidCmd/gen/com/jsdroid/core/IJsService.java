/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\88\\workspace\\jsdroid_cmd\\jsdroid\\com\\jsdroid\\core\\IJsService.aidl
 */
package com.jsdroid.core;
public interface IJsService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.jsdroid.core.IJsService
{
private static final java.lang.String DESCRIPTOR = "com.jsdroid.core.IJsService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.jsdroid.core.IJsService interface,
 * generating a proxy if needed.
 */
public static com.jsdroid.core.IJsService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.jsdroid.core.IJsService))) {
return ((com.jsdroid.core.IJsService)iin);
}
return new com.jsdroid.core.IJsService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.jsdroid.core.IJsService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
