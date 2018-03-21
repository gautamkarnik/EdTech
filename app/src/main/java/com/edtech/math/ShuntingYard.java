package com.edtech.math;
// Source: http://rosettacode.org/wiki/Parsing/Shunting-yard_algorithm#Java
// Note: does not support negative numbers

import android.util.Log;

import java.util.Stack;
import java.lang.*;


public class ShuntingYard {


    public static final String CLASS_NAME = ShuntingYard.class.getSimpleName();

    public void main(String[] args) {
        String infix = "3 + 4 × 2 ÷ ( 1 - 5 ) ^ 2 ^ 3";
        System.out.printf("infix:   %s%n", infix);
        System.out.printf("postfix: %s%n", infixToPostfix(infix));
        
        infix = "5 + 3 × 2";
        System.out.printf("infix:   %s%n", infix);
        System.out.printf("postfix: %s%n", infixToPostfix(infix));
        
        //bug here
        infix = "5 × - 2";
        System.out.printf("infix:   %s%n", infix);
        System.out.printf("postfix: %s%n", infixToPostfix(infix));

    }

    public static String infixToPostfix(String infix) {

        Log.d(CLASS_NAME, "Calculating infixToPostfix");

        final String ops = "-+÷×^";
        StringBuilder sb = new StringBuilder();
        Stack<Integer> s = new Stack<>();
		
        for (String token : infix.split("\\s")) {
            if (token.isEmpty())
                continue;
            char c = token.charAt(0);
            int idx = ops.indexOf(c);
			
            // check for operator
            if (idx != -1) {
                if (s.isEmpty())
                    s.push(idx);
				
                else {
                    while (!s.isEmpty()) {
                        int prec2 = s.peek() / 2;
                        int prec1 = idx / 2;
                        if (prec2 > prec1 || (prec2 == prec1 && c != '^'))
                            sb.append(ops.charAt(s.pop())).append(' ');
                        else break;
                    }
                    s.push(idx);
                }
            } 
            else if (c == '(') {
                s.push(-2); // -2 stands for '('
            } 
            else if (c == ')') {
                // until '(' on stack, pop operators.
                while (s.peek() != -2)
                    sb.append(ops.charAt(s.pop())).append(' ');
                s.pop();
            }
            else {
                sb.append(token).append(' ');
            }
        }
        while (!s.isEmpty())
            sb.append(ops.charAt(s.pop())).append(' ');
        return sb.toString();
    }
}