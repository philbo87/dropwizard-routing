package com.astonish.dropwizard.db.route;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Set;

import com.astonish.dropwizard.db.route.store.RouteStore;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Responsible for holding all DAOs for all possible routes.
 */
public class DAORouter {
    protected ImmutableMap<Optional<String>, ImmutableMap<Class<?>, Object>> daosByRoute = ImmutableMap
            .<Optional<String>, ImmutableMap<Class<?>, Object>> of();

    /**
     * Retrieves a type-casted DAO from the {@link DAORouter}.
     * 
     * @param daoClass
     *            the the type of DAO
     * @return the DAO associated with the current route
     */
    @SuppressWarnings("unchecked")
    public <T> T getDAO(final Class<T> daoClass) {
        checkNotNull(daoClass, "daoClass is required");

        final ImmutableMap<Class<?>, Object> routeDAOs = daosByRoute.get(Optional.fromNullable(RouteStore.getInstance()
                .getRoute()));
        checkState(null != routeDAOs, "No route found for Route[" + RouteStore.getInstance().getRoute() + "]");
        checkState(null != routeDAOs.get(daoClass), "Unknown DAO[" + daoClass.getSimpleName() + "]");

        return (T) routeDAOs.get(daoClass);
    }

    /**
     * Get all routes for this {@link DAORouter}.
     * 
     * @return
     */
    public Set<Optional<String>> keySet() {
        return daosByRoute.keySet();
    }
}
