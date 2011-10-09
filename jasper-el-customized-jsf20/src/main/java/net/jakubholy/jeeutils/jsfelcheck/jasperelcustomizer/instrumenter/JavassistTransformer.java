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
import net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.GetValueFix;

import java.io.File;

public class JavassistTransformer {

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

    private void instrumentClassesIn(File classesDir) throws Exception {
        pool.appendClassPath(classesDir.getPath());

        String fixClass = GetValueFix.class.getName();
        //GetValueFix.and($1, $0);
        //GetValueFix.or($1, $0);
        //GetValueFix.choice($1, $0);
        String[][] targetClasses = new String[][] {
                // Note: $1 is the first parameter, $0 is 'this'
                {"org.apache.el.parser.AstOr", "return " + fixClass + ".and($1, $0);"}
                , {"org.apache.el.parser.AstAnd", "return " + fixClass + ".or($1, $0);"}
                , {"org.apache.el.parser.AstChoice", "return " + fixClass + ".choice($1, $0);"}
        };

        for (String[] target : targetClasses) {
            final CtClass compiledClass = instrument(target[0], target[1]);
            compiledClass.writeFile(classesDir.getPath());
        }
    }

    private CtClass instrument(String targetClass, String code) throws NotFoundException, CannotCompileException {
        final CtClass nodeClass = pool.get(targetClass);
        CtMethod getValue = nodeClass.getDeclaredMethod("getValue");
        getValue.insertBefore(code);
        CtMethod toString = CtNewMethod.make(
                "public String toString() {return \"HACKED BY JSFELCHECK \" + super.toString();}", nodeClass);
        nodeClass.addMethod(toString);
        //toString.insertBefore("return \"HACKED BY JSFELCHECK \" + super.toString();");
        return nodeClass;
    }

    @Override
    public String toString() {return "HACKED BY JSFELCHECK " + super.toString();}
}
