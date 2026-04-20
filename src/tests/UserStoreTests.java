package tests;

import server.auth.PasswordUtil;
import server.auth.UserStore;

import java.io.File;

public class UserStoreTests {

    public static void main(String[] args) throws Exception {
        int failed = 0;

        // Test PasswordUtil
        String pw = "s3cret";
        String hashed = PasswordUtil.hashPassword(pw);
        if (!PasswordUtil.verifyPassword(pw, hashed)) {
            System.err.println("PasswordUtil verify failed");
            failed++;
        } else {
            System.out.println("PasswordUtil OK");
        }

        // Create temp file for UserStore
        File tmp = File.createTempFile("userstore_test", ".txt");
        tmp.deleteOnExit();

        UserStore store = new UserStore(tmp);

        // create user
        boolean created = store.authenticateOrCreate("testuser", "oldpass");
        if (!created) {
            System.err.println("Failed to create user");
            failed++;
        }

        // set keyword
        boolean saved = store.setKeyword("testuser", "kw1");
        if (!saved) {
            System.err.println("Failed to save keyword");
            failed++;
        }
        String got = store.getKeyword("testuser");
        if (!"kw1".equals(got)) {
            System.err.println("Keyword mismatch: expected kw1 got " + got);
            failed++;
        }

        // change password
        boolean changed = store.changePassword("testuser", "oldpass", "newpass");
        if (!changed) {
            System.err.println("Failed to change password");
            failed++;
        }
        boolean verifyOld = store.authenticateOrCreate("testuser", "oldpass");
        if (verifyOld) {
            System.err.println("Old password should not authenticate");
            failed++;
        }

        // delete user
        boolean deleted = store.deleteUser("testuser");
        if (!deleted) {
            System.err.println("Failed to delete user");
            failed++;
        }

        // Reload from file to ensure deletion persisted
        UserStore reloaded = new UserStore(tmp);
        String kwAfter = reloaded.getKeyword("testuser");
        if (kwAfter != null) {
            System.err.println("User still present after delete; keyword=" + kwAfter);
            failed++;
        }

        if (failed == 0) {
            System.out.println("ALL TESTS PASSED");
            System.exit(0);
        } else {
            System.err.println(failed + " TEST(S) FAILED");
            System.exit(2);
        }
    }
}
