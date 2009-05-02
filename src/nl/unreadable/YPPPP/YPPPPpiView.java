package nl.unreadable.YPPPP;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import nl.unreadable.YPPPP.model.YPPPPPirate;


public class YPPPPpiView extends JFrame{
	public static final long serialVersionUID = 9L;
	
	private JTextField nameTxt;
	private JLabel nameLab;
	private JTable pirateTable;
	private Hashtable<String, Integer[]> pirateData;
	private String[] columnNames = {"Name", "Gunning", "Bilge", "Sailing", "Rigging", "Carpentry", "Swordfighting", "Rumble", "DNav", "BNav", "TH"};	
	private JButton piEnterBut, piCopyBut, piDelBut, piClearBut, piQuitBut, piDisableBut;
	private JComboBox oceanChoice;
	
	private Clipboard systemClipboard;
	
	private Hashtable<String,Integer> statToInt;
	private Hashtable<Integer,String> intToStat;
	Pattern statPattern = Pattern.compile("</b>.*/<b>");
	Pattern oceanStatPattern = Pattern.compile("ocean-wide&nbsp;<b>");
	Matcher tempMatch;
	private BufferedReader in;
	private String line;
	
	public String ocean = "cobalt";
	
	public YPPPPpiView()
	{
		JFrame.setDefaultLookAndFeelDecorated(true);	
	    try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
	      } catch (Exception e) {
	        System.out.println("Special look failed to load! No pretties for you :(");
	      }
		this.setTitle("Pirate Informer of YPPPP: Yohoho Puzzle Pirate Pillage Program");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(365,400);
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		// Global box
		Container content = this.getContentPane();
		JPanel allBox = new JPanel();
		allBox.setLayout(new BoxLayout(allBox, BoxLayout.PAGE_AXIS));
		
