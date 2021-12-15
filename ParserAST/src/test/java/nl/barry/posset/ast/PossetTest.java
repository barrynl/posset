package nl.barry.posset.ast;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class PossetTest {

	private Posset p = null;
	private Node n1 = null;
	private Node n2 = null;
	private Node n3 = null;

	@Before
	public void setUp() throws Exception {
		this.p = new Posset("TestPosset", false);
		this.n1 = new Node("first", "A");
		this.n2 = new Node("second", null);
		this.n3 = new Node("first", null);

		p.addChild("AnotherTestPosset", this.n1);
		p.addChild("AnotherTestPosset", this.n2);
	}

	@Test
	public void testAddChild() {
		assertEquals(2, p.getChildNodes().size());
		assertEquals(new HashSet<Node>(Arrays.asList(this.n1, this.n2)), p.getChildNodes());
	}

	@Test
	public void testGetName() {
		assertEquals("TestPosset", this.p.getName());
	}

	@Test
	public void testGetChildNodes() {
		assertEquals(new HashSet<Node>(Arrays.asList(this.n1, this.n2)), p.getChildNodes());
	}

	@Test
	public void testGetChildType() {
		assertEquals("AnotherTestPosset", this.p.getChildType(this.n1));
		assertEquals("AnotherTestPosset", this.p.getChildType(this.n2));
		assertEquals(this.p.getChildType(n1), this.p.getChildType(this.n2));
	}

}
