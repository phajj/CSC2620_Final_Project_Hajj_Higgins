# CSC2620_Final_Project_Hajj_Higgins

## Authentication changes

- `LOGIN` no longer creates users. Use `REGISTER` (client command) or `UserStore.createUser(...)` to create accounts explicitly.
- Server-side API (in `server.auth.UserStore`):
	- `createUser(String username, String password)` — create a new user (returns false if username exists).
	- `authenticateExistingUser(String username, String password)` — authenticate an existing user (returns false if user missing or password wrong).
	- `register(...)` delegates to `createUser(...)` for backward compatibility.

This makes the semantics explicit and avoids accidental account creation on login attempts.

See the migration guide for details: [MIGRATION.md](MIGRATION.md)