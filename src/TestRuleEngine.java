import java.util.*;

public class TestRuleEngine {
    public static void main(String[] args) {
        // Test cases
        String rule1 = "((age > 30 AND department = 'Sales') OR (age < 25 AND department = 'Marketing')) AND (salary > 50000 OR experience > 5)";
        String rule2 = "((age > 30 AND department = 'Marketing')) AND (salary > 20000 OR experience > 5)";

        Node ast1 = RuleEngine.createRule(rule1);
        Node ast2 = RuleEngine.createRule(rule2);

        List<String> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);
        Node combinedAst = RuleEngine.combineRules(rules);

        Map<String, Object> data = new HashMap<>();
        data.put("age", 35);
        data.put("department", "Sales");
        data.put("salary", 60000);
        data.put("experience", 3);

        System.out.println("Rule 1 evaluation: " + RuleEngine.evaluateRule(ast1, data));
        System.out.println("Rule 2 evaluation: " + RuleEngine.evaluateRule(ast2, data));
        System.out.println("Combined rules evaluation: " + RuleEngine.evaluateRule(combinedAst, data));
    }
}