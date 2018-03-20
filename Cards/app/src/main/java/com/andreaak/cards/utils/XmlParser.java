package com.andreaak.cards.utils;

import com.andreaak.cards.model.LessonItem;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlParser {

    public static LessonItem parseLesson(String path){

        return parseLesson(new File(path));
    }

    public static LessonItem parseLesson(File lessonFile){

        String name = Utils.getFileNameWithoutExtensions(lessonFile.getName());
        LessonItem lesson = new LessonItem(name, lessonFile.getAbsolutePath());
            try {
                InputSource input = new InputSource(new FileReader(lessonFile));
                Document doc  = parseXML(input);
                NodeList words = doc.getElementsByTagName("word");
                for(int i = 0; i < words.getLength(); i++){
                    Node node = words.item(i);
                    WordItem word = parseWord(node);
                    lesson.add(word);
                }

            } catch (FileNotFoundException e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        return lesson;
    }

    public static ArrayList<LessonItem> parseLessons(String path){

        ArrayList<LessonItem> lessons = new ArrayList<>();
        File directory =  new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });

        for (File lessonFile : files) {
            try {

                String name = Utils.getFileNameWithoutExtensions(lessonFile.getName());
                LessonItem lesson = new LessonItem(name, lessonFile.getAbsolutePath());

                InputSource input = new InputSource(new FileReader(lessonFile));
                Document doc  = parseXML(input);
                NodeList words = doc.getElementsByTagName("word");
                for(int i = 0; i < words.getLength(); i++){
                    Node node = words.item(i);
                    WordItem word = parseWord(node);
                    lesson.add(word);
                }
                if(!lesson.getWords().isEmpty()) {
                    lessons.add(lesson);
                }
            } catch (FileNotFoundException e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return lessons;
    }

    private static WordItem parseWord(Node node) {

        WordItem word = new WordItem();

        NodeList items = node.getChildNodes();
        for(int i = 0; i < items.getLength(); i++){
            Node item = items.item(i);
            short type = item.getNodeType();
            if(type == 1) {
                String language = item.getNodeName();
                String value = item.getFirstChild().getNodeValue();
                word.addItem(language, value);
            }
        }
        return word;
    }

    private static Document parseXML(InputSource source) {
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
}
