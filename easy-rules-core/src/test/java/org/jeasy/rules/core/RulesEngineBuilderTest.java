/**
 * The MIT License
 *
 *  Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.jeasy.rules.core;

import org.jeasy.rules.api.RuleListener;
import org.jeasy.rules.api.RulesEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RulesEngineBuilderTest {

    @Mock
    private RuleListener ruleListener;

    @Test
    public void testCreationWithDefaultParameters() {
        RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine().build();

        assertThat(rulesEngine).isNotNull();
        RulesEngineParameters parameters = rulesEngine.getParameters();

        assertThat(parameters.getName()).isEqualTo(RulesEngine.DEFAULT_NAME);
        assertThat(parameters.getPriorityThreshold()).isEqualTo(RulesEngine.DEFAULT_RULE_PRIORITY_THRESHOLD);

        assertThat(parameters.isSkipOnFirstAppliedRule()).isFalse();
        assertThat(parameters.isSkipOnFirstFailedRule()).isFalse();
        assertThat(parameters.isSkipOnFirstNonTriggeredRule()).isFalse();
    }

    @Test
    public void testCreationWithCustomParameters() {
        String name = "myRulesEngine";
        int expectedThreshold = 10;

        RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine()
                .named(name)
                .withRuleListener(ruleListener)
                .withRulePriorityThreshold(expectedThreshold)
                .withSilentMode(true)
                .withSkipOnFirstNonTriggeredRule(true)
                .withSkipOnFirstAppliedRule(true)
                .withSkipOnFirstFailedRule(true)
                .build();

        assertThat(rulesEngine).isNotNull();
        RulesEngineParameters parameters = rulesEngine.getParameters();

        assertThat(parameters.getName()).isEqualTo(name);
        assertThat(parameters.getPriorityThreshold()).isEqualTo(expectedThreshold);
        assertThat(parameters.isSilentMode()).isTrue();
        assertThat(parameters.isSkipOnFirstAppliedRule()).isTrue();
        assertThat(parameters.isSkipOnFirstFailedRule()).isTrue();
        assertThat(parameters.isSkipOnFirstNonTriggeredRule()).isTrue();
    }
}