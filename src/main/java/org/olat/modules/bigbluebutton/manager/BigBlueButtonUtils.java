/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.bigbluebutton.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonUtils {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonUtils.class);
	
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";
	public static final String CHECKSUM_ERROR = "checksumError";
	
    protected static Document getDocumentFromEntity(String content) throws Exception {
    	try(InputStream in=new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        return dBuilder.parse(in);
    	} catch(Exception e) {
    		throw e;
    	}
    }

    protected static Document getDocumentFromEntity(HttpEntity entity) throws Exception {
    	try(InputStream in=entity.getContent()) {
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(in);
	        print(doc);
	        return doc;
    	} catch(Exception e) {
    		throw e;
    	}
    }
    
    protected static boolean checkSuccess(Document document, BigBlueButtonErrors errors) {
    	String returnCode = getFirstElementValue(document.getDocumentElement(), "returncode");
    	print(document);
    	if(SUCCESS.equals(returnCode)) {
    		return true;
    	} else if(FAILED.equals(returnCode)) {
    		String messageKey = getFirstElementValue(document.getDocumentElement(), "messageKey");
    		String message = getFirstElementValue(document.getDocumentElement(), "message");
    		errors.append(new BigBlueButtonError(message, messageKey));
    	}
    	return false;
    }
    
    protected static List<BigBlueButtonRecording> getRecordings(Document document) {
    	List<BigBlueButtonRecording> recordings = new ArrayList<>();
    	NodeList recordingList = document.getElementsByTagName("recording");
    	for(int i=recordingList.getLength(); i-->0; ) {
    		Element recordingEl = (Element)recordingList.item(i);
    		String recordId = getFirstElementValue(recordingEl, "recordID");
    		String meetingId = getFirstElementValue(recordingEl, "meetingID"); 
    		String name = getFirstElementValue(recordingEl, "name");
    		Date startTime = toDate(getFirstElementValue(recordingEl, "startTime"));
    		Date endTime = toDate(getFirstElementValue(recordingEl, "endTime"));
    		
    		NodeList playbackList = recordingEl.getElementsByTagName("playback");
    		for(int j=playbackList.getLength(); j-->0; ) {
    			Element playbackEl = (Element)playbackList.item(j);
    			NodeList formatList = playbackEl.getElementsByTagName("format");
    			for(int k=formatList.getLength(); k-->0; ) {
    				Element formatEl = (Element)formatList.item(k);
    				String url = getFirstElementValue(formatEl, "url");
    				String type = getFirstElementValue(formatEl, "type");
    				recordings.add(BigBlueButtonRecordingImpl.valueOf(recordId, name, meetingId, startTime, endTime, url, type));
    			}
    		}
    	}
    	return recordings;
    }
    
    private static Date toDate(String val) {
    	if(StringHelper.isLong(val)) {
    		Long time = Long.parseLong(val);
    		return new Date(time.longValue());
    	}
    	
    	return null;
    }
    
    protected static String getFirstElementValue(Element parent, String tagName) {
        Element element = getFirstElement(parent, tagName);
        return (element == null) ? "" : getCharacterDataFromElement(element);
    }
    
    protected static Element getFirstElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return (nodes != null && nodes.getLength() > 0) ? (Element) (nodes.item(0)) : null;
    }
    
    protected static String getCharacterDataFromElement(Element e) {
    	StringBuilder sb = new StringBuilder();
    	for(Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
    		if (child instanceof CharacterData) {
            	CharacterData cd = (CharacterData)child;
                sb.append(cd.getData());
            }
    	}
        return sb.toString();
    }
    
	protected static void print(Document document) {
		if(log.isDebugEnabled()) {
			try(StringWriter writer = new StringWriter()) {
		    	TransformerFactory factory = TransformerFactory.newInstance();
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				Transformer transformer = factory.newTransformer();
				Source source = new DOMSource(document);
				transformer.transform(source, new StreamResult(writer));
				writer.flush();
				log.info(writer.toString());
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
