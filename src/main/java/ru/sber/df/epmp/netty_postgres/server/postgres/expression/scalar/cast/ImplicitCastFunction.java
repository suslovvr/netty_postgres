/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package ru.sber.df.epmp.netty_postgres.server.postgres.expression.scalar.cast;
/*
import com.example.spring.netty.spring_netty.server.postgres.data.Input;
import com.example.spring.netty.spring_netty.server.postgres.exceptions.ConversionException;
import com.example.spring.netty.spring_netty.server.postgres.expression.scalar.ScalarFunctionModule;
import com.example.spring.netty.spring_netty.server.postgres.expression.symbol.Literal;
import com.example.spring.netty.spring_netty.server.postgres.expression.symbol.Symbol;
import com.example.spring.netty.spring_netty.server.postgres.metadata.NodeContext;
import com.example.spring.netty.spring_netty.server.postgres.metadata.Scalar;
import com.example.spring.netty.spring_netty.server.postgres.metadata.TransactionContext;
import com.example.spring.netty.spring_netty.server.postgres.metadata.functions.BoundSignature;
import com.example.spring.netty.spring_netty.server.postgres.metadata.functions.Signature;
import com.example.spring.netty.spring_netty.server.postgres.types.DataType;
import com.example.spring.netty.spring_netty.server.postgres.types.DataTypes;
import com.example.spring.netty.spring_netty.server.postgres.types.TypeSignature;
import com.example.spring.netty.spring_netty.server.postgres.user.UserLookup;
import org.jetbrains.annotations.Nullable;
*/
import java.util.List;

//import static com.example.spring.netty.spring_netty.server.postgres.metadata.functions.TypeVariableConstraint.typeVariable;

public class ImplicitCastFunction //extends Scalar<Object, Object>
{

    public static final String NAME = "_cast";
/*
    public static void register(ScalarFunctionModule module) {
        module.register(
            Signature.scalar(
                NAME,
                TypeSignature.parse("E"),
                DataTypes.STRING.getTypeSignature(),
                DataTypes.UNDEFINED.getTypeSignature()
            ).withTypeVariableConstraints(typeVariable("E")),
            ImplicitCastFunction::new
        );
    }

    @Nullable
    private final DataType<?> targetType;

    private ImplicitCastFunction(Signature signature, BoundSignature boundSignature) {
        this(signature, boundSignature, null);
    }

    private ImplicitCastFunction(Signature signature, BoundSignature boundSignature, @Nullable DataType<?> targetType) {
        super(signature, boundSignature);
        this.targetType = targetType;
    }

    @Override
    public Scalar<Object, Object> compile(List<Symbol> args, String currentUser, UserLookup userLookup) {
        assert args.size() == 2 : "number of arguments must be 2";
        Symbol input = args.get(1);
        if (input instanceof Input) {
            String targetTypeValue = (String) ((Input<?>) input).value();
            var targetTypeSignature = TypeSignature.parse(targetTypeValue);
            var targetType = targetTypeSignature.createType();
            return new ImplicitCastFunction(
                signature,
                boundSignature,
                targetType
            );
        }
        return this;
    }

    @Override
    public Object evaluate(TransactionContext txnCtx, NodeContext nodeCtx, Input<Object>[] args) {
        assert args.length == 1 || args.length == 2 : "number of args must be 1 or 2";
        var arg = args[0].value();
        if (targetType == null) {
            var targetTypeSignature = TypeSignature.parse((String) args[1].value());
            var targetType = targetTypeSignature.createType();
            return castToTargetType(targetType, arg);
        } else {
            return castToTargetType(targetType, arg);
        }
    }

    private static Object castToTargetType(DataType<?> targetType, Object arg) {
        try {
            return targetType.implicitCast(arg);
        } catch (ConversionException e) {
            throw e;
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ConversionException(arg, targetType);
        }
    }

    @Override
    public Symbol normalizeSymbol(io.crate.expression.symbol.Function symbol,
                                  TransactionContext txnCtx,
                                  NodeContext nodeCtx) {
        Symbol argument = symbol.arguments().get(0);

        var targetTypeAsString = (String) ((Input<?>) symbol.arguments().get(1)).value();
        var targetType = TypeSignature.parse(targetTypeAsString).createType();

        if (argument.valueType().equals(targetType)) {
            return argument;
        }

        if (argument instanceof Input<?> input) {
            Object value = input.value();
            try {
                return Literal.ofUnchecked(targetType, targetType.implicitCast(value));
            } catch (ConversionException e) {
                throw e;
            } catch (ClassCastException | IllegalArgumentException e) {
                throw new ConversionException(argument, targetType);
            }
        }
        return symbol;
    }

 */
}
