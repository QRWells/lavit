package lavit.stateviewer;

import java.awt.Color;
import java.awt.Shape;
import java.util.LinkedHashSet;

public class Node {

	public long id;

	public String state;
	public String label;
	boolean accept;
	public boolean inCycle;

	public int depth;
	public int nth;

	public boolean dummy;
	public boolean weak;

	private LinkedHashSet<Transition> toes;
	private LinkedHashSet<Node> fromes;

	public NodeSet childSet;
	public NodeSet parentSet;

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
