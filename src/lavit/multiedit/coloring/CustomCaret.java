package lavit.multiedit.coloring;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * <p>�����Τ����򤵤줿�Ȥ��˥���ݡ��ͥ�Ȥ�ü�ޤǥϥ��饤�Ȥ����褦�˳�ĥ���������åȤǤ���</p>
 * @author Yuuki SHINOBU
 */
@SuppressWarnings("serial")
public class CustomCaret extends DefaultCaret
{
	private static final class SelectionPainter implements
			Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int offs0, int offs1, Shape bounds,
				JTextComponent c)
		{
			Rectangle alloc = bounds.getBounds();
			try
			{
				TextUI mapper = c.getUI();
				Rectangle p0 = mapper.modelToView(c, offs0);
				Rectangle p1 = mapper.modelToView(c, offs1);

				g.setColor(c.getSelectionColor());

				if (p0.y == p1.y) // ñ��ι�
				{
					Rectangle r = p0.union(p1);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
				else // ʣ����
				{
					// ���Ϲԡ������ϰϤλ�����������ޤ�
					int p0ToRight = alloc.x + alloc.width - p0.x;
					g.fillRect(p0.x, p0.y, p0ToRight, p0.height);

					// ���Τ����򤵤�Ƥ����
					if ((p0.y + p0.height) != p1.y)
					{
						g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y
								- (p0.y + p0.height));
					}

					// �ǽ��ԡ���Ƭ���������ϰϤν����ޤ�
					g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
				}
			}
			catch (BadLocationException e)
			{
			}
		}
	}

	protected Highlighter.HighlightPainter getSelectionPainter()
	{
		return new SelectionPainter();
	}
}
