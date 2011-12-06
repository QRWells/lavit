/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * <p>�ȥ饤�ڤ��Ѥ������񥪥֥������ȤǤ���</p>
 * <p>
 * �Ρ��ɤ����ܤ��ʤ���ޥå��󥰤��뤳�Ȥǡ�ʸ���󤬤��μ�����˴ޤޤ�Ƥ��뤫�����ǿ���̵�ط��ʻ��֤�
 * Ƚ�Ǥ��뤳�Ȥ��Ǥ��ޤ���
 * </p>
 * @author Yuuki SHINOBU
 */
public final class TrieTreeMatcher
{
	// node of prefix trie tree.
	// <root>--[f]--[i]--[n]--[a]--[l]--<accept>
	//                              |
	//                              +---[l]--[y]--<accept>
	private static final class Node
	{
		private boolean _accept = false;
		private Map<Character, Node> _children;

		public void add(String str)
		{
			if (str.length() > 0)
			{
				if (_children == null)
					_children = new TreeMap<Character, Node>();

				char c = str.charAt(0);
				Node child = _children.get(c);

				if (child == null)
					_children.put(c, child = new Node());

				child.add(str.substring(1));
			}
			else
			{
				_accept = true;
			}
		}

		public Node transition(char c)
		{
			return _children.get(c);
		}

		public boolean canTransition(char c)
		{
			return _children != null && _children.containsKey(c);
		}

		public boolean accepts()
		{
			return _accept;
		}
	}

	private Node _rootNode = new Node();
	private Node _current;
	private boolean _failed = false;

	/**
	 * <p>���μ��񥪥֥������Ȥ�������ޤ���</p>
	 */
	public TrieTreeMatcher()
	{
		reset();
	}

	/**
	 * <p>���μ��񥪥֥������Ȥξ��֤��������ޤ�����Ͽ���줿���ƤϺ�����ޤ���</p>
	 */
	public void reset()
	{
		_failed = false;
		_current = _rootNode;
	}

	/**
	 * <p>���μ��񥪥֥������Ȥ�ʸ������ɲä��ޤ���</p>
	 * @param word �ɲä���ʸ����
	 */
	public void add(String word)
	{
		_rootNode.add(word);
	}

	/**
	 * <p>ʸ�� {@code c} ��Ϳ���ƾ��֤����ܤ����ޤ���</p>
	 * @param c ʸ��
	 */
	public void transition(char c)
	{
		if (!_failed)
		{
			if (_current.canTransition(c))
			{
				_current = _current.transition(c);
			}
			else
			{
				_failed = true;
			}
		}
	}

	/**
	 * <p>���μ��񥪥֥������Ȥ����߼������֤Ǥ��뤫�֤��ޤ���</p>
	 * @return �������֤Ǥ������ {@code true}
	 */
	public boolean accepts()
	{
		return !_failed && _current.accepts();
	}
}
