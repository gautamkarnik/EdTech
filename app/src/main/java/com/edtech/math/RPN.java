package com.edtech.math;
// Source: http://rosettacode.org/wiki/Parsing/RPN_calculator_algorithm#Java_2
// Note: does not support negative numbers

import android.util.Log;

import java.util.LinkedList;
import java.lang.*;

public class RPN {

    public static final String CLASS_NAME = RPN.class.getSimpleName();;

	public static double evalRPN(String expr){
		String cleanExpr = cleanExpr(expr);
		LinkedList<Double> stack = new LinkedList<Double>();
        Log.d(CLASS_NAME, "evaluating expression: "+ expr);
        Log.d(CLASS_NAME, "cleaned expression: "+ cleanExpr);
        try {
            for(String token:cleanExpr.split("\\s")){
                Double tokenNum = null;
                try{
                    tokenNum = Double.parseDouble(token);
                }catch(NumberFormatException e){}
                if(tokenNum != null){
                    stack.push(Double.parseDouble(token+""));
                }else if(token.equals("×")){
                    double secondOperand = stack.pop();
                    double firstOperand = stack.pop();
                    stack.push(firstOperand * secondOperand);
                }else if(token.equals("÷")){
                    double secondOperand = stack.pop();
                    double firstOperand = stack.pop();
                    stack.push(firstOperand / secondOperand);
                }else if(token.equals("-")){
                    double secondOperand = stack.pop();
                    double firstOperand = stack.pop();
                    stack.push(firstOperand - secondOperand);
                }else if(token.equals("+")){
                    double secondOperand = stack.pop();
                    double firstOperand = stack.pop();
                    stack.push(firstOperand + secondOperand);
                }else if(token.equals("^")){
                    double secondOperand = stack.pop();
                    double firstOperand = stack.pop();
                    stack.push(Math.pow(firstOperand, secondOperand));
                }else{//just in case
                    return 0;
                }
            }
            return(stack.pop());
        } catch(Exception e) {
            Log.d(CLASS_NAME,"Invalid expression.");
            return 0;
        }
	}
	
	private static String cleanExpr(String expr){
		//remove all non-operators, non-whitespace, and non digit chars
		return expr.replaceAll("[^\\×\\÷\\+\\-\\d/\\s]", "");
	}
	
}