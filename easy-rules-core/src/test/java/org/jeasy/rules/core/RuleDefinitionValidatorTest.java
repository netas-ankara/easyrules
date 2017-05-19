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


import org.jeasy.rules.annotation.*;
import org.junit.Before;
import org.junit.Test;

public class RuleDefinitionValidatorTest {

    private RuleDefinitionValidator ruleDefinitionValidator;

    @Before
    public void setup(){
        ruleDefinitionValidator = new RuleDefinitionValidator();
    }

    /*
     * Rule annotation test
     */
    @Test(expected = IllegalArgumentException.class)
    public void notAnnotatedRuleMustNotBeAccepted() {
        ruleDefinitionValidator.validateRuleDefinition(new Object());
    }

    @Test
    public void withCustomAnnotationThatIsItselfAnnotatedWithTheRuleAnnotation() throws Throwable {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMetaRuleAnnotation());
    }

    /*
     * Conditions methods tests
     */
    @Test(expected = IllegalArgumentException.class)
    public void conditionMethodMustBeDefined() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutCondition());
    }

    @Test(expected = IllegalArgumentException.class)
    public void conditionMethodMustBePublic() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNotPublicConditionMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConditionMethodHasOneNonAnnotatedParameter_thenThisParameterMustBeOfTypeFacts() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithConditionMethodHavingOneArgumentNotOfTypeFacts());
    }

    @Test(expected = IllegalArgumentException.class)
    public void conditionMethodMustReturnBooleanType() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithConditionMethodHavingNonBooleanReturnType());
    }

    /*
     * Action method tests
     */
    @Test(expected = IllegalArgumentException.class)
    public void actionMethodMustBeDefined() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutActionMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void actionMethodMustBePublic() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNotPublicActionMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void actionMethodMustHaveNoArguments() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingOneArgumentNotOfTypeFacts());
    }

    /*
     * Priority method tests
     */

    @Test(expected = IllegalArgumentException.class)
    public void priorityMethodMustBeUnique() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMoreThanOnePriorityMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void priorityMethodMustBePublic() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNotPublicPriorityMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void priorityMethodMustHaveNoArguments() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingArguments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void priorityMethodReturnTypeMustBeInteger() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingNonIntegerReturnType());
    }
}
