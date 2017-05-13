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
package org.easyrules.spring.boot.integration;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AspectConfiguration {

	@Aspect
	@Component
	public class LogAspect {

		final Logger logger = LoggerFactory.getLogger(LogAspect.class);

		@PostConstruct
		public void init() {
			logger.info("log aspect initialized");
		}

		@Pointcut ("!@target(org.springframework.context.annotation.Configuration)")
		public void springConfigurationAnnotations() {
			// all NoLog annotation methods
		}

		@Pointcut ("execution(* org.easyrules.spring.boot.integration..*..*(..))")
		public void allMethods() {
			// all NoLog annotation methods
		}

		@Pointcut ("!execution(* org.easyrules.spring.boot.integration..*..*Bean.get*(..))")
		public void allGetMethods() {
			// all get methods
		}

		@Pointcut ("!execution(* org.easyrules.spring.boot.integration..*..*Bean.is*(..))")
		public void allIsMethods() {
			// all is methods
		}

		@Pointcut ("!execution(* org.easyrules.spring.boot.integration..*..*Bean.set*(..))")
		public void allSetMethods() {
			// all set methods
		}

		@Around (value = "allMethods() && springConfigurationAnnotations() && allGetMethods() && allIsMethods() ", argNames = "joinPoint")
		public Object aroundProxiedBeans(ProceedingJoinPoint joinPoint) throws Throwable {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();

			Object returnValue;
			long endTime;
			long startTime;

			startTime = System.nanoTime();
			returnValue = joinPoint.proceed();
			endTime = System.nanoTime();

			logger.info(signature.toString() + " - duration " + (endTime - startTime) / 1000000);

			return returnValue;
		}
	}
}
