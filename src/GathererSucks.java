


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import java.util.*;
import java.lang.StringBuilder;


import org.w3c.dom.Document; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Node; 

import org.htmlcleaner.*;


public class GathererSucks {
	private XPathFactory xPathfactory;
	private XPath xpath;
	private JsonWriterFactory writerFactory;
	
	private LinkedHashMap<String,String> manaMap;
	private LinkedHashMap<String,String> rarityMap;
	private LinkedHashMap<String,String> errorMap;

	private static final boolean DEBUG = true;
	
	private boolean isFlip = false;
	private boolean isLeft = false;
	private boolean isCreature = false;
	private boolean isPlaneswalker = false;
	private boolean hasManaCost = true;
	private boolean hasFlavorText = true;
	private boolean hasImage = true;
	private boolean hasVotes = true;
	private boolean hasRating = true;
	private boolean hasCMC = true;
	private boolean hasSupertype = true;
	private boolean hasSubtype = true;
	private boolean hasCardText = true;
	private boolean hasArtist = true;
	private boolean hasRarity = true;
	private boolean hasLoyPowTou = true;
	private boolean hasNumber = true;
	
	public GathererSucks(){
		xPathfactory = XPathFactory.newInstance(); 
		xpath = xPathfactory.newXPath();

		LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>(1);
	    properties.put(JsonGenerator.PRETTY_PRINTING, true);
	    writerFactory = Json.createWriterFactory(properties);

		manaMap = new LinkedHashMap<String,String> ();
		manaMap.put("White","W");
		manaMap.put("Blue","U");
		manaMap.put("Black","B");
		manaMap.put("Red","R");
		manaMap.put("Green","G");

		manaMap.put("Phyrexian White","WP");
		manaMap.put("Phyrexian Blue","UP");
		manaMap.put("Phyrexian Black","BP");
		manaMap.put("Phyrexian Red","RP");
		manaMap.put("Phyrexian Green","GP");

		manaMap.put("White or Blue","(W//U)");
		manaMap.put("Blue or Black","(U//B)");
		manaMap.put("Black or Red","(B//R)");
		manaMap.put("Red or Green","(R//G)");
		manaMap.put("Green or White","(G//W)");

		manaMap.put("White or Black","(W//B)");
		manaMap.put("Blue or Red","(U//R)");
		manaMap.put("Black or Green","(B//G)");
		manaMap.put("Red or White","(R//W)");
		manaMap.put("Green or Blue","(G//U)");

		rarityMap = new LinkedHashMap<String,String> ();
		rarityMap.put("M", "Mythic Rare");
		rarityMap.put("R", "Rare");
		rarityMap.put("U", "Uncommon");
		rarityMap.put("C", "Common");
		rarityMap.put("L", "Basic Land");
		rarityMap.put("S", "Special");

		errorMap = new LinkedHashMap<String,String> ();
	}

	public static void main(String []argv){ 
		GathererSucks itsTrue = new GathererSucks();
		if(argv.length > 0){
			String setName  = argv[0];
			//String setName  = "Innistrad";
			//System.out.println(setName);  
			//debugPrintLn(setListURL);
			itsTrue.xPathSearchResults(setName);
		}else{
			System.err.println("We need a set name for this to work... argv: " + argv.length);
			System.err.println("Testing all sets");
			itsTrue.testAllSets();
		}
	
	} 

	private void testAllSets(){

		String setListURL = "http://gatherer.wizards.com/Pages/Default.aspx";  
		
		debugPrint("Acquiring Webpage...");	
		Document doc;
		try {
			doc = getADoc(setListURL);
			printSuccess(true,setListURL);
		} catch (ParserConfigurationException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("XPath error while trying to create the Magic Set DOM");
			debugPrintLn("Returning empty handed\n");
			return;
		} catch (IOException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("IO error while trying to retieve the Magic Set");
			debugPrintLn("Returning empty handed\n");
			return;
		}


		NodeList listOfSets = null;
		debugPrint("Acquiring Set List...");
		String regex = "//*[@id='ctl00_ctl00_MainContent_Content_SearchControls_setAddText']/option/text()";
		try{
			listOfSets = (NodeList) xpath.evaluate(regex, doc, XPathConstants.NODESET);
			printSuccess(true,""); 
		} catch (XPathExpressionException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("XPath error while trying to create the DOM for the  Magic Set List");
			debugPrintLn("Returning empty handed\n");
			return;
		} 
		
		for (int listIndex = 0; listIndex< listOfSets.getLength(); ++listIndex){
			String setName = listOfSets.item(listIndex).getNodeValue();
			debugPrintLn("Parsing: " + setName);
			debugPrintLn("");
			debugPrintLn("");
			debugPrintLn("");
			xPathSearchResults(setName);	
			debugPrintLn("");
			debugPrintLn("");
			debugPrintLn("");
			debugPrintLn("Parsing of " + setName + " Completed");


		}

		Iterator it = errorMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        debugPrintLn(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }


	}

