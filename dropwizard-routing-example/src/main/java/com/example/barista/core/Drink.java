/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.example.barista.core;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A drink.
 */
public class Drink {
    private final Barista barista;
    private final Date brewDate;
    private final String name;
    private final Recipe recipe;
    private final Store store;

    public Drink(Barista barista, Date brewDate, String name, Recipe recipe, Store store) {
        this.barista = barista;
        this.brewDate = brewDate == null ? null : new Date(brewDate.getTime());
        this.name = name;
        this.recipe = recipe;
        this.store = store;
    }

    /**
     * @return the barista
     */
    public Barista getBarista() {
        return barista;
    }

    /**
     * @return the brewDate
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
    public Date getBrewDate() {
        return brewDate == null ? null : new Date(brewDate.getTime());
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the recipe
     */
    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * @return the store
     */
    public Store getStore() {
        return store;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.barista, this.brewDate, this.name, this.recipe, this.store);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Drink)) {
            return false;
        }

        final Drink that = (Drink) obj;
//@formatter:off
        return Objects.equal(this.barista, that.barista)
                && Objects.equal(this.brewDate, that.brewDate)
                && Objects.equal(this.name, that.name)
                && Objects.equal(this.recipe, that.recipe)
                && Objects.equal(this.store, that.store);
//@formatter:on
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
//@formatter:off
        return MoreObjects.toStringHelper(this)
                .add("barista", this.barista)
                .add("brewDate", this.brewDate)
                .add("name", this.name)
                .add("recipe", this.recipe)
                .add("store", this.store)
                .toString();
//@formatter:on
    }
}
