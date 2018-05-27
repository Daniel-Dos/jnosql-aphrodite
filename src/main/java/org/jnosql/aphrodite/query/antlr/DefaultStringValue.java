/*
 *  Copyright (c) 2018 Otávio Santana and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */

package org.jnosql.aphrodite.query.antlr;

import org.jnosql.aphrodite.query.StringValue;

import java.util.Objects;

final class DefaultStringValue implements StringValue {

    private final String value;

    DefaultStringValue(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultStringValue)) {
            return false;
        }
        DefaultStringValue that = (DefaultStringValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    public static StringValue of(SelectParser.StringContext context) {
        String text = context.STRING().getText();
        return getStringValue(text);
    }

    public static StringValue of(DeleteParser.StringContext context) {
        String text = context.STRING().getText();
        return getStringValue(text);
    }

    private static StringValue getStringValue(String text) {
        return new DefaultStringValue(text.substring(1, text.length() - 1));
    }
}
