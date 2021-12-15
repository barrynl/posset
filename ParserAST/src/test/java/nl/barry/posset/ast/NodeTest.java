package nl.barry.posset.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {

	private Node n1;
	private Node n2;
	private Node n3;
	private Node n4;

	@Before
	public void setUp() throws Exception {
		n1 = new Node("TestNode", "SomeVarName");
		n2 = new Node("TestNode2", null);
		n3 = new Node("TestNode", "SomeVarName");
		n4 = new Node("AnotherTestNode", null);
	}

	@Test
	public void testGetName() {
		assertEquals("TestNode", n1.getName());
	}

	@Test
	public void testGetVar() {
		assertEquals("SomeVarName", n1.getVar());
	}

	@Test
	public void testNotEquals() {
		assertNotEquals(n1, n2);
	}

	@Test
	public void testEqual() {
		assertEquals(n1, n1);
		assertEquals(n1, n3);
	}

	@Test
	public void testAddChild() {
		this.n2.addChild(n3);
		this.n2.addChild(n4);
		assertEquals(Arrays.asList(this.n3, this.n4), this.n2.getChildren());
	}

	@Test
	public void testCopyAndRenameVars() {
		n1.addChild(n2);
		n1.addChild(n4);
		n2.addChild(n3);
		String somePrefix = "Prefix-";
		Node copyNode = n1.copyAndRenameVars(somePrefix);

		assertEquals(copyNode.getVar(), somePrefix + n1.getVar());

		for (Node child : copyNode.getChildren()) {
			if (child.getName().equals("TestNode2")) {
				assertNull(child.getVar());

				for (Node child2 : child.getChildren()) {
					assertEquals(child2.getName(), "TestNode");
					assertEquals(child2.getVar(), somePrefix + n3.getVar());
				}
			} else if (child.getName().equals("AnotherTestNode")) {
				assertNull(child.getVar());
			}
		}

	}

	/**
	 * Merges the two trees by their names only! And additionally merges all
	 * instances of variables that ooccur at the same node.
	 */
	@Test
	public void testMergeTreeSuccess() {

		n1.addChild(n2);
		n3.addChild(n4);

		// check if the returned tree is the union.
		Node n = n1.merge(n3);

		assertEquals(n.getName(), n1.getName());
		assertEquals(n.getName(), n3.getName());

		assertEquals("SomeVarName", n.getVar());

		List<Node> children = n.getChildren();

		assertTrue(children.contains(n2));
		assertTrue(children.contains(n4));
	}

	@Test
	public void testMergeTreeFail() {
		n1.addChild(n2);
		n4.addChild(n3);

		Node n = n1.merge(n4);

		assertNull(n);
	}

	@Test
	public void testMergeTreeComplexSucccess() {

		// first tree
		Node n1 = new Node("1", null);
		// second level
		Node n1_1 = new Node("1.1", null);
		n1.addChild(n1_1);
		Node n1_2 = new Node("1.2", null);
		n1.addChild(n1_2);
		// third level
		Node n1_1_1 = new Node("1.1.1", null);
		n1_1.addChild(n1_1_1);
		// fourth level
		Node n1_1_1_1 = new Node("1.1.1.1", null);
		n1_1_1.addChild(n1_1_1_1);

		// second tree
		// first level
		Node n2 = new Node("1", null);
		// second level
		Node n2_2 = new Node("1.2", null);
		n2.addChild(n2_2);
		Node n2_3 = new Node("1.3", null);
		n2.addChild(n2_3);
		Node n2_3_1 = new Node("1.3.1", null);
		n2_3.addChild(n2_3_1);
		Node n2_3_1_1 = new Node("1.3.1.1", null);
		n2_3_1.addChild(n2_3_1_1);

		Node newNode = n1.merge(n2);

		assertNotNull(newNode);

		assertEquals("1", newNode.getName());

		String name, name2, name3;
		for (Node n : newNode.getChildren()) {
			name = n.getName();
			assertTrue(name.equals("1.1") || name.equals("1.2") || name.equals("1.3"));

			if (name.equals("1.1")) {
				for (Node another : n.getChildren()) {
					name2 = another.getName();
					assertEquals("1.1.1", name2);
					if (name2.equals("1.1.1")) {
						for (Node anotherOther : another.getChildren()) {
							name3 = anotherOther.getName();
							assertEquals("1.1.1.1", name3);

						}
					}
				}
			} else if (name.equals("1.3")) {

			}
		}
	}

	public void testMergeOverlappingVariables() {

	}

}
