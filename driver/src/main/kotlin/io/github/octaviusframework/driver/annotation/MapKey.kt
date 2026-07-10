package io.github.octaviusframework.driver.annotation

/**
 * Annotation used to specify a custom key for a property
 * during object to/from map conversion or composite mapping.
 *
 * @property name Key name that will be used in the map or database composite.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MapKey(val name: String)
