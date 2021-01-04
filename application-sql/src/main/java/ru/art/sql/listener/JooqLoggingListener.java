package ru.art.sql.listener;

import lombok.*;
import org.apache.logging.log4j.Logger;
import org.jooq.*;
import org.jooq.impl.*;
import org.jooq.tools.*;
import static java.lang.Boolean.*;
import static java.lang.System.*;
import static java.text.MessageFormat.*;
import static java.util.Arrays.*;
import static java.util.Objects.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static org.jooq.ExecuteType.*;
import static org.jooq.impl.DSL.*;
import static org.jooq.tools.StringUtils.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.ThreadContextExtensions.*;
import static ru.art.sql.constants.SqlModuleConstants.LoggingMessages.*;
import java.util.function.*;

@RequiredArgsConstructor
public class JooqLoggingListener extends LoggerListener {
    @Getter(lazy = true)
    private final static Logger logger = loggingModule().getLogger(JooqLoggingListener.class);
    private final Supplier<Boolean> tracingEnabled;

    @Override
    public void renderEnd(ExecuteContext context) {
        super.renderEnd(context);
        if (!tracingEnabled.get()) {
            return;
        }
        Configuration configuration = context.configuration();
        String newline = TRUE.equals(configuration.settings().isRenderFormatted()) ? NEW_LINE : EMPTY_STRING;
        configuration = abbreviateBindVariables(configuration);

        String[] batchSql = context.batchSQL();
        if (nonNull(context.query())) {
            String query = newline + using(configuration).renderInlined(context.query());
            putIfNotNull(SQL_QUERY_KEY, query);
            getLogger().info(format(EXECUTING_QUERY, query));
            remove(SQL_QUERY_KEY);
            return;
        }

        if (nonNull(context.routine())) {
            String routine = newline + using(configuration).renderInlined(context.routine());
            putIfNotNull(SQL_ROUTINE_KEY, routine);
            getLogger().info(format(EXECUTING_ROUTINE, routine));
            remove(SQL_ROUTINE_KEY);
            return;
        }

        if (isNotEmpty(context.sql())) {
            String query = newline + context.sql();
            putIfNotNull(SQL_QUERY_KEY, query);
            getLogger().info(format(context.type() == BATCH ? EXECUTING_BATCH_QUERY : EXECUTING_QUERY, query));
            remove(SQL_QUERY_KEY);
            return;
        }

        if (batchSql.length > 0 && batchSql[batchSql.length - 1] != null) {
            stream(batchSql).forEach(sql -> {
                String query = newline + sql;
                putIfNotNull(SQL_QUERY_KEY, query);
                getLogger().info(format(EXECUTING_BATCH_QUERY, query));
                remove(SQL_QUERY_KEY);
            });
        }
    }

    private Configuration abbreviateBindVariables(Configuration configuration) {
        VisitListenerProvider[] oldProviders = configuration.visitListenerProviders();
        VisitListenerProvider[] newProviders = new VisitListenerProvider[oldProviders.length + 1];
        arraycopy(oldProviders, 0, newProviders, 0, oldProviders.length);
        newProviders[newProviders.length - 1] = new DefaultVisitListenerProvider(new BindValueAbbreviator());
        return configuration.derive(newProviders);
    }

    private static class BindValueAbbreviator extends DefaultVisitListener {
        private static final int maxLength = 2000;

        @Override
        public void visitStart(VisitContext context) {
            if (isNull(context.renderContext())) {
                return;
            }
            QueryPart part = context.queryPart();

            if (!(part instanceof Param<?>)) {
                return;
            }
            Param<?> param = (Param<?>) part;
            Object value = param.getValue();

            if (value instanceof String && ((String) value).length() > maxLength) {
                context.queryPart(val(abbreviate((String) value, maxLength)));
                return;
            }

            if (value instanceof byte[] && ((byte[]) value).length > maxLength) {
                context.queryPart(val(copyOf((byte[]) value, maxLength)));
            }
        }

    }
}
