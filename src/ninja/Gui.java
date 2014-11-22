package ninja;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.swing.*;

public class Gui extends JFrame {
	private static final long serialVersionUID = 1L;
	JTextArea console;
	SymbolController mainCont;
	
	public Gui() {
		mainCont = new SymbolController();
		console = new JTextArea();
		
    	//Components
		JFrame frame = new JFrame("Ninja");
		JPanel main = createPanel(500,500);
		JPanel buttons = createPanel(150,500);
		JPanel comboBox = createPanel(65, 32);
        JScrollPane text = new JScrollPane(console);
        JButton showSources = new JButton("Show Sources");
        JButton addSource = new JButton("Add Source");
        JButton removeSource = new JButton("Remove Source");
        JButton refreshAll = new JButton("Refresh All");
        ArrayList<String> symbols = getSymbolList();
        JComboBox getData = new JComboBox(symbols.toArray());
        JLabel manage = new JLabel("Manage List:");
        JLabel research = new JLabel("Refresh Data:");
        
        //Configure components
        frame.setSize(700,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(main);
        frame.setLayout(new BorderLayout());
        
        main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));
        
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        
        getData.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        console.setEditable(false);
        
        //Action Events
        showSources.addActionListener(new ActionListener() {    
        	public void actionPerformed(ActionEvent e) {        
        		try {
					FileReader in = new FileReader(SymbolController.sourceList);
					BufferedReader reader = new BufferedReader(in);
					clearText();
					String currLine;
					while((currLine = reader.readLine()) != null) {
						println(currLine);
					}
					reader.close();
					in.close();
				} 
        		catch (FileNotFoundException e1) {}
				catch (IOException e2){}
        	}
        });
        
        addSource.addActionListener(new ActionListener() {    
        	public void actionPerformed(ActionEvent e) {        
        		try {
        			FileReader in = new FileReader(SymbolController.sourceList);
					BufferedReader reader = new BufferedReader(in);
					ArrayList<String> sources = new ArrayList<String>();
					String currLine;
					while ((currLine = reader.readLine()) != null) {
						sources.add(currLine);
					}
					reader.close();
					in.close();
					FileWriter out = new FileWriter(SymbolController.sourceList);
					BufferedWriter writer = new BufferedWriter(out);
					for(String i: sources) {
						writer.write(i + "\n");
					}
					String input = JOptionPane.showInputDialog("Enter source to add: ");
					if(input != null){
						input = input.toUpperCase();
						writer.write(input + "\n");
					}
					writer.close();
					out.close();
				} 
        		catch (FileNotFoundException e1) {}
				catch (IOException e2){}
				
        	}
        });
        
        removeSource.addActionListener(new ActionListener() {    
        	public void actionPerformed(ActionEvent e) {        
        		try {
        			FileReader in = new FileReader(SymbolController.sourceList);
					BufferedReader reader = new BufferedReader(in);
					ArrayList<String> sources = new ArrayList<String>();
					String currLine;
					while ((currLine = reader.readLine()) != null) {
						sources.add(currLine);
					}
					reader.close();
					in.close();
					String input = JOptionPane.showInputDialog("Enter source to remove: ");
					if(input != null) {
						FileWriter out = new FileWriter(SymbolController.sourceList);
						BufferedWriter writer = new BufferedWriter(out);
						for(String i: sources) {
							if(input.compareTo(i) != 0) {
								writer.write(i + "\n");
							}
						}
						writer.close();
						out.close();
					}
				} 
        		catch (FileNotFoundException e1) {}
				catch (IOException e2){}
        	}
        });
        
