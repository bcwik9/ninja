
package ninja;

import java.util.ArrayList;

public class Symbol {
	String symbol;
	ArrayList<Integer> months;
	ArrayList<Integer> years;
	ArrayList<OptionChain> chains;

	public Symbol(String symbl, ArrayList<Integer> mnths, ArrayList<Integer> yrs) {
		chains = new ArrayList<OptionChain>();
		months = mnths;
		symbol = symbl;
		years = yrs;
		for (int i = 0; i < mnths.size(); i++) {
			chains.add(new OptionChain(mnths.get(i), years.get(i), symbol));
		}
	}
	
}