	private void xPathSearchResults (String setName){

		String htmlSetName = setName.replaceAll(" ", "+");
		String setListURL = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=checklist&sort=cn+&set=%5b%22"+htmlSetName+"%22%5d";


		Document doc;
		try {
			doc = getADoc(setListURL);
			printSuccess(true,"");
		} catch (ParserConfigurationException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("XPath error while trying to create the Magic Set DOM");
			debugPrintLn("Returning empty handed\n");
			return;
		} catch (IOException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("IO error while trying to retieve the Magic Set");
			debugPrintLn("Returning empty handed\n");
			return;
		}

		debugPrint("Compling xPath..."); 
		XPathExpression expr;
		try {
			String theExpr = "//table[@class='checklist']/tbody/tr[td/a/@href]";
			expr = xpath.compile(theExpr);
			printSuccess(true,theExpr);
		} catch (XPathExpressionException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("XPath error while trying to complie the XPath for the Card List DOM");
			debugPrintLn("Returning empty handed\n");
			return;
		}

		debugPrint("Evaluating xPath..."); 
		NodeList nl;
		try {
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			printSuccess(true,"");
		} catch (XPathExpressionException e) {
			printSuccess(false,"");
			e.printStackTrace();
			debugPrintLn("XPath error while trying to execute the XPath for the Card List DOM");
			debugPrintLn("Returning empty handed\n");
			return;
		}

		debugPrintLn("Printing Cards\n"); 
		if(nl.getLength() > 0){
			JsonObjectBuilder theMagicSetBuilder = buildSetJson(nl,setName);
			JsonObject theMagicSet = theMagicSetBuilder.build();
			JsonWriter jsonWriterOut = writerFactory.createWriter(System.out);
	        jsonWriterOut.writeObject(theMagicSet);
	        jsonWriterOut.close();

		}else{
			debugPrintLn("Invalid Search Query: " + nl.getLength() + " Cards Found\n");
		}
	}



