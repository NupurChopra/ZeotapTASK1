import java.util.*;
import java.util.regex.*;

public class RuleEngine {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\(|\\)|AND|OR|>|<|=|\\w+|'[^']*')");

    public static Node createRule(String ruleString) {
        List<String> tokens = tokenize(ruleString);
        return parseExpression(tokens);
    }

    private static List<String> tokenize(String ruleString) {
        Matcher matcher = TOKEN_PATTERN.matcher(ruleString);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static Node parseExpression(List<String> tokens) {
        Stack<Node> nodeStack = new Stack<>();
        Stack<String> operatorStack = new Stack<>();

        for (String token : tokens) {
            if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.peek().equals("(")) {
                    processOperator(nodeStack, operatorStack);
                }
                operatorStack.pop(); // Remove "("
            } else if (token.equals("AND") || token.equals("OR")) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(token)) {
                    processOperator(nodeStack, operatorStack);
                }
                operatorStack.push(token);
            } else {
                nodeStack.push(new Node("operand", token, null, null));
            }
        }

        while (!operatorStack.isEmpty()) {
            processOperator(nodeStack, operatorStack);
        }

        return nodeStack.pop();
    }

    private static void processOperator(Stack<Node> nodeStack, Stack<String> operatorStack) {
        String operator = operatorStack.pop();
        Node right = nodeStack.pop();
        Node left = nodeStack.pop();
        nodeStack.push(new Node("operator", operator, left, right));
    }

    private static int precedence(String operator) {
        return operator.equals("AND") ? 2 : operator.equals("OR") ? 1 : 0;
    }

    public static Node combineRules(List<String> rules) {
        if (rules.isEmpty()) return null;
        List<Node> nodes = new ArrayList<>();
        for (String rule : rules) {
            nodes.add(createRule(rule));
        }
        while (nodes.size() > 1) {
            Node left = nodes.remove(0);
            Node right = nodes.remove(0);
            nodes.add(0, new Node("operator", "AND", left, right));
        }
        return nodes.get(0);
    }

    public static boolean evaluateRule(Node ast, Map<String, Object> data) {
        if (ast == null) {
            throw new IllegalArgumentException("AST node cannot be null");
        }
        if ("operand".equals(ast.type)) {
            return evaluateOperand(ast.value, data);
        } else if ("operator".equals(ast.type)) {
            boolean leftEval = evaluateRule(ast.left, data);
            boolean rightEval = evaluateRule(ast.right, data);
            return "AND".equals(ast.value) ? leftEval && rightEval : leftEval || rightEval;
        }
        throw new IllegalArgumentException("Invalid node type: " + ast.type);
    }

    private static boolean evaluateOperand(String condition, Map<String, Object> data) {
        Pattern operandPattern = Pattern.compile("(\\w+)\\s*(>|<|=)\\s*([\\w.']+)");
        Matcher matcher = operandPattern.matcher(condition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid operand: " + condition);
        }

        String key = matcher.group(1);
        String operator = matcher.group(2);
        String value = matcher.group(3).replace("'", ""); // Remove quotes if present

        if (!data.containsKey(key)) {
            return false;
        }

        Object dataValue = data.get(key);
        return compareValues(dataValue, value, operator);
    }

    private static boolean compareValues(Object dataValue, String value, String operator) {
        if (dataValue instanceof Number && isNumeric(value)) {
            double numDataValue = ((Number) dataValue).doubleValue();
            double numValue = Double.parseDouble(value);
            switch (operator) {
                case ">":
                    return numDataValue > numValue;
                case "<":
                    return numDataValue < numValue;
                case "=":
                    return numDataValue == numValue;
                default:
                    throw new IllegalArgumentException("Invalid operator for numeric comparison: " + operator);
            }
        } else {
            int comparisonResult = dataValue.toString().compareTo(value);
            switch (operator) {
                case ">":
                    return comparisonResult > 0;
                case "<":
                    return comparisonResult < 0;
                case "=":
                    return comparisonResult == 0;
                default:
                    throw new IllegalArgumentException("Invalid operator for string comparison: " + operator);
            }
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}