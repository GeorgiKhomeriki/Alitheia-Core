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

/**
 * WSUserGroup5.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package eu.sqooss.scl.axis2.datatypes;
/**
 *  WSUserGroup5 bean class
 */

public  class WSUserGroup5
implements org.apache.axis2.databinding.ADBBean{

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
            "http://datatypes.services.web.service.impl.sqooss.eu/xsd",
            "WSUserGroup",
    "ns1");



    /**
     * field for WSUserGroup
     */

    protected eu.sqooss.scl.axis2.datatypes.WSUserGroup localWSUserGroup ;


    /**
     * Auto generated getter method
     * @return eu.sqooss.scl.axis2.datatypes.WSUserGroup
     */
    public  eu.sqooss.scl.axis2.datatypes.WSUserGroup getWSUserGroup(){
        return localWSUserGroup;
    }



    /**
     * Auto generated setter method
     * @param param WSUserGroup
     */
    public void setWSUserGroup(eu.sqooss.scl.axis2.datatypes.WSUserGroup param){

        this.localWSUserGroup=param;


    }




    /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
            new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

            public void serialize(
                    javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {


                //We can safely assume an element has only one type associated with it

                if (localWSUserGroup==null){
                    throw new RuntimeException("Property cannot be null!");
                }
                localWSUserGroup.getOMElement(
                        MY_QNAME,
                        factory).serialize(xmlWriter);


            }

            /**
             * Util method to write an attribute with the ns prefix
             */
            private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (xmlWriter.getPrefix(namespace) == null) {
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);

                }

                xmlWriter.writeAttribute(namespace,attName,attValue);

            }

            /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
                {
                    xmlWriter.writeAttribute(attName,attValue);
                }
                else
                {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace,attName,attValue);
                }
            }

            /**
             * Register a namespace prefix
             */
            private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

            /**
             * Create a prefix
             */
            private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
            }
        };


        return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                MY_QNAME,factory,dataSource);

    }


    /**
     * databinding method to get an XML representation of this object
     *
     */
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){




        //We can safely assume an element has only one type associated with it
        return localWSUserGroup.getPullParser(MY_QNAME);

    }



    /**
     *  Factory class that keeps the parse method
     */
    public static class Factory{


        /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
        public static WSUserGroup5 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WSUserGroup5 object = new WSUserGroup5();
            int event;
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();


                boolean isReaderMTOMAware = false;

                try{
                    isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
                }catch(java.lang.IllegalArgumentException e){
                    isReaderMTOMAware = false;
                }



                while(!reader.isEndElement()) {
                    if (reader.isStartElement() ){

                        if (reader.isStartElement() && new javax.xml.namespace.QName("http://datatypes.services.web.service.impl.sqooss.eu/xsd","WSUserGroup").equals(reader.getName())){

                            object.setWSUserGroup(eu.sqooss.scl.axis2.datatypes.WSUserGroup.Factory.parse(reader));

                        }  // End of if for expected property start element

                        else{
                            // A start element we are not expecting indicates an invalid parameter was passed
                            throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        }

                    } else reader.next();  
                }  // end of while loop



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

    }//end of factory class



}

//vi: ai nosi sw=4 ts=4 expandtab
