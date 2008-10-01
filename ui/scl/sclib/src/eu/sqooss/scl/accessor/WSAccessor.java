/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
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

package eu.sqooss.scl.accessor;

import java.lang.reflect.Array;

/**
 * The <code>WSAccessor</code> class is the superclass of all accessors.
 * It contains a type constant for each accessor. 
 */
public abstract class WSAccessor {

    /**
     * The <code>Type</code> constants are used in
     * {@link WSSession#getAccessor(Type)}
     */
    public static enum Type {
        PROJECT,
        USER,
        METRIC
    }

    /**
     * Normalize a web-service result which is an array to
     * undo the munging applied by Axis. You can't send zero-
     * length arrays or null arrays, apparently, so this
     * function un-munges the representation [null] into
     * a zero length array.
     *
     * If the web-service result is not an array, nothing
     * happens, so this is safe to apply to any web-service result.
     *
     * @param result Object, possibly an array, to de-munge
     * @return Un-munged object, if applicable
     */
    protected Object normalizeWSArrayResult(Object result) {
        if ((result != null) && (result.getClass().isArray()) &&
                (Array.getLength(result) != 0) && (Array.get(result, 0) == null)) {
            return Array.newInstance(result.getClass().getComponentType(), 0);
        } else {
            return result;
        }
    }

    /**
     * The method normalizes the web-service array parameter.
     * You can't use null or zero-length array.
     *  
     * @param arr - the long array
     * 
     * @return <code>true</code> if the array isn't null and isn't empty
     */
    protected boolean isNormalizedWSArrayParameter(long[] arr) {
        return ((arr != null) && (arr.length > 0));
    }

    /**
     * The method normalizes the web-service array parameter.
     * You can't use null or zero-length array.
     *  
     * @param arr - the long array
     * 
     * @return <code>true</code> if the array isn't null and isn't empty
     */
    protected boolean isNormalizedWSArrayParameter(String[] arr) {
        return ((arr != null) && (arr.length > 0));
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
