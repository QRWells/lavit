package lavit.rejectstateviewer;

import java.awt.Color;
import java.awt.Shape;
import java.util.LinkedHashSet;

public class Node {

	public long id;

	public int depth;
	public int nth;

	private boolean marked;
	private boolean inFrame;

	//�����Ϣ
	private Shape shape;
	private double radius;
	private Color fillColor;
	private Color drawColor;

	//ʪ����ǥ����ѿ�
	public double dy;
	public double ddy;

}