	private JsonObjectBuilder buildSetJson(NodeList nl, String setName ){
		resetFlags();
		
		JsonObjectBuilder theMagicSetBuilder = Json.createObjectBuilder();
		
		String idHeader = "ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
		String prevName = "";
		String prevNumber = "";
		String nextNumber = "";
		String errorNode = "";
		int successCount = 0;
		int cardCount = nl.getLength();
		debugPrintLn("Valid Search Query: " + cardCount + " Cards Found\n");
		for (int i = 0; i< cardCount; ++i){

			JsonObjectBuilder aCardBuilder = Json.createObjectBuilder();
			
			//Card Name and Number then check if it's already been processed previously
			String regex = "./td/a/text()";
			String name = "";
			errorNode = "Name";
			debugPrint("Locating "+errorNode+"...");
			try {
				name = (String) xpath.evaluate(regex,nl.item(i), XPathConstants.STRING);
				printSuccess(true, name);
			} catch (XPathExpressionException e) {
				printSuccess(false,"");
				e.printStackTrace();
				ErrorPrintSkip(name, errorNode);
				resetFlags();
				continue;
			}


			
			errorNode = "Prev Number";
			debugPrint("Locating "+errorNode+"...");
			printSuccess(true, prevNumber);

			String number = "";
			errorNode = "Number";
			debugPrint("Locating "+errorNode+"...");
			try {
				number = getCheckListInfo("number",nl.item(i));
				number = number.trim();
				if(number.isEmpty()){
					hasNumber = false;
				}
				printSuccess(true, number);
			} catch (XPathExpressionException e) {
				printSuccess(false,"");
				e.printStackTrace();
				ErrorPrintSkip(name, errorNode);
				resetFlags();
				continue;
			}
			

			if(hasNumber){
				errorNode = "Next Number";
				debugPrint("Locating "+errorNode+"...");
				try {
					if((i+1) < nl.getLength())
						nextNumber = getCheckListInfo("number",nl.item(i+1));
					else
						nextNumber = "";
					printSuccess(true, nextNumber);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintSkip(name, errorNode);
					resetFlags();
					continue;
				}
			}
			
			regex = "./td/a/@href";
			errorNode = "Card Link";
			debugPrint("Locating "+errorNode+"...");
			//Node href = (Node) xpath.evaluate(regex,nl.item(i), XPathConstants.NODE);
			Node href = null;
			try {
				href = getAnXpathNode(regex,nl.item(i),"href");
				printSuccess(true, href.getNodeValue());
			} catch (XPathExpressionException e) {
				printSuccess(false,"");
				e.printStackTrace();
				ErrorPrintSkip(name, errorNode);
				resetFlags();
				continue;
			}
			
			
			//if(!number.equals("27")) continue;
			
			if(!name.equals(prevName)){

				//If the current number matches either the previous or next entry in the list 
				//Then we know we're dealing with one half of a split card
				//If it matches the next entry then we're dealing with the default side
				//If it matches the previous entry then we're dealing with the other side
				if(hasNumber){	
					if(number.equals(nextNumber)){
						isFlip = true;
						isLeft = true;
					}else if(number.equals(prevNumber)){
						isFlip = true;
						isLeft = false;
						prevNumber = "";
					}
				}

				prevName = name;

				//Artist
				errorNode = "Artist";
				debugPrint("Locating "+errorNode+"...");
				String artist = "";
				try {
					artist = getCheckListInfo("artist",nl.item(i));
					printSuccess(true,artist);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);
					hasArtist = false;
				}

				//Get Rarity and Convert it to its full name
				errorNode = "Rarity";
				debugPrint("Locating "+errorNode+"...");
				String rarity = "";
				try {
					rarity = getCheckListInfo("rarity",nl.item(i));
					rarity = rarityMap.get(rarity) == null ? rarity : rarityMap.get(rarity);
					printSuccess(true,rarity);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);
					hasRarity = false;
				}

				//debugPrintLn("Name: " + name + " Rarity: " + rarity + " Artist: " + artist);

				//Get the Card Url and MultiverseID
				String relCardURL = href.getNodeValue();
				String cardURL = relCardURL.replace("..","http://gatherer.wizards.com/Pages");
				String multiverseID = relCardURL.replace("../Card/Details.aspx?multiverseid=","");

				//Retrieves the DOM for the current card
				Document cardDoc = null;
				try {
					cardDoc = getADoc(cardURL);
				} catch (ParserConfigurationException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintSkip(name, errorNode);
					resetFlags();
					continue;
				} catch (IOException  e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintSkip(name, errorNode);
					resetFlags();
					continue;
				}

				//Get XPath Nodes needed to get the remaining information
				//To know which Nodes to get we need to know
				//a) If we're dealing with a split card 
				//b) Which side of the split card we're currently on
				String tdID = "", divID = "", sideNum = "";
				Node cardDetails = null, cardDetailsImgSide = null, cardDetailTextSide = null;
				errorNode = "Card Details";
				debugPrint("Locating "+errorNode+"...");
				try {
					if(isFlip){
						debugPrint("This looks like a split card...");
						tdID = idHeader+"cardComponent";
						divID = idHeader+"ctl0";
						sideNum = "";
						prevNumber = number;
						errorNode = "New Prev Number";
						debugPrint("Locating "+errorNode+"...");
						printSuccess(true, prevNumber);
						if(isLeft) {
							number = number + "a";
							isLeft = true;
							tdID = tdID + "0";
							sideNum = "2";
							divID = divID + sideNum + "_componentWrapper";
							
							regex = "//td[@id='"+tdID+"']/div[@id='"+divID+"']/table/tbody/tr";
							cardDetails = getAnXpathNode(regex, cardDoc, "CardDetails");
							
							regex = "./td[@id='"+idHeader+"ctl02_Td1']";
							cardDetailsImgSide  = getAnXpathNode(regex, cardDetails, "cardDetailsImgRate");
	
							regex = "//td[@id='"+idHeader+"ctl02_rightCol']";
							cardDetailTextSide  = getAnXpathNode(regex, cardDetails, "cardDetailsAllText");
	
						}else{
							number = number + "b";
							isLeft = false;
							tdID = tdID + "1";
							sideNum = "3";
							divID = divID + sideNum + "_componentWrapper";
							
							regex = "//td[@id='"+tdID+"']/div[@id='"+divID+"']/table/tbody/tr";
							cardDetails = getAnXpathNode(regex, cardDoc, "CardDetails");
	
							regex = "./td[@id='"+idHeader+"ctl03_Td1']";
							cardDetailsImgSide  = getAnXpathNode(regex, cardDetails, "cardDetailsImgRate");
	
							regex = "//td[@id='"+idHeader+"ctl03_rightCol']";
							cardDetailTextSide  = getAnXpathNode(regex, cardDetails, "cardDetailsAllText");
						}
	
					}else{
						regex = "//table[@class='cardDetails']/tbody/tr[td/@id='"+idHeader+"leftColumn']";
						cardDetails = getAnXpathNode(regex, cardDoc, "CardDetails");
	
						regex = "./td[@id='"+idHeader+"leftColumn']";
						cardDetailsImgSide  = getAnXpathNode(regex, cardDetails, "cardDetailsImgRate");
	
						regex = "./td[@id='"+idHeader+"rightCol']";
						cardDetailTextSide  = getAnXpathNode(regex, cardDetails, "cardDetailsAllText");
					}
					printSuccess(true,"");

				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintSkip(name, errorNode);
					resetFlags();
					continue;
				}
				//debugPrintLn("Number: " + number);
				
				//Image URL
				if(isFlip)
					regex = "./img/@src";
				else
					regex = "./div[@class='cardImage']/img/@src";
				Node img = null;
				String imgURL = "";
				errorNode = "Image URL";
				debugPrint("Locating "+errorNode+"...");
				try {
					img = getAnXpathNode(regex, cardDetailsImgSide, "Image URL");
					//Node img = (Node) xpath.evaluate(regex,cardDetailsImgRate, XPathConstants.NODE);

					imgURL = img.getNodeValue();
					imgURL = imgURL.replace("../..","http://gatherer.wizards.com");
					imgURL = imgURL.replace("amp;","");
					
					//debugPrintLn("Image: "+ imgURL);
					printSuccess(true,imgURL);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);
					hasImage = false;
				}

