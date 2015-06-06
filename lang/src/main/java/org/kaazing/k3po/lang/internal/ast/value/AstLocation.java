/*
 * Copyright 2014, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaazing.k3po.lang.internal.ast.value;

public abstract class AstLocation extends AstValue {

    public abstract <R, P> R accept(LocationVisitor<R, P> visitor, P parameter) throws Exception;

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) throws Exception {
        if (visitor instanceof LocationVisitor) {
            return accept((LocationVisitor<R, P>) visitor, parameter);
        }
        else {
            return accept(visitor, parameter);
        }
    }

    public interface LocationVisitor<R, P> extends Visitor<R, P> {

        R visit(AstUriLiteralValue value, P parameter) throws Exception;

    }

}
