package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Set;

/**
 * Immutable index used to reference and find a mapped VersionSetReference.
 *
 * @author wmatsushita
 */
public final class MappingKey implements Comparable<MappingKey>, Serializable {

    private final Set<Scope> scopeSet;

    @JsonCreator
    public MappingKey(@JsonProperty("scopeSet") Set<Scope> scopeSet) {

        this.scopeSet = scopeSet;
    }

    /**
     * @return the scopeSet
     */
    public Set<Scope> getScopeSet() {

        return scopeSet;

    }

    /**
     * This implements an arbitrary ordering that respects equals (equal
     * instances return 0 when compared) and enables binary search inside the
     * mapping for performance purposes.
     */
    @Override
    public int compareTo(MappingKey o) {

        Integer thisScopeSetScore = (scopeSet == null) ? 0 : scopeSet.hashCode();
        Integer thatScopeSetScore = (o.getScopeSet() == null) ? 0 : o.getScopeSet().hashCode();

        return thisScopeSetScore.compareTo(thatScopeSetScore);

    }

    @Override
    public int hashCode() {

        return Objects.hashCode(scopeSet);

    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj || !(obj instanceof MappingKey)) {
            return false;
        }

        MappingKey that = (MappingKey) obj;

        return Objects.equal(scopeSet, that.getScopeSet());
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this).add("scopeSet", scopeSet).toString();
    }

}