				//XPath Node which contains Votes and Ratings
				if(isFlip){
					regex = "./div/div[@id='"+idHeader+"ctl0"+sideNum+"_playerRatingRow']/div/div/div/div";
					regex = regex + "[@id='"+idHeader+"ctl0"+sideNum+"_currentRating_textRatingContainer']";
				}else {
					String ratingDiv = "div[@id='"+idHeader+"playerRatingRow']";
					regex = "./div[@class='CommunityRatings']/"+ratingDiv+"/div[@class='value']/div[@class='stars']";
					regex = regex + "/div[@class='starRating']/div[@class='textRating']"; 	
				}
				Node ratings = null;
				errorNode = "Votes/Ratings";
				debugPrint("Locating "+errorNode+"...");
				try {
					ratings = getAnXpathNode(regex, cardDetailsImgSide, "Ratings"); 
					//Node ratings = (Node) xpath.evaluate(regex,cardDetailsImgRate, XPathConstants.NODE); 
					printSuccess(true,"");
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);					
					hasVotes = false;
					hasRating = false;
				}
				
				//Rating
				String avgRating =  "", votes = "";
				errorNode = "Rating";
				debugPrint("Locating "+errorNode+"...");
				if(hasRating){
					regex="./span[@class='textRatingValue']/text()";
					try {
						avgRating = (String) xpath.evaluate(regex,ratings, XPathConstants.STRING);
						printSuccess(true,avgRating);
					} catch (XPathExpressionException e) {
						printSuccess(false,"");
						e.printStackTrace();
						errorNode = "Rating";
						ErrorPrintNoElem(name, errorNode);	
						hasRating = false;
					} 
				}

				//Votes
				errorNode = "Votes";
				debugPrint("Locating "+errorNode+"...");
				regex="./span[@class='totalVotesValue']/text()";
				if (hasVotes) {
					try {
						votes = (String) xpath.evaluate(regex, ratings,
								XPathConstants.STRING);
						printSuccess(true,votes);
					} catch (XPathExpressionException e) {
						printSuccess(false,"");
						e.printStackTrace();
						errorNode = "Votes";
						ErrorPrintNoElem(name, errorNode);	
						hasVotes = false;
					}
				}
				//debugPrintLn("MultiverseID: " + multiverseID + " Rating: " + avgRating + " Votes: " + votes);

				//Mana Cost 
				if(isFlip){
					regex = "./div/div[@id='"+idHeader+"ctl0"+sideNum+"_manaRow']";
					regex = regex + "/div/img";
					
				}else{
					regex = "./div/div[@id='"+idHeader+"manaRow']/div[@class='value']/img";
				}

				//debugPrintLn("Mana Cost Regex: " + regex);
				NodeList manaCostList = null;
				errorNode = "Mana Cost";
				debugPrint("Locating "+errorNode+"...");
				try {
					manaCostList = (NodeList) xpath.evaluate(regex, cardDetailTextSide, XPathConstants.NODESET);
					printSuccess(true,"");
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);						
					hasManaCost = false;
				} 
				StringBuilder manaCost = new StringBuilder();

				if(manaCostList.getLength() > 0 && hasManaCost){
					errorNode = "Mana Cost (Image)";
					debugPrint("Locating "+errorNode+"...");
					try {
						for (int j = 0; j< manaCostList.getLength(); ++j){
							manaCost.append(findManaSymbol(manaCostList.item(j)));
						}
						
						if(hasManaCost){
							//debugPrint("Mana Cost: ");
							if(manaCost.toString().isEmpty()){
								//debugPrintLn("N/A");
								hasManaCost = false;
							}else{
								printSuccess(true,manaCost.toString());
							}
						}
					} catch (XPathExpressionException e) {
						printSuccess(false,"");
						e.printStackTrace();
						ErrorPrintNoElem(name, errorNode);						
						hasManaCost = false;
					} 
				}else{
					hasManaCost = false;
				}

				//CMC
				if(isFlip){
					regex = "./div/div[@id='"+idHeader+"ctl0"+sideNum;
					regex = regex + "_cmcRow']/div[@class='value']/text()";
				}else{
					regex = "./div/div[@id='"+idHeader+"cmcRow']/div[@class='value']/text()";
				}
				
				String cmc = "";
				errorNode = "CMC";
				debugPrint("Locating "+errorNode+"...");
				try {
					cmc = (String) xpath.evaluate(regex,cardDetailTextSide, XPathConstants.STRING);
					cmc = cmc.trim(); 
					if(cmc.isEmpty()){
						cmc="0";
					}
					//debugPrintLn("CMC: " + cmc);
					printSuccess(true,cmc);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);						
					hasCMC  = false;
				}

				//Acquires the Type line text which contains both Type and Subtypes
				if(isFlip){
					regex = "./div/div[@id='"+idHeader+"ctl0"+sideNum;
					regex = regex + "_typeRow']/div[@class='value']/text()";
				}else{
					regex = "./div/div[@id='"+idHeader+"typeRow']/div[@class='value']/text()";
				}
				String typeLine = "";
				errorNode = "Typeline";
				debugPrint("Locating "+errorNode+"...");
				try {
					typeLine = (String) xpath.evaluate(regex,cardDetailTextSide, XPathConstants.STRING);
					typeLine = typeLine.trim(); 
					//debugPrintLn("Type: " + typeLine);
					printSuccess(true,typeLine);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);						
					hasSupertype  = false;						
					hasSubtype = false;
				}
				//Supertypes and Subtypes
				JsonArrayBuilder superTypesBuilder = null;
				JsonArrayBuilder subTypesBuilder = null;
				if (hasSupertype || hasSubtype) {
					errorNode = "Supertypes";
					debugPrint("Locating "+errorNode+"...");
					String[] splitTypes = typeLine.split("&mdash;");
					superTypesBuilder = getTypes(splitTypes[0]);
					subTypesBuilder = null;
					if (splitTypes.length > 1) {
						errorNode = "Subtypes";
						debugPrint("Locating "+errorNode+"...");
						subTypesBuilder = getTypes(splitTypes[1]);

					} else {
						hasSubtype = false;
					}
				}
				//Card Text
				String rowID = "";
				if(isFlip){
					rowID = idHeader+"ctl0"+sideNum+"_textRow";
				}else{
					rowID = idHeader+"textRow";
				}
				regex = "./div/div[@id='"+rowID+"']/div[@class='value']/div[@class='cardtextbox']";
				NodeList textList = null;
				errorNode = "Card Text(Node Set)";
				debugPrint("Locating "+errorNode+"...");
				try {
					textList = (NodeList) xpath.evaluate(regex,cardDetailTextSide, XPathConstants.NODESET);
					printSuccess(true,"");
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);						
					hasCardText  = false;
				}
				StringBuilder text = new StringBuilder();
				StringBuilder printText = new StringBuilder();

				String cardText = "";
				if (hasCardText) {
					errorNode = "Card Text(Node)";
					debugPrint("Locating "+errorNode+"...");
					try {
						for (int k = 0; k < textList.getLength(); ++k) {
							String appendString = buildCardText(textList.item(k));
							text.append(appendString);
							printText.append(appendString);
							int testK = k + 1;
							if (testK < textList.getLength()) {
								text.append("\n");
							}
						}
						cardText = text.toString().replace("&minus;", "-");
						//debugPrintLn("Text: " + cardText);
						printSuccess(true,printText.toString());
					} catch (XPathExpressionException e) {
						printSuccess(false,"");
						e.printStackTrace();
						ErrorPrintNoElem(name, errorNode);						
						hasCardText  = false;
					}
				}
				//Flavor Text
				if(isFlip){
					rowID = idHeader+"ctl0"+sideNum+"_flavorRow";
				}else{
					rowID = idHeader+"flavorRow";
				}
				regex = "./div/div[@id='"+rowID+"']/div[@class='value']/div[@class='cardtextbox']";

				NodeList flavorList = null;
				errorNode = "Flavor Text (Node Set)";
				debugPrint("Locating "+errorNode+"...");
				try {
					flavorList = (NodeList) xpath.evaluate(regex,cardDetailTextSide, XPathConstants.NODESET);
					printSuccess(true,"");
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					ErrorPrintNoElem(name, errorNode);						
					hasFlavorText = false;
				}
				StringBuilder flavorBuild = new StringBuilder();
				StringBuilder printFlavor = new StringBuilder();

				String flavorText = "";
				if (hasFlavorText) {
					errorNode = "Flavor Text (Node)";
					debugPrint("Locating "+errorNode+"...");
					try {
						for (int k = 0; k < flavorList.getLength(); ++k) {
							String appendFlavor = buildCardText(flavorList.item(k));
							flavorBuild.append(appendFlavor);
							printFlavor.append(appendFlavor);

							int testK = k + 1;
							if (testK < flavorList.getLength()) {
								flavorBuild.append("\n");
							}
						}
						flavorText = flavorBuild.toString();
						if (!flavorText.isEmpty()) {
							flavorText = flavorText.replace("&quot;", "\"");
							//debugPrintLn("Flavor Text: " + flavorText);
						} else {
							hasFlavorText = false;
						}
						printSuccess(true,printFlavor.toString());
					} catch (XPathExpressionException e) {
						printSuccess(false,"");
						e.printStackTrace();
						ErrorPrintNoElem(name, errorNode);						
						hasFlavorText = false;
					}
				}
				//Power-Toughness/Loyalty
				if(isFlip){
					rowID = idHeader+"ctl0"+sideNum+"_ptRow";
				}else{
					rowID = idHeader+"ptRow";
				}
				regex = "./div/div[@id='"+rowID+"']/div[@class='value']/text()";
				String powTou = "";
				errorNode = "LoyPowTou";
				debugPrint("Locating "+errorNode+"...");
				try {
					powTou = (String) xpath.evaluate(regex,cardDetailTextSide, XPathConstants.STRING);
					String printPowTou = (powTou == null) ? "" : powTou.trim();
					printSuccess(true,printPowTou);
				} catch (XPathExpressionException e) {
					printSuccess(false,"");
					e.printStackTrace();
					errorNode = "LoyPowTou";
					ErrorPrintNoElem(name, errorNode);						
					hasLoyPowTou  = false;
				}
				String[] powTouArray = null;
				if(powTou != null){
					powTou = powTou.trim();
					if(typeLine.contains("Creature") && !typeLine.contains("Enchant ")){
						powTouArray = powTou.split("/");
						//debugPrintLn("powTouArray: " +  powTouArray.length + " POWTOU " + powTou);
						if(powTouArray.length == 2){
							powTouArray[0] = powTouArray[0].trim();
							powTouArray[1] = powTouArray[1].trim();
							//debugPrintLn("Power: " + powTouArray[0] + " Toughness: " + powTouArray[1]);
						}
						isCreature = true;
						

					}else if (typeLine.contains("Planeswalker")){
						//debugPrintLn("Loyalty: " + powTou);
						isPlaneswalker = true;
					}
				}

				
				//Build JSON object for Card and add to set JSON object
				//If we don't have data for a key-value pair
				//Then we don't show the pair at all
				aCardBuilder.add("name", name);
				if(hasManaCost) aCardBuilder.add("mana", manaCost.toString());
				if(hasCMC) aCardBuilder.add("cmc", cmc);
				if(hasRarity) aCardBuilder.add("rarity", rarity);
				if(isCreature && hasLoyPowTou) aCardBuilder.add("power", powTouArray[0]);
				if(isCreature && hasLoyPowTou) aCardBuilder.add("toughness", powTouArray[1]);
				if(isPlaneswalker && hasLoyPowTou) aCardBuilder.add("loyalty", powTou);
				if(hasNumber)aCardBuilder.add("number", number);
				if(hasArtist)aCardBuilder.add("artist", artist);
				if(hasSupertype)aCardBuilder.add("supertypes", superTypesBuilder);
				if(hasSubtype)aCardBuilder.add("subtypes", subTypesBuilder);
				if(hasCardText)aCardBuilder.add("text", cardText);
				if(hasFlavorText) aCardBuilder.add("flavor", flavorText);
				aCardBuilder.add("multiverseid", multiverseID);
				if(hasImage)aCardBuilder.add("img", imgURL);
				if(hasRating)aCardBuilder.add("rating", avgRating);
				if(hasVotes)aCardBuilder.add("votes", votes);

				theMagicSetBuilder.add(name,aCardBuilder);
			}
			successCount++;
			resetFlags();
			debugPrintLn("");
		}
		if(successCount < cardCount){
			debugPrintLn("successCount: " + successCount);
			debugPrintLn("cardCount: " + cardCount);
			debugPrintLn("setName: " + setName);
			String errorFraction = successCount + "/" + cardCount;
			debugPrintLn("errorFraction: " + errorFraction);
			errorMap.put(setName, errorFraction);
		}

		return theMagicSetBuilder;
	}

	//This takes a space delimited string and breaks into a JSON Array
	private JsonArrayBuilder getTypes(String theTypes){
		String[] superType = theTypes.trim().split(" ");
		if (superType.length == 0) return null;
		StringBuilder superTypes = new StringBuilder("[");
		JsonArrayBuilder superTypesBuilder = Json.createArrayBuilder();
		for(int superIndex = 0; superIndex < superType.length; superIndex++){
			superTypesBuilder.add(superType[superIndex]);
			superTypes.append("\"" + superType[superIndex] + "\"");
			int superIndexTest = superIndex + 1;
			if(superIndexTest<superType.length){
				superTypes.append(",");
			}

		}
		superTypes.append("]");
		debugPrintLn(superTypes.toString());

		return superTypesBuilder;

	}

	//This grabs all the nodes in div containing card text and iterates through them
	//If a Node is text it will append that text to the result
	//If a Node is an Image it will use findManaSymbol to get it's text version and append to the result
	//If the Node is an <i> then recursively call this function appending what's returned
	private String buildCardText(Node aNode) throws XPathExpressionException{
		String cardText = "";
		StringBuilder theCardText = new StringBuilder(cardText);
		String regex = "./node()";
		NodeList textContents = (NodeList) xpath.evaluate(regex,aNode, XPathConstants.NODESET); 
		for(int l = 0; l < textContents.getLength(); ++ l){
			String nodeName = textContents.item(l).getNodeName();
			if(nodeName.equals("#text")){
				theCardText.append(textContents.item(l).getNodeValue());
			}else if(nodeName.equals("img")){
				theCardText.append(findManaSymbol(textContents.item(l)));
			}else if(nodeName.equals("i")){
				theCardText.append(buildCardText(textContents.item(l)));
			}
		}
		return theCardText.toString();

	}

	//This takes an image XPath Node and grabs it alt text and uses it as a key 
	//to get the symbol it corresponds to if one exists, returns the alt value otherwise
	private String findManaSymbol(Node manaSym) throws XPathExpressionException{
		StringBuilder manaSymbol = new StringBuilder("o");
		String regex = "./@alt";
		String aManaImg = (String) xpath.evaluate(regex,manaSym, XPathConstants.STRING); 
		String aManaValue = manaMap.get(aManaImg) == null ? aManaImg : manaMap.get(aManaImg);
		manaSymbol.append(aManaValue);

		return manaSymbol.toString();
	}

	private String getCheckListInfo (String attrName, Node aNode) throws XPathExpressionException{
		String regex = "./td[@class='"+attrName+"']/text()";
		return (String) xpath.evaluate(regex,aNode, XPathConstants.STRING);
	}
	
	private Node getAnXpathNode(String regex, Node aNode, String aNodeName) 
				throws XPathExpressionException{
		Node ret  = (Node) xpath.evaluate(regex, aNode, XPathConstants.NODE);
		if(ret == null)
			debugPrintLn(aNodeName + "Node is Null: " + regex);
		return ret;
	}
	
	private Node getAnXpathNode(String regex, Document aDoc, String aNodeName) 
				throws XPathExpressionException{
		Node ret  = (Node) xpath.evaluate(regex, aDoc, XPathConstants.NODE);
		if(ret == null)
			debugPrintLn(aNodeName + "Node is Null: " + regex);
		return ret;
	}
	
	private void ErrorPrintNoElem(String name, String errorNode){
		debugPrintLn("\nCan't find the XPath DOM node(s) needed to parse " + name + "'s "+errorNode+"."); 
		debugPrintLn("This Card object won't have a "+errorNode+" element.");
		debugPrintLn("See the above debug text for more info.\n");
	}
	
	private void ErrorPrintSkip(String name, String errorNode){
		debugPrintLn("\nCan't find the XPath DOM node(s) needed to parse " + name + "'s "+errorNode+"."); 
		debugPrintLn("Skipping to the next card");
		debugPrintLn("See the above debug text for more info.\n");
	}
	
	private void printSuccess(boolean itDid, String aValue){
		if (itDid) {
			debugPrintLn("Successful: " + aValue);
		}else{
			debugPrintLn("Uhh Houston, you're gonna wanna see this\n");
		}
	}

	private Document getADoc (String aURL) throws ParserConfigurationException, IOException{
		Document doc = null;
		URL url = new URL(aURL);  
		debugPrint("Connecting to: " + aURL + "...");  
		URLConnection urlConnection = url.openConnection();
		printSuccess(true,"");

		debugPrint("Acquiring Stream...");  
		InputStream theStream = urlConnection.getInputStream();
		printSuccess(true,"");

		debugPrint("Cleaning Html..."); 
		TagNode tn = new HtmlCleaner().clean(theStream);
		printSuccess(true,"");

		debugPrint("Creating DOM..."); 
		doc = new DomSerializer(new CleanerProperties()).createDOM(tn);
		printSuccess(true,"");

		return doc;

	}

	private void resetFlags(){
		isCreature = false; isPlaneswalker = false;
		
		hasManaCost = true; hasFlavorText = true; hasImage = true;
		hasVotes = true; hasRating = true; hasCMC = true; hasNumber = true;
		hasSupertype = true; hasSubtype = true; hasCardText = true;
		hasArtist = true; hasRarity = true; hasLoyPowTou = true;
		
		isFlip = false; isLeft = false;
	}

	private void debugPrintLn(String value){
		if(DEBUG){
			System.err.println(value);
		}
	}

	private void debugPrint(String value){
		if(DEBUG){
			System.err.print(value);
		}
	}
 
}