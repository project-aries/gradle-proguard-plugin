/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aries.gradle.proguard.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import proguard.gradle.ProGuardTask

/**
 *
 * @author cdancy
 */
class ProguardJar extends proguard.gradle.ProGuardTask {
	
    @TaskAction
    def start(){
        println 'hello from GreetingTask'
    }
}

