package com.andreaak.cards.utils;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.DeVerbItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlParser {

    public static LessonItem parseLesson(String path) {

        return parseLesson(new File(path));
    }

    public static LessonItem parseLesson(File lessonFile) {

        LessonItem lesson = new LessonItem(lessonFile);
        parseLesson(lesson);
        return lesson;
    }

    public static LessonItem parseLesson(LessonItem lesson) {

        lesson.clear();
        try {
            InputSource input = new InputSource(new FileReader(lesson.getFile()));
            Document doc = getXMLDocument(input);
            NodeList words = doc.getElementsByTagName("word");
            for (int i = 0; i < words.getLength(); i++) {
                Node node = words.item(i);
                WordItem word = parseWord(node, i);
                lesson.add(word);
            }

        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return lesson;
    }

    public static boolean parseVerbForm(VerbForm verbForm) {

        verbForm.clear();
        try {
            InputSource input = new InputSource(new FileReader(verbForm.getFile()));
            Document doc = getXMLDocument(input);
            NodeList rus = doc.getElementsByTagName("ru");
            String ru = "";
            if(rus.getLength() > 0) {
                Node firstChild = rus.item(0).getFirstChild();
                if(firstChild != null) {
                    ru = firstChild.getNodeValue();
                }
            }
            NodeList verbFormItems = doc.getElementsByTagName("VerbForm");
            for (int i = 0; i < verbFormItems.getLength(); i++) {
                Node node = verbFormItems.item(i);
                VerbFormItem verbFormItem = parseVerbForm(node, i, ru);
                verbForm.add(verbFormItem);
            }

        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static ArrayList<LessonItem> parseLessons(String path) {

        ArrayList<LessonItem> lessons = new ArrayList<>();
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(AppConfigs.getInstance().LessonsPrefix);
            }
        });

        for (File lessonFile : files) {
            try {

                LessonItem lesson = new LessonItem(lessonFile);

                InputSource input = new InputSource(new FileReader(lessonFile));
                Document doc = getXMLDocument(input);
                NodeList words = doc.getElementsByTagName("word");

                for (int i = 0; i < words.getLength(); i++) {
                    Node node = words.item(i);
                    WordItem word = parseWord(node, i);
                    lesson.add(word);
                }
                if (!lesson.getWords().isEmpty()) {
                    lessons.add(lesson);
                }
            } catch (FileNotFoundException e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return lessons;
    }

    public static void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        SAXParser saxParser = factory.newSAXParser();
        File file = new File("test.xml");

        DefaultHandler handler = new DefaultHandler() {

            boolean bfname = false;
            boolean blname = false;
            boolean bnname = false;
            boolean bsalary = false;

            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {

                System.out.println("Start Element :" + qName);

                if (qName.equalsIgnoreCase("FIRSTNAME")) {
                    bfname = true;
                }

                if (qName.equalsIgnoreCase("LASTNAME")) {
                    blname = true;
                }

                if (qName.equalsIgnoreCase("NICKNAME")) {
                    bnname = true;
                }

                if (qName.equalsIgnoreCase("SALARY")) {
                    bsalary = true;
                }

            }

            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {

                System.out.println("End Element :" + qName);

            }

            public void characters(char ch[], int start, int length) throws SAXException {

                if (bfname) {
                    System.out.println("First Name : " + new String(ch, start, length));
                    bfname = false;
                }

                if (blname) {
                    System.out.println("Last Name : " + new String(ch, start, length));
                    blname = false;
                }

                if (bnname) {
                    System.out.println("Nick Name : " + new String(ch, start, length));
                    bnname = false;
                }

                if (bsalary) {
                    System.out.println("Salary : " + new String(ch, start, length));
                    bsalary = false;
                }

            }

        };

        saxParser.parse(file, handler);    // specify handler
    }

    private static WordItem parseWord(Node node, int id) {

        WordItem word = new WordItem(id);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String nodeTag = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();
                word.addItem(nodeTag, value);
            }
        }
        return word;
    }

    private static VerbFormItem parseVerbForm(Node node, int id, String ru) {

        VerbFormItem verbFormItem = new VerbFormItem(id, ru);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String nodeTag = item.getNodeName();
                Node firstChild = item.getFirstChild();
                if(firstChild == null) {
                    continue;
                }
                String value = firstChild.getNodeValue();
                verbFormItem.addItem(nodeTag, value);
            }
        }
        return verbFormItem;
    }


    private static Document getXMLDocument(InputSource source) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(source);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateXML(String lessonFile, String lang1, String value1,
                                    String lang2, String value2, HashMap<String, String> map) {
        try {
            InputSource input = new InputSource(new FileReader(lessonFile));
            Document doc = getXMLDocument(input);
            NodeList words = doc.getElementsByTagName("word");
            for (int i = 0; i < words.getLength(); i++) {
                Node word = words.item(i);
                if (!isEditWord(word, lang1, value1, lang2, value2)) {
                    continue;
                }
                NodeList items = word.getChildNodes();
                for (int j = 0; j < items.getLength(); j++) {
                    Node item = items.item(j);
                    short type = item.getNodeType();
                    if (type == 1) {
                        String language = item.getNodeName();
                        if (map.containsKey(language)) {
                            item.getFirstChild().setNodeValue(map.get(language));
                        }
                    }
                }
            }

            return writeXmlFile(doc, lessonFile);
        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isEditWord(Node word, String lang1, String value1,
                                      String lang2, String value2) {

        boolean isValue1 = false;
        boolean isValue2 = false;

        NodeList items = word.getChildNodes();
        for (int j = 0; j < items.getLength(); j++) {
            Node item = items.item(j);
            short type = item.getNodeType();
            if (type == 1) {
                String language = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();
                if (language.equals(lang1) && value.equals(value1)) {
                    isValue1 = true;
                }
                if (language.equals(lang2) && value.equals(value2)) {
                    isValue2 = true;
                }
            }
        }
        return isValue1 && isValue2;
    }

    public static boolean writeXmlFile(Document doc, String lessonFile) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            File file = new File(lessonFile);

            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return true;
        } catch (TransformerConfigurationException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (TransformerException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }


    public static VerbLessonItem parseVerbLesson(String path) {

        return parseVerbLesson(new File(path));
    }

    public static VerbLessonItem parseVerbLesson(File lessonFile) {

        VerbLessonItem lesson = new VerbLessonItem(lessonFile.getName(), lessonFile.getAbsolutePath());
        try {
            InputSource input = new InputSource(new FileReader(lessonFile));
            Document doc = getXMLDocument(input);
            NodeList words = doc.getElementsByTagName("verb");
            for (int i = 0; i < words.getLength(); i++) {
                Node node = words.item(i);

                VerbItem word = getVerbItem(lesson.getLanguage(), i);

                parseVerb(node, word);
                lesson.add(word);
            }

        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return lesson;
    }

    private static VerbItem getVerbItem(String language, int id) {
        if(language == VerbLessonItem.English) {
            return new VerbItem(id);
        } else if(language == VerbLessonItem.Deutsch) {
            return new DeVerbItem(id);
        }
        return new VerbItem(id);
    }

    private static VerbItem parseVerb(Node node, VerbItem verb) {

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String tag = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();
                verb.addTag(tag, value);
            }
        }
        return verb;
    }
}
