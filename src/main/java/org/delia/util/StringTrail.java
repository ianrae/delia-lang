package org.delia.util;

public class StringTrail {
    private StringBuilder trail = new StringBuilder();
    private boolean isEmpty = true;
    
    public void add(String s) {
    	if (s == null) {
    		return;
    	}
        if (isEmpty) {
        	isEmpty = false;
            trail.append(s);
        } else {
            trail.append(';');
            trail.append(s);
        }
    }
    
    public String getTrail() {
        return trail.toString();
    }

}
