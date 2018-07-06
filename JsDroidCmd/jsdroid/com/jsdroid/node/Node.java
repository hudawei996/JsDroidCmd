package com.jsdroid.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Node implements Cloneable {
	public static Node clone(Node node) {
		try {
			return (Node) node.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	public static Node parseNode(String json) {
		return new Gson().fromJson(json, Node.class);
	}

	public long sourceId;
	public int depth;
	public int index;
	public String pkg;
	public String clazz;
	public String text;
	public String desc;
	public String res;
	public boolean checkable;
	public boolean checked;
	public boolean clickable;
	public boolean enabled;
	public boolean focusable;
	public boolean focused;
	public boolean longClickable;
	public boolean scrollable;
	public boolean selected;
	public Rect rect;
	public Node parent;
	public List<Node> children;
	public boolean password;

	// 查询
	public List<Node> findNodes(BySelector selector) {
		List<Node> ret = new ArrayList<Node>();
		for (Node node : ByMatcher.findMatches(selector, this)) {
			ret.add(node);
		}
		return ret;
	}

	public String getBy() {
		String by = "By";
		if (res != null && res.trim().length() > 0) {
			by += ".res('" + res + "')";
		}
		if (text != null && text.trim().length() > 0) {
			by += ".text('" + text + "')";
		}
		if (desc != null && desc.trim().length() > 0) {
			by += ".desc('" + desc + "')";
		}
		if (clazz != null && clazz.trim().length() > 0) {
			by += ".clazz('" + clazz + "')";
		}
		return by;
	}

	public Node getChild(int i) {
		return children == null ? null : children.get(i);
	}

	public int getChildCount() {
		return children == null ? 0 : children.size();
	}

	public BySelector getSeletor() {
		return By.text(text).res(res).desc(desc).clazz(clazz);
	}

	// 给子节点赋值深度和parent属性，获取json数据的时候parent不赋值，因为解析parent属性时候会产生死循环
	public void setParentAndDepth(int depth) {
		this.depth = depth;
		for (int i = 0; i < getChildCount(); i++) {
			getChild(i).parent = this;
			getChild(i).index = i;
			getChild(i).setParentAndDepth(depth + 1);
		}
	}

	public String toString() {
		return new Gson().toJson(this);
	}

	public void copyFromNode(Node node) {
		text = node.text;
		res = node.res;
		clazz = node.clazz;
		pkg = node.pkg;
		desc = node.desc;
		checkable = node.checkable;
		checked = node.checked;
		clickable = node.clickable;
		enabled = node.enabled;
		focusable = node.focusable;
		focused = node.focused;
		scrollable = node.scrollable;
		longClickable = node.longClickable;
		password = node.password;
		selected = node.selected;
	}

}
