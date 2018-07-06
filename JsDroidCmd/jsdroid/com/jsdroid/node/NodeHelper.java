package com.jsdroid.node;

public class NodeHelper {
	// 得到一个获取其节点的函数
	public static String getFindFunction(Node root, Node node, String linestart) {
		root.setParentAndDepth(0);
		
		if (linestart == null) {
			linestart = "";
		}
		String linestart2 = linestart + "    ";

		Node parent = getTheOnlyParent(root, node);
		String functionString = "function(){\n";
		String by = parent.getBy();
		String findNode = linestart2 + "var node = findBiew(" + by + ")\n";
		String getChild = "";

		Node temp = node;
		System.out.println("start fucntion");
		for (;;) {
			if (temp == parent || temp == null) {
				break;
			}
			getChild = linestart2 + "if(node!=null){\n" + linestart2
					+ "    node = node.getChildren().get(" + temp.index
					+ ");\n" + linestart2 + "}\n" + getChild;
			temp = temp.parent;
		}
		functionString += findNode + getChild;
		functionString += linestart + "}\n";
		System.out.println("end fucntion");
		return functionString;
	}

	// 点击一个点，获取节点
	public static Node getTapNode(Node root, int x, int y) {
		for (int i = 0; i < root.getChildCount(); i++) {
			Node node = getTapNode(root.getChild(i), x, y);
			if (node != null) {
				return node;
			}
		}
		if (root.rect.contains(x, y)) {
			return root;
		}
		return null;
	}

	// 得到唯一性的父节点
	static Node getTheOnlyParent(Node root, Node node) {
		Node temp = node;
		System.out.println("start get only node");
		for (;;) {
			BySelector selector = temp.getSeletor();
			if (isTheOnlySelector(root, selector)) {
				System.out.println("end get only node");
				return temp;
			}
			if (temp.parent == root) {
				System.out.println("end get only node");
				return temp;
			}
			temp = temp.parent;
		}

	}

	// 判断是否是唯一选择器
	static boolean isTheOnlySelector(Node root, BySelector selector) {
		if (root.findNodes(selector).size() == 1) {
			return true;
		}
		return false;
	}
}
