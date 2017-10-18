package org.dhatim.safesql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractSafeSqlBuilder<S extends AbstractSafeSqlBuilder<S>> implements SafeSqlizable, SafeSqlAppendable {

    static class Position {

        private final int sqlPosition;
        private final int paramPosition;

        private Position(int sqlPosition, int paramPosition) {
            this.sqlPosition = sqlPosition;
            this.paramPosition = paramPosition;
        }

    }

    private static final String DEFAULT_SEPARATOR = ", ";
    private static final char[] HEX_CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    protected final S myself;

    protected final StringBuilder sqlBuilder;
    protected final List<Object> parameters;

    public AbstractSafeSqlBuilder(Class<S> selfType, StringBuilder stringBuilder, List<Object> parameters) {
        this.myself = selfType.cast(this);
        this.sqlBuilder = stringBuilder;
        this.parameters = parameters;
    }

    @Override
    public S param(int num) {
        appendObject(num);
        return myself;
    }

    @Override
    public S param(long num) {
        appendObject(num);
        return myself;
    }

    @Override
    public S param(double num) {
        appendObject(num);
        return myself;
    }

    @Override
    public S param(boolean bool) {
        appendObject(bool);
        return myself;
    }

    @Override
    public S param(Object obj) {
        appendObject(obj);
        return myself;
    }

    @Override
    public S params(Object... parameters) {
        if (parameters.length == 1) {
            param(parameters[0]);
        } else if (parameters.length != 0) {
            for (int i=0; i<parameters.length; i++) {
                if (i > 0) {
                    append(DEFAULT_SEPARATOR);
                }
                param(parameters[i]);
            }
        }
        return myself;
    }

    @Override
    public S params(Iterable<?> iterable) {
        paramsIterator(DEFAULT_SEPARATOR, iterable.iterator());
        return myself;
    }

    @Override
    public S params(Stream<?> stream) {
        paramsIterator(DEFAULT_SEPARATOR, stream.iterator());
        return myself;
    }

    private void paramsIterator(String delimiter, Iterator<?> iterator) {
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                append(delimiter);
            }
            param(iterator.next());
        }
    }

    private void paramsIterator(SafeSql delimiter, Iterator<?> iterator) {
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                append(delimiter);
            }
            param(iterator.next());
        }
    }

    @Override
    public S append(SafeSql sql) {
        sqlBuilder.append(sql.asSql());
        parameters.addAll(Arrays.asList(sql.getParameters()));
        return myself;
    }

    @Override
    public S append(SafeSqlizable sqlizable) {
        sqlizable.appendTo(this);
        return myself;
    }

    @Override
    public S append(String s) {
        sqlBuilder.append(s);
        return myself;
    }

    @Override
    public S append(char ch) {
        sqlBuilder.append(ch);
        return myself;
    }

    @Override
    public S append(int i) {
        sqlBuilder.append(i);
        return myself;
    }

    @Override
    public S append(long l) {
        sqlBuilder.append(l);
        return myself;
    }

    private void append(AbstractSafeSqlBuilder<?> other) {
        sqlBuilder.append(other.sqlBuilder);
        parameters.addAll(other.parameters);
    }

    /**
     * write a string literal by escaping
     *
     * @param s Append this string as literal string in SQL code
     * @return a reference to this object.
     */
    @Override
    public S literal(String s) {
        sqlBuilder.append(SafeSqlUtils.escapeString(s));
        return myself;
    }

    @Override
    public S format(String sql, Object... args) {
        SafeSqlUtils.formatTo(this, sql, args);
        return myself;
    }

    @Override
    public S joined(String delimiter, Iterable<String> iterable) {
        SafeSqlJoiner joiner = new SafeSqlJoiner(SafeSqlUtils.fromConstant(delimiter));
        iterable.forEach(joiner::add);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joined(String delimiter, String prefix, String suffix, Iterable<String> iterable) {
        SafeSqlJoiner joiner = new SafeSqlJoiner(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix));
        iterable.forEach(joiner::add);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joined(String delimiter, Stream<String> stream) {
        SafeSqlJoiner joiner = stream.collect(() -> new SafeSqlJoiner(SafeSqlUtils.fromConstant(delimiter)),
                SafeSqlJoiner::add, SafeSqlJoiner::merge);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joined(String delimiter, String prefix, String suffix, Stream<String> stream) {
        SafeSqlJoiner joiner = stream.collect(() -> new SafeSqlJoiner(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix)),
                SafeSqlJoiner::add, SafeSqlJoiner::merge);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joinedSafeSqls(SafeSql delimiter, Iterable<SafeSql> iterable) {
        return joinedSafeSqls(delimiter, SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, iterable);
    }

    @Override
    public S joinedSafeSqls(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Iterable<SafeSql> iterable) {
        SafeSqlJoiner joiner = new SafeSqlJoiner(delimiter, prefix, suffix);
        iterable.forEach(joiner::add);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joinedSafeSqls(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Stream<SafeSql> stream) {
        SafeSqlJoiner joiner = stream.collect(() -> new SafeSqlJoiner(delimiter, prefix, suffix), SafeSqlJoiner::add, SafeSqlJoiner::merge);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joinedSafeSqls(SafeSql delimiter, Stream<SafeSql> stream) {
        return joinedSafeSqls(delimiter, SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, stream);
    }

    @Override
    public S joinedSafeSqls(String delimiter, Iterable<SafeSql> iterable) {
        return joinedSafeSqls(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, iterable);
    }

    @Override
    public S joinedSafeSqls(String delimiter, Stream<SafeSql> stream) {
        return joinedSafeSqls(delimiter, "", "", stream);
    }

    @Override
    public S joinedSafeSqls(String delimiter, String prefix, String suffix, Iterable<SafeSql> iterable) {
        return joinedSafeSqls(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix), iterable);
    }

    @Override
    public S joinedSafeSqls(String delimiter, String prefix, String suffix, Stream<SafeSql> stream) {
        return joinedSafeSqls(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix), stream);
    }

    @Override
    public S joinedSqlizables(SafeSql delimiter, Iterable<? extends SafeSqlizable> iterable) {
        return joinedSqlizables(delimiter, SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, iterable);
    }

    @Override
    public S joinedSqlizables(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Iterable<? extends SafeSqlizable> iterable) {
        SafeSqlJoiner joiner = new SafeSqlJoiner(delimiter, prefix, suffix);
        iterable.forEach(joiner::add);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joinedSqlizables(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Stream<? extends SafeSqlizable> stream) {
        SafeSqlJoiner joiner = stream.collect(() -> new SafeSqlJoiner(delimiter, prefix, suffix), SafeSqlJoiner::add, SafeSqlJoiner::merge);
        joiner.appendTo(this);
        return myself;
    }

    @Override
    public S joinedSqlizables(SafeSql delimiter, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(delimiter, SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, stream);
    }

    @Override
    public S joinedSqlizables(String delimiter, Iterable<? extends SafeSqlizable> iterable) {
        return joinedSqlizables(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, iterable);
    }

    @Override
    public S joinedSqlizables(String delimiter, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.EMPTY, SafeSqlUtils.EMPTY, stream);
    }

    @Override
    public S joinedSqlizables(String delimiter, String prefix, String suffix, Iterable<? extends SafeSqlizable> iterable) {
        return joinedSqlizables(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix), iterable);
    }

    @Override
    public S joinedSqlizables(String delimiter, String prefix, String suffix, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(SafeSqlUtils.fromConstant(delimiter), SafeSqlUtils.fromConstant(prefix), SafeSqlUtils.fromConstant(suffix), stream);
    }

    /**
     * Write a byte array as literal in PostgreSQL
     *
     * @param bytes bytes to write as literal
     * @return a reference to this object.
     */
    @Override
    public S literal(byte[] bytes) {
        sqlBuilder.append("'\\x");
        for (byte b : bytes) {
            sqlBuilder.append(HEX_CODE[(b >> 4) & 0xF]);
            sqlBuilder.append(HEX_CODE[(b & 0xF)]);
        }
        sqlBuilder.append('\'');
        return myself;
    }

    @Override
    public S identifier(String identifier) {
        sqlBuilder.append(SafeSqlUtils.mayEscapeIdentifier(identifier));
        return myself;
    }

    @Override
    public S identifier(String container, String identifier) {
        if (null == container) {
            return identifier(identifier);
        } else {
            sqlBuilder.append(SafeSqlUtils.mayEscapeIdentifier(container)).append('.').append(SafeSqlUtils.mayEscapeIdentifier(identifier));
            return myself;
        }
    }

    protected final String mayEscapeIdentifier(String identifier) {
        return SafeSqlUtils.mayEscapeIdentifier(identifier);
    }

    /**
     * @deprecated Use {@link #literal(String)} instead.
     */
    @Deprecated
    public S appendStringLiteral(String s) {
        return literal(s);
    }

    /**
     * @deprecated Use {@link #format(String, Object...)} instead.
     */
    @Deprecated
    public S appendFormat(String sql, Object... args) {
        return format(sql, args);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(String, Iterable)} instead
     */
    @Deprecated
    public S appendJoined(String delimiter, Collection<? extends SafeSqlizable> collection) {
        return joinedSqlizables(delimiter, collection);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(String, String, String, Iterable)} instead
     */
    @Deprecated
    public S appendJoined(String delimiter, String prefix, String suffix, Collection<? extends SafeSqlizable> collection) {
        return joinedSqlizables(delimiter, prefix, suffix, collection);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(String, Stream)} instead
     */
    @Deprecated
    public S appendJoined(String delimiter, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(delimiter, stream);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(String, String, String, Stream)} instead
     */
    @Deprecated
    public S appendJoined(String delimiter, String prefix, String suffix, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(delimiter, prefix, suffix, stream);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(SafeSql, Iterable)} instead
     */
    @Deprecated
    public S appendJoined(SafeSql delimiter, Collection<? extends SafeSqlizable> collection) {
        return joinedSqlizables(delimiter, collection);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(SafeSql, SafeSql, SafeSql, Iterable)} instead
     */
    @Deprecated
    public S appendJoined(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Collection<? extends SafeSqlizable> collection) {
        return joinedSqlizables(delimiter, prefix, suffix, collection);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(SafeSql, Stream)} instead
     */
    @Deprecated
    public S appendJoined(SafeSql delimiter, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(delimiter, stream);
    }

    /**
     * @deprecated Use {@link #joinedSafeSqls(SafeSql, SafeSql, SafeSql, Stream)} instead
     */
    @Deprecated
    public S appendJoined(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Stream<? extends SafeSqlizable> stream) {
        return joinedSqlizables(delimiter, prefix, suffix, stream);
    }

    /**
     * @deprecated Use {@link #literal(byte[])} instead.
     */
    @Deprecated
    public S appendByteLiteral(byte[] bytes) {
        return literal(bytes);
    }

    /**
     * @deprecated Use {@link #identifier(String)} instead.
     */
    @Deprecated
    public S appendIdentifier(String identifier) {
        return identifier(identifier);
    }

    /**
     * @deprecated Use {@link #identifier(String, String)} instead.
     */
    @Deprecated
    public S appendIdentifier(String container, String identifier) {
        return identifier(container, identifier);
    }

    @Deprecated
    public S params(String delimiter, Collection<?> collection) {
        paramsIterator(delimiter, collection.iterator());
        return myself;
    }

    @Deprecated
    public S params(String delimiter, String prefix, String suffix, Collection<?> collection) {
        append(prefix);
        paramsIterator(delimiter, collection.iterator());
        append(suffix);
        return myself;
    }

    @Deprecated
    public S params(String delimiter, Stream<?> stream) {
        paramsIterator(delimiter, stream.iterator());
        return myself;
    }

    @Deprecated
    public S params(String delimiter, String prefix, String suffix, Stream<?> stream) {
        append(prefix);
        paramsIterator(delimiter, stream.iterator());
        append(suffix);
        return myself;
    }

    @Deprecated
    public S params(SafeSql delimiter, Collection<?> collection) {
        paramsIterator(delimiter, collection.iterator());
        return myself;
    }

    @Deprecated
    public S params(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Collection<?> collection) {
        append(prefix);
        paramsIterator(delimiter, collection.iterator());
        append(suffix);
        return myself;
    }

    @Deprecated
    public S params(SafeSql delimiter, Stream<?> stream) {
        paramsIterator(delimiter, stream.iterator());
        return myself;
    }

    @Deprecated
    public S params(SafeSql delimiter, SafeSql prefix, SafeSql suffix, Stream<?> stream) {
        append(prefix);
        paramsIterator(delimiter, stream.iterator());
        append(suffix);
        return myself;
    }

    @Override
    public SafeSql toSafeSql() {
        return new SafeSqlImpl(sqlBuilder.toString(), parameters.toArray());
    }

    @Override
    public void appendTo(SafeSqlAppendable builder) {
        if (builder instanceof AbstractSafeSqlBuilder<?>) {
            ((AbstractSafeSqlBuilder<?>) builder).append(this);
        } else {
            builder.append(toSafeSql());
        }
    }

    public boolean isEmpty() {
        return sqlBuilder.length() == 0 && parameters.isEmpty();
    }

    private void appendObject(Object o) {
        sqlBuilder.append('?');
        parameters.add(o);
    }

    Position getLength() {
        return new Position(sqlBuilder.length(), parameters.size());
    }

    void setLength(Position position) {
        sqlBuilder.setLength(position.sqlPosition);
        int currentSize = parameters.size();
        if (position.paramPosition < currentSize) {
            parameters.subList(position.paramPosition, currentSize).clear();
        }
    }

    void append(SafeSqlBuilder other, Position after) {
        sqlBuilder.append(other.sqlBuilder, after.sqlPosition, other.sqlBuilder.length());
        int afterLength = after.paramPosition;
        parameters.addAll(other.parameters.subList(afterLength, other.parameters.size() - afterLength));
    }

    static Position getLength(SafeSql sql) {
        return new Position(sql.asSql().length(), sql.getParameters().length);
    }

}