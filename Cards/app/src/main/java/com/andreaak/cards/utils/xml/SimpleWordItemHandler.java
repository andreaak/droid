package com.andreaak.cards.utils.xml;

import com.andreaak.cards.model.SimpleWordItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class SimpleWordItemHandler extends DefaultHandler {

    private StringBuilder buffer;
    private SimpleWordItem word;
    private final String path;

    public final ArrayList<SimpleWordItem> words = new ArrayList<>(128);

    SimpleWordItemHandler(String path) {
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
                if (buffer == null) {
                    buffer = new StringBuilder();
                } else {
                    buffer.setLength(0);
                }
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (buffer != null) {
            buffer.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "word":
                if (word != null) {
                    words.add(word);
                }
                break;
            case "ru":
            case "en":
            case "de":
            case "de_wordclass":
            case "de_info":
            case "en_wordclass":
            case "en_info":
                if (word != null && buffer != null) {
                    word.addItem(qName, buffer.toString());
                }
                break;
        }
    }
}
