/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aries.gradle.proguard.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * Entry point for the GradleProguardPlugin.
 *
 * @author cdancy
 */
class GradleProguardPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = 'proguard'
    public static final String CONFIGURATION_NAME = 'proguard'
    public static final String TASK_GROUP = 'Proguard'
    public static final String TASK_NAME = 'proguardJar'
    
    @Override
    void apply(Project project) {

        // 1.) create proguard extension
        final GradleProguardPluginExtension extension = createProguardExtension(project)
        
        // 2.) create proguard configuration
        final Configuration configuration = createProguardConfiguration(project, extension);
        
        // 3.) create proguard task
        final ProguardJarTask proguardJar = createProguardJarTask(project);
    }

    // Create proguard extension if it does not already exist
    public static GradleProguardPluginExtension createProguardExtension(final Project project) {
        GradleProguardPluginExtension extension = project.rootProject.extensions.findByName(EXTENSION_NAME)
        if (!extension) {
            extension = project.extensions.create(EXTENSION_NAME, GradleProguardPluginExtension)
        }
        extension
    }

    // Create proguard configuration if it does NOT already exist
    public static Configuration createProguardConfiguration(final Project project, final GradleProguardPluginExtension extension) {
        Configuration configuration = project.rootProject.configurations.findByName(CONFIGURATION_NAME)
        if (!configuration) {
            configuration = project.configurations.create(CONFIGURATION_NAME)
                    .setVisible(false)
                    .setTransitive(true)
                    .setDescription('The Proguard Java libraries to be used for this project.')

            // if no repositories were defined fallback to buildscript
            // repositories to resolve dependencies as a last resort
            project.afterEvaluate {
                if (project.repositories.size() == 0) {
                    project.repositories.addAll(project.buildscript.repositories.collect())
                }
            }

            configuration.defaultDependencies { dependencies ->
                dependencies.add(project.dependencies.create("net.sf.proguard:proguard-parent:${extension.getToolVersion()}"))
            }
        }
        configuration
    }

    // Create proguard task if it does not already exist
    public static ProguardJarTask createProguardJarTask(final Project project) {
        ProguardJarTask proguardJar = project.rootProject.tasks.findByName(TASK_NAME)
        if (!proguardJar) {
            proguardJar = project.tasks.create(TASK_NAME, ProguardJarTask)
            proguardJar.group = TASK_GROUP
            proguardJar.description = 'Create a Proguard jar'
        }
        proguardJar
    }
}

