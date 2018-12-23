package com.ethercis.dao.access.jooq;

import org.openehr.rm.datatypes.text.DvCodedText;

public class DBDvCodedText {

    String codedDvCodedText;

    public DBDvCodedText(String codedDvCodedText) {
        this.codedDvCodedText = codedDvCodedText;
    }

    public DvCodedText decode(){
        String[] tokens = codedDvCodedText.split("::");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("failed to parse DvCodedText \'" + codedDvCodedText + "\', wrong number of tokens.");
        } else {
            String[] tokens2 = tokens[1].split("\\|");
            if (tokens2.length != 2) {
                throw new IllegalArgumentException("failed to parse DvCodedText \'" + codedDvCodedText + "\', wrong number of tokens.");
            } else {
                return new DvCodedText(tokens2[1], tokens[0], tokens2[0]);
            }
        }
    }


}
