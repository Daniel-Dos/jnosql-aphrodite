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
package org.jnosql.aphrodite.antlr;


import org.antlr.v4.runtime.tree.ParseTree;
import org.jnosql.query.DelQuery;
import org.jnosql.query.DelQuerySupplier;
import org.jnosql.query.Value;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public final class AntlrDelQuerySupplier extends AbstractSupplier implements DelQuerySupplier {

    private List<Value<?>> keys = Collections.emptyList();

    @Override
    public void exitKeys(QueryParser.KeysContext ctx) {
        this.keys =  ctx.value().stream().map(ValueConverter::get).collect(toList());
    }

    @Override
    Function<QueryParser, ParseTree> getParserTree() {
        return QueryParser::del;
    }

    @Override
    public DelQuery apply(String query) {
        runQuery(query);
        return new DefaultDelQuery(keys);
    }

}
