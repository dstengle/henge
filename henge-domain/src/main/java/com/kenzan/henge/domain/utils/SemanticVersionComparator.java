package com.kenzan.henge.domain.utils;

import com.google.common.base.Preconditions;
import com.kenzan.henge.exception.RuntimeHengeException;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;


/**
 * Utility class for comparing semantic version strings. 
 *
 * @author wmatsushita
 */
public class SemanticVersionComparator implements Comparator<String>, Serializable {

    /**
     * Compares the version strings. It only supports version numbers with 
     * characters containing numbers and dots. 
     * Ex: 1.0.1 
     * 
     * @param version1 the reference version string.
     * @param version2 the other version string to compare version1 to.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws RuntimeHengeException if the two objects are unrelated
     *         due to the specified object's name not matching this instance's
     *         name.
     */
    public int compare(String version1, String version2) {
        
        Preconditions.checkArgument(
            StringUtils.isNotBlank(version1) && StringUtils.isNotBlank(version2), 
            "Arguments to compare must not be blank");

        String[] thisVersion, otherVersion;
        thisVersion = version1.split("\\.");
        otherVersion = version2.split("\\.");

        int length = Math.max(thisVersion.length, otherVersion.length);

        for (int i = 0; i < length; i++) {
            int thisPart = i < thisVersion.length ? Integer.parseInt(thisVersion[i]) : 0;
            int otherPart = i < otherVersion.length ? Integer.parseInt(otherVersion[i]) : 0;
            if (thisPart < otherPart)
                return -1;
            if (thisPart > otherPart)
                return 1;
        }

        return 0;
        
    }

}
