/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aries.gradle.proguard.plugin

/**
 * Extension point for GradleProguardPlugin.
 *
 * @author cdancy
 */
class GradleProguardPluginExtension {
    
    public static final String DEFAULT_TOOL_VERSION = '5.3.3'

    public String toolVersion;
    
    public String getToolVersion() {
        toolVersion ?: DEFAULT_TOOL_VERSION
    }
}

