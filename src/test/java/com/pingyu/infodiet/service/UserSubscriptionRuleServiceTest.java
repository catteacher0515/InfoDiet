package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.impl.UserSubscriptionRuleServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSubscriptionRuleServiceTest {

    @Test
    void addRuleShouldCreateEnabledRuleWithDefaultWeight() {
        InMemoryUserSubscriptionRuleService service = new InMemoryUserSubscriptionRuleService();

        boolean result = service.addRule(UserSubscriptionRule.builder()
                .userId(1L)
                .ruleType(" keyword_include ")
                .ruleValue(" agent ")
                .build());

        assertEquals(true, result);
        assertEquals(1, service.items.size());
        assertEquals("keyword_include", service.items.getFirst().getRuleType());
        assertEquals("agent", service.items.getFirst().getRuleValue());
        assertEquals(1, service.items.getFirst().getRuleWeight());
        assertEquals(1, service.items.getFirst().getStatus());
    }

    @Test
    void listEnabledRulesByUserIdShouldOnlyReturnEnabledRules() {
        InMemoryUserSubscriptionRuleService service = new InMemoryUserSubscriptionRuleService();
        service.items.add(UserSubscriptionRule.builder().id(1L).userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(2).status(1).build());
        service.items.add(UserSubscriptionRule.builder().id(2L).userId(1L).ruleType("author").ruleValue("Google").ruleWeight(5).status(1).build());
        service.items.add(UserSubscriptionRule.builder().id(3L).userId(1L).ruleType("channel").ruleValue("UC123").ruleWeight(3).status(0).build());

        List<UserSubscriptionRule> rules = service.listEnabledRulesByUserId(1L);

        assertEquals(2, rules.size());
        assertEquals(List.of("keyword_include", "author"), rules.stream().map(UserSubscriptionRule::getRuleType).toList());
    }

    @Test
    void removeRuleShouldDeleteMatchedRule() {
        InMemoryUserSubscriptionRuleService service = new InMemoryUserSubscriptionRuleService();
        service.items.add(UserSubscriptionRule.builder().id(1L).userId(1L).ruleType("keyword_include").ruleValue("agent").status(1).build());
        service.items.add(UserSubscriptionRule.builder().id(2L).userId(1L).ruleType("author").ruleValue("Google").status(1).build());

        boolean removed = service.removeRule(1L, "keyword_include", "agent");

        assertEquals(true, removed);
        assertEquals(1, service.items.size());
        assertEquals("author", service.items.getFirst().getRuleType());
    }

    private static class InMemoryUserSubscriptionRuleService extends UserSubscriptionRuleServiceImpl {

        private final List<UserSubscriptionRule> items = new ArrayList<>();

        @Override
        public boolean save(UserSubscriptionRule entity) {
            items.add(entity);
            return true;
        }

        @Override
        public boolean remove(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.removeIf(item ->
                    item.getUserId().equals(1L)
                            && "keyword_include".equals(item.getRuleType())
                            && "agent".equals(item.getRuleValue()));
        }

        @Override
        public List<UserSubscriptionRule> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.stream()
                    .filter(item -> item.getUserId().equals(1L) && item.getStatus() != null && item.getStatus() == 1)
                    .toList();
        }
    }
}
