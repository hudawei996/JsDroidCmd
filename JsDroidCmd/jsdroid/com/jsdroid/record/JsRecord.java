package com.jsdroid.record;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.jsdroid.node.Node;

public class JsRecord {
	public int xmin;
	public int xmax;
	public int ymin;
	public int ymax;
	public int imageWidth;
	public int imageHeight;
	public long time = System.currentTimeMillis();
	public int rotation;
	public String imageName;
	public List<Node> nodes;
	public List<Point> points = new ArrayList<Point>();
	@Override
	public String toString() {
		return "JsRecord [xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin
				+ ", ymax=" + ymax + ", imageWidth=" + imageWidth
				+ ", imageHeight=" + imageHeight + ", time=" + time
				+ ", rotation=" + rotation + ", imageName=" + imageName + "]";
	}
	public int getRealX(int x,int screenWidth){
		return (x-xmin)*screenWidth/(xmax-xmin);
	}
	public int getRealY(int y,int screenHeight){
		return (y-ymin)*screenHeight/(ymax-ymin);
	}

}
