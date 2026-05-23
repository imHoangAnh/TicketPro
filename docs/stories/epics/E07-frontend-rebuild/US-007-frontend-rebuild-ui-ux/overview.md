# Overview

## Current Behavior

The frontend has a basic structure but suffers from several gaps and legacy implementations:
- It relies on old legacy API endpoints (like `/active`, `/create`, `/order/cas`, `1/list`) rather than the clean, redesigned `/api/...` REST endpoints.
- No real login, registration, or JWT token management is wired up, meaning the authentication and authorization (ADMIN role) constraints are not actively enforced on routes or requests.
- The UI/UX is basic and simple, with inconsistent components, inline styles, and limited polish.

## Target Behavior

A complete rebuild of the React frontend that is both fully functional and visually striking:
- **API Alignment:** Fully aligns with the standard `/api` contracts defined in `docs/product/api.md` (Public events, Auth, Orders, and Admin management).
- **Authentication & Security:** Real login/register pages. Axios interceptors attach JWT access tokens, handle `401` automatic token refresh retry logic using the HTTP cookie-backed rotation (with `X-Requested-With: XMLHttpRequest`), and block unauthorized access.
- **Role-Based Guards:** Private route guards that restrict orders/checkout to authenticated users, and the system manager dashboard to `ADMIN` users only.
- **Booking Flow:** Fully polished, beautiful multi-step booking process. Event Detail -> Choose Ticket Type & Quantity -> Cart Checkout -> Select payment method (Mock Success or VNPAY Sandbox) -> Redirect / Payment Callback validation -> Success screen.
- **Admin Dashboard:** Elegant control panel for Admins to manage events, ticket types, and monitor orders with filters.
- **Beautiful UI/UX:** A highly polished, modern design utilizing refined CSS variables, smooth animations, interactive feedback states, professional typography, alert/notification toasts, and complete mobile-first responsiveness.

## Affected Users

- **Public Users:** Browse events, search, view active tickets.
- **Authenticated Users (USER):** Book tickets, checkout, view order history, cancel orders, process mock or VNPAY sandbox payments.
- **Admin Users (ADMIN):** Manage events (create, update, activate/deactivate, soft-delete), manage ticket types, list and filter all orders.

## Affected Product Docs

- `docs/product/frontend.md`
- `docs/product/api.md`

## Non-Goals

- Comprehensive Jest/Cypress automated test coverage for frontend components (since it is optional for the initial CV scope).
