/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.pool.sizeof.filter;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Filter that will filter fields, based on the {@link IgnoreSizeOf} annotation
 *
 * @author Chris Dennis
 */
public final class AnnotationSizeOfFilter implements SizeOfFilter {


    private static final String IGNORE_SIZE_OF_VM_ARGUMENT = "net.sf.ehcache.pool.sizeof.ignore.pattern";

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSizeOfFilter.class.getName());

    //default is *ehcache.*IgnoreSizeOf
    private static final String IGNORE_SIZE_OF_DEFAULT_REGEXP = ".*ehcache\\..*IgnoreSizeOf$";
    private static Pattern ignoreSizeOfPattern;

    static {
        String ignoreSizeOfRegexpVMArg = System.getProperty(IGNORE_SIZE_OF_VM_ARGUMENT);
        String ignoreSizeOfRegexp = ignoreSizeOfRegexpVMArg != null ? ignoreSizeOfRegexpVMArg :  IGNORE_SIZE_OF_DEFAULT_REGEXP;
        try {
            ignoreSizeOfPattern = Pattern.compile(ignoreSizeOfRegexp);
            LOG.info("Using regular expression provided through VM argument "
                      + IGNORE_SIZE_OF_VM_ARGUMENT
                      + " for IgnoreSizeOf annotation : "
                      + ignoreSizeOfRegexp);
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid regular expression provided through VM argument"
                      + IGNORE_SIZE_OF_VM_ARGUMENT
                      + " : \n"
                      + e.getMessage()
                      + "\n using default regular expression for IgnoreSizeOf annotation : "
                      + IGNORE_SIZE_OF_DEFAULT_REGEXP);
            ignoreSizeOfPattern = Pattern.compile(IGNORE_SIZE_OF_DEFAULT_REGEXP);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields) {
        for (Iterator<Field> it = fields.iterator(); it.hasNext();) {

            Field field = it.next();
            IgnoreSizeOf annotationOnField = getAnnotationOn(field, IgnoreSizeOf.class);
            if (annotationOnField != null) {
                it.remove();
            }
        }
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    public boolean filterClass(Class<?> klazz) {
        boolean classAnnotated = isAnnotationPresentOrInherited(klazz);
        Package pack = klazz.getPackage();
        IgnoreSizeOf annotationOnPackage = pack == null ? null : getAnnotationOn(pack, IgnoreSizeOf.class);
        boolean packageAnnotated = annotationOnPackage == null ? false : true;
        return !classAnnotated && !packageAnnotated;
    }

    private boolean isAnnotationPresentOrInherited(final Class<?> instanceKlazz) {
        Class<?> klazz = instanceKlazz;
        while (klazz != null) {
            IgnoreSizeOf annotationOnClass = getAnnotationOn(klazz, IgnoreSizeOf.class);
            if (annotationOnClass != null && (klazz == instanceKlazz || annotationOnClass.inherited())) {
                return true;
            }
            klazz = klazz.getSuperclass();
        }
        return false;
    }

    private boolean validateCustomAnnotationPattern(String canonicalName) {
        Matcher matcher = ignoreSizeOfPattern.matcher(canonicalName);

        boolean found = matcher.find();
        if (found) {
            LOG.debug(canonicalName + " matched IgnoreSizeOf annotation pattern " + ignoreSizeOfPattern);
        }
        return found;
    }
  
    //EHC-938 : looking for all types of IgnoreSizeOf annotations
    private  <T extends Annotation> T getAnnotationOn(AnnotatedElement element, Class<T> referenceAnnotation) {
        T matchingAnnotation = null;
        Annotation[] annotations = element.getAnnotations();
        boolean foundMatchingAnnotationOnce = false;
        for (Annotation annotation : annotations) {
            if (validateCustomAnnotationPattern(annotation.annotationType().getName())) {
                if (matchingAnnotation != null) {
                    throw new IllegalStateException("You are not allowed to use more than one @IgnoreSizeOf annotations for the same element : "
                                                    + element.toString());
                }
                matchingAnnotation = AnnotationProxyFactory.getAnnotationProxy(annotation, referenceAnnotation);
            }
        }
        return matchingAnnotation;
    }
}
