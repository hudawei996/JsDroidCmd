importPackage(java.lang)
importPackage(java.io)
importPackage(java.util)
importPackage(java.util.regex)
importPackage(com.jsdroid.script)
importPackage(com.jsdroid.uiautomator2)
importPackage(com.jsdroid.util)
importPackage(com.jsdroid.transaction)
importPackage(com.jsdroid.input)

importClass(android.content.ComponentName)
importClass(android.content.Intent)
importClass(android.app.ActivityManagerNative)
importClass(android.app.ActivityThread)

/**
 * 启动activity
 */
function startActivity(intent){
	am = ActivityManagerNative.getDefault();
	try {
		//4.2+
		am.startActivity(null, "android", intent, null, null, null, 0,Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, null, null);
	} catch (e) {
	}
	try {
		//4.1-
		am.startActivity(null, intent,null, null, 0,null, null, 0,true,false,null,null,null);
	} catch (e) {
	}
}

/**
 * 解锁
 */
function unlock(){
	try {
	  	device.wakeUp();	
		var km = device.getAutomatorBridge().getContext().getSystemService("keyguard");
		km.newKeyguardLock("StartupReceiver").disableKeyguard();
	} catch (err) {
	}
}


/**
 * 启动app
 */
function launch(pkg){
	context = ActivityThread.currentActivityThread().getSystemContext();
	intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
	startActivity(intent)
}

/**
 * 转换为java字符串
 */
function toStr(obj){
	return new java.lang.String(obj)
}

/**
 * 转换为数字
 */
function toInt(obj){
	return parseInt(str(obj))
}

/**
 * 等待func返回非空
 */
function wait(func,timeout){
    var endtime = time()+timeout
    while(time()<endtime){
        var result= func()
		if(result!=null){
			return result
		}
        sleep(100)
    }
}

/**
 * 等待View
 */
function waitView(res,timeout){
    return wait(function(){
        var view = findView(res)
        if(view!=null){
            return view
        }
    },timeout)
}

/**
 * 等待activity
 */
function waitAct(name,timeout){
    return wait(function(){
        var top = device.getAct()
        if(top.contains(name)){
            return top;
        }
    },timeout)
}

/**
 * 查找控件点击
 */
function click(res){
	try{findView(res).click()}catch(e){
		return false
	}
	return true
}

/**
 * 读取文本
 */
function read(file){
	try{return FileUtil.readAllText(file).trim()}catch(e){return ""}
}

/**
 * 保存文本
 */
function write(file,content){
	try{FileUtil.write(file,content)}catch(e){}
}

/**
 * 添加文本
 */
function append(file,content){
	try{FileUtil.append(file,content)}catch(e){}
}

/**
 * 执行shell
 */
function execute(cmd){
	return ShellUtil.exec(cmd)
}

/**
 * 滑动到顶部
 */
function scrollToTop(res){
	var lastPosition = null
	while(true){
		var list = findView(res)
		if(list==null){
			break
		}
		var thisPosition = list.getAllText()
		if(lastPosition==null||thisPosition!=lastPosition){
			//非重复，滑动
			list.swipe(Direction.DOWN,parseFloat(0.8),10000)
			try{device.waitForIdle(2000)}catch(e){}
		}else{
			//重复，退出循环
			break
		}
		lastPosition = thisPosition
	}
}

/**
 * 滑动到底部
 */
function scrollToBottom(res){
	var lastPosition = null
	while(true){
		var list = findView(res)
		if(list==null){
			break
		}
		var thisPosition = list.getAllText()
		if(lastPosition==null||thisPosition!=lastPosition){
			//非重复，滑动
			list.swipe(Direction.UP,parseFloat(0.8),10000)
		}else{
			//重复，退出循环
			break
		}
		lastPosition = thisPosition
	}
}
/**
 * 返回，直到界面出现
 */
function back(by,count){
	for(var i=0;i<count;i++){
		if(waitView(by,2000)!=null){
			return true;
		}
		device.pressBack();
	}
}

/**
 * 返回，直到界面出现
 */
function back(by){
	back(by,5)
}


