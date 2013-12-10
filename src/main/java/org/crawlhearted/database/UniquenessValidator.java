package org.crawlhearted.database;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

/**
 * This is a validator for the database models. It checks if the given attribute is unique in the database.
 *
 * Source: https://groups.google.com/forum/#!topic/activejdbc-group/idx8lGfRzfo
 */

public class UniquenessValidator extends ValidatorAdapter {

    private final String attribute;

    public UniquenessValidator(String attribute) {
        this.attribute = attribute;
        setMessage(attribute + " should be unique!");
    }

    @Override
    public void validate(Model m) {
        if(Base.count(Model.getTableName(), attribute + " = ? ", m.get(attribute)) > 0) {
            m.addValidator(this, attribute);
        }
    }

}
