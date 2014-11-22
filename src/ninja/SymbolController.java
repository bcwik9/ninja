package ninja;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SymbolController {
	static String sourceList = "sourceList.txt";
	static String tempPageSource = "tempPageSource.txt";
	static String yieldPage = "http://finance.yahoo.com/bonds/composite_bond_rates";
	static String cdfValues = "cdf.txt";
	static float yield;
	static ArrayList<Double> cdf;
	
	ArrayList<Symbol> symbols;
	
	
	public SymbolController(){
		symbols = new ArrayList<Symbol>();
		yield = -1;
		cdf = null;
	}
	
	public static void updateYield(){
		downloadPage(yieldPage, tempPageSource);
		FileReader fr;
		try {
			fr = new FileReader(tempPageSource);
			BufferedReader reader = new BufferedReader(fr);
			while(!reader.readLine().contains("6 Month"));
			yield = stringToNumbers(reader.readLine(),0);
		} 
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
	}
	
	public static void updateCDF(){
		ArrayList<Double> temp = new ArrayList<Double>();
		FileReader fr;
		String currLine;
		try {
			fr = new FileReader(cdfValues);
			BufferedReader reader = new BufferedReader(fr);
			while((currLine = reader.readLine())!=null){
				temp.add(Double.parseDouble(currLine));
			}
		} 
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		cdf = temp;
	}
	
	//-3.5 to 4, array holds 0-1500
	public static double calculateCDF(double in){
		if( cdf == null) {
			updateCDF();
		}
		if(in < -3.5){
			return 0;
		}
		if(in > 4){
			return 1;
		}
		int find = (int) (((in + 3.5)/7.5)*1500);
		return cdf.get(find);
	}
	
	public static BigDecimal calculateBS2(Option op, OptionChain chain, BigDecimal volatility){
		BigDecimal tempExpire = new BigDecimal((double) chain.daysTillExpiration/365);
		if(yield == -1){
			updateYield();
		}
		double temp = (double) chain.last/op.strikePrice;
		BigDecimal d1 = new BigDecimal(Math.log(temp));
		BigDecimal origVol = new BigDecimal(volatility.doubleValue());
		volatility = volatility.pow(2);
		volatility = volatility.divide(new BigDecimal(2));
		d1 = d1.add(tempExpire.multiply(volatility.add(new BigDecimal(yield))));
		BigDecimal d2 = origVol.multiply(new BigDecimal(Math.sqrt(tempExpire.doubleValue())));
		d1 = d1.divide(d2, BigDecimal.ROUND_DOWN);
		d2 = d1.subtract(d2);
		d1 = new BigDecimal(calculateCDF(d1.doubleValue()));
		d2 = new BigDecimal (calculateCDF(d2.doubleValue()));
		d1 = d1.multiply(new BigDecimal(chain.last));
		d2 = d2.multiply(new BigDecimal(op.strikePrice * Math.exp(tempExpire.multiply(new BigDecimal(-yield)).doubleValue())));
		return d1.subtract(d2);
	}
	
	public static double calculateBS(Option op, OptionChain chain, double volatility){
		double tempExpire = (double) chain.daysTillExpiration/365;
		if(yield == -1){
			updateYield();
		}
		double d1 = Math.log(chain.last/op.strikePrice);
		d1 = d1 + (tempExpire*(yield + (Math.pow(volatility, 2)/2)));
		double d2 = volatility * Math.sqrt(tempExpire);
		d1 = d1 / d2;
		d2 = d1 - d2;
		d1 = calculateCDF(d1);
		d2 = calculateCDF(d2);
		d1 = chain.last*d1;
		return d1 - (op.strikePrice * Math.exp(-yield*tempExpire) * d2);
	}
	
	public static double calculateBS(double strikePrice, double stockPrice, double daysTillExpire, double volatility){
		double tempExpire = (double) daysTillExpire/365;
		if(yield == -1){
			updateYield();
		}
		double d1 = Math.log(stockPrice/strikePrice);
		d1 = d1 + (tempExpire*(yield + (Math.pow(volatility, 2)/2)));
		double d2 = volatility * Math.sqrt(tempExpire);
		d1 = d1 / d2;
		d2 = d1 - d2;
		d1 = calculateCDF(d1);
		d2 = calculateCDF(d2);
		d1 = stockPrice*d1;
		return d1 - (strikePrice * Math.exp(-yield*tempExpire) * d2);
	}
	
	public static void downloadPage(String site, String file){
		try {
			URL url = new URL(site);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			FileWriter out = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(out);
			String inputLine;
			while((inputLine = reader.readLine())!=null){
				writer.write(inputLine + "\r\n");
			}
			writer.close();
			out.close();
		} 
		catch (MalformedURLException e){}
		catch (IOException e1){}
	}
	
	public static float stringToNumbers(String str, int skip) {   
		boolean lastWasNum= false;
		StringBuffer strBuff = new StringBuffer();
		str = str.replaceAll(",", "");
		char c;
		int count = 0;
		for (int i = 0; i < str.length() ; i++) {      
			c = str.charAt(i);
			if (Character.isDigit(c) || c == '.' || c == '-') {      
				if(count == skip){
					strBuff.append(c);
				}
				lastWasNum = true;
			} else {
				if(lastWasNum){
					count++;
					lastWasNum = false;
				}
			}
			if(count > skip){
				break;
			}
		}
		return Float.parseFloat(strBuff.toString());
	}
	
	public static ArrayList<Integer> getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
        String date = formatter.format(new Date());
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(Integer.parseInt(date.substring(0, 2)));
        temp.add(Integer.parseInt(date.substring(3, 5)));
        temp.add(Integer.parseInt(date.substring(6, 10)));
        return temp;
	}
	
	public static int calculateExpiration(int month, int year) {
		SimpleDateFormat formatter = new SimpleDateFormat("E");
        String curDayWeek = formatter.format(new Date());
        int curDayOfWeek = 0;
        if(curDayWeek.compareTo("Sun") == 0){
        	curDayOfWeek = 1;
        } else if(curDayWeek.compareTo("Mon") == 0){
        	curDayOfWeek = 2;
        } else if(curDayWeek.compareTo("Tue") == 0){
        	curDayOfWeek = 3;
        } else if(curDayWeek.compareTo("Wed") == 0){
        	curDayOfWeek = 4;
        } else if(curDayWeek.compareTo("Thu") == 0){
        	curDayOfWeek = 5;
        } else if(curDayWeek.compareTo("Fri") == 0){
        	curDayOfWeek = 6;
        } else if(curDayWeek.compareTo("Sat") == 0){
        	curDayOfWeek = 7;
        }
        ArrayList<Integer> tempDate = getCurrentDate();
		int curDay = tempDate.get(0);
		int curMonth = tempDate.get(1);
		int days = 0;
		switch(curDayOfWeek){
		case(Calendar.SATURDAY): 
			days = 20;
			break;
		case(Calendar.SUNDAY): 
			days= 19;
			break;
		case(Calendar.MONDAY): 
			days= 18;
			break;
		case(Calendar.TUESDAY): 
			days = 17;
			break;
		case(Calendar.WEDNESDAY): 
			days=16;
			break;
		case(Calendar.THURSDAY): 
			days= 15;
			break;
		case(Calendar.FRIDAY): 
			days=14;
			break;
		}
		boolean subtractedDays = false;
		while(curMonth != month){
			switch(curMonth){
			case(1):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(2):
				if(!subtractedDays){
					days += 28-curDay;
					subtractedDays = true;
				} else {
					days += 28;
				}
				break;
			case(3):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(4):
				if(!subtractedDays){
					days += 30-curDay;
					subtractedDays = true;
				} else {
					days += 30;
				}
				break;
			case(5):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(6):
				if(!subtractedDays){
					days += 30-curDay;
					subtractedDays = true;
				} else {
					days += 30;
				}
				break;
			case(7):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(8):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(9):
				if(!subtractedDays){
					days += 30-curDay;
					subtractedDays = true;
				} else {
					days += 30;
				}
				break;
			case(10):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			case(11):
				if(!subtractedDays){
					days += 30-curDay;
					subtractedDays = true;
				} else {
					days += 30;
				}
				break;
			case(12):
				if(!subtractedDays){
					days += 31-curDay;
					subtractedDays = true;
				} else {
					days += 31;
				}
				break;
			}
			if(curMonth != 12){
				curMonth++;
			} else {
				curMonth = 1;
			}
		}
		if(month == curMonth){
			return days = days + 1 - curDay;
		} else {
			return days;
		}
	}
	
	public static double calculatePDF(double in){
		double partOne = 1/Math.sqrt(2*Math.PI);
		double partTwo = -Math.pow(in, 2);
		partTwo = Math.exp(partTwo/2);
		return partOne * partTwo;
	}
	
}
