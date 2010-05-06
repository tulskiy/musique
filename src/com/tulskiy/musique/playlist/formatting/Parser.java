/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.playlist.formatting;

import com.tulskiy.musique.playlist.formatting.tokens.*;
import com.tulskiy.musique.playlist.formatting.tokens.MethodExpression;
import com.tulskiy.musique.playlist.formatting.tokens.ParameterExpression;

import java.util.Stack;
import java.util.StringTokenizer;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class Parser {
    public Expression parse(String text) {
        text = text.replaceAll("\\s*,\\s*", ",");
        StringTokenizer st = new StringTokenizer(text, "$%,\'[])", true);
        MethodExpression root = new MethodExpression("eval");
        Stack<MethodExpression> stack = new Stack<MethodExpression>();
        stack.push(root);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.equals("%")) {
                String s = st.nextToken();
                s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
                stack.peek().addExpression(new ParameterExpression(s));
                if (!st.nextToken().equals("%")) {
                    System.err.println("Unknown token!");
                    break;
                }
            } else if (token.equals("$")) {
                String s = st.nextToken();
                s = s.substring(0, s.length() - 1);
                MethodExpression m = new MethodExpression(s);
                stack.peek().addExpression(m);
                stack.push(m);
            } else if (token.equals(")") || token.equals("]")) {
                stack.pop();
            } else if (token.equals(",")) {
                //ignore
            } else if (token.equals("\'")) {
                StringBuilder sb = new StringBuilder();
                while (st.hasMoreTokens()) {
                    String str = st.nextToken();
                    if (str.equals("\'"))
                        break;
                    sb.append(str);
                }

                stack.peek().addExpression(new TextExpression(sb.toString()));
            } else if (token.equals("[")) {
                MethodExpression m = new MethodExpression("notNull");
                stack.peek().addExpression(m);
                stack.push(m);
            } else {
                stack.peek().addExpression(new TextExpression(token));
            }
        }

        return root;
    }
}
