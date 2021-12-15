package nl.barry.posset;

import java.io.PrintWriter;

import nl.barry.posset.debug.TableOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Table;

public class TableOutputStreamTest {

	@Test
	public void test() {
		
		TableOutputStream os = new TableOutputStream();
		
		PrintWriter pw = new PrintWriter(os);
		
		pw.print("Hello;this;is;my;name\nand;this;is;my;age");
		pw.close();
		
		Table<Integer, Integer, String> t = os.getTable();
		
		Assert.assertEquals("Hello", t.get(1, 1));
		Assert.assertEquals("this", t.get(1, 2));
		Assert.assertEquals("is", t.get(1,3));
		Assert.assertEquals("my", t.get(1,4));
		Assert.assertEquals("name", t.get(1, 5));
		
		Assert.assertEquals("and", t.get(2, 1));
		Assert.assertEquals("this", t.get(2, 2));
		Assert.assertEquals("is", t.get(2,3));
		Assert.assertEquals("my", t.get(2, 4));
		Assert.assertEquals("age", t.get(2, 5));
		
	}

}
