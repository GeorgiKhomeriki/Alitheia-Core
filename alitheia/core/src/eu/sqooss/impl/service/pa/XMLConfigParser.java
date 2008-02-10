/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package eu.sqooss.impl.service.pa;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

//XML - Validation schema related
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;

//XML - Parser related
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//XML - DOM tree related
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import eu.sqooss.service.pa.ConfigUtils;
import eu.sqooss.service.pa.MetricConfig;

public class XMLConfigParser implements ConfigUtils{
    private static final String METRIC_ELEMENT  = "metric";

    private static final String METRIC_NAME     = "name";
    
    // User-defined XML datatypes
    private static final String TYPE_BYTE_ARRAY     = "ByteArray";
    private static final Object TYPE_STRING_ARRAY   = "StringArray";

    // Holds the XSD validation schema
    private File validation_schema = null;

    // Holds the XML based configuration file
    private File config_file = null;

    // Holds the DOM parser for the supplied configuration file
    private Document config_parser = null;

    private static class MetricConfigImpl implements MetricConfig {

        // A storage for string based configuration parameters
        private Hashtable<String, String> stringValues =
            new Hashtable<String, String>();

        // A storage for byte array based configuration parameters
        private Hashtable<String, ByteBuffer> byteArrays =
            new Hashtable<String, ByteBuffer>();

        // A storage for string array based configuration parameters
        private Hashtable<String, String[]> stringArrays =
            new Hashtable<String, String[]>();

        /********************************************************************
         * Parsers for user-defined XML datatypes
         */

        private ByteBuffer parseByteArray (Element e) {
            if (e.hasChildNodes()) {
                NodeList childs = e.getChildNodes();
                ByteBuffer values = ByteBuffer.allocate(10);
                for (int i=0; i < childs.getLength(); i++) {
                    Node c = childs.item(i);
                    // Skip any empty element
                    if ((c.getNodeType() == Node.ELEMENT_NODE)
                            && (c.getTextContent() != null)) {
                        Byte nextByte = new Byte(c.getTextContent());
                        values.put(nextByte.byteValue());
                    }
                }
                return values;
            }
            return null;
        }

        private String[] parseStringArray (Element e) {
            if (e.hasChildNodes()) {
                NodeList childs = e.getChildNodes();
                Vector<String> values = new Vector<String>();
                for (int i=0; i < childs.getLength(); i++) {
                    Node c = childs.item(i);
                    // Skip any empty element
                    if ((c.getNodeType() == Node.ELEMENT_NODE)
                            && (c.getTextContent() != null)) {
                        String nextString = new String(c.getTextContent());
                        values.add(nextString);
                    }
                }
                return (String[]) values.toArray(new String[]{});
            }
            return null;
        }

        private void parseElement (Element e) {
            if (e.getSchemaTypeInfo().getTypeName()
                    .equals(TYPE_BYTE_ARRAY)) {
                byteArrays.put(
                        e.getNodeName(),
                        parseByteArray(e));
            }
            if (e.getSchemaTypeInfo().getTypeName()
                    .equals(TYPE_STRING_ARRAY)) {
                stringArrays.put(
                        e.getNodeName(),
                        parseStringArray(e));
            }
            else {
                stringValues.put(
                        e.getNodeName(),
                        e.getTextContent().trim());
            }
        }

        public MetricConfigImpl(Element node) {
            // Retrieve the attributes of this node
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.item(i).getNodeName() != METRIC_NAME) {
                        stringValues.put (
                                attributes.item(i).getNodeName(),
                                attributes.item(i).getNodeValue());
                    }
                }
            }

            // Retrieve the child nodes for this node
            if (node.hasChildNodes()) {
                NodeList childs = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    switch (childs.item(j).getNodeType()) {
                    case Node.ELEMENT_NODE:
                        if (childs.item(j).getTextContent() != null) {
                            parseElement((Element) childs.item(j));
                        }
                        break;
                    }
                }
            }
        }

        public boolean containsKey(String value) {
            return stringValues.containsKey(value);
        }

        public String getString(String key) {
            return stringValues.get(key);
        }

        public Hashtable<String, String> getConfiguration() {
            return stringValues;
        }

        public Set<String> keySet() {
            return stringValues.keySet();
        }

        public byte[] getByteArray(String key) {
            if (byteArrays.containsKey(key)) {
                return byteArrays.get(key).array();
            }
            return null;
        }

        public String[] getStringArray(String key) {
            if (stringArrays.containsKey(key)) {
                return stringArrays.get(key);
            }
            return null;
        }

    }

    /**
     * Simple constructor requiring only a configuration file
     * 
     * @param config_file       the name of the XML-based configuration file
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public XMLConfigParser(String config_file) {
        this.config_file = new File(config_file);

        // Obtain a XML parser factory
        DocumentBuilderFactory parser_factory =
            DocumentBuilderFactory.newInstance();

        // Obtain a XML parser and retrieve the DOM tree
        try {
            this.config_parser =
                parser_factory.newDocumentBuilder().parse(this.config_file);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * When the XML-based configuration file is accompanied with a validation
     * schema, then this constructor must be used.
     * 
     * @param config_file       the name of the XML-based configuration file
     * @param validation_schema the name of the XSD validation schema file
     */
    public XMLConfigParser(String config_file, String validation_schema) {
        this.config_file = new File(config_file);
        this.validation_schema = new File(validation_schema);

        // Obtain a schema factory
        SchemaFactory schema_factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Create a custom XML validation schema from the XSD template
        Schema custom_schema;
        try {
            custom_schema = schema_factory.newSchema(this.validation_schema);
            // Obtain a XML parser factory
            DocumentBuilderFactory parser_factory =
                DocumentBuilderFactory.newInstance();

            // Instruct the parser factory to use the customised XML schema
            parser_factory.setSchema(custom_schema);

            // Obtain a XML parser and retrieve the DOM tree
            try {
                this.config_parser =
                    parser_factory.newDocumentBuilder().parse(this.config_file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public HashMap<String, MetricConfig> getMetricsConfiguration() {
        HashMap<String, MetricConfig> metrics_config =
            new HashMap<String, MetricConfig>();

        if (config_parser != null) {
            NodeList configuration =
                config_parser.getElementsByTagName(METRIC_ELEMENT);
            for (int i = 0; i < configuration.getLength(); i++) {
                Element metric_element = (Element) configuration.item(i);
                MetricConfig next_metric_config =
                    new MetricConfigImpl(metric_element);

                metrics_config.put(
                        metric_element.getAttribute(METRIC_NAME),
                        next_metric_config);
            }

            return(metrics_config);
        }

        return null;
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
