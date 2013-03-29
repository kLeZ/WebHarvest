package org.webharvest.utils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;

/**
 * @author Vladimir Nikic
 * Date: May 9, 2007
 */
public class XmlValidator extends DefaultHandler {

    private int lineNumber, columnNumber;
    private Exception exception;

    public boolean parse(InputSource in) {
        try {
            SAXParser parser = XmlUtil.getSAXParserFactory(false, false).newSAXParser();

            // call parsing
            parser.parse(in, this);

            this.exception = null;
            this.lineNumber = 0;
            this.columnNumber = 0;

            return true;
        } catch (IOException e) {
            this.exception = e;
        } catch (ParserConfigurationException e) {
            this.exception = e;
        } catch (SAXParseException e) {
            this.exception = e;
            this.lineNumber = e.getLineNumber();
            this.columnNumber = e.getColumnNumber();
        } catch (SAXException e) {
            this.exception = e;
        }
        return false;
    }

    public Exception getException() {
        return exception;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

}