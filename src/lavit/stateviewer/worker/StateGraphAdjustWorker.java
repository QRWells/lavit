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

package lavit.stateviewer.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.util.NodeYComparator;

public class StateGraphAdjustWorker extends SwingWorker<Object,Object>{
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;
	private boolean endFlag;
	private boolean changeActive;

	private ProgressFrame frame;

	private double xInterval;
	private double yInterval;

	public StateGraphAdjustWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
		this.endFlag = false;
		this.changeActive = true;
	}

	public void waitExecute(){
		this.changeActive = false;
		selectExecute();
		while(!endFlag){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	public void selectExecute(){
		if(drawNodes.size()<1000){
			atomic();
		}else{
			ready();
			execute();
		}
	}

	public void atomic(){
		ready(false);
		doInBackground();
		done();
	}

	public void ready(){
		ready(true);
	}

	public void ready(boolean open){
		if(changeActive) panel.setActive(false);
		if(open){
			frame = new ProgressFrame();
			addPropertyChangeListener(frame);
		}
	}

	public void end() {
		panel.autoCentering();
		if(xInterval==0){ xInterval = (double)panel.getWidth()/(drawNodes.getDepth()+1)/panel.getZoom(); }
		for(StateNode node : drawNodes.getAllNode()){
			node.setPosition((node.depth+1)*xInterval,node.getY());
		}
		panel.autoCentering();
		if(changeActive) panel.setActive(true);
		if(frame!=null) frame.dispose();
		this.endFlag = true;
	}

	@Override
	protected Object doInBackground(){

		int endNode = 0;
		double w = (double)panel.getWidth();
		double h = (double)panel.getHeight();

		panel.setZoom(1.0);

		xInterval = 0;
		yInterval = Double.MAX_VALUE;
		for(int i=0;i<drawNodes.getDepth();++i){
			double t = h/(drawNodes.getSizeOfDepth(i)+1);
			if(t<yInterval){
				yInterval = t;
			}
		}
		if(yInterval>30&&(w/(drawNodes.getDepth()+1))>30){ xInterval=yInterval=30; }
		if(yInterval<10){ yInterval=10; }

		//Ŭ����x����
		for(StateNode node : drawNodes.getAllNode()){
			node.setPosition((node.depth+1)*yInterval,node.getY());
		}

		//������֤�������ܸ�ʿ��������
		ArrayList<ArrayList<StateNode>> depthNode = drawNodes.getDepthNode();
		for(ArrayList<StateNode> nodes : depthNode){

			//ʿ�Ѿ�����
			for(StateNode node : nodes){
				int c = 0;
				double t = 0;
				for(StateNode from : node.getFromNodes()){
					if(from.depth<node.depth){
						++c;
						t += from.getY();
					}
				}
				if(c!=0){
					node.setPosition(node.getX(),t/c);
				}else{
					node.setPosition(node.getX(),0);
				}
			}

			//�ۤ���
			detanglingNodes(nodes,yInterval);

			//progress����
			endNode += nodes.size();
			setProgress(100*endNode/(drawNodes.size()*3));
			if(isCancelled()){ end(); return null; }

			/*
			//�⤵���������¸
			double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
			for(StateNode node : nodes){
				if(node.getY()<minY){
					minY = node.getY();
				}
				if(maxY<node.getY()){
					maxY = node.getY();
				}
			}
			*/
		}
		/*
		//����⤵�ο��������
		int maxDepth = 0;
		double maxHeight = Double.MIN_VALUE;
		for(int i=0;i<depthHeight.size();++i){
			if(maxHeight<depthHeight.get(i)){
				maxDepth=i;
				maxHeight=depthHeight.get(i);
			}
		}

		//����⤵�����������ʿ�����֥Хå��ȥ�å�
		for(int i=maxDepth-1;i>=0;--i){

			ArrayList<StateNode> nodes = depthNode.get(i);

			//ʿ�Ѿ�����
			for(StateNode node : nodes){
				int c = 0;
				double t = 0;
				for(StateNode to : node.getToNodes()){
					if(to.depth>node.depth){
						++c;
						t += to.getY();
					}
				}
				if(c!=0){
					node.setPosition(node.getX(),t/c);
				}
			}

			//�ۤ���(counter�Ǥ��ڤ�)
			for(int counter=0;counter<100;++counter){
				boolean res = detanglingNodes(nodes,yInterval*3/2);
				if(!res) break;
			}
		}
		*/

		//�ǿ��Ρ��ɤ����������ʿ��������
		for(int i=depthNode.size()-1;i>=0;--i){

			ArrayList<StateNode> nodes = depthNode.get(i);

			//ʿ�Ѿ�����
			for(StateNode node : nodes){
				int c = 0;
				double t = 0;
				for(StateNode to : node.getToNodes()){
					if(node.depth<to.depth){
						++c;
						t += to.getY();
					}
				}
				if(c!=0){
					node.setPosition(node.getX(),t/c);
				}
			}

			//�ۤ���
			detanglingNodes(nodes,yInterval);

			//progress����
			endNode += nodes.size();
			setProgress(100*endNode/(drawNodes.size()*3));
			if(isCancelled()){ end(); return null; }
		}


		//����Ρ��ɤ���������衦���ܸ�ʿ��������
		for(int i=0;i<depthNode.size();++i){

			ArrayList<StateNode> nodes = depthNode.get(i);

			//ʿ�Ѿ�����
			for(StateNode node : nodes){
				int c = 0;
				double t = 0;
				for(StateNode to : node.getToNodes()){
					if(to.depth-1==node.depth){
						++c;
						t += to.getY();
					}
				}
				for(StateNode from : node.getFromNodes()){
					if(from.depth+1==node.depth){
						++c;
						t += from.getY();
					}
				}
				if(c!=0){
					node.setPosition(node.getX(),t/c);
				}
			}

			//�ۤ���
			detanglingNodes(nodes,yInterval);

			//progress����
			endNode += nodes.size();
			setProgress(100*endNode/(drawNodes.size()*3));
			if(isCancelled()){ end(); return null; }
		}

		if(frame!=null) frame.end();
		end();
		return null;
	}

//	�ۤ���
	private void detanglingNodes(ArrayList<StateNode> nodes,double yInterval){

		//�����Ȥ���
		ArrayList<StateNode> sortNodes = new ArrayList<StateNode>(nodes);
		Collections.sort(sortNodes, new NodeYComparator());

		ArrayList<StateNode> revsortNodes = new ArrayList<StateNode>(sortNodes);
		Collections.reverse(revsortNodes);

		//��Υ��0�ΥΡ��ɤ�ۤ���
		while(true){
			boolean res = zeroDetanlingNodes(sortNodes,revsortNodes,yInterval);
			if(!res||isCancelled()) break;
		}

		//��ޤ��ʴֳֶ���
		double minY=Double.MAX_VALUE,maxY=Double.MIN_VALUE;
		for(StateNode node: nodes){
			minY = Math.min(minY,node.getY());
			maxY = Math.max(maxY,node.getY());
		}
		double par = (yInterval*nodes.size())/(maxY-minY);
		if((maxY-minY)>1&&par>1){
			double p = (maxY+minY)/2;
			for(StateNode node: nodes){
				node.setPosition(node.getX(),((node.getY()-p)*par)+p);
			}
		}

		//�Ρ�����дֳֶ���
		for(int counter=0;counter<100;++counter){
			boolean res = nonzeroDetanlingNodes(sortNodes,revsortNodes,yInterval);
			if(!res||isCancelled()) break;
		}
	}

	private boolean zeroDetanlingNodes(ArrayList<StateNode> sortNodes,ArrayList<StateNode> revsortNodes,double yInterval){

		//��Υ��0�ΥΡ��ɤ򸫤Ĥ���
		double zoreY = 0;
		HashSet<StateNode> zeroDistances = new HashSet<StateNode>();

		for(int i=0;i<sortNodes.size()-1;++i){
			StateNode n1 = sortNodes.get(i);
			StateNode n2 = sortNodes.get(i+1);
			double distance = Math.abs(n1.getY()-n2.getY());

			if(distance==0){
				if(zeroDistances.size()==0){
					zoreY = n2.getY();
					zeroDistances.add(n1);
					zeroDistances.add(n2);
				}else if(zoreY==n2.getY()){
					zeroDistances.add(n2);
				}
			}else{
				if(zeroDistances.size()>0){ break; }
			}
		}


		//��Υ��0�ΥΡ��ɤ�̵���ʤä��齪λ
		if(zeroDistances.size()==0){ return false; }


		//�Ǿ���Υ�Σ��ĤΥΡ��ɤΰ�ư���ΰ�׻��ν���
		double dy;    //��ư��
		double pivot; //�濴��
		double moveMaxY; //��ư�ΰ���
		double moveMinY; //��ư�ΰ貼��

		for(StateNode n : sortNodes){ n.unmark(); }

		dy = yInterval*(zeroDistances.size()-1)/2;
		pivot = zoreY;

		moveMaxY = zoreY+dy+yInterval*3;
		moveMinY = zoreY-dy-yInterval*3;

		//no�ǥ����Ȥ���
		List<StateNode> list = new ArrayList<StateNode>(zeroDistances);
		Collections.sort(list, new Comparator<StateNode>() {
			public int compare(StateNode n1, StateNode n2) {
				if(n1.id<n2.id){
					return -1;
				}else if(n1.id>n2.id){
					return 1;
				}else{
					return 0;
				}
			}
		});

		double y = pivot-dy;
		for(StateNode n : list){
			n.setPosition(n.getX(),y);
			n.mark();
			y += yInterval;
		}


		//��ư�ΰ��׻����ʤ���Ρ��ɰ�ư
		for(StateNode n : sortNodes){
			if(n.isMarked()) continue;
			if(n.getY()<pivot) continue;
			if(moveMaxY<n.getY()) break;
			moveMaxY += yInterval;
			n.setPosition(n.getX(), n.getY()+dy);
			n.mark();
		}
		for(StateNode n : revsortNodes){
			if(n.isMarked()) continue;
			if(n.getY()>pivot) continue;
			if(moveMinY>n.getY()) break;
			moveMinY -= yInterval;
			n.setPosition(n.getX(), n.getY()-dy);
			n.mark();
		}

		return true;
	}

	private boolean nonzeroDetanlingNodes(ArrayList<StateNode> sortNodes,ArrayList<StateNode> revsortNodes,double yInterval){

		//�Ǿ���Υ�Σ��ĤΥΡ��ɤ򸫤Ĥ���
		StateNode move1=null,move2=null;
		double distanceMin = Double.MAX_VALUE;

		for(int i=0;i<sortNodes.size()-1;++i){
			StateNode n1 = sortNodes.get(i);
			StateNode n2 = sortNodes.get(i+1);
			double distance = Math.abs(n1.getY()-n2.getY());

			if(distance<distanceMin){
				distanceMin = distance;
				move1 = n1;
				move2 = n2;
			}
		}

		//�Ťʤ��Τ�̵���ʤä��齪λ
		if(distanceMin>yInterval){ return false; }

		//�Ǿ���Υ�Σ��ĤΥΡ��ɤΰ�ư���ΰ�׻��ν���
		double dy;    //��ư��
		double pivot; //�濴��
		double moveMaxY; //��ư�ΰ���
		double moveMinY; //��ư�ΰ貼��

		for(StateNode n : sortNodes){ n.unmark(); }

		dy = yInterval*(2-1)/2;
		pivot = (move1.getY()+move2.getY())/2;

		if(move1.getY()>=pivot){
			//move1>=move2
			moveMaxY = move1.getY()+dy+yInterval;
			moveMinY = move2.getY()-dy-yInterval;
			move1.setPosition(move1.getX(), move1.getY()+dy);
			move2.setPosition(move2.getX(), move2.getY()-dy);
			move1.mark();
			move2.mark();
		}else{
			//move1<move2
			moveMaxY = move2.getY()+dy+yInterval;
			moveMinY = move1.getY()-dy-yInterval;
			move2.setPosition(move2.getX(), move2.getY()+dy);
			move1.setPosition(move1.getX(), move1.getY()-dy);
			move2.mark();
			move1.mark();
		}


		//��ư�ΰ��׻����ʤ���Ρ��ɰ�ư
		for(StateNode n : sortNodes){
			if(n.isMarked()) continue;
			if(n.getY()<pivot) continue;
			if(moveMaxY<n.getY()) break;
			moveMaxY += yInterval;
			n.setPosition(n.getX(), n.getY()+dy);
			n.mark();
		}
		for(StateNode n : revsortNodes){
			if(n.isMarked()) continue;
			if(n.getY()>pivot) continue;
			if(moveMinY>n.getY()) break;
			moveMinY -= yInterval;
			n.setPosition(n.getX(), n.getY()-dy);
			n.mark();
		}

		return true;
	}


	private boolean innerDetanlingNodes(ArrayList<StateNode> sortNodes,ArrayList<StateNode> revsortNodes,double yInterval){

		//�Ǿ���Υ�Σ��ĤΥΡ��ɤ򸫤Ĥ���
		StateNode move1=null,move2=null;
		double distanceMin = Double.MAX_VALUE;
		double zoreY = 0;
		HashSet<StateNode> zeroDistances = new HashSet<StateNode>();

		/*
		for(StateNode n1 : sortNodes){
			for(StateNode n2 : sortNodes){
				if(n1.no==n2.no){ break; }
				//if(n1.no<=n2.no){ continue; }
				double distance = Math.abs(n1.getY()-n2.getY());

				if(distance==0){
					distanceMin = 0;
					if(zeroDistances.size()==0){
						zoreY = n1.getY();
						zeroDistances.add(n1);
						zeroDistances.add(n2);
					}else if(zoreY==n1.getY()){
						zeroDistances.add(n1);
						zeroDistances.add(n2);
					}
				}else if(distance<distanceMin){
					distanceMin = distance;
					move1 = n1;
					move2 = n2;
				}
			}
		}
		*/

		for(int i=0;i<sortNodes.size()-1;++i){
			StateNode n1 = sortNodes.get(i);
			StateNode n2 = sortNodes.get(i+1);
			double distance = Math.abs(n1.getY()-n2.getY());
			if(zeroDistances.size()>0&&distance>0){ break; }

			if(distance==0){
				distanceMin = 0;
				if(zeroDistances.size()==0){
					zoreY = n2.getY();
					zeroDistances.add(n1);
					zeroDistances.add(n2);
				}else if(zoreY==n2.getY()){
					zeroDistances.add(n2);
				}
			}else if(distance<distanceMin){
				distanceMin = distance;
				move1 = n1;
				move2 = n2;
			}
		}

		//�Ťʤ��Τ�̵���ʤä��齪λ
		if(distanceMin>yInterval){ return false; }

		//�Ǿ���Υ�Σ��ĤΥΡ��ɤΰ�ư���ΰ�׻��ν���
		double dy;    //��ư��
		double pivot; //�濴��
		double moveMaxY; //��ư�ΰ���
		double moveMinY; //��ư�ΰ貼��

		for(StateNode n : sortNodes){ n.unmark(); }

		if(zeroDistances.size()==0){

			dy = yInterval*(2-1)/2;
			pivot = (move1.getY()+move2.getY())/2;

			if(move1.getY()>=pivot){
				//move1>=move2
				moveMaxY = move1.getY()+dy+yInterval;
				moveMinY = move2.getY()-dy-yInterval;
				move1.setPosition(move1.getX(), move1.getY()+dy);
				move2.setPosition(move2.getX(), move2.getY()-dy);
				move1.mark();
				move2.mark();
			}else{
				//move1<move2
				moveMaxY = move2.getY()+dy+yInterval;
				moveMinY = move1.getY()-dy-yInterval;
				move2.setPosition(move2.getX(), move2.getY()+dy);
				move1.setPosition(move1.getX(), move1.getY()-dy);
				move2.mark();
				move1.mark();
			}

		}else{

			dy = yInterval*(zeroDistances.size()-1)/2;
			pivot = zoreY;

			moveMaxY = zoreY+dy+yInterval*3;
			moveMinY = zoreY-dy-yInterval*3;

			//no�ǥ����Ȥ���
			List<StateNode> list = new ArrayList<StateNode>(zeroDistances);
			Collections.sort(list, new Comparator<StateNode>() {
				public int compare(StateNode n1, StateNode n2) {
					if(n1.id<n2.id){
						return -1;
					}else if(n1.id>n2.id){
						return 1;
					}else{
						return 0;
					}
				}
			});

			double y = pivot-dy;
			for(StateNode n : list){
				n.setPosition(n.getX(),y);
				n.mark();
				y += yInterval;
			}
		}

		//��ư�ΰ��׻����ʤ���Ρ��ɰ�ư
		for(StateNode n : sortNodes){
			if(n.isMarked()) continue;
			if(n.getY()<pivot) continue;
			if(moveMaxY<n.getY()) break;
			moveMaxY += yInterval;
			n.setPosition(n.getX(), n.getY()+dy);
			n.mark();
		}
		for(StateNode n : revsortNodes){
			if(n.isMarked()) continue;
			if(n.getY()>pivot) continue;
			if(moveMinY>n.getY()) break;
			moveMinY -= yInterval;
			n.setPosition(n.getX(), n.getY()-dy);
			n.mark();
		}
		/*
		boolean hit=true;
		while(hit){
			hit = false;
			for(StateNode n : nodes){
				if(n.isMarked()) continue;
				if(pivot<=n.getY()&&n.getY()<=moveMaxY){
					moveMaxY += yInterval;
					n.setPosition(n.getX(), n.getY()+dy);
					n.mark();
					//nowMaxY = Math.max(nowMaxY,n.getY());
					hit = true;
				}else if(moveMinY<=n.getY()&&n.getY()<pivot){
					moveMinY -= yInterval;
					n.setPosition(n.getX(), n.getY()-dy);
					n.mark();
					//nowMinY = Math.min(nowMinY,n.getY());
					hit = true;
				}
			}
		}
		*/

		return true;
	}



	/*
	private ArrayList<StateNode> getHitNodes(double yInterval,ArrayList<StateNode> noes, ArrayList<StateNode> hits){
		ArrayList<StateNode> newhits = new ArrayList<StateNode>();
		for(StateNode n : noes){
			for(StateNode h : hits){
				if(Math.abs(n.getY()-h.getY())<=yInterval){
					newhits.add(n);
				}
			}
		}
		//�������ҥåȤ����Ρ��ɤ�����С��Ƶ�Ū�ˤ���˿������ҥåȤ���Ρ��ɤ�õ��
		if(newhits.size()>0){
			noes.removeAll(newhits);
			hits.addAll(newhits);
			newhits.addAll(getHitNodes(yInterval,noes,newhits));
		}
		return newhits;
	}

	private StateNode getMinYNode(ArrayList<StateNode> nodes){
		StateNode min = null;
		double minY= Double.MAX_VALUE;
		for(StateNode node : nodes){
			if(node.getY()<minY){
				minY = node.getY();
				min = node;
			}
		}
		return min;
	}
	*/


	private class ProgressFrame extends JDialog implements PropertyChangeListener,ActionListener {
		private JPanel panel;
		private JProgressBar bar;
		private JButton cancel;

		private ProgressFrame(){
			panel = new JPanel();

			bar = new JProgressBar(0,100);
			bar.setStringPainted(true);
			panel.add(bar);

			cancel = new JButton(Lang.d[2]);
			cancel.addActionListener(this);
			panel.add(cancel);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Adjust Reset");
			setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			setAlwaysOnTop(true);
			setResizable(false);

	        pack();
	        setLocationRelativeTo(panel);
	        addWindowListener(new ChildWindowListener(this));
	        setVisible(true);
		}

		public void end(){
			bar.setValue(100);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress".equals(evt.getPropertyName())) {
				bar.setValue((Integer)evt.getNewValue());
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==cancel){
				if(!isDone()){
					cancel(false);
				}
			}
		}
	}


}
