package com.droidcrypt.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class AccountName {

    /**
     * An array of sample (dummy) items.
     */
    public static List<AccountItem> ITEMS = new ArrayList<AccountItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, AccountItem> ITEM_MAP = new HashMap<String, AccountItem>();

    static {
        // Add 3 sample items.
        addItem(new AccountItem("1", "Google"));
        addItem(new AccountItem("2", "Facebook"));
        addItem(new AccountItem("3", "Microsoft"));
        addItem(new AccountItem("4", "Skype"));
        addItem(new AccountItem("5", "Other"));
    }

    private static void addItem(AccountItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class AccountItem {
        public String id;
        public String content;

        public AccountItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
