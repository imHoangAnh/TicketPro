# Exec Plan

## Goal

Rebuild the React + Vite frontend application from the ground up around the official `/api/*` REST contract, introducing robust auth handling, role-based private routes, modern UI layouts, elite UX micro-interactions, and a seamless booking-to-payment flow.

## Scope

In scope:
- **Design System & Tokens:** Overhaul `src/index.css` with a premium dark-indigo and golden-amber palette, modern card styling, interactive states, loading indicators, custom modals, and floating alert notifications.
- **Service Layer Overhaul:** Rewrite `src/services/api.js` using a clean Axios client with `withCredentials: true`, headers including `X-Requested-With: XMLHttpRequest` on mutative cookies requests, and an interceptor for auto-renewing access tokens.
- **Protected Routing:** Secure routes in `src/App.jsx` using `PrivateRoute` and `AdminRoute` guards.
- **Auth Views:** Create polished, beautiful Login and Register pages.
- **Public Event Browsing:** Beautify `Home.jsx`, `TicketsPage.jsx` (with search and tag filters), and `TicketDetailPage.jsx` (showing active events and ticket types dynamically).
- **Checkout & Payment:** Revamp `CartPage.jsx` and `BookingSuccessPage.jsx`. Integrate both Mock success payment and VNPAY sandbox redirect flows. Implement real-time status updates and cancellation of PENDING orders.
- **Admin Management Panel:** Refactor `ManagerPage.jsx` into a premium Admin dashboard split into three tabbed views: Events, Ticket Types, and Orders.

Out of scope:
- Frontend unit/integration test suites (optional for CV scope).
- Making modifications to the backend codebase.

## Risk Classification

Risk flags:
- **Auth:** Manages access/refresh token exchange and auth headers.
- **Authorization:** Directs users based on USER or ADMIN roles.
- **External systems:** Integrates VNPAY payments, callback verification, and redirects.
- **Public contracts:** Aligns with backend REST APIs.
- **Existing behavior:** Completely overwrites and replaces legacy React components.

Hard gates:
- Auth logic must handle Refresh token rotation.
- Admin routes must block non-admins.
- Payment callback signatures must resolve correctly.

## Work Phases

1. **Discovery:** Confirm the running backend URLs and port (1122), and check endpoints.
2. **Design Tokens:** Update CSS custom properties in `src/index.css` to build a cohesive premium design system.
3. **API & Interceptors:** Refactor `api.js` to handle all standard backend operations, including the retry/refresh loop.
4. **Auth & Protection:** Build Login, Register, and wrap private routes.
5. **Main User Pages:** Polish Header, Footer, Home, TicketsPage, and TicketDetailPage.
6. **Cart & Booking Flow:** Connect order reservation, payment gateways (Mock and VNPAY), and order status tracking.
7. **Admin Dashboard:** Redesign `ManagerPage.jsx` for beautiful event, ticket type, and order management.
8. **Polishing & Verification:** Fix code style, run linting checks, build the application, and test manually.

## Stop Conditions

Pause for human confirmation if:
- API endpoint paths or request/response formats differ from `api.md`.
- Build tools or runtime environments throw unexpected compilation errors.
