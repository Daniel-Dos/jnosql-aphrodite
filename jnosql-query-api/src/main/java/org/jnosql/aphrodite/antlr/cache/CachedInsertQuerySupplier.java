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
package org.jnosql.aphrodite.antlr.cache;

import org.jnosql.aphrodite.antlr.AntlrInsertQuerySupplier;
import org.jnosql.query.InsertQuery;
import org.jnosql.query.InsertQuerySupplier;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The {@link AntlrInsertQuerySupplier} cache wrapper.
 */
public final class CachedInsertQuerySupplier implements InsertQuerySupplier {

    private final Map<String, InsertQuery> cached;

    private final InsertQuerySupplier supplier;

    public CachedInsertQuerySupplier() {
        supplier = new AntlrInsertQuerySupplier();
        this.cached = TTLCache.of(5, TimeUnit.MINUTES, supplier);
    }

    @Override
    public InsertQuery apply(String query) {
        Objects.requireNonNull(query, "query is required");
        return cached.get(query);
    }
}
