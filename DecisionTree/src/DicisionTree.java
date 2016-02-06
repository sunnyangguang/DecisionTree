
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class DicisionTree {

	public static void main(String[] args) throws Exception {
//		String[] attrNames = new String[] { "XB", "XC", "XD","XE","XF","XG","XH","XI","XJ","XK","XL","XM","XN",
//				"XO","XP","XQ","XR","XS","XT","XU"};
		
		String[] attrNames = new String[] {"<!!!2years","missedPayments","defaulted"};
		
		//String[] attrNames = new String[] { "AGE", "INCOME", "STUDENT","CREDIT_RATING" };

		// è¯»å�–æ ·æœ¬é›†
		Map<Object, List<Sample>> samples = readSamples(attrNames);

		// ç”Ÿæˆ�å†³ç­–æ ‘
		Object decisionTree = generateDecisionTree(samples, attrNames);

		// è¾“å‡ºå†³ç­–æ ‘
		outputDecisionTree(decisionTree, 0, null);
	}

	/**
	 * è¯»å�–å·²åˆ†ç±»çš„æ ·æœ¬é›†ï¼Œè¿”å›žMapï¼šåˆ†ç±» -> å±žäºŽè¯¥åˆ†ç±»çš„æ ·æœ¬çš„åˆ—è¡¨
	 */
	static Map<Object, List<Sample>> readSamples(String[] attrNames) {
		
		//String fileName = "training_set.csv";
		String fileName = "carModel.csv";
		File file = new File(fileName);
		
		
		// get the total rows of the csv file (not including the attributes row)
		int rows = 0;
		try {
			rows = getRows(fileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("Total rows of CVS file is : " + rows);
		
		
		Object[][] rawData = new Object[rows][];
		// Set each row as an array and put into the two dimensional array
		int curRow = 0;
		
		try {
			Scanner inputStream = new Scanner(file);
			inputStream.nextLine();

			while(inputStream.hasNext()){
				String data = inputStream.next();
				String[] values = data.split(",");
				rawData[curRow] = values;
				
				System.out.println(data + "***");
				curRow++;
				System.out.println(curRow);
				
			}
			inputStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Matrix :");
		System.out.println(Arrays.deepToString(rawData));
		
		// æ ·æœ¬å±žæ€§å�Šå…¶æ‰€å±žåˆ†ç±»ï¼ˆæ•°ç»„ä¸­çš„æœ€å�Žä¸€ä¸ªå…ƒç´ ä¸ºæ ·æœ¬æ‰€å±žåˆ†ç±»ï¼‰


		// è¯»å�–æ ·æœ¬å±žæ€§å�Šå…¶æ‰€å±žåˆ†ç±»ï¼Œæž„é€ è¡¨ç¤ºæ ·æœ¬çš„Sampleå¯¹è±¡ï¼Œå¹¶æŒ‰åˆ†ç±»åˆ’åˆ†æ ·æœ¬é›†
		Map<Object, List<Sample>> ret = new HashMap<Object, List<Sample>>();
		for (Object[] row : rawData) {
			Sample sample = new Sample();
			int i = 0;
			for (int n = row.length - 1; i < n; i++)
				sample.setAttribute(attrNames[i], row[i]);
			sample.setCategory(row[i]);
			List<Sample> samples = ret.get(row[i]);
			if (samples == null) {
				samples = new LinkedList<Sample>();
				ret.put(row[i], samples);
			}
			samples.add(sample);
		}

		return ret;
	}
	// Get the rows of a CSV file
	public static int getRows(String str) throws IOException{
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new FileReader(new File(str)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lnr.skip(Long.MAX_VALUE);
		// Finally, the LineNumberReader object should be closed to prevent resource leak
		lnr.close();
		return lnr.getLineNumber();
	}
	
	/**
	 * æž„é€ å†³ç­–æ ‘
	 */
	static Object generateDecisionTree(
			Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {

		// å¦‚æžœå�ªæœ‰ä¸€ä¸ªæ ·æœ¬ï¼Œå°†è¯¥æ ·æœ¬æ‰€å±žåˆ†ç±»ä½œä¸ºæ–°æ ·æœ¬çš„åˆ†ç±»
		if (categoryToSamples.size() == 1)
			return categoryToSamples.keySet().iterator().next();

		// å¦‚æžœæ²¡æœ‰ä¾›å†³ç­–çš„å±žæ€§ï¼Œåˆ™å°†æ ·æœ¬é›†ä¸­å…·æœ‰æœ€å¤šæ ·æœ¬çš„åˆ†ç±»ä½œä¸ºæ–°æ ·æœ¬çš„åˆ†ç±»ï¼Œå�³æŠ•ç¥¨é€‰ä¸¾å‡ºåˆ†ç±»
		if (attrNames.length == 0) {
			int max = 0;
			Object maxCategory = null;
			for (Entry<Object, List<Sample>> entry : categoryToSamples
					.entrySet()) {
				int cur = entry.getValue().size();
				if (cur > max) {
					max = cur;
					maxCategory = entry.getKey();
				}
			}
			return maxCategory;
		}

		// é€‰å�–æµ‹è¯•å±žæ€§
		Object[] rst = chooseBestTestAttribute(categoryToSamples, attrNames);

		// å†³ç­–æ ‘æ ¹ç»“ç‚¹ï¼Œåˆ†æ”¯å±žæ€§ä¸ºé€‰å�–çš„æµ‹è¯•å±žæ€§
		Tree tree = new Tree(attrNames[(Integer) rst[0]]);

		// å·²ç”¨è¿‡çš„æµ‹è¯•å±žæ€§ä¸�åº”å†�æ¬¡è¢«é€‰ä¸ºæµ‹è¯•å±žæ€§
		String[] subA = new String[attrNames.length - 1];
		for (int i = 0, j = 0; i < attrNames.length; i++)
			if (i != (Integer) rst[0])
				subA[j++] = attrNames[i];

		// æ ¹æ�®åˆ†æ”¯å±žæ€§ç”Ÿæˆ�åˆ†æ”¯
		@SuppressWarnings("unchecked")
		Map<Object, Map<Object, List<Sample>>> splits =
		/* NEW LINE */(Map<Object, Map<Object, List<Sample>>>) rst[2];
		for (Entry<Object, Map<Object, List<Sample>>> entry : splits.entrySet()) {
			Object attrValue = entry.getKey();
			Map<Object, List<Sample>> split = entry.getValue();
			Object child = generateDecisionTree(split, subA);
			tree.setChild(attrValue, child);
		}

		return tree;
	}

	/**
	 * é€‰å�–æœ€ä¼˜æµ‹è¯•å±žæ€§ã€‚æœ€ä¼˜æ˜¯æŒ‡å¦‚æžœæ ¹æ�®é€‰å�–çš„æµ‹è¯•å±žæ€§åˆ†æ”¯ï¼Œåˆ™ä»Žå�„åˆ†æ”¯ç¡®å®šæ–°æ ·æœ¬
	 * çš„åˆ†ç±»éœ€è¦�çš„ä¿¡æ�¯é‡�ä¹‹å’Œæœ€å°�ï¼Œè¿™ç­‰ä»·äºŽç¡®å®šæ–°æ ·æœ¬çš„æµ‹è¯•å±žæ€§èŽ·å¾—çš„ä¿¡æ�¯å¢žç›Šæœ€å¤§
	 * è¿”å›žæ•°ç»„ï¼šé€‰å�–çš„å±žæ€§ä¸‹æ ‡ã€�ä¿¡æ�¯é‡�ä¹‹å’Œã€�Map(å±žæ€§å€¼->(åˆ†ç±»->æ ·æœ¬åˆ—è¡¨))
	 */
	static Object[] chooseBestTestAttribute(
			Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {

		int minIndex = -1; // æœ€ä¼˜å±žæ€§ä¸‹æ ‡
		double minValue = Double.MAX_VALUE; // æœ€å°�ä¿¡æ�¯é‡�
		Map<Object, Map<Object, List<Sample>>> minSplits = null; // æœ€ä¼˜åˆ†æ”¯æ–¹æ¡ˆ

		// å¯¹æ¯�ä¸€ä¸ªå±žæ€§ï¼Œè®¡ç®—å°†å…¶ä½œä¸ºæµ‹è¯•å±žæ€§çš„æƒ…å†µä¸‹åœ¨å�„åˆ†æ”¯ç¡®å®šæ–°æ ·æœ¬çš„åˆ†ç±»éœ€è¦�çš„ä¿¡æ�¯é‡�ä¹‹å’Œï¼Œé€‰å�–æœ€å°�ä¸ºæœ€ä¼˜
		for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {
			int allCount = 0; // ç»Ÿè®¡æ ·æœ¬æ€»æ•°çš„è®¡æ•°å™¨

			// æŒ‰å½“å‰�å±žæ€§æž„å»ºMapï¼šå±žæ€§å€¼->(åˆ†ç±»->æ ·æœ¬åˆ—è¡¨)
			Map<Object, Map<Object, List<Sample>>> curSplits =
			/* NEW LINE */new HashMap<Object, Map<Object, List<Sample>>>();
			for (Entry<Object, List<Sample>> entry : categoryToSamples
					.entrySet()) {
				Object category = entry.getKey();
				List<Sample> samples = entry.getValue();
				for (Sample sample : samples) {
					Object attrValue = sample
							.getAttribute(attrNames[attrIndex]);
					Map<Object, List<Sample>> split = curSplits.get(attrValue);
					if (split == null) {
						split = new HashMap<Object, List<Sample>>();
						curSplits.put(attrValue, split);
					}
					List<Sample> splitSamples = split.get(category);
					if (splitSamples == null) {
						splitSamples = new LinkedList<Sample>();
						split.put(category, splitSamples);
					}
					splitSamples.add(sample);
				}
				allCount += samples.size();
			}

			// è®¡ç®—å°†å½“å‰�å±žæ€§ä½œä¸ºæµ‹è¯•å±žæ€§çš„æƒ…å†µä¸‹åœ¨å�„åˆ†æ”¯ç¡®å®šæ–°æ ·æœ¬çš„åˆ†ç±»éœ€è¦�çš„ä¿¡æ�¯é‡�ä¹‹å’Œ
			double curValue = 0.0; // è®¡æ•°å™¨ï¼šç´¯åŠ å�„åˆ†æ”¯
			for (Map<Object, List<Sample>> splits : curSplits.values()) {
				double perSplitCount = 0;
				for (List<Sample> list : splits.values())
					perSplitCount += list.size(); // ç´¯è®¡å½“å‰�åˆ†æ”¯æ ·æœ¬æ•°
				double perSplitValue = 0.0; // è®¡æ•°å™¨ï¼šå½“å‰�åˆ†æ”¯
				for (List<Sample> list : splits.values()) {
					double p = list.size() / perSplitCount;
					perSplitValue -= p * (Math.log(p) / Math.log(2));
				}
				curValue += (perSplitCount / allCount) * perSplitValue;
			}

			// é€‰å�–æœ€å°�ä¸ºæœ€ä¼˜
			if (minValue > curValue) {
				minIndex = attrIndex;
				minValue = curValue;
				minSplits = curSplits;
			}
		}

		return new Object[] { minIndex, minValue, minSplits };
	}

	/**
	 * å°†å†³ç­–æ ‘è¾“å‡ºåˆ°æ ‡å‡†è¾“å‡º
	 */
	static void outputDecisionTree(Object obj, int level, Object from) {
		for (int i = 0; i < level; i++)
			System.out.print("|-----");
		if (from != null)
			System.out.printf("(%s):", from);
		if (obj instanceof Tree) {
			Tree tree = (Tree) obj;
			String attrName = tree.getAttribute();
			System.out.printf("[%s = ?]\n", attrName);
			for (Object attrValue : tree.getAttributeValues()) {
				Object child = tree.getChild(attrValue);
				outputDecisionTree(child, level + 1, attrName + " = "
						+ attrValue);
			}
		} else {
			System.out.printf("[CATEGORY = %s]\n", obj);
		}
	}

	/**
	 * æ ·æœ¬ï¼ŒåŒ…å�«å¤šä¸ªå±žæ€§å’Œä¸€ä¸ªæŒ‡æ˜Žæ ·æœ¬æ‰€å±žåˆ†ç±»çš„åˆ†ç±»å€¼
	 */
	static class Sample {

		private Map<String, Object> attributes = new HashMap<String, Object>();

		private Object category;

		public Object getAttribute(String name) {
			return attributes.get(name);
		}

		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}

		public Object getCategory() {
			return category;
		}

		public void setCategory(Object category) {
			this.category = category;
		}

		public String toString() {
			return attributes.toString();
		}

	}

	/**
	 * å†³ç­–æ ‘ï¼ˆé�žå�¶ç»“ç‚¹ï¼‰ï¼Œå†³ç­–æ ‘ä¸­çš„æ¯�ä¸ªé�žå�¶ç»“ç‚¹éƒ½å¼•å¯¼äº†ä¸€æ£µå†³ç­–æ ‘
	 * æ¯�ä¸ªé�žå�¶ç»“ç‚¹åŒ…å�«ä¸€ä¸ªåˆ†æ”¯å±žæ€§å’Œå¤šä¸ªåˆ†æ”¯ï¼Œåˆ†æ”¯å±žæ€§çš„æ¯�ä¸ªå€¼å¯¹åº”ä¸€ä¸ªåˆ†æ”¯ï¼Œè¯¥åˆ†æ”¯å¼•å¯¼äº†ä¸€æ£µå­�å†³ç­–æ ‘
	 */
	static class Tree {

		private String attribute;

		private Map<Object, Object> children = new HashMap<Object, Object>();

		public Tree(String attribute) {
			this.attribute = attribute;
		}

		public String getAttribute() {
			return attribute;
		}

		public Object getChild(Object attrValue) {
			return children.get(attrValue);
		}

		public void setChild(Object attrValue, Object child) {
			children.put(attrValue, child);
		}

		public Set<Object> getAttributeValues() {
			return children.keySet();
		}

	}

}
