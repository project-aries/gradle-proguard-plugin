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
        
        // 1.) create proguard task
        createProguardJarTask(project);
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

