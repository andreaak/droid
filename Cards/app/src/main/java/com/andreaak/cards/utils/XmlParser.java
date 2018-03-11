package com.andreaak.cards.utils;

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

    public static ArrayList<LessonItem> parseLessonsXmlFiles(String path){

        ArrayList<LessonItem> lessons = new ArrayList<>();
        File directory =  new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });

        for (File file : files) {
            try {

                LessonItem lesson = new LessonItem(file.getName());

                InputSource input = new InputSource(new FileReader(file));
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
                e.printStackTrace();
            }
        }
        return lessons;
    }

    private static WordItem parseWord(Node node) {
        String ru = null;
        String en = null;
        String en_tr = null;

        NodeList items = node.getChildNodes();
        for(int i = 0; i < items.getLength(); i++){
            Node item = items.item(i);
            switch(item.getNodeName()){
                case "ru":
                    ru = item.getFirstChild().getNodeValue();
                    break;
                case "en":
                    en = item.getFirstChild().getNodeValue();
                    break;
                case "en_tr":
                    en_tr = item.getFirstChild().getNodeValue();
                    break;
            }
        }
        return new WordItem(ru, en, en_tr);
    }

    private static Document parseXML(InputSource source) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(source);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
