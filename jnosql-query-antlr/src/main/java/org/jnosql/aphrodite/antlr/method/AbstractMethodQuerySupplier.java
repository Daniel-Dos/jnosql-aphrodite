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
package org.jnosql.aphrodite.antlr.method;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jnosql.aphrodite.antlr.MethodBaseListener;
import org.jnosql.aphrodite.antlr.MethodLexer;
import org.jnosql.aphrodite.antlr.MethodParser;
import org.jnosql.aphrodite.antlr.QueryErrorListener;
import org.jnosql.query.Condition;
import org.jnosql.query.ConditionValue;
import org.jnosql.query.Operator;
import org.jnosql.query.ParamValue;
import org.jnosql.query.Where;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static org.jnosql.query.Operator.AND;
import static org.jnosql.query.Operator.EQUALS;
import static org.jnosql.query.Operator.NOT;
import static org.jnosql.query.Operator.OR;

abstract class AbstractMethodQuerySupplier extends MethodBaseListener {

    protected Where where;

    protected Condition condition;

    protected boolean and = true;

    protected void runQuery(String query) {

        CharStream stream = CharStreams.fromString(query);
        MethodLexer lexer = new MethodLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MethodParser parser = new MethodParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(QueryErrorListener.INSTANCE);
        parser.addErrorListener(QueryErrorListener.INSTANCE);

        ParseTree tree = getParserTree().apply(parser);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);

        if (Objects.nonNull(condition)) {
            this.where = new MethodWhere(condition);
        }
    }

    abstract Function<MethodParser, ParseTree> getParserTree();

    @Override
    public void exitEq(MethodParser.EqContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        ParamValue paramValue = new MethodParamValue(variable);
        checkCondition(new MethodCondition(variable, EQUALS, paramValue), hasNot);
    }

    private void checkCondition(Condition condition, boolean hasNot) {
        Condition newCondition = checkNotCondition(condition, hasNot);
        if (Objects.isNull(this.condition)) {
            this.condition = newCondition;
            return;
        }
        if (and) {
            appendCondition(AND, newCondition);
        } else {
            appendCondition(OR, newCondition);
        }

    }

    private String getVariable(MethodParser.VariableContext ctx) {
        String text = ctx.getText();
        String capital = String.valueOf(text.charAt(0)).toUpperCase(Locale.US);
        return capital.concat(text.substring(1));
    }


    private boolean isAppendable(Condition condition) {
        return (AND.equals(condition.getOperator()) || OR.equals(condition.getOperator()));
    }

    private boolean isNotAppendable() {
        return !isAppendable(this.condition);
    }

    private Condition checkNotCondition(Condition condition, boolean hasNot) {
        if (hasNot) {
            ConditionValue conditions = MethodConditionValue.of(Collections.singletonList(condition));
            return new MethodCondition("_NOT", NOT, conditions);
        } else {
            return condition;
        }
    }

    private void appendCondition(Operator operator, Condition newCondition) {

        if (operator.equals(this.condition.getOperator())) {
            ConditionValue conditionValue = ConditionValue.class.cast(this.condition.getValue());
            List<Condition> conditions = new ArrayList<>(conditionValue.get());
            conditions.add(newCondition);
            this.condition = new MethodCondition("_" + operator.name(), operator, MethodConditionValue.of(conditions));
        } else if (isNotAppendable()) {
            List<Condition> conditions = Arrays.asList(this.condition, newCondition);
            this.condition = new MethodCondition("_" + operator.name(), operator, MethodConditionValue.of(conditions));
        } else {
            List<Condition> conditions = ConditionValue.class.cast(this.condition.getValue()).get();
            Condition lastCondition = conditions.get(conditions.size() - 1);

            if (isAppendable(lastCondition) && operator.equals(lastCondition.getOperator())) {
                List<Condition> lastConditions = new ArrayList<>(ConditionValue.class.cast(lastCondition.getValue()).get());
                lastConditions.add(newCondition);

                Condition newAppendable = new MethodCondition("_" + operator.name(),
                        operator, MethodConditionValue.of(lastConditions));

                List<Condition> newConditions = new ArrayList<>(conditions.subList(0, conditions.size() - 1));
                newConditions.add(newAppendable);
                this.condition = new MethodCondition(this.condition.getName(), this.condition.getOperator(),
                        MethodConditionValue.of(newConditions));
            } else {
                Condition newAppendable = new MethodCondition("_" + operator.name(),
                        operator, MethodConditionValue.of(Collections.singletonList(newCondition)));

                List<Condition> newConditions = new ArrayList<>(conditions);
                newConditions.add(newAppendable);
                this.condition = new MethodCondition(this.condition.getName(), this.condition.getOperator(),
                        MethodConditionValue.of(newConditions));
            }

        }
    }
}
