package com.andreaak.cards.utils;

import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

        LessonItem lesson = new LessonItem(lessonFile.getName(), lessonFile.getAbsolutePath());
        try {
            InputSource input = new InputSource(new FileReader(lessonFile));
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

    public static ArrayList<LessonItem> parseLessons(String path) {

        ArrayList<LessonItem> lessons = new ArrayList<>();
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });

        for (File lessonFile : files) {
            try {

                LessonItem lesson = new LessonItem(lessonFile.getName(), lessonFile.getAbsolutePath());

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

    private static WordItem parseWord(Node node, int id) {

        WordItem word = new WordItem(id);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String language = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();
                word.addItem(language, value);
            }
        }
        return word;
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
        } catch (TransformerException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
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
                VerbItem word = parseVerb(node, i);
                lesson.add(word);
            }

        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return lesson;
    }

    private static VerbItem parseVerb(Node node, int id) {

        VerbItem verb = new VerbItem(id);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String tag = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();

                switch (tag) {
                    case "infinitive":
                        verb.infinitive = value;
                        break;
                    case "infinitive_tr":
                        verb.infinitiveTrans = value;
                        break;
                    case "pastSimple":
                        verb.pastSimple = value;
                        break;
                    case "pastSimple_tr":
                        verb.pastSimpleTrans = value;
                        break;
                    case "pastParticiple":
                        verb.pastParticiple = value;
                        break;
                    case "pastParticiple_tr":
                        verb.pastParticipleTrans = value;
                        break;
                    case "translation":
                        verb.translation = value;
                        break;
                }
            }
        }
        return verb;
    }
}
