package com.andreaak.cards.utils;

import com.andreaak.cards.model.DeVerbItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
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

    public static LessonItem parseLesson(String path, String prefix) {

        LessonItem lesson = new LessonItem(new File(path), prefix, true);
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

    public static LessonItem parseSimpleLesson(File lessonFile, String prefix) {

        LessonItem lesson = new LessonItem(lessonFile, prefix, false);
        lesson.clear();
        try {

            ArrayList<SimpleWordItem> words = getSimpleWordItems(lesson.getFile().getPath());
            lesson.addAll(words);
        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
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

//    public static ArrayList<LessonItem> parseLessons(String path, String prefix) {
//
//        ArrayList<LessonItem> lessons = new ArrayList<>();
//        File directory = new File(path);
//        File[] files = directory.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File file, String s) {
//                return s.startsWith(AppConfigs.getInstance().LessonsPrefix);
//            }
//        });
//
//        for (File lessonFile : files) {
//            try {
//
//                LessonItem lesson = new LessonItem(lessonFile, prefix);
//
//                InputSource input = new InputSource(new FileReader(lessonFile));
//                Document doc = getXMLDocument(input);
//                NodeList words = doc.getElementsByTagName("word");
//
//                for (int i = 0; i < words.getLength(); i++) {
//                    Node node = words.item(i);
//                    WordItem word = parseWord(node, i);
//                    lesson.add(word);
//                }
//                if (!lesson.getWords().isEmpty()) {
//                    lessons.add(lesson);
//                }
//            } catch (FileNotFoundException e) {
//                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//        }
//        return lessons;
//    }

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

    public static ArrayList<SimpleWordItem> getSimpleWordItems(String path) throws Exception {
        SAXParserFactory fabrique = SAXParserFactory.newInstance();
        SAXParser parser = fabrique.newSAXParser();

        File file = new File(path);
        BookHandler handler = new BookHandler(path);
        parser.parse(file, handler);

        return handler.words;
    }

    public static class BookHandler extends DefaultHandler {

        private StringBuilder buffer;
        private SimpleWordItem word;
        private String path;

        public ArrayList<SimpleWordItem> words = new ArrayList<>();

        BookHandler(String path){
            this.path = path;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            switch (qName) {
                case "word":
                    word = new SimpleWordItem(path);
                    break;
                case "ru":
                case "en":
                case "de":
                case "de_wordclass":
                case "de_info":
                    if(buffer == null) {
                        buffer = new StringBuilder();
                    } else {
                        buffer.setLength(0);
                    }

                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            String content = new String(ch, start, length);
            if (buffer != null)
                buffer.append(content);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (qName) {
                case "word":
                    words.add(word);
                    break;
                case "ru":
                case "en":
                case "de":
                case "de_wordclass":
                case "de_info":
                    word.addItem(qName, buffer.toString());
                    break;
            }
        }

        @Override
        public void endDocument() throws SAXException {

        }
    }

//    public static void main(String file) throws Exception {
//        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//        InputStream in = new FileInputStream(file);
//        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
//        streamReader.nextTag(); // Advance to "book" element
//        streamReader.nextTag(); // Advance to "person" element
//
//        int persons = 0;
//        while (streamReader.hasNext()) {
//            if (streamReader.isStartElement()) {
//                switch (streamReader.getLocalName()) {
//                    case "first": {
//                        System.out.print("First Name : ");
//                        System.out.println(streamReader.getElementText());
//                        break;
//                    }
//                    case "last": {
//                        System.out.print("Last Name : ");
//                        System.out.println(streamReader.getElementText());
//                        break;
//                    }
//                    case "age": {
//                        System.out.print("Age : ");
//                        System.out.println(streamReader.getElementText());
//                        break;
//                    }
//                    case "person" : {
//                        persons ++;
//                    }
//                }
//            }
//            streamReader.next();
//        }
//        System.out.print(persons);
//        System.out.println(" persons");
//    }


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
