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
 */
//import com.example.spring.netty.spring_netty.server.postgres.types.TypeSignature;

//import static io.crate.metadata.functions.TypeVariableConstraint.typeVariable;

public class ExplicitCastFunction //extends Scalar<Object, Object>
{

    public static final String NAME = "cast";
/*
    public static void register(ScalarFunctionModule module) {
        module.register(
            Signature
                .scalar(
                    NAME,
                    TypeSignature.parse("E"),
                    TypeSignature.parse("V"),
                    TypeSignature.parse("V"))
                .withTypeVariableConstraints(typeVariable("E"), typeVariable("V")),
            ExplicitCastFunction::new
        );
    }
*/
//    private final DataType<?> returnType;
/*
    private ExplicitCastFunction(Signature signature, BoundSignature boundSignature) {
        super(signature, boundSignature);
        this.returnType = boundSignature.returnType();
    }

    @Override
    public Object evaluate(TransactionContext txnCtx, NodeContext nodeCtx, Input<Object>[] args) {
        try {
            return returnType.explicitCast(args[0].value(), txnCtx.sessionSettings());
        } catch (ConversionException e) {
            throw e;
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ConversionException(args[0].value(), returnType);
        }
    }

    @Override
    public Symbol normalizeSymbol(io.crate.expression.symbol.Function symbol,
                                  TransactionContext txnCtx,
                                  NodeContext nodeCtx) {
        Symbol argument = symbol.arguments().get(0);
        if (argument.valueType().equals(returnType)) {
            return argument;
        }

        if (argument instanceof Input) {
            Object value = ((Input<?>) argument).value();
            try {
                return Literal.ofUnchecked(returnType, returnType.explicitCast(value, txnCtx.sessionSettings()));
            } catch (ConversionException e) {
                throw e;
            } catch (ClassCastException | IllegalArgumentException e) {
                throw new ConversionException(argument, returnType);
            }
        }
        return symbol;
    }

 */
}
