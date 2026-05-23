# Validation

## Proof Strategy

The frontend rebuild is validated by ensuring that:
1. **Compilation & Packaging:** The React project successfully compiles and builds with no compilation, routing, or linting errors via `npm run build`.
2. **REST Endpoints Mapping:** The newly implemented service methods in `api.js` accurately match the `/api/...` contracts in `api.md`.
3. **Authentication & Guards:**
   - Standard user `user@example.com` can login and obtain an access token.
   - Access token is attached to authenticated requests automatically.
   - Admin routes `/system/manager` block standard USER accounts and redirect them.
   - Admin user `admin@example.com` can access `/system/manager`.
   - On `401 Unauthorized`, Axios interceptors atomically rotate refresh tokens and retry the original request.
4. **End-to-End Booking:**
   - A public user can browse active events and active ticket types.
   - An authenticated user can select a ticket, enter a quantity, reserve stock in Redis (creating a `PENDING` order), and complete payment via Mock success or VNPAY sandbox redirection.
   - An order detail screen confirms transitions to `PAID` or allows `CANCELLED` transitions.
5. **Admin Operations:**
   - Admin dashboard enables creation, updating, activation, deactivation, and soft-deletion of events.
   - Admin can add or edit ticket types.
   - Admin can list and view all orders.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Verify `api.js` functions successfully construct endpoints, map response envelopes (`result`), and inject headers |
| Integration | Axios token interceptor manages 401 intercept, sequential calls, queueing, and `X-Requested-With: XMLHttpRequest` header |
| Platform | React build `npm run build` compiles with zero warnings or errors |
| User E2E | Manual execution of entire checkout flow: Event detail -> Select General Admission -> Cart -> Reserve -> Pay Mock -> Redirect to Success |
| Admin E2E | Manual execution of admin flow: Login -> Access Manager Dashboard -> Create Event -> Create Ticket Type -> Activate -> Verify on Home Page |

## Fixtures

- **Standard User:** `user@example.com` / `password123` (seeding in `ticket_init.sql` with role `USER`)
- **Admin User:** `admin@example.com` / `password123` (seeding in `ticket_init.sql` with roles `USER` and `ADMIN`)
- **VNPAY Sandbox Gateway:** Sandbox endpoints and checkout simulated parameters.

## Commands

Before verifying the frontend, verify the backend is running at `http://localhost:1122`.

```bash
# From xxxx.fe.com folder:
npm run lint
npm run build
```

## Acceptance Evidence

Implemented and validated on 2026-05-23.

### Current Review Note

Follow-up review on 2026-05-23 found this evidence is stale. `npm run lint`
currently fails with eight errors, and `npm run build` fails because several
components import `ticketService` and `managerService` from `src/services/api.js`
even though that file no longer exports those names. Treat E07 as changed until
the frontend API client and component imports are repaired and validation is
rerun.

### 1. Static Validation (Linter)
Running `npm run lint` yields zero warnings and zero errors:
```text
> frontend@0.0.0 lint
> eslint .
```

### 2. Compilation and Bundling (Build)
Running `npm run build` bundles the application flawlessly:
```text
> frontend@0.0.0 build
> vite build

vite v8.0.9 building client environment for production...
transforming...✓ 1796 modules transformed.
rendering chunks...
computing gzip size...
dist/index.html                   0.94 kB │ gzip:   0.56 kB
dist/assets/index-TmWCOuz1.css   23.85 kB │ gzip:   4.65 kB
dist/assets/index-B_1aWR7_.js   376.43 kB │ gzip: 111.85 kB

✓ built in 497ms
```

### 3. Verification Details
- **Design Overhaul:** Expanded `src/index.css` with a premium palette, crisp forms, high-fidelity tables, elegant overlays, loading indicators, custom dropdowns, and alert toasts.
- **API Mapping:** Overhauled `src/services/api.js` to exactly align with the `/api` contracts.
- **Token Interceptor:** Implemented Axios interceptor with atomic refresh.
- **Route Guards:** Embedded custom `PrivateRoute` and `AdminRoute` in `src/App.jsx`.
- **Integrated Customer Flow:** Home event card lists -> Details selection -> Cart -> Reserved order -> Mock success or VNPAY redirect -> Callback validation -> Success receipts.
- **Unified Admin Board:** High-fidelity admin cockpit for Event and nested Ticket Type CRUD management, and Order supervision.
