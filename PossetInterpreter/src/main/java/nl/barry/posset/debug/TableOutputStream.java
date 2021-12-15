package nl.barry.posset.debug;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class TableOutputStream extends OutputStream {

	/**
	 * both starting at 1
	 */
	private int row = 1;
	private int col = 1;

	private Table<Integer, Integer, String> output;

	private char sepChar = ';';

	public TableOutputStream() {
		output = HashBasedTable.create();
	}

	public TableOutputStream(char separatorCharacter) {
		this();
		sepChar = separatorCharacter;
	}

	@Override
	public void write(int b) throws IOException {

		if (b == '\n') {
			row++;
			col = 1;
		} else if (b == sepChar) {
			col++;
		} else {
			String val = output.get(row, col);

			if (val == null) {
				val = Character.toString((char)b);
			} else {
				val = val + Character.toString((char)b);
			}
			output.put(row, col, val);
		}
	}
	
	
	public Table<Integer, Integer, String> getTable()
	{
		return output;
	}
}
