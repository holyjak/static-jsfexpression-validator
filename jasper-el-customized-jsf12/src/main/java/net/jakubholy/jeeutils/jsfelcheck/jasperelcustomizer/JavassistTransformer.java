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

package net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.File;

public class JavassistTransformer {

    private final ClassPool pool = ClassPool.getDefault();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument: directory with classes to transform");
        }
        File classesDir = new File(args[0]);    // TODO Check it is directory

        new JavassistTransformer().camel(classesDir);

        System.out.println(">>>" + JavassistTransformer.class.getSimpleName() + ": Trasnformation done for "
                + classesDir.getAbsolutePath());
    }

    private void camel(File classesDir) throws Exception {
        pool.appendClassPath(classesDir.getPath());

        String[] targetClasses = new String[] {
                "org.apache.el.parser.AstOr"
                , "org.apache.el.parser.AstAnd"
                , "org.apache.el.parser.AstChoice"
        };

        for (String targetClass : targetClasses) {
            final CtClass compiledClass = instrument(targetClass);
            compiledClass.writeFile(classesDir.getPath());
        }

        // TODO Modify toString() to return "HACKED BY JSFELCHECK" + super.. so that the analyzer can at runtime
        // verify that the hacked impl. is indeed used
    }

    private CtClass instrument(String targetClass) throws NotFoundException, CannotCompileException {
        final CtClass compiledClass = pool.get(targetClass);
        CtMethod m = compiledClass.getDeclaredMethod("getValue");
        m.insertBefore("throw new UnsupportedOperationException(\"Exception injected by Holy!\");");
        return compiledClass;
    }
}
