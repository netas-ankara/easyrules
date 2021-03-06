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

import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.api.Rule;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Logger;

public class RuleProxy implements InvocationHandler {

    private static final Logger LOGGER = Logger.getLogger(RuleProxy.class.getName());

    private Object target;

    private static RuleDefinitionValidator ruleDefinitionValidator = new RuleDefinitionValidator();

    private RuleProxy(final Object target) {
        this.target = target;
    }

    /**
     * Makes the rule object implement the {@link Rule} interface.
     *
     * @param rule the annotated rule object.
     * @return a proxy that implements the {@link Rule} interface.
     */
    public static Rule asRule(final Object rule) {
        Rule result;
        if (Utils.getInterfaces(rule).contains(Rule.class)) {
            result = (Rule) rule;
        } else {
            ruleDefinitionValidator.validateRuleDefinition(rule);
            result = (Rule) Proxy.newProxyInstance(
                    Rule.class.getClassLoader(),
                    new Class[]{Rule.class, Comparable.class},
                    new RuleProxy(rule));
        }
        return result;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("getName")) {
            return getRuleName();
        }
        if (methodName.equals("getDescription")) {
            return getRuleDescription();
        }
        if (methodName.equals("getPriority")) {
            return getRulePriority();
        }
        if (methodName.equals("evaluate")) {
            Facts facts = (Facts) args[0];
            Method conditionMethod = getConditionMethod();
            List<Object> actualParameters = getActualParameters(conditionMethod, facts);
            return conditionMethod.invoke(target, actualParameters.toArray()); // validated upfront
        }
        if (methodName.equals("execute")) {
            for (ActionMethodOrderBean actionMethodBean : getActionMethodBeans()) {
                Facts facts = (Facts) args[0];
                Method actiomMethod = actionMethodBean.getMethod();
                List<Object> actualParameters = getActualParameters(actiomMethod, facts);
                actiomMethod.invoke(target, actualParameters.toArray());
            }
        }
        if (methodName.equals("equals")) {
            return target.equals(args[0]);
        }
        if (methodName.equals("hashCode")) {
            return target.hashCode();
        }
        if (methodName.equals("toString")) {
            return target.toString();
        }
        if (methodName.equals("compareTo")) {
            Method compareToMethod = getCompareToMethod();
            if (compareToMethod != null) {
                return compareToMethod.invoke(target, args);
            } else {
                Rule otherRule = (Rule) args[0];
                return compareTo(otherRule);
            }
        }
        return null;
    }

    private List<Object> getActualParameters(Method method, Facts facts) {
        Parameter[] parameters = method.getParameters(); // validated upfront
        List<Object> actualParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Fact annotation = parameter.getAnnotation(Fact.class); // validated upfront
            if (annotation == null) { // validated upfront, there may be only one parameter not annotated and which is of type Facts.class
                actualParameters.add(facts);
            } else {
                String factName = annotation.value();
                Object fact = facts.get(factName);
                if (fact == null) {
                    throw new RuntimeException(String.format("No fact named %s found in known facts", factName));
                }
                actualParameters.add(fact);
            }
        }
        return actualParameters;
    }

    private int compareTo(final Rule otherRule) throws Exception {
        String otherName = otherRule.getName();
        int otherPriority = otherRule.getPriority();
        String name = getRuleName();
        int priority = getRulePriority();

        if (priority < otherPriority) {
            return -1;
        } else if (priority > otherPriority) {
            return 1;
        } else {
            return name.compareTo(otherName);
        }
    }

    private int getRulePriority() throws Exception {
        int priority = Rule.DEFAULT_PRIORITY;

        Method[] methods = getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Priority.class)) {
                priority = (Integer) method.invoke(target);
                break;
            }
        }
        return priority;
    }

    private Method getConditionMethod() {
        Method[] methods = getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Condition.class)) {
                return method;
            }
        }
        return null;
    }

    private Set<ActionMethodOrderBean> getActionMethodBeans() {
        Method[] methods = getMethods();
        Set<ActionMethodOrderBean> actionMethodBeans = new TreeSet<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Action.class)) {
                Action actionAnnotation = method.getAnnotation(Action.class);
                int order = actionAnnotation.order();
                actionMethodBeans.add(new ActionMethodOrderBean(method, order));
            }
        }
        return actionMethodBeans;
    }

    private Method getCompareToMethod() {
        Method[] methods = getMethods();
        for (Method method : methods) {
            if (method.getName().equals("compareTo")) {
                return method;
            }
        }
        return null;
    }

    private Method[] getMethods() {
        return getTargetClass().getMethods();
    }

    private org.jeasy.rules.annotation.Rule getRuleAnnotation() {
        return Utils.findAnnotation(org.jeasy.rules.annotation.Rule.class, getTargetClass());
    }

    private String getRuleName() {
        org.jeasy.rules.annotation.Rule rule = getRuleAnnotation();
        return rule.name().equals(Rule.DEFAULT_NAME) ? getTargetClass().getSimpleName() : rule.name();
    }

    private String getRuleDescription() {
        // Default description = "when " + conditionMethodName + " then " + comma separated actionMethodsNames
        StringBuilder description = new StringBuilder();
        appendConditionMethodName(description);
        appendActionMethodsNames(description);

        org.jeasy.rules.annotation.Rule rule = getRuleAnnotation();
        return rule.description().equals(Rule.DEFAULT_DESCRIPTION) ? description.toString() : rule.description();
    }

    private void appendConditionMethodName(StringBuilder description) {
        Method method = getConditionMethod();
        if (method != null) {
            description.append("when ");
            description.append(method.getName());
            description.append(" then ");
        }
    }

    private void appendActionMethodsNames(StringBuilder description) {
        Iterator<ActionMethodOrderBean> iterator = getActionMethodBeans().iterator();
        while (iterator.hasNext()) {
            description.append(iterator.next().getMethod().getName());
            if (iterator.hasNext()) {
                description.append(",");
            }
        }
    }

    private Class<?> getTargetClass() {
        return target.getClass();
    }

}
