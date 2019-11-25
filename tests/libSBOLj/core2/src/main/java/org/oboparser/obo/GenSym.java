package org.oboparser.obo;

class GenSym {
	
	public static void main(String[] args) { 
		GenSym s = new GenSym("test");
		for(int i = 0; i < 100; i++) { 
			System.out.println(s.nextSymbol());
		}
	}

	private String prefix; 
	private long id;
	private int digits; 
	private String fmt;
	
	public GenSym(String pref) { 
		prefix = pref; 
		id = 0;
		setDigits(9);
	}
	
	public void setDigits(int d) { 
		digits = d;
		fmt = String.format("%%s%%0%dd", digits); 
	}
	
	public String nextSymbol() {
		String sym = String.format(fmt, prefix, id++);
		return sym;
	}
}
