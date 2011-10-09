/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.instrumenter;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.DuplicateMemberException;
import net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.GetValueFix;

import java.io.File;

public class JavassistTransformer {
    
    private static class InstrumentationInstructions {
        private final String className;
        private final String methodName;
        private final String code;

        private InstrumentationInstructions(String className, String methodName, String code) {
            this.methodName = methodName;
            this.code = code;
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public String getCode() {
            return code;
        }

        public String getMethodName() {
            return methodName;
        }
    }

    private final ClassPool pool = ClassPool.getDefault();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument: directory with classes to transform");
        }
        File classesDir = new File(args[0]);    // TODO Check it is directory

        new JavassistTransformer().instrumentClassesIn(classesDir);

        System.out.println(">>>" + JavassistTransformer.class.getSimpleName() + ": Trasnformation done for "
                + classesDir.getAbsolutePath());
    }
    
    private static InstrumentationInstructions target(String className, String methodName, String code) {
        return new InstrumentationInstructions(className, methodName, code);
    }

    private void instrumentClassesIn(File classesDir) throws Exception {
        pool.appendClassPath(classesDir.getPath());

        String fixClass = GetValueFix.class.getName();
        InstrumentationInstructions[] targetClasses = new InstrumentationInstructions[] {
                // Note: $1 is the first parameter, $0 is 'this'
                target("com.sun.faces.el.impl.AndOperator", "shouldEvaluate", "return true;")
                , target("com.sun.faces.el.impl.OrOperator", "shouldEvaluate", "return true;")
                , target("com.sun.faces.el.impl.ConditionalExpression", "evaluate", "return " + fixClass + ".choice($1, $0);")
        };

        for (InstrumentationInstructions target : targetClasses) {
            final CtClass compiledClass = instrument(target);
            compiledClass.writeFile(classesDir.getPath());
        }
    }

    private CtClass instrument(InstrumentationInstructions target) throws NotFoundException, CannotCompileException {
        final CtClass nodeClass = pool.get(target.getClassName());
        CtMethod getValue = nodeClass.getDeclaredMethod(target.getMethodName());
        getValue.insertBefore(target.getCode());

        String toStringBody = "return \"HACKED BY JSFELCHECK \" + super.toString();";
        try {
            CtMethod toString = CtNewMethod.make(
                "public String toString() {" + toStringBody + "}", nodeClass);
            nodeClass.addMethod(toString);
        } catch (DuplicateMemberException e) {
            CtMethod toString = nodeClass.getDeclaredMethod("toString");
            toString.insertBefore(toStringBody);
        }

        return nodeClass;
    }

    @Override
    public String toString() {return "HACKED BY JSFELCHECK " + super.toString();}
}
