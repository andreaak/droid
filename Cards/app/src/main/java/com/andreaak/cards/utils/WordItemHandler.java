package com.andreaak.cards.utils;

import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class WordItemHandler extends DefaultHandler {

    private StringBuilder buffer;
    private WordItem word;
    public WordItem result;
    private SimpleWordItem wordItem;
    private String lang;

    WordItemHandler(SimpleWordItem wordItem, String lang){
        this.wordItem = wordItem;
        this.lang = lang;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        switch (qName) {
            case "word":
                word = new WordItem(-1);
                break;
            case "ru":
            case "de":
            case "de_tr":
            case "de_wordclass":
            case "de_info":
            case "de_level":
            case "de_example":
            case "de_description":
            case "de_gptdescription":
            case "de_prap":
            case "en":
            case "en_tr":
            case "en_wordclass":
            case "en_info":
            case "en_level":
            case "en_example":
            case "en_description":
            case "en_gptdescription":
            case "en_prap":
                if(buffer == null) {
                    buffer = new StringBuilder();
                } else {
                    buffer.setLength(0);
                }

                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length);
        if (buffer != null)
            buffer.append(content);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
            case "word":
                if (Utils.isEqual(word.getValue(lang), wordItem.getValue(lang))
                        && Utils.isEqual(word.getWordClass(), wordItem.getWordClass())
                        && Utils.isEqual(word.getInfo(lang), wordItem.getInfo(lang))) {
                    result = word;
                    throw new SAXException(new BreakParsingException());
                }
                break;
            case "ru":
            case "de":
            case "de_tr":
            case "de_wordclass":
            case "de_info":
            case "de_level":
            case "de_example":
            case "de_description":
            case "de_gptdescription":
            case "de_prap":
            case "en":
            case "en_tr":
            case "en_wordclass":
            case "en_info":
            case "en_level":
            case "en_example":
            case "en_description":
            case "en_gptdescription":
            case "en_prap":
                word.addItem(qName, buffer.toString());
                break;
        }
    }

   @Override
    public void endDocument() throws SAXException {

    }
}
