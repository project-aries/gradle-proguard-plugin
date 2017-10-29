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
 * Entry point for the GradleProguardPlugin
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
        final GradleProguardPluginExtension extension = project.extensions.create(EXTENSION_NAME, GradleProguardPluginExtension)
        
        // 2.) create proguard configuration
        final Configuration configuration = createProguardConfiguration(project, extension);
        
        // 3.) create proguard task
        final ProguardJar proguardJar = createProguardTask(project);
    }

    // Create proguard configuration if it does NOT already exist
    public static Configuration createProguardConfiguration(final Project project, final GradleProguardPluginExtension extension) {
        Configuration configuration = project.configurations.getByName(CONFIGURATION_NAME)
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
    
    public static ProguardJar createProguardTask(final Project project) {
        ProguardJar proguardJar = project.tasks.getByName(TASK_NAME)
        if (proguardJar == null) {
            proguardJar = project.tasks.create(TASK_NAME, ProguardJar)
            proguardJar.group = TASK_GROUP
            proguardJar.description = 'Create a Proguard jar'
            proguardJar.conventionMapping.with {
                map('classifier') {
                    'pro'
                }
            }

            proguardJar.manifest.inheritFrom project.tasks.jar.manifest
        }
        proguardJar
    }
}

