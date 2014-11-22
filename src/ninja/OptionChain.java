package ninja;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class OptionChain {
	ArrayList<Option> calls;
	ArrayList<Option> puts;
	float last;
	float change;
	float percentChange;
	int volume;
	float strikeMode;
	int numDontFitStrike;
	int daysTillExpiration;
	int month;
	int year;
	
	//temp
	public OptionChain(){
		calls = new ArrayList<Option>();
		puts = new ArrayList<Option>();
	}
	
	public OptionChain(int month, int year, String symbol){
		this.month = month;
		daysTillExpiration = SymbolController.calculateExpiration(month,year);
		calls = new ArrayList<Option>();
		puts = new ArrayList<Option>();
		float last;
		float nc;
		int vol;
		int opInt;
		float strike;
		try {
			String sourceURL = "http://research.scottrade.com/public/stocks/options/options.asp?strikeorexp=x&callorput=B&showmoney=A&expMonth=" + month +"&expYear="+ SymbolController.getCurrentDate().get(2) + "&type=T&symbol=" + symbol; 
			SymbolController.downloadPage(sourceURL, SymbolController.tempPageSource);
			FileReader fr = new FileReader(SymbolController.tempPageSource);
			BufferedReader reader = new BufferedReader(fr);
			String currLine;
			while ((currLine = reader.readLine()) != null){
				if(currLine.contains("inMoney") || currLine.contains("nearMoney") || currLine.contains("outMoney")){
					reader.readLine();
					reader.readLine();
					reader.readLine();
					last = SymbolController.stringToNumbers(reader.readLine(),0);
					nc = SymbolController.stringToNumbers(reader.readLine(),0);
					reader.readLine();
					vol = (int) SymbolController.stringToNumbers(reader.readLine(),0);
					opInt = (int) SymbolController.stringToNumbers(reader.readLine(),0);
					reader.readLine();
					reader.readLine();
					strike = SymbolController.stringToNumbers(reader.readLine(),7);
					calls.add(new Option(last,nc,vol,opInt,strike));
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					last = SymbolController.stringToNumbers(reader.readLine(),0);
					nc = SymbolController.stringToNumbers(reader.readLine(),0);
					reader.readLine();
					vol = (int) SymbolController.stringToNumbers(reader.readLine(),0);
					opInt = (int) SymbolController.stringToNumbers(reader.readLine(),0);
					puts.add( new Option(last,nc,vol,opInt,strike));
				} else if(currLine.contains("Industry:")) {
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					this.last = SymbolController.stringToNumbers(reader.readLine(),0);
					reader.readLine();
					currLine = reader.readLine();
					if(currLine.contains("noChangeArrow")){
						this.change = SymbolController.stringToNumbers(currLine,0);
					} else {
						this.change = SymbolController.stringToNumbers(currLine,3);
					}
					reader.readLine();
					reader.readLine();
					this.percentChange = SymbolController.stringToNumbers(reader.readLine(),1);
					this.volume = (int) SymbolController.stringToNumbers(reader.readLine(),1);
				}
			}
		} 
		catch (FileNotFoundException e) {} 
		catch (IOException e) {}
		filterResults();
		findStrikeMode();
		calculateImpliedVolatility();
	}
	
	public void calculateImpliedVolatility(){
		for(Option op: calls){
			int itTimes = 0;
			double curVolatility = 1.5;
			double lastVolatility = -1;
			double lastLarger = 3;
			double lastSmaller = 0;
			double curBS = -1;
			double lastBS = -1;
			double curDiff = -1;
			double bestDiff = 9999999;
			double bestVolatility = -1;
			double temp;
			
			
			/*BigDecimal curVol = new BigDecimal(1.5);
			BigDecimal lastVol = null;
			BigDecimal smaller = new BigDecimal(0);
			BigDecimal larger = new BigDecimal(3);
			while(op.last != curBS && itTimes < 100){
				lastVol = curVol;
				lastBS = curBS;
				curBS = SymbolController.calculateBS2(op, this, curVol).doubleValue();
				if(op.last > curBS){
					smaller = new BigDecimal(curVol.doubleValue());
					curVol = larger.add(curVol).divide(new BigDecimal(2));
				} else if (op.last < curBS){
					larger = new BigDecimal(curVol.doubleValue());
					curVol = smaller.add(curVol).divide(new BigDecimal(2));
				}
				itTimes++;
				//System.out.println("Times: " + itTimes+ " Find: " + op.last + " Cur: " + curBS + " curVolatility:" + curVolatility);
			}
			op.impliedVolatility = curVolatility;
			System.out.println("Times: " + itTimes+ " Find: " + op.last + " Cur: " + curBS + " SameBS: " + (curBS==lastBS) + " SameVol: " + (lastVol.compareTo(curVol)==0)+ " curVolatility: " + curVol.toPlainString());
			
			*/
			
			//implementation
			while(itTimes < 100){
				lastVolatility = curVolatility;
				lastBS = curBS;
				//curBS = SymbolController.calculateBS2(op, this, new BigDecimal(curVolatility)).doubleValue();
				curBS = SymbolController.calculateBS(op, this, curVolatility);
				curDiff = op.last - curBS;
				if(op.last > curBS){
					lastSmaller = curVolatility;
					curVolatility = (lastLarger + curVolatility)/2;
				} else if (op.last < curBS){
					lastLarger = curVolatility;
					curVolatility = (lastSmaller + curVolatility)/2;
				}
				itTimes++;
				System.out.println("Times: " + itTimes+ " Find: " + op.last + " Cur: " + curBS + " curVolatility:" + curVolatility + " Upper: " + lastLarger + " Lower: " + lastSmaller);
			}
			op.impliedVolatility = curVolatility;
			System.out.println("Times: " + itTimes+ " Find: " + op.last + " Cur: " + curBS + " SameBS: " + (curBS==lastBS) + " SameVol: " + (lastVolatility==curVolatility)+ " curVolatility: " + curVolatility);
			
			
			//Implementation
			/*for(double i = .1; i < 2; i += .1){
				curDiff = op.last - SymbolController.calculateBS(op, this, i);
				curDiff = Math.abs(curDiff);
				if(curDiff < bestDiff){
					bestDiff = curDiff;
					bestVolatility = i;
				}
			}
			temp = bestVolatility + .1;
			double i;
			if(bestVolatility  == .1) {
				i = .01;
			} else {
				i = bestVolatility - .1;
			}
			while(i < temp){
				curDiff = op.last - SymbolController.calculateBS(op, this, i);
				curDiff = Math.abs(curDiff);
				if(curDiff < bestDiff){
					bestDiff = curDiff;
					bestVolatility = i;
				}
				i+= .01;
			}
			temp = bestVolatility + .01;
			if(bestVolatility == .01){
				i = .001;
			} else {
				i = bestVolatility - .01;
			}
			while(i < temp){
				curDiff = op.last - SymbolController.calculateBS(op, this, i);
				curDiff = Math.abs(curDiff);
				if(curDiff < bestDiff){
					bestDiff = curDiff;
					bestVolatility = i;
				}
				i+= .001;
			}
			op.impliedVolatility = bestVolatility;*/
		}
	}
	
	public void filterResults(){
		float prevLast = 9999999;
		int i =0;
		while(i < calls.size()) {
			Option curr = calls.get(i);
			if(curr.volume == 0 || prevLast < curr.last){
				calls.remove(i);
			} else {
				prevLast = curr.last;
				i++;
			}
		}
		i = 0;
		prevLast = -9999999;
		while(i < puts.size()) {
			Option curr = puts.get(i);
			if(curr.volume == 0 || prevLast > curr.last){
				puts.remove(i);
			} else {
				prevLast = curr.last;
				i++;
			}
		}
	}
	
	public void findStrikeMode() {
		if(calls.size() != 0){
			HashMap<Float, Integer> map = new HashMap<Float, Integer>();
			int timesFound, max = 0;
			float prevPrice = calls.get(0).strikePrice, diff, curr, maxKey = 0;
			for(int i = 1; i < calls.size(); i++){
				curr = calls.get(i).strikePrice;
				diff = curr - prevPrice;
				if(map.containsKey(diff)){
					timesFound = map.get(diff);
					timesFound++;
					map.put(diff, timesFound);
				} else {
					map.put(diff, 1);
				}
				prevPrice = curr;
			}
			Set<Float> set = map.keySet();
			for(Float i: set){
				timesFound = map.get(i);
				if(max < timesFound){
					max = timesFound;
					maxKey = i;
				}
			}
			strikeMode = maxKey;
			timesFound = 0;
			for(Float i: set){
				if(i != maxKey){
					timesFound = timesFound + map.get(i);
				}	
			}
			numDontFitStrike = timesFound;
		} else {
			strikeMode = -1;
			numDontFitStrike = -1;
		}
	}
	
}

