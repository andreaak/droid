package com.andreaak.cards.utils;

import androidx.annotation.Nullable;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.DeVerbItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class XmlParser {

    public static LessonItem parseLesson(String path, String prefix) {

        LessonItem lesson = new LessonItem(new File(path), prefix, true);
        parseLesson(lesson);
        return lesson;
    }

    public static LessonItem parseLesson(LessonItem lesson) {

        lesson.clear();

        Document doc = getXMLDocument(lesson.getFile());
        NodeList words = doc.getElementsByTagName("word");
        for (int i = 0; i < words.getLength(); i++) {
            Node node = words.item(i);
            WordItem word = parseWord(node, i);
            lesson.add(word);
        }

        return lesson;
    }

    public static ArrayList<SimpleWordItem> getSimpleWordItems(String path) {

        try {
            SAXParserFactory fabrique = SAXParserFactory.newInstance();
            SAXParser parser = fabrique.newSAXParser();

            File file = new File(path);
            SimpleWordItemHandler handler = new SimpleWordItemHandler(path);
            parser.parse(file, handler);

            return handler.words;
        } catch (FileNotFoundException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static boolean parseVerbForm(VerbForm verbForm) {

        verbForm.clear();

        Document doc = getXMLDocument(verbForm.getFile());
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
        return true;
    }


    public static WordItem getWordItem(SimpleWordItem wordItem, String lang) {

        WordItemHandler handler = new WordItemHandler(wordItem, lang);
        try {

            SAXParserFactory fabrique = SAXParserFactory.newInstance();
            SAXParser parser = fabrique.newSAXParser();
            File file = new File(wordItem.getPath());
            parser.parse(file, handler);

        } catch (SAXException e) {
            if (!(e.getCause() instanceof BreakParsingException)) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        } catch (ParserConfigurationException | IOException e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return handler.result;
    }

    private static WordItem parseWord(Node node, int id) {

        WordItem word = new WordItem(id);

        NodeList items = node.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            short type = item.getNodeType();
            if (type == 1) {
                String nodeTag = item.getNodeName();
                Node child = item.getFirstChild();
                if(child != null) {
                    String value = item.getFirstChild().getNodeValue();
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

    private static Document getXMLDocument(File file){
        try {
            InputSource input = new InputSource(new FileReader(file));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(input);
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public static VerbLessonItem parseVerbLesson(String path) {

        return parseVerbLesson(new File(path));
    }

    public static VerbLessonItem parseVerbLesson(File lessonFile) {

        VerbLessonItem lesson = new VerbLessonItem(lessonFile.getName(), lessonFile.getAbsolutePath());

        Document doc = getXMLDocument(lessonFile);
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

    public static final String FileKey = "file";

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

            Cache.getInstance().add(FileKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createXml(File xmlFile, XmlDelegate delegate) throws ParserConfigurationException, TransformerException {

        // Создаем XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();


        delegate.execute(document);

        // Сохранение
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
        );

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }

    public static void mergeXmlFiles(List<String> xmlFiles) {

        try {
            String directory = AppConfigs.getInstance().getStudyDir();
            String fileName = getNextFilePath(directory, true);
            File outputFile = new File(directory, fileName);

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            // Новый итоговый XML
            Document resultDoc = builder.newDocument();

            // Корневой элемент
            Element root =
                    resultDoc.createElement("words");

            resultDoc.appendChild(root);

            // Проходим по всем XML
            for (String xmlFile : xmlFiles) {

                File file = new File(xmlFile);
                Document doc = builder.parse(file);

                NodeList words =
                        doc.getElementsByTagName("word");

                for (int i = 0; i < words.getLength(); i++) {

                    Node importedNode =
                            resultDoc.importNode(
                                    words.item(i),
                                    true
                            );

                    root.appendChild(importedNode);
                }
            }

            // Сохранение
            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();

            Transformer transformer =
                    transformerFactory.newTransformer();

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.transform(
                    new DOMSource(resultDoc),
                    new StreamResult(outputFile)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getLastNumber(File dir, String date) {
        // Ищем следующий порядковый номер
        int maxNumber = 0;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                // Проверяем формат YYYYMMDD_XX.xml
                if (name.startsWith(date + "_") && name.endsWith(".xml")) {
                    try {

                        String numberPart =
                                name.replace("combined_", "").substring(
                                        date.length() + 1,
                                        name.length() - 4
                                );

                        int number = Integer.parseInt(numberPart);
                        if (number > maxNumber) {
                            maxNumber = number;
                        }

                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // Следующий номер
        return maxNumber;
    }

    public static boolean addItemToFile(WordItem wordItem, String primaryLang, String secondaryLang) {


        try {

            String directory = AppConfigs.getInstance().getStudyDir();
            String fileName = Cache.getInstance().getItem(FileKey);
            if(Utils.isEmpty(fileName)) {
                return false;
            }

            File file = new File(directory, fileName);

            // Загрузка XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            // Корневой элемент <words>
            Element root = doc.getDocumentElement();

            // Создаем <word>
            Element word = doc.createElement("word");

            // <ru>
            Element ru = doc.createElement(primaryLang);
            ru.setTextContent(wordItem.getValue(primaryLang));
            word.appendChild(ru);

            // <de>
            Element de = doc.createElement(secondaryLang);
            de.setTextContent(wordItem.getValue(secondaryLang));
            word.appendChild(de);

            // <de_wordclass>
            Element wordClass = doc.createElement( String.format("%s_wordclass", secondaryLang));
            wordClass.setTextContent(wordItem.getWordClass());
            word.appendChild(wordClass);

            // <de_level>
            Element level = doc.createElement(String.format("%s_level", secondaryLang));
            level.setTextContent(wordItem.getLevel(secondaryLang));
            word.appendChild(level);

            // <de_description>
            Element description = doc.createElement(String.format("%s_description", secondaryLang));
            description.setTextContent(wordItem.getDescription(secondaryLang));
            word.appendChild(description);

            // <de_gptdescription>
            Element gpt = doc.createElement(String.format("%s_gptdescription", secondaryLang));
            gpt.setTextContent(wordItem.getGPTDescription(secondaryLang));
            word.appendChild(gpt);

            Element example = doc.createElement(String.format("%s_example", secondaryLang));
            example.setTextContent(wordItem.getExample(secondaryLang));
            word.appendChild(example);

            Element tr = doc.createElement(String.format("%s_tr", secondaryLang));
            tr.setTextContent(wordItem.getTranscription(secondaryLang));
            word.appendChild(tr);

            Element info = doc.createElement(String.format("%s_info", secondaryLang));
            info.setTextContent(wordItem.getInfo(secondaryLang));
            word.appendChild(info);

            Element prap = doc.createElement(String.format("%s_prap", secondaryLang));
            prap.setTextContent(wordItem.getPrap(secondaryLang));
            word.appendChild(prap);

            // Добавляем <word> в <words>
            root.appendChild(word);

            // Сохраняем XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "4"
            );

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            transformer.transform(source, result);

            System.out.println("Новый word добавлен.");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean openLastFile() {
        String directory = AppConfigs.getInstance().getStudyDir();
        try {

            String fileName = getLastFilePath(directory);
            if (fileName == null) {
                return false;
            }

            Cache.getInstance().add(FileKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Nullable
    private static String getLastFilePath(String directory) {
        // Создаем папку если нет
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Текущая дата
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int number = getLastNumber(dir, date);
        if(number == 0) {
            return null;
        }
        // Форматируем как 01, 02, 03...
        String sequence = String.format("%02d", number);
        // Имя файла
        String fileName = date + "_" + sequence + ".xml";
        return fileName;
    }

    @Nullable
    private static String getNextFilePath(String directory, boolean isCombined) {
        // Создаем папку если нет
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Текущая дата
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int number = getLastNumber(dir, date) + 1;

        // Форматируем как 01, 02, 03...
        String sequence = String.format("%02d", number);
        // Имя файла
        String fileName = date + "_" + (isCombined ? "combined_" : "") + sequence + ".xml";
        return fileName;
    }
}

interface XmlDelegate {
    void execute(Document document);
}