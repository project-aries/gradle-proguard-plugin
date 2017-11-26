/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aries.gradle.proguard.plugin

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/*

    Tests to exercise various final variables within ProguardContants

*/
class ProguardConstantsTest {
    
    @Test
    public void test_JAVA_LIBS_areImmutable() {
        try {
            ProguardConstants.JAVA_LIBS << 'Hello,World!'
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
        }
    }	
}

