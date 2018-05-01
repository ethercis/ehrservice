package com.ethercis.aql.sql.binding;

import com.ethercis.aql.definition.VariableDefinition;

import java.util.List;

/**
 * Created by christian on 4/27/2018.
 */
public class WhereClause {

    List whereItems;

    public WhereClause(List whereItems) {
        this.whereItems = whereItems;
    }

    public String compositionName() {

        for (int cursor = 0; cursor < whereItems.size(); cursor++) {
            Object item = whereItems.get(cursor);

            if (item instanceof VariableDefinition) {

            }
        }

        return null;

    }
}
