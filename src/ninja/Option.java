package ninja;

public class Option {
	float last;
	float netChange;
	int volume;
	int openInterest;
	float strikePrice;
	double impliedVolatility;
	
	//temp
	public Option(){
	}
	
	public Option(float last, float netChange, int volume, int openInterest, float strikePrice){
		this.last = last;
		this.netChange = netChange;
		this.volume = volume;
		this.openInterest = openInterest;
		this.strikePrice = strikePrice;
		impliedVolatility = -1;
	}
	
	public String toString(){
		return "Option=   Last: " + last + " Net Change: " + netChange + " Volume: " + volume + " Open Interest: " + openInterest + " Strike Price: " + strikePrice;
	}
}
