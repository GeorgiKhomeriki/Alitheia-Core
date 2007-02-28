/*$Id: */
package eu.sqooss.vcs;

import java.util.Iterator;
import java.util.Vector;

public class CommitLog implements Iterable<CommitLogEntry> {

    /*TODO:perhaps arrange entries by date*/
    private Vector<CommitLogEntry> entries;

    private Revision start, end;

    public CommitLog(Revision start, Revision end) {
	if ((start == null) || (end == null)) {
	    throw new IllegalArgumentException();
	}

	this.start = start;
	this.end = end;
	entries = new Vector<CommitLogEntry>();
    }

    public Revision getStart() {
	return start;
    }

    public Revision getEnd() {
	return end;
    }

    Vector<CommitLogEntry> getEntries() {
	return entries;
    }

    public void add(CommitLogEntry entry) {
	entries.add(entry);
    }

    public boolean remove(CommitLogEntry entry) {
	return entries.remove(entry);
    }

    public void clear() {
	entries.clear();
    }

    public int size() {
	return entries.size();
    }

    public Iterator<CommitLogEntry> iterator() {
	return entries.iterator();
    }
}
