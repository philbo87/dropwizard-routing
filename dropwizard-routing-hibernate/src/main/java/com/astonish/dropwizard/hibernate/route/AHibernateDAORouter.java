package com.astonish.dropwizard.hibernate.route;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SessionFactory;

import com.astonish.dropwizard.db.route.DAORouter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Responsible for holding all DAOs for all possible routes.
 */
public abstract class AHibernateDAORouter extends DAORouter {
    /**
     * @param sessionFactoryMap
     *            map of {@link Optional} route keys to their corresponding {@link SessionFactory}.
     * @throws NullPointerException
     *             if sessionFactoryMap is null or any {@link Entry} in sessionFactoryMap has a null value with a
     *             non-null key
     * @throws IllegalStateException
     *             if sessionFactoryMap is empty
     */
    public AHibernateDAORouter(final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        checkNotNull(sessionFactoryMap);
        checkState(!sessionFactoryMap.isEmpty());

        final Map<Optional<String>, ImmutableMap<Class<?>, Object>> daosByRoute = new LinkedHashMap<>();
        for (Entry<Optional<String>, SessionFactory> e : sessionFactoryMap.entrySet()) {
            SessionFactory factory = checkNotNull(e.getValue());
            daosByRoute.put(e.getKey(), constructDAOs(factory));
        }

        this.daosByRoute = ImmutableMap.copyOf(daosByRoute);
    }

    protected abstract ImmutableMap<Class<?>, Object> constructDAOs(final SessionFactory factory);
}
