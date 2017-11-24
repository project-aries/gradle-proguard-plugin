/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aries.gradle.proguard.plugin

import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.internal.file.copy.DestinationRootCopySpec
import org.gradle.api.internal.file.copy.FileCopyAction
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.internal.reflect.Instantiator
import proguard.gradle.ProGuardTask

/**
 * Custom Proguard task to make things more gradle-like. 
 *
 * @author cdancy
 */
class ProguardJarTask extends ProGuardTask {

    private static final def JAVA_LIBS = ["${System.getProperty('java.home')}/lib/rt.jar",
                                          "${System.getProperty('java.home')}/lib/jsse.jar",
                                          "${System.getProperty('java.home')}/lib/jce.jar"].asImmutable()

    @Input
    @Optional
    public String classifier
    
    @Input
    @Optional
    public File destinationDir

    @Override
    @TaskAction
    void proguard(){
        logger.quiet 'Creating Proguard jar...'

        // 1.) Add any inputs the user has passed in.
        if (this.getInputs().getHasInputs()) { 
            for (def possibleFile : this.getInputs().files.files) {
                def possibleFilePath = possibleFile.path
                if (!getInJarFiles().contains(possibleFilePath)) {
                    this.injars(possibleFilePath)
                }
            }
        }

        // 2.) Add OOTB java libs as library jars.
        JAVA_LIBS.each {
            this.libraryjars(it)    
        }
        
        for (def possibleInJar : getInJarFiles()) {
            logger.quiet "In Jar File: ${possibleInJar}"
        }
        
        for (def possibleFile : getLibraryJarFileCollection().files) {
            logger.quiet "Found lib jar ${possibleFile}"
        }

        this.outjars("${destinationDir.path}/helloWorld.jar")
        super.proguard()
    }
}