		// Name box where names are entered
		JPanel nameBox = new JPanel();
		nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.LINE_AXIS));
		nameLab = new JLabel("Pirate Name:"); nameBox.add(nameLab);
		nameTxt = new JTextField("name"); nameTxt.addKeyListener(new KeyAdapter() {public void keyPressed(KeyEvent evt) {int key = evt.getKeyCode(); if (key == KeyEvent.VK_ENTER) addPirate();}}); nameBox.add(nameTxt);
		piEnterBut = new JButton("Enter"); piEnterBut.addActionListener(new EnterHandler()); nameBox.add(piEnterBut);
		allBox.add(nameBox);
		
		// Table to show all the data
		pirateData = new Hashtable<String,Integer[]>();
		pirateTable = new JTable(new HashTableModel());
		JScrollPane scrollPane = new JScrollPane(pirateTable);
		pirateTable.setFillsViewportHeight(true);
		//TableCellRenderer renderer = new CustomTableCellRenderer();
		TableCellRenderer head = new iconHeaderRenderer();
		TableCellRenderer cell = new statTableCellRenderer();
		
		int cnt = 0;
		for (Enumeration<TableColumn> e = pirateTable.getTableHeader().getColumnModel().getColumns(); e.hasMoreElements();){
			TableColumn col = e.nextElement();
			col.setHeaderRenderer(head);
			col.setCellRenderer(cell);
			col.setPreferredWidth(10);
			switch (cnt){
			case 0: col.setHeaderValue(new TextOrIcon("Name", null)); col.setPreferredWidth(100); break;
			case 1:	col.setHeaderValue(new TextOrIcon("Gun", new ImageIcon("icons/gun.png"))); break;
			case 2: col.setHeaderValue(new TextOrIcon("Bilge", new ImageIcon("icons/bilge.png"))); break;
			case 3: col.setHeaderValue(new TextOrIcon("Sail", new ImageIcon("icons/sail.png"))); break;
			case 4: col.setHeaderValue(new TextOrIcon("Rig", new ImageIcon("icons/rig.png"))); break;
			case 5: col.setHeaderValue(new TextOrIcon("Carp", new ImageIcon("icons/carp.png"))); break;
			case 6: col.setHeaderValue(new TextOrIcon("SF", new ImageIcon("icons/sf.png"))); break;
			case 7: col.setHeaderValue(new TextOrIcon("Rumble", new ImageIcon("icons/rumble.png"))); break;
			case 8: col.setHeaderValue(new TextOrIcon("Dnav", new ImageIcon("icons/dnav.png"))); break;
			case 9: col.setHeaderValue(new TextOrIcon("Bnav", new ImageIcon("icons/bnav.png"))); break;
			case 10: col.setHeaderValue(new TextOrIcon("TH", new ImageIcon("icons/th.png"))); break;
			}
			cnt++;
		}		
		

		allBox.add(scrollPane);
		
		// Button box for few other options
		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
		//piCopyBut = new JButton("Copy"); buttonBox.add(piCopyBut);
		piDelBut = new JButton("Delete"); piDelBut.addActionListener(new ClearHandler()); buttonBox.add(piDelBut);
		piClearBut = new JButton("Clear All"); piClearBut.addActionListener(new ClearAllHandler()); buttonBox.add(piClearBut);
		piDisableBut = new JButton("Disable"); piDisableBut.addActionListener(new DisableHandler()); buttonBox.add(piDisableBut);
		piQuitBut = new JButton("Exit"); piQuitBut.addActionListener(new ExitHandler()); buttonBox.add(piQuitBut);
		String[] oceans = {"midnight","cobalt","viridian","sage","hunter","opal","malachite","ice"}; oceanChoice = new JComboBox(oceans); oceanChoice.addActionListener(new OceanChangeHandler(oceanChoice)); buttonBox.add(oceanChoice);		
		allBox.add(buttonBox);
		
		content.add(allBox);
		statToInt = new Hashtable<String,Integer>();
		statToInt.put("Able", 0); statToInt.put("Distinguished", 1); statToInt.put("Respected", 2); statToInt.put("Master", 3); statToInt.put("Renowned", 4); statToInt.put("Grand-Master", 5); statToInt.put("Legendary", 6); statToInt.put("Ultimate", 7);
		intToStat = new Hashtable<Integer,String>();
		intToStat.put(0, "Able"); intToStat.put(1, "Distinguished"); intToStat.put(2, "Respected"); intToStat.put(3, "Master"); intToStat.put(4, "Renowned"); intToStat.put(5, "Grand-Master"); intToStat.put(6, "Legendary"); intToStat.put(7, "Ultimate");
	}
	
	private void addPirate(){
		//System.out.println(nameTxt.getText());
		YPPPPPirate p = new YPPPPPirate();
		p = getPirateInfo(nameTxt.getText());
		Integer[] test = {p.getGunning(), p.getBilge(), p.getSailing(), p.getRigging(), p.getCarpentry(), p.getSF(), p.getRumble(), p.getDNav(), p.getBNav(), p.getTH()};
		//pirateData.put("Btza", test);
		pirateData.put(nameTxt.getText(), test);
		//((HashTableModel) pirateTable.getModel()).fireTableRowsInserted(pirateTable.getModel().getRowCount()-1, pirateTable.getModel().getRowCount());
		((HashTableModel) pirateTable.getModel()).fireTableDataChanged();
	}
	
	private YPPPPPirate getPirateInfo(String name)
	{
		try{
			YPPPPPirate p = new YPPPPPirate(name);
			URL url = new URL("http://" + ocean + ".puzzlepirates.com/yoweb/pirate.wm?target=" + name);
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			//in = new BufferedReader(new FileReader("btza.txt"));
			line = in.readLine();
			p.setSF(readStatLine("Swordfighting", "Bilging",p));
			p.setBilge(readStatLine("Bilging","Sailing",p));
			p.setSailing(readStatLine("Sailing","Rigging",p));
			p.setRigging(readStatLine("Rigging","Navigating",p));
			p.setDNav(readStatLine("Navigating","Battle Navigation",p));
			p.setBNav(readStatLine("Battle Navigation","Gunning",p));
			p.setGunning(readStatLine("Gunning","Carpentry",p));
			p.setCarpentry(readStatLine("Carpentry","Rumble",p));
			p.setRumble(readStatLine("Rumble","Treasure Haul", p));
			p.setTH(readStatLine("Treasure Haul","Spades",p));
			in.close();
			return p;
			
		}catch(Exception e){
			return new YPPPPPirate();
		}
	}
	int readStatLine(String stat, String nextStat, YPPPPPirate p) throws Exception{
		// read till we are really at stat
		while (!line.contains("alt=\"" + stat + "\"></a></td>") && (line = in.readLine()) != null && !line.contains("alt=\"" + stat + "\"></a></td>")){}
		while((line=in.readLine()) != null && !line.contains("/")){}
		int stand = readStat(line);
		// read till we get ocean-wide or we are at stat
		while ((line = in.readLine()) != null && !line.contains("ocean-wide") && !line.contains("alt=\"" + nextStat + "\"></a></td>")){}
		return stand;
	}
	int readStat(String line){
		tempMatch = statPattern.matcher(line);
		if (!tempMatch.find()) return 0;
		return statToInt.get(line.substring(tempMatch.end(), line.length()-4));
	}
	int readOceanStat(String line){
		tempMatch = oceanStatPattern.matcher(line);
		if (!tempMatch.find()) return 0;
		return statToInt.get(line.substring(tempMatch.end(), line.length()-12));
	}

	class CopyHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){systemClipboard.setContents(new StringSelection(""), null);}
	}
	class ExitHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){System.exit(0);}
	}
	class EnterHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){addPirate();}
	}
	class ClearHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){clear();}
	}
	class ClearAllHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){clearAll();}
	}
	class DisableHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){Disable();}
	}
	class OceanChangeHandler implements ActionListener{
		private JComboBox combobox;
		OceanChangeHandler(JComboBox box){combobox = box; }
		public void actionPerformed(ActionEvent e){ocean = (String) combobox.getSelectedItem();}
	}
	public void Disable(){
		this.setEnabled(false);
		this.setVisible(false);
	}
	public void clear(){
		int index = pirateTable.getSelectedRow();
		pirateData.remove(pirateTable.getValueAt(index,0));
		((HashTableModel) pirateTable.getModel()).fireTableDataChanged();
	}
	public void clearAll(){
		pirateData.clear();
		((HashTableModel) pirateTable.getModel()).fireTableDataChanged();
	}
	public void Update()
	{
	}

	class HashTableModel extends AbstractTableModel {
		public static final long serialVersionUID = 9L;
		//private Hashtable<String, String[]> data; 
		
		public int getRowCount(){
			return pirateData.size();
		}
		public int getColumnCount(){
			return columnNames.length;
		}
		public Object getValueAt(int row, int column){
			int cnt = 0;
			Vector<String> keys = new Vector<String>(pirateData.keySet());
			Collections.sort(keys);
			for (Enumeration<String> e = keys.elements(); e.hasMoreElements();){
				Object el = e.nextElement();
				if (cnt == row){
					if (column == 0) // name
						return el;
					Integer [] dat = pirateData.get(el);
					return dat[column-1];
				}
				cnt++;
			}
			// empty object	
			return new Object();
		}
	}
	public class statTableCellRenderer extends DefaultTableCellRenderer 
	{
		public static final long serialVersionUID = 9L;
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if( value instanceof Integer ){
				Integer val = (Integer) value;
				switch (val){
				case 0: cell.setBackground(Color.WHITE); break;
				case 1: cell.setBackground(Color.GRAY); break;
				case 2: cell.setBackground(Color.CYAN); break;
				case 3:	cell.setBackground(Color.BLUE); break;
				case 4:	cell.setBackground(Color.GREEN); break;
				case 5:	cell.setBackground(Color.YELLOW); break;
				case 6: cell.setBackground(Color.ORANGE); break;
				case 7: cell.setBackground(Color.RED); break;
				}
			}
			else
				cell.setBackground(Color.WHITE);
			return cell;
		}
	}
    // This class is used to hold the text and icon values
    // used by the renderer that renders both text and icons
	class TextOrIcon {
    	TextOrIcon(String text, Icon icon) {
        	this.text = text;
            this.icon = icon;
        }
        String text;
        Icon icon;
	}
	
    // This customized renderer can render objects of the type TextandIcon
	public class iconHeaderRenderer extends DefaultTableCellRenderer {
		public static final long serialVersionUID = 9L;
		public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
            // Inherit the colors and font from the header component
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}
			if (value instanceof TextOrIcon) {
				Icon temp = ((TextOrIcon)value).icon;
				setIcon(temp);
				if (temp != null)
					setText("");
				else
					setText(((TextOrIcon)value).text);
			} else {
				setText((value == null) ? "" : value.toString());
				setIcon(null);
			}
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(JLabel.CENTER);
			return this;
		}
	};
	class HashtableChanged implements TableModelListener{
		public void tableChanged(TableModelEvent e){}
	}
}
