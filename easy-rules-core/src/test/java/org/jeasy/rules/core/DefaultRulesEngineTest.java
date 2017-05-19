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

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.api.RuleListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link DefaultRulesEngine}.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class DefaultRulesEngineTest extends AbstractTest {

    @Mock
    private BasicRule rule, anotherRule;

    @Mock
    private RuleListener ruleListener;

    private AnnotatedRule annotatedRule;

    @Before
    public void setup() throws Exception {
        super.setup();
        when(rule.getName()).thenReturn("r");
        when(rule.getDescription()).thenReturn("d");
        when(rule.getPriority()).thenReturn(1);
        annotatedRule = new AnnotatedRule();
    }

    @Test
    public void whenConditionIsTrue_thenActionShouldBeExecuted() throws Exception {
        when(rule.evaluate(facts)).thenReturn(true);

        rules.clear();// FIXME
        rules.register(rule);

        rulesEngine.fire(rules, facts);

        verify(rule).execute(facts);
    }

    @Test
    public void whenConditionIsFalse_thenActionShouldNotBeExecuted() throws Exception {
        when(rule.evaluate(facts)).thenReturn(false);
        rules.clear();// FIXME
        rules.register(rule);

        rulesEngine.fire(rules, facts);

        verify(rule, never()).execute(facts);
    }

    @Test
    public void rulesMustBeTriggeredInTheirNaturalOrder() throws Exception {
        when(rule.evaluate(facts)).thenReturn(true);
        when(anotherRule.evaluate(facts)).thenReturn(true);
        when(anotherRule.compareTo(rule)).thenReturn(1);
        rules.clear();// FIXME
        rules.register(rule);
        rules.register(anotherRule);

        rulesEngine.fire(rules, facts);

        InOrder inOrder = inOrder(rule, anotherRule);
        inOrder.verify(rule).execute(facts);
        inOrder.verify(anotherRule).execute(facts);
    }

    @Test
    public void rulesMustBeCheckedInTheirNaturalOrder() throws Exception {
        when(rule.evaluate(facts)).thenReturn(true);
        when(anotherRule.evaluate(facts)).thenReturn(true);
        when(anotherRule.compareTo(rule)).thenReturn(1);
        rules.clear();// FIXME
        rules.register(rule);
        rules.register(anotherRule);

        rulesEngine.check(rules, facts);

        InOrder inOrder = inOrder(rule, anotherRule);
        inOrder.verify(rule).evaluate(facts);
        inOrder.verify(anotherRule).evaluate(facts);
    }

    @Test
    public void actionsMustBeExecutedInTheDefinedOrder() {
        rules.clear(); // FIXME
        rules.register(annotatedRule);
        rulesEngine.fire(rules, facts);
        assertEquals("012", annotatedRule.getActionSequence());
    }

    @Test
    public void annotatedRulesAndNonAnnotatedRulesShouldBeUsableTogether() throws Exception {
        when(rule.evaluate(facts)).thenReturn(true);
        rules.clear(); // FIXME
        rules.register(rule);
        rules.register(annotatedRule);

        rulesEngine.fire(rules, facts);

        verify(rule).execute(facts);
        assertThat(annotatedRule.isExecuted()).isTrue();
    }

    @Test
    public void whenRuleNameIsNotSpecified_thenItShouldBeEqualToClassNameByDefault() throws Exception {
        org.jeasy.rules.api.Rule rule = RuleProxy.asRule(new DummyRule());
        assertThat(rule.getName()).isEqualTo("DummyRule");
    }

    @Test
    public void whenRuleDescriptionIsNotSpecified_thenItShouldBeEqualToConditionNameFollowedByActionsNames() throws Exception {
        org.jeasy.rules.api.Rule rule = RuleProxy.asRule(new DummyRule());
        assertThat(rule.getDescription()).isEqualTo("when condition then action1,action2");
    }

    @Test
    public void testCheckRules() throws Exception {
        // Given
        when(rule.evaluate(facts)).thenReturn(true);
        rules.clear(); // FIXME
        rules.register(rule);
        rules.register(annotatedRule);

        // When
        Map<org.jeasy.rules.api.Rule, Boolean> result = rulesEngine.check(rules, facts);

        // Then
        assertThat(result).hasSize(2);
        for (org.jeasy.rules.api.Rule r : rules) {
            assertThat(result.get(r)).isTrue();
        }
    }

    @Test
    public void listenerShouldBeInvokedBeforeCheckingRules() throws Exception {
        // Given
        when(rule.evaluate(facts)).thenReturn(true);
        when(ruleListener.beforeEvaluate(rule, facts)).thenReturn(true);
        rulesEngine = RulesEngineBuilder.aNewRulesEngine()
                .withRuleListener(ruleListener)
                .build();
        rules.clear(); // FIXME
        rules.register(rule);

        // When
        rulesEngine.check(rules, facts);

        // Then
        verify(ruleListener).beforeEvaluate(rule, facts);
    }

    @Test
    public void testGetRuleListeners() throws Exception {
        rulesEngine = RulesEngineBuilder.aNewRulesEngine()
                .withRuleListener(ruleListener)
                .build();

        assertThat(rulesEngine.getRuleListeners())
                .containsExactly(ruleListener);
    }

    @After
    public void clearRules() {
        rules.clear();
    }

    @org.jeasy.rules.annotation.Rule(name = "myRule", description = "my rule description")
    public class AnnotatedRule {

        private boolean executed;

        private String actionSequence = "";

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then0() throws Exception {
            actionSequence += "0";
        }

        @Action(order = 1)
        public void then1() throws Exception {
            actionSequence += "1";
        }

        @Action(order = 2)
        public void then2() throws Exception {
            actionSequence += "2";
            executed = true;
        }

        @Priority
        public int getPriority() {
            return 0;
        }

        public boolean isExecuted() {
            return executed;
        }

        public String getActionSequence() {
            return actionSequence;
        }

    }

    @org.jeasy.rules.annotation.Rule
    public class DummyRule {

        @Condition
        public boolean condition() {
            return true;
        }

        @Action(order = 1)
        public void action1() throws Exception {
            // no op
        }

        @Action(order = 2)
        public void action2() throws Exception {
            // no op
        }
    }

}
