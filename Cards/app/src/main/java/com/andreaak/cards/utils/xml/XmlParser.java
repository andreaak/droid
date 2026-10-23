package com.andreaak.cards.utils.xml;

import androidx.annotation.Nullable;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.DeVerbItem;
import com.andreaak.cards.model.Lang;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.BreakParsingException;
import com.andreaak.cards.utils.Cache;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlParser {

    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    public static final String CurrentStudyFileKey = "currentstudyfilekey";

    public static LessonItem parseLesson(LessonItem lesson) {
        lesson.clear();

        Document doc = getXMLDocument(lesson.getFile());
        if (doc == null) {
            return lesson;
        }

        NodeList words = doc.getElementsByTagName("word");
        for (int i = 0; i < words.getLength(); i++) {
            Node node = words.item(i);
            WordItem word = parseWord(node, i);
            lesson.add(word);
        }

        return lesson;
    }

    public static ArrayList<SimpleWordItem> getSimpleWordItems(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            SAXParser parser = SAX_PARSER_FACTORY.newSAXParser();
            SimpleWordItemHandler handler = new SimpleWordItemHandler(path);
            parser.parse(inputStream, handler);
            return handler.words;
        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public static boolean parseVerbForm(VerbForm verbForm) {
        verbForm.clear();

        Document doc = getXMLDocument(verbForm.getFile());
        if (doc == null) {
            return false;
        }

        NodeList rus = doc.getElementsByTagName("ru");
        String ru = "";
        if (rus.getLength() > 0) {
            Node firstChild = rus.item(0).getFirstChild();
            if (firstChild != null) {
                ru = firstChild.getNodeValue();
            }
        }
        NodeList verbFormItems = doc.getElementsByTagName("VerbForm");
        for (int i = 0; i < verbFormItems.getLength(); i++) {
            Node node = verbFormItems.item(i);
            VerbFormItem verbFormItem = parseVerbForm(node, i, ru);
            verbForm.add(verbFormItem);
        }
        return true;
    }

    public static WordItem getWordItem(SimpleWordItem wordItem, String lang) {
        if (wordItem == null || wordItem.getPath() == null) {
            return null;
        }

        File file = new File(wordItem.getPath());
        if (!file.exists()) {
            return null;
        }

        WordItemHandler handler = new WordItemHandler(wordItem, lang);
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            SAXParser parser = SAX_PARSER_FACTORY.newSAXParser();
            parser.parse(is, handler);
        } catch (SAXException e) {
            if (!(e.getException() instanceof BreakParsingException)) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        WordItem wi = handler.result;
        if (wi != null) {
            wi.setPath(wordItem.getPath());
        }
        return wi;
    }

    private static WordItem parseWord(Node node, int id) {
        WordItem word = new WordItem(id);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String nodeTag = item.getNodeName();
                Node child = item.getFirstChild();
                if (child != null) {
                    String value = child.getNodeValue();
                    word.addItem(nodeTag, value);
                }
            }
        }
        return word;
    }

    private static VerbFormItem parseVerbForm(Node node, int id, String ru) {
        VerbFormItem verbFormItem = new VerbFormItem(id, ru);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String nodeTag = item.getNodeName();
                Node firstChild = item.getFirstChild();
                if (firstChild == null) {
                    continue;
                }
                String value = firstChild.getNodeValue();
                verbFormItem.addItem(nodeTag, value);
            }
        }
        return verbFormItem;
    }

    private static Document getXMLDocument(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            InputSource input = new InputSource(reader);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(input);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            return null;
        }
    }

    public static VerbLessonItem parseVerbLesson(String path) {
        return parseVerbLesson(new File(path));
    }

    public static VerbLessonItem parseVerbLesson(File lessonFile) {
        VerbLessonItem lesson = new VerbLessonItem(lessonFile.getName(), lessonFile.getAbsolutePath());

        Document doc = getXMLDocument(lessonFile);
        if (doc == null) {
            return lesson;
        }

        NodeList words = doc.getElementsByTagName("verb");
        for (int i = 0; i < words.getLength(); i++) {
            Node node = words.item(i);

            VerbItem word = getVerbItem(lesson.getLanguage(), i);

            parseVerb(node, word);
            lesson.add(word);
        }
        return lesson;
    }

    private static VerbItem getVerbItem(String language, int id) {
        if (Lang.DE.getCode().equalsIgnoreCase(language)) {
            return new DeVerbItem(id);
        }
        return new VerbItem(id);
    }

    private static VerbItem parseVerb(Node node, VerbItem verb) {
        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String tag = item.getNodeName();
                Node firstChild = item.getFirstChild();
                if (firstChild != null) {
                    verb.addTag(tag, firstChild.getNodeValue());
                }
            }
        }
        return verb;
    }

    public static void createFile() {
        String directory = AppConfigs.getInstance().getStudyDir();
        try {
            String fileName = getNextFilePath(directory, false);
            File xmlFile = new File(directory, fileName);

            XmlDelegate delegate = (Document document) -> {
                Element root = document.createElement("words");
                document.appendChild(root);
            };

            createXml(xmlFile, delegate);

            Cache.getInstance().add(CurrentStudyFileKey, fileName);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        }
    }

    private static void createXml(File xmlFile, XmlDelegate delegate) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        delegate.execute(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }

    public static void mergeXmlFiles(List<String> xmlFiles) {
        try {
            String directory = AppConfigs.getInstance().getStudyDir();
            String fileName = getNextFilePath(directory, true);
            File outputFile = new File(directory, fileName);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document resultDoc = builder.newDocument();
            Element root = resultDoc.createElement("words");
            resultDoc.appendChild(root);

            for (String xmlFile : xmlFiles) {
                File file = new File(xmlFile);
                if (!file.exists()) continue;

                Document doc;
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    doc = builder.parse(is);
                }

                NodeList words = doc.getElementsByTagName("word");
                for (int i = 0; i < words.getLength(); i++) {
                    Node importedNode = resultDoc.importNode(words.item(i), true);
                    root.appendChild(importedNode);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(resultDoc), new StreamResult(outputFile));
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        }
    }

    private static int getLastNumber(File dir, String date) {
        int maxNumber = 0;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(date + "_") && name.endsWith(".xml")) {
                    try {
                        String numberPart = name.replace("combined_", "").substring(date.length() + 1, name.length() - 4);
                        int number = Integer.parseInt(numberPart);
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxNumber;
    }

    private static String getChildTagText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            Node node = list.item(0);
            return node != null && node.getTextContent() != null ? node.getTextContent() : "";
        }
        return "";
    }

    private static void appendChildElement(Document doc, Element parent, String tagName, String textContent) {
        if (textContent != null) {
            Element element = doc.createElement(tagName);
            element.setTextContent(textContent);
            parent.appendChild(element);
        }
    }

    public static boolean addItemToFile(WordItem wordItem, String primaryLang, String secondaryLang) {
        try {
            String directory = AppConfigs.getInstance().getStudyDir();
            String fileName = Cache.getInstance().getItem(CurrentStudyFileKey);
            if (Utils.isEmpty(fileName)) {
                return false;
            }

            File file = new File(directory, fileName);
            if (!file.exists()) {
                return false;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                doc = builder.parse(is);
            }

            Element root = doc.getDocumentElement();
            NodeList words = root.getElementsByTagName("word");

            String txt = wordItem.getValue(secondaryLang) + wordItem.getWordClass() + wordItem.getInfo(secondaryLang);
            if (Utils.isEmpty(txt)) {
                return false;
            }

            for (int i = 0; i < words.getLength(); i++) {
                Element word = (Element) words.item(i);
                String deText = getChildTagText(word, secondaryLang)
                        + getChildTagText(word, secondaryLang + "_wordclass")
                        + getChildTagText(word, secondaryLang + "_info");
                if (txt.equals(deText)) {
                    return false;
                }
            }

            Element word = doc.createElement("word");

            appendChildElement(doc, word, primaryLang, wordItem.getValue(primaryLang));
            appendChildElement(doc, word, secondaryLang, wordItem.getValue(secondaryLang));
            appendChildElement(doc, word, String.format("%s_wordclass", secondaryLang), wordItem.getWordClass());
            appendChildElement(doc, word, String.format("%s_level", secondaryLang), wordItem.getLevel(secondaryLang));
            appendChildElement(doc, word, String.format("%s_description", secondaryLang), wordItem.getDescription(secondaryLang));
            appendChildElement(doc, word, String.format("%s_gptdescription", secondaryLang), wordItem.getGPTDescription(secondaryLang));
            appendChildElement(doc, word, String.format("%s_example", secondaryLang), wordItem.getExample(secondaryLang));
            appendChildElement(doc, word, String.format("%s_tr", secondaryLang), wordItem.getTranscription(secondaryLang));
            appendChildElement(doc, word, String.format("%s_info", secondaryLang), wordItem.getInfo(secondaryLang));
            appendChildElement(doc, word, String.format("%s_prap", secondaryLang), wordItem.getPrap(secondaryLang));

            root.appendChild(word);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            Logger.d(Constants.LOG_TAG, "Новый word добавлен.");
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static boolean openFile(String fileName) {
        try {
            if (fileName == null) {
                return false;
            }
            Cache.getInstance().add(CurrentStudyFileKey, fileName);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Nullable
    private static String getNextFilePath(String directory, boolean isCombined) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        int number = getLastNumber(dir, date) + 1;

        String sequence = String.format(Locale.US, "%02d", number);
        return date + "_" + (isCombined ? "combined_" : "") + sequence + ".xml";
    }
}

interface XmlDelegate {
    void execute(Document document);
}
