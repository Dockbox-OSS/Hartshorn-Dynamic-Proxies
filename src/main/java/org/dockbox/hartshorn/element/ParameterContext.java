/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.element;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ParameterContext<T> extends AnnotatedElementContext<Parameter> implements TypedElementContext<T> {

    private final Parameter parameter;
    private final boolean isVarargs;

    private String name;
    private TypeContext<T> type;
    private TypeContext<T> genericType;
    private ExecutableElementContext<?, ?> declaredBy;
    private List<TypeContext<?>> typeParameters;

    private ParameterContext(final Parameter parameter) {
        this.parameter = parameter;
        this.isVarargs = parameter.isVarArgs();
    }

    public static <T> ParameterContext<T> of(final Parameter parameter) {
        return new ParameterContext<>(parameter);
    }

    public boolean varargs() {
        return this.isVarargs;
    }

    @Override
    public String name() {
        if (this.name == null) {
            this.name = this.element().getName();
        }
        return this.name;
    }

    public ExecutableElementContext<?, ?> declaredBy() {
        if (this.declaredBy == null) {
            final Executable executable = this.element().getDeclaringExecutable();
            if (executable instanceof Method) this.declaredBy = MethodContext.of((Method) executable);
            else if (executable instanceof Constructor) this.declaredBy = ConstructorContext.of((Constructor) executable);
            else throw new RuntimeException("Unexpected executable type: " + executable.getName());
        }
        return this.declaredBy;
    }

    @Override
    public String qualifiedName() {
        return String.format("%s -> %s[%s]",
                this.declaredBy().qualifiedName(),
                this.name(),
                this.type().qualifiedName()
        );
    }

    @Override
    public TypeContext<T> type() {
        if (this.type == null) {
            this.type = TypeContext.of((Class<T>) this.element().getType());
        }
        return this.type;
    }

    @Override
    public TypeContext<T> genericType() {
        if (this.genericType == null) {
            final Type genericType = this.element().getParameterizedType();

            if (genericType instanceof ParameterizedType) this.genericType = TypeContext.of((ParameterizedType) genericType);
            else this.genericType = this.type();
        }
        return this.genericType;
    }

    @Override
    protected Parameter element() {
        return this.parameter;
    }

    public List<TypeContext<?>> typeParameters() {
        if (this.typeParameters == null) {
            final Type type = this.element().getParameterizedType();
            if (type instanceof ParameterizedType ) {
                this.typeParameters = Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
                        .map(t -> {
                            if (t instanceof WildcardType) return WildcardTypeContext.create();
                            else if (t instanceof Class) return TypeContext.of((Class<?>) t);
                            else return TypeContext.VOID;
                        })
                        .collect(Collectors.toList());
            }
            else {
                this.typeParameters = new ArrayList<>();
            }
        }
        return this.typeParameters;
    }
}
