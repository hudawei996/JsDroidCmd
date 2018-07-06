/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsdroid.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class ByMatcher {
	private BySelector mSelector;
	private boolean mShortCircuit;

	private ByMatcher(BySelector selector, boolean shortCircuit) {
		mSelector = selector;
		mShortCircuit = shortCircuit;
	}

	static Node findMatch(BySelector selector, Node root) {

		ByMatcher matcher = new ByMatcher(selector, true);
		List<Node> matches = matcher.findMatches(root);
		if (!matches.isEmpty()) {
			return matches.get(0);
		}
		return null;
	}

	static List<Node> findMatches(BySelector selector, Node root) {

		List<Node> ret = new ArrayList<Node>();
		ByMatcher matcher = new ByMatcher(selector, false);
		ret.addAll(matcher.findMatches(root));
		return ret;
	}

	private List<Node> findMatches(Node root) {
		List<Node> ret = findMatches(root, 0, 0,
				new SinglyLinkedList<PartialMatch>());

		// If no matches were found
		if (ret.isEmpty()) {
			// Run watchers and retry
			ret = findMatches(root, 0, 0, new SinglyLinkedList<PartialMatch>());
		}

		return ret;
	}

	private List<Node> findMatches(Node node, int index, int depth,
			SinglyLinkedList<PartialMatch> partialMatches) {

		List<Node> ret = new ArrayList<Node>();

		// Update partial matches
		for (PartialMatch partialMatch : partialMatches) {
			partialMatches = partialMatch.update(node, index, depth,
					partialMatches);
		}

		// Create a new match, if necessary
		PartialMatch currentMatch = PartialMatch.accept(node, mSelector, index,
				depth);
		if (currentMatch != null) {
			partialMatches = SinglyLinkedList.prepend(currentMatch,
					partialMatches);
		}

		// For each child
		int numChildren = node.getChildCount();
		for (int i = 0; i < numChildren; i++) {
			Node child = node.getChild(i);
			if (child == null) {
				continue;
			}

			// Add any matches found under the child subtree
			ret.addAll(findMatches(child, i, depth + 1, partialMatches));

			// Return early if we sound a match and shortCircuit is true
			if (!ret.isEmpty() && mShortCircuit) {
				return ret;
			}
		}

		// Finalize match, if necessary
		if (currentMatch != null && currentMatch.finalizeMatch()) {
			ret.add(Node.clone(node));
		}

		return ret;
	}

	static private boolean checkCriteria(Pattern criteria, String value) {
		if (criteria == null) {
			return true;
		}
		return criteria.matcher(value != null ? value : "").matches();
	}

	static private boolean checkCriteria(Boolean criteria, boolean value) {
		if (criteria == null) {
			return true;
		}
		return criteria.equals(value);
	}

	static private class PartialMatch {
		private final int matchDepth;
		private final BySelector matchSelector;
		private final List<PartialMatch> partialMatches = new ArrayList<PartialMatch>();

		private PartialMatch(BySelector selector, int depth) {
			matchSelector = selector;
			matchDepth = depth;
		}

		public static PartialMatch accept(Node node, BySelector selector,
				int index, int depth) {
			return accept(node, selector, index, depth, depth);
		}

		public static PartialMatch accept(Node node, BySelector selector,
				int index, int absoluteDepth, int relativeDepth) {

			if ((selector.mMinDepth != null && relativeDepth < selector.mMinDepth)
					|| (selector.mMaxDepth != null && relativeDepth > selector.mMaxDepth)) {
				return null;
			}

			PartialMatch ret = null;
			if (checkCriteria(selector.mClazz, node.clazz)
					&& checkCriteria(selector.mDesc, node.desc)
					&& checkCriteria(selector.mPkg, node.pkg)

					&& checkCriteria(selector.mRes, node.res)
					&& checkCriteria(selector.mText, node.text)
					&& checkCriteria(selector.mChecked, node.checked)
					&& checkCriteria(selector.mCheckable, node.checkable)
					&& checkCriteria(selector.mClickable, node.clickable)
					&& checkCriteria(selector.mEnabled, node.enabled)
					&& checkCriteria(selector.mFocused, node.focused)
					&& checkCriteria(selector.mFocusable, node.focusable)
					&& checkCriteria(selector.mLongClickable,
							node.longClickable)
					&& checkCriteria(selector.mScrollable, node.scrollable)
					&& checkCriteria(selector.mSelected, node.selected)) {

				ret = new PartialMatch(selector, absoluteDepth);
			}
			return ret;
		}

		public SinglyLinkedList<PartialMatch> update(Node node, int index,
				int depth, SinglyLinkedList<PartialMatch> rest) {

			for (BySelector childSelector : matchSelector.mChildSelectors) {
				PartialMatch m = PartialMatch.accept(node, childSelector,
						index, depth, depth - matchDepth);
				if (m != null) {
					partialMatches.add(m);
					rest = SinglyLinkedList.prepend(m, rest);
				}
			}
			return rest;
		}

		public boolean finalizeMatch() {
			Set<BySelector> matches = new HashSet<BySelector>();
			for (PartialMatch p : partialMatches) {
				if (p.finalizeMatch()) {
					matches.add(p.matchSelector);
				}
			}

			return matches.containsAll(matchSelector.mChildSelectors);
		}
	}

	private static class SinglyLinkedList<T> implements Iterable<T> {

		private final Node<T> mHead;

		public SinglyLinkedList() {
			this(null);
		}

		private SinglyLinkedList(Node<T> head) {
			mHead = head;
		}

		public static <T> SinglyLinkedList<T> prepend(T data,
				SinglyLinkedList<T> rest) {
			return new SinglyLinkedList<T>(new Node<T>(data, rest.mHead));
		}

		public Iterator<T> iterator() {
			return new Iterator<T>() {
				private Node<T> mNext = mHead;

				@Override
				public boolean hasNext() {
					return mNext != null;
				}

				@Override
				public T next() {
					T ret = mNext.data;
					mNext = mNext.next;
					return ret;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		private static class Node<T> {
			public final T data;
			public final Node<T> next;

			public Node(T d, Node<T> n) {
				data = d;
				next = n;
			}
		}
	}
}
