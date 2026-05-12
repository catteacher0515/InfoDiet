# Frontend Auth Workspace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a usable frontend workspace for InfoDiet and add a JWT-based auth layer that supports login, registration, admin user creation, and role-based access areas.

**Architecture:** Extend the existing Spring Boot app with a lightweight JWT auth layer built on `user_profile`, then add a separate `frontend/` SPA that consumes authenticated APIs and renders public, workspace, admin, and ops areas. Keep orchestration in the existing backend and add only the minimal page-facing aggregate APIs needed for the first product version.

**Tech Stack:** Spring Boot 3, MyBatis Flex, Hutool, JUnit 5, Vite, React, TypeScript

---

### Task 1: Add Auth Data Model

**Files:**
- Modify: `sql/create_table.sql`
- Create: `sql/alter_user_profile_add_auth_fields.sql`
- Modify: `src/main/java/com/pingyu/infodiet/model/entity/UserProfile.java`
- Test: `src/test/java/com/pingyu/infodiet/service/UserProfileServiceTest.java`

- [ ] Add `username`, `password`, `role` to `user_profile`
- [ ] Add entity fields and keep existing profile fields unchanged
- [ ] Add tests that assert create/update flows preserve auth fields

### Task 2: Implement JWT Auth Backend

**Files:**
- Create: `src/main/java/com/pingyu/infodiet/model/dto/auth/AuthLoginRequest.java`
- Create: `src/main/java/com/pingyu/infodiet/model/dto/auth/AuthRegisterRequest.java`
- Create: `src/main/java/com/pingyu/infodiet/model/dto/auth/AdminCreateUserRequest.java`
- Create: `src/main/java/com/pingyu/infodiet/model/dto/auth/LoginUserVO.java`
- Create: `src/main/java/com/pingyu/infodiet/service/AuthService.java`
- Create: `src/main/java/com/pingyu/infodiet/service/impl/AuthServiceImpl.java`
- Create: `src/main/java/com/pingyu/infodiet/controller/AuthController.java`
- Create: `src/main/java/com/pingyu/infodiet/config/JwtProperties.java`
- Create: `src/main/java/com/pingyu/infodiet/utils/JwtUtils.java`
- Create: `src/main/java/com/pingyu/infodiet/model/auth/LoginUser.java`
- Create: `src/main/java/com/pingyu/infodiet/model/auth/LoginUserContext.java`
- Create: `src/main/java/com/pingyu/infodiet/filter/JwtAuthenticationFilter.java`
- Modify: `src/main/java/com/pingyu/infodiet/config/InfoDietProperties.java`
- Test: `src/test/java/com/pingyu/infodiet/service/AuthServiceTest.java`
- Test: `src/test/java/com/pingyu/infodiet/controller/AuthControllerTest.java`

- [ ] Write failing tests for register, login, me, logout, admin create
- [ ] Implement minimal JWT creation and parsing
- [ ] Add request filter and current-user context
- [ ] Run auth tests and make them green

### Task 3: Add Dashboard Aggregate APIs

**Files:**
- Create: `src/main/java/com/pingyu/infodiet/model/dto/dashboard/WorkspaceDashboardVO.java`
- Create: `src/main/java/com/pingyu/infodiet/model/dto/dashboard/AdminDashboardVO.java`
- Create: `src/main/java/com/pingyu/infodiet/model/dto/dashboard/OpsDashboardVO.java`
- Create: `src/main/java/com/pingyu/infodiet/service/DashboardService.java`
- Create: `src/main/java/com/pingyu/infodiet/service/impl/DashboardServiceImpl.java`
- Create: `src/main/java/com/pingyu/infodiet/controller/DashboardController.java`
- Test: `src/test/java/com/pingyu/infodiet/service/DashboardServiceTest.java`
- Test: `src/test/java/com/pingyu/infodiet/controller/DashboardControllerTest.java`

- [ ] Add failing tests for three dashboard endpoints
- [ ] Implement minimal aggregate counts using existing services/mappers
- [ ] Keep workspace dashboard scoped to current user

### Task 4: Add Admin User List API

**Files:**
- Create: `src/main/java/com/pingyu/infodiet/model/dto/user/UserListItemVO.java`
- Modify: `src/main/java/com/pingyu/infodiet/service/UserProfileService.java`
- Modify: `src/main/java/com/pingyu/infodiet/service/impl/UserProfileServiceImpl.java`
- Modify: `src/main/java/com/pingyu/infodiet/controller/UserProfileController.java`
- Test: `src/test/java/com/pingyu/infodiet/service/UserProfileServiceTest.java`
- Test: `src/test/java/com/pingyu/infodiet/controller/UserProfileControllerTest.java`

- [ ] Add failing tests for admin user list behavior
- [ ] Add list endpoint that returns product-facing fields
- [ ] Keep old endpoints available unless they block frontend use

### Task 5: Initialize Frontend

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/tsconfig.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/index.html`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/App.tsx`
- Create: `frontend/src/styles/*.css`

- [ ] Scaffold frontend project
- [ ] Add base layout, theme variables, and router
- [ ] Add API client with JWT header support

### Task 6: Build Auth Pages And Guards

**Files:**
- Create: `frontend/src/pages/auth/LoginPage.tsx`
- Create: `frontend/src/pages/auth/RegisterPage.tsx`
- Create: `frontend/src/routes/ProtectedRoute.tsx`
- Create: `frontend/src/store/auth.ts`
- Test: frontend manual verification

- [ ] Implement login and register flows
- [ ] Persist token and user info
- [ ] Redirect by role after login

### Task 7: Build Workspace, Admin, Ops Shell

**Files:**
- Create: `frontend/src/layout/AppShell.tsx`
- Create: `frontend/src/pages/workspace/*.tsx`
- Create: `frontend/src/pages/admin/*.tsx`
- Create: `frontend/src/pages/ops/*.tsx`
- Create: `frontend/src/components/*.tsx`

- [ ] Add left navigation and top bar
- [ ] Render menus by role
- [ ] Connect dashboard and list pages to live backend APIs

### Task 8: Verify, Commit, Push

**Files:**
- Modify: `README.md` if startup instructions are needed

- [ ] Run backend targeted tests
- [ ] Run frontend build
- [ ] Start app and verify login plus core pages
- [ ] Commit and push in logical chunks
