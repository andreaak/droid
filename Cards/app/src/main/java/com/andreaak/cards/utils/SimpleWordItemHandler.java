package com.andreaak.cards.utils;

import com.andreaak.cards.model.SimpleWordItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class SimpleWordItemHandler extends DefaultHandler {

    private StringBuilder buffer;
    private SimpleWordItem word;
    private String path;

    public ArrayList<SimpleWordItem> words = new ArrayList<>();

    SimpleWordItemHandler(String path){
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
            case "en_wordclass":
            case "en_info":
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
            case "en_wordclass":
            case "en_info":
                word.addItem(qName, buffer.toString());
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {

    }
}
