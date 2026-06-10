package com.google.ai.edge.gallery.feature.jarvis

/**
 * Global feature flag for Jarvis Mode.
 * When false, all Jarvis-related logic, services, and UI are disabled.
 */
object JarvisFeatureFlag {
    /**
     * Default value is false to ensure non-invasive extension.
     */
    const val IS_ENABLED = false
}