        getData.addActionListener(new ActionListener() {    
        	public void actionPerformed(ActionEvent e) {
        		ArrayList<Integer> months = new ArrayList<Integer>();
        		ArrayList<Integer> years = new ArrayList<Integer>();
        		String currLine;
        		JComboBox comboBox = (JComboBox)e.getSource();
                String symbol = (String)comboBox.getSelectedItem();
                SymbolController.downloadPage("http://research.scottrade.com/public/stocks/options/options.asp?symbol=" + symbol, symbol + ".txt");
				try {
					FileReader in = new FileReader(symbol + ".txt");
					BufferedReader reader = new BufferedReader(in);
					while((currLine = reader.readLine()) != null){
						if(currLine.contains("expMonth")){
							break;
						}
					}
					months.add((int)SymbolController.stringToNumbers(currLine, 0));
					years.add((int)SymbolController.stringToNumbers(currLine, 1));
					months.add((int)SymbolController.stringToNumbers(currLine, 3));
					years.add((int)SymbolController.stringToNumbers(currLine, 4));
					months.add((int)SymbolController.stringToNumbers(currLine, 6));
					years.add((int)SymbolController.stringToNumbers(currLine, 7));
					months.add((int)SymbolController.stringToNumbers(currLine, 9));
					years.add((int)SymbolController.stringToNumbers(currLine, 10));
					Symbol temp = new Symbol(symbol, months, years);
					mainCont.symbols.add(temp);
				}
				catch (FileNotFoundException e1) {}
				catch (IOException e2) {}
				clearText();
				println("Refreshed / Loaded data for " + symbol);
        	}
        });
        
        refreshAll.addActionListener(new ActionListener() {    
        	public void actionPerformed(ActionEvent e) {
        		ArrayList<String> symbols = getSymbolList();
        		clearText();
        		println("Refreshed / Loaded data for:");
        		for(String symbol: symbols){
        			ArrayList<Integer> months = new ArrayList<Integer>();
            		ArrayList<Integer> years = new ArrayList<Integer>();
            		String currLine;
                    SymbolController.downloadPage("http://research.scottrade.com/public/stocks/options/options.asp?symbol=" + symbol, symbol + ".txt");
    				try {
    					FileReader in = new FileReader(symbol + ".txt");
    					BufferedReader reader = new BufferedReader(in);
    					while((currLine = reader.readLine()) != null){
    						if(currLine.contains("expMonth")){
    							break;
    						}
    					}
    					months.add((int)SymbolController.stringToNumbers(currLine, 0));
    					years.add((int)SymbolController.stringToNumbers(currLine, 1));
    					months.add((int)SymbolController.stringToNumbers(currLine, 3));
    					years.add((int)SymbolController.stringToNumbers(currLine, 4));
    					months.add((int)SymbolController.stringToNumbers(currLine, 6));
    					years.add((int)SymbolController.stringToNumbers(currLine, 7));
    					months.add((int)SymbolController.stringToNumbers(currLine, 9));
    					years.add((int)SymbolController.stringToNumbers(currLine, 10));
    					Symbol temp = new Symbol(symbol, months, years);
    					mainCont.symbols.add(temp);
    					println(symbol);
    				}
    				catch (FileNotFoundException e1) {}
    				catch (IOException e2) {}
        		}
        	}
        });
        
        //Add components to main
        comboBox.add(getData);
        buttons.add(research);
        buttons.add(comboBox);
        buttons.add(refreshAll);
        buttons.add(manage);
        buttons.add(showSources);
        buttons.add(addSource);
        buttons.add(removeSource);
        main.add(buttons);
        main.add(text);
    }
	
	public JPanel createPanel(int width, int height){
		JPanel temp = new JPanel();
		temp.setMaximumSize(new Dimension(width,height));
		temp.setMinimumSize(new Dimension(width,height));
		temp.setPreferredSize(new Dimension(width,height));
        return temp;
	}
	
	public void println(String in) {
		console.append(in + "\n");
	}
	
	public void clearText(){
		console.setText(""); 
	}
	
	public ArrayList<String> getSymbolList() {
		ArrayList<String> ret = new ArrayList<String>();
		FileReader fr;
		try {
			fr = new FileReader(SymbolController.sourceList);
			BufferedReader reader = new BufferedReader(fr);
			String currLine;
			while ((currLine = reader.readLine()) != null) {
				ret.add(currLine);
			}
			reader.close();
			fr.close();
		} 
		catch (FileNotFoundException e) {} 
		catch (IOException e) {}
		return ret;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	Gui gui = new Gui();
		    	//temp
		    	SymbolController.yield = (float) .04;
		    	OptionChain chain = new OptionChain();
		    	chain.last = 10;
		    	chain.daysTillExpiration = 2;
		    	Option op = new Option();
		    	op.strikePrice = (float) 9.8;
		    	op.last = 1;
		    	chain.calls.add(op);
		    	chain.calculateImpliedVolatility();
		    }
		});
	}
	
}
