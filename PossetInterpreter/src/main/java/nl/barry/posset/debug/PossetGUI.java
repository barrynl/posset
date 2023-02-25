package nl.barry.posset.debug;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import nl.barry.posset.Interpreter;
import nl.barry.posset.runtime.CollapsedVariablesPosset;
import nl.barry.posset.runtime.DerivedPosset;
import nl.barry.posset.runtime.DerivedPossy;
import nl.barry.posset.runtime.Element;
import nl.barry.posset.runtime.Posset;
import nl.barry.posset.runtime.Possy;
import nl.barry.posset.runtime.PrimePossy;

public class PossetGUI extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("main");
	private JTree tree = new JTree(rootNode);
	private Interpreter possetInterpreter = new Interpreter();
	private Posset mainPosset;

	public PossetGUI(InputStream source) throws Exception {
		super(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		JScrollPane jsp = new JScrollPane(tree);
		tabbedPane.addTab("Possies", jsp);
		tabbedPane.addTab("Posset", new JPanel());

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private Icon possetIcon = UIManager.getIcon("FileView.hardDriveIcon");

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;

				String text = ((DefaultMutableTreeNode) value).getChildCount() + ": " + this.getText();

				Object userObject = n.getUserObject();
				if (userObject instanceof Posset)
					setIcon(possetIcon);

				if (!(userObject instanceof String)) {
					Element p = (Element) userObject;
					CollapsedVariablesPosset posset = (CollapsedVariablesPosset) p.getGeneratedByPosset();

					Object userObject2 = ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) value).getParent())
							.getUserObject();

					DerivedPosset parentPosset = null;
					String name = "";

					if (!(userObject2 instanceof String)) {
						if (userObject2 instanceof Possy)
							parentPosset = (DerivedPosset) ((Possy) userObject2).getGeneratedByPosset();
						else if (userObject instanceof Posset)
							parentPosset = (DerivedPosset) ((DerivedPosset) userObject2).getGeneratedByPosset();

						if (parentPosset != null) {

							for (String na : parentPosset.getChildNames()) {
								if (parentPosset.getChildPosset(na).equals(posset)) {
									name = na;
									break;
								}
							}
						}
					}
					text = ((DefaultMutableTreeNode) value).getChildCount()
							+ (name.isEmpty() ? "" : "(" + posset.getName() + " " + name + ")");
				}

				if (userObject instanceof PrimePossy) {
					text = text + ": " + ((PrimePossy) userObject).getId();
				}

				setText(text);

				return c;
			}
		});

		possetInterpreter.loadSourceFile(source);
		possetInterpreter.prepareExecution();
		mainPosset = possetInterpreter.getMainPosset();
	}

	public static void main(String[] args) throws Exception {

		if (args.length <= 0 || args.length > 1) {
			System.out.println("PossetGUI should be started with a source file.");
			System.exit(1);
		}
		PossetGUI temp = new PossetGUI(new FileInputStream(args[0]));
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setContentPane(temp);
		frame.pack();
		Thread updater = new Thread(temp.new CustomThread());
		updater.start();
		frame.setVisible(true);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	public class CustomThread implements Runnable {
		@Override
		public void run() {

			Iterator<? extends Element> iter = mainPosset.iterator();

			System.out.println("Has elems: " + iter.hasNext());

			while (iter.hasNext()) {
				Element elem = iter.next();
				updateTree(elem, (DefaultMutableTreeNode) tree.getModel().getRoot());
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void updateTree(final Element nodeToAdd, DefaultMutableTreeNode parent) {

			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			DefaultMutableTreeNode child = convertToTreeNode(nodeToAdd);

			model.insertNodeInto(child, parent, parent.getChildCount());
//			tree.scrollPathToVisible(new TreePath(child.getPath()));
		}

		public DefaultMutableTreeNode convertToTreeNode(Element elem) {
			var node = new DefaultMutableTreeNode(elem);
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			if (elem instanceof Possy) {
				if (elem instanceof DerivedPossy) {
					DerivedPossy dp = (DerivedPossy) elem;

					for (Element e : dp.getSubPossies()) {
						model.insertNodeInto(convertToTreeNode(e), node, node.getChildCount());
					}
				}
			} else if (elem instanceof Posset) {
				Posset p = (Posset) elem;
				Iterator<? extends Element> iter = p.iterator();
				while (iter.hasNext()) {
					Element e = iter.next();
					updateTree(e, node);
				}
			}
			return node;
		}

	}

}
