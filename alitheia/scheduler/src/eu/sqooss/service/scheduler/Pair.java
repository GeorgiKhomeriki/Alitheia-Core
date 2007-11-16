/*
This file is part of the Alitheia system, developed by the SQO-OSS
consortium as part of the IST FP6 SQO-OSS project, number 033331.

Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
Copyright 2007 by KDAB (www.kdab.net)
Author: Christoph Schleifenbaum <christoph@kdab.net>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package eu.sqooss.service.scheduler;

import java.lang.Comparable;

/**
 * This class is a container for an object pair.
 * TODO: Move me to sqoutils
 *
 * @author Christoph Schleifenbaum
 */
public class Pair<T1 extends Comparable<T1>, T2 extends Comparable<T2>> implements Comparable<Pair<T1,T2>> {

    /**
     * First value.
     */
    public T1 first;

    /**
     * Second value.
     */
    public T2 second;

    /**
     * Default contructor.
     * Fills both values with null.
     */
    public Pair() {
        this.first = null;
        this.second = null;
    }

    /**
     * Constructor.
     * @param first The value used for first.
     * @param second The value used for second.
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Compares this object with the specified object for order.
     * @param other the Object to be compared.
     * @return negative integer, zero, or a positive integer as this object is 
     * less than, equal to, or greater than the specified object.
     */
    public int compareTo(Pair<T1,T2> other) {
        if (this == other) {
            return 0;
        }

        int firstComp = first.compareTo( other.first );
        
        return (firstComp != 0) ? firstComp : second.compareTo(other.second);
    }
}
