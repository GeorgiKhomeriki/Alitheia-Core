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

package eu.sqooss.scl.result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * A WSResultEntry tries to be as generic as possible by storing
 * data to the least common denominator of containers, the byte array.
 * Each entry has an assosiated mime type. Mime types are used by the
 * users of the object to convert the byte array store to something 
 * they can use. Some convenience conversion functions are provided
 * with this object.
 * 
 *  Supported mime types include:
 *  <ul>
 *      <li>type/{integer, float, double}</li>
 *      <li>text/{plain, html, xml}</li>
 *      <li>image/{gif, png, jpeg}</li>
 *  </ul>
 * 
 */
public class WSResultEntry {

    /** Represents "type/integer" MIME type. */
    public static final String MIME_TYPE_TYPE_INTEGER = "type/integer";
    
    /** Represents "type/float" MIME type. */
    public static final String MIME_TYPE_TYPE_FLOAT   = "type/float";
    
    /** Represents "type/double" MIME type. */
    public static final String MIME_TYPE_TYPE_DOUBLE  = "type/double";
    
    /** Represents "text/plain" MIME type. */
    public static final String MIME_TYPE_TEXT_PLAIN   = "text/plain";
    
    /** Represents "text/html" MIME type. */
    public static final String MIME_TYPE_TEXT_HTML    = "text/html";
    
    /** Represents "text/xml" MIME type. */
    public static final String MIME_TYPE_TEXT_XML     = "text/xml";
    
    /** Represents "image/gif" MIME type. */
    public static final String MIME_TYPE_IMAGE_GIF    = "image/gif";
    
    /** Represents "image/png" MIME type. */
    public static final String MIME_TYPE_IMAGE_PNG    = "image/png";
    
    /** Represents "image/jpeg" MIME type. */
    public static final String MIME_TYPE_IMAGE_JPEG   = "image/jpeg";
    
    private Object value;
    private byte[] valueByteArray;
    private String mimeType;
    
    /**
     * Creates a new WSResultEntry object.
     * The constructor creates the byte array with the {@link java.io.ObjectOutputStream#writeObject(Object)}.
     * (i.e. the primitive types are represent with their wrapper)
     * 
     * @param value - An object encapsulating the value to be returned
     * @param mimeType - The mime type of the result value
     * @throws RuntimeException - When serialization of the passed object fails
     * @throws IllegalArgumentException if MIME type isn't compatible with the value
     */
    public WSResultEntry(Object value, String mimeType) {
        validate(value, mimeType);
        this.value = value;
        this.mimeType = mimeType;

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(value);
            oout.close();
            valueByteArray = bout.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * The byte array representation is made with the {@link java.io.ObjectOutputStream#writeObject(Object)}.
     * The primitive types are represent with their wrappers.
     * @return The byte array representation of the <code>WSResultEntry</code>.
     */
    public byte[] getByteArray() {
        return valueByteArray;
    }
    
    /**
     * @return The MIME type of the <code>WSResultEntry</code>.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return The integer value of the <code>WSResultEntry</code>.
     * @throws IllegalStateException if the WSResultEntry isn't an integer.
     */
    public int getInteger() {
        if (value instanceof Integer) {
            return (Integer)value;
        } else {
            throw new IllegalStateException("The metric result entry isn't integer!");
        }
    }
    
    /**
     * @return The float value of the <code>WSResultEntry</code>.
     * @throws IllegalStateException if the WSResultEntry isn't a float.
     */
    public float getFloat() {
        if (value instanceof Float) {
            return (Float)value;
        } else {
            throw new IllegalStateException("The metric result entry isn't float!");
        }
    }
    
    /**
     * @return The double value of the <code>WSResultEntry</code>.
     * @throws IllegalStateException if the WSResultEntry isn't a double.
     */
    public double getDouble() {
        if (value instanceof Double) {
            return (Double)value;
        } else {
            throw new IllegalStateException("The metric result entry isn't double!");
        }
    }
    
    /**
     * @return The string value of the <code>WSResultEntry</code>.
     * @throws IllegalStateException if the WSResultEntry isn't a string.
     */
    public String getString() {
        if (value instanceof String) {
            return (String)value;
        } else {
            throw new IllegalStateException("The metric result entry isn't string!");
        }
    }
    
    public String toXML() {
        throw new UnsupportedOperationException("Coming soon!");
    }
    
    public static WSResultEntry fromXML(String xml) {
        throw new UnsupportedOperationException("Coming soon!");
    }
    
    private void validate(Object value, String mimeType) {
        if (((MIME_TYPE_TYPE_INTEGER.equals(mimeType)) && (value instanceof Integer)) ||
                ((MIME_TYPE_TYPE_FLOAT.equals(mimeType)) && (value instanceof Float)) ||
                ((MIME_TYPE_TYPE_DOUBLE.equals(mimeType)) && (value instanceof Double))) {
            return;
        }
        
        if ((MIME_TYPE_TEXT_PLAIN.equals(mimeType) ||
                MIME_TYPE_TEXT_HTML.equals(mimeType) ||
                MIME_TYPE_TEXT_XML.equals(mimeType)) && (value instanceof String)) {
            return;
        }
        
        if (MIME_TYPE_IMAGE_GIF.equals(mimeType) &&
                MIME_TYPE_IMAGE_JPEG.equals(mimeType) &&
                MIME_TYPE_IMAGE_PNG.equals(mimeType)) {
            return;
        }
        
        throw new IllegalArgumentException("The MIME type isn't compatible with the value!");
    }
}


//vi: ai nosi sw=4 ts=4 expandtab
