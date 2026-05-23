# Design

## Domain Model

On the frontend, the domain entities mirror the backend DTO structure:
- **User/Auth:** `id`, `email`, `role` (USER or ADMIN), `accessToken`.
- **Event:** `id`, `title`, `description`, `location`, `startTime`, `endTime`, `status` (ACTIVE, INACTIVE, DRAFT), `ticketTypes` (list of TicketTypes).
- **TicketType:** `id`, `name`, `price`, `totalQuantity`, `reservedQuantity`, `soldQuantity`.
- **Order:** `id`, `orderNumber`, `ticketTypeId`, `quantity`, `totalPrice`, `status` (PENDING, PAID, CANCELLED), `createdAt`.
- **Payment:** `id`, `orderId`, `amount`, `paymentMethod` (MOCK, VNPAY), `status` (SUCCESS, FAILED), `transactionId`.

## Application Flow

### 1. Authentication State Flow
- On app load, the frontend checks if an `accessToken` is stored (in memory or localStorage). It calls `GET /api/auth/me` to fetch current user profile and role.
- If unauthenticated, access is permitted only to `/`, `/tickets`, `/ticket/:id`, `/login`, and `/register`.
- If a request receives `401 Unauthorized`, an Axios interceptor catches it:
  - If a refresh token request is already in progress, queue the original request.
  - Else, issue `POST /api/auth/refresh` (with `withCredentials: true` and `X-Requested-With: XMLHttpRequest`).
  - If refresh succeeds, retry the original request with the new access token.
  - If refresh fails, clear auth state and redirect to `/login`.

### 2. Event Booking & Payment Flow
- **Event Detail Page:** User selects a ticket type and quantity, then clicks "Book Now".
- **Cart/Checkout Page:** Displays selected ticket details, quantity, and total. User logs in if not already. User clicks "Confirm & Reserve":
  - Calls `POST /api/orders` which reserves stock via Redis Lua and returns a `PENDING` order.
  - User chooses payment method:
    - **Mock Payment:** Calls `POST /api/payments/{orderId}/mock-success`, then redirects directly to `/booking-success` with success state.
    - **VNPAY Sandbox:** Calls `POST /api/payments/{orderId}/vnpay`, receiving a payment URL. Frontend redirects user to VNPAY Sandbox. After payment, VNPAY redirects back to our callback page `/booking-success` with query params (which are sent to backend for signature verification).
- **Booking Success Page:** Polls or displays order status based on order info and query params.

### 3. Admin Operations Flow
- **Admin Event Panel:** List all events. Create new event, edit event details, change event status (active/inactive), soft-delete event.
- **Admin Ticket Type Panel:** Create or update ticket types for an event.
- **Admin Order Panel:** Monitor all orders across the system.

## Interface Contract

We'll define clean methods in `src/services/api.js`:
- **Auth:** `authService.login`, `authService.register`, `authService.logout`, `authService.refresh`, `authService.getMe`.
- **Public/Event:** `eventService.getEvents`, `eventService.getEventById`.
- **User/Order:** `orderService.createOrder`, `orderService.getMyOrders`, `orderService.getOrderById`, `orderService.cancelOrder`.
- **Payment:** `paymentService.payMockSuccess`, `paymentService.getVNPAYUrl`.
- **Admin:** `adminService.createEvent`, `adminService.updateEvent`, `adminService.deleteEvent`, `adminService.setEventStatus`, `adminService.createTicketType`, `adminService.updateTicketType`, `adminService.deleteTicketType`, `adminService.getOrders`.

All requests pass through an Axios instance that attaches `Authorization: Bearer <token>` and handles the atomic token refresh logic.

## UI / Platform Impact

- **Visual Theme:** Modern design using deep Indigo (`#1e1b4b` / `#4338ca`) for primary accents, Emerald (`#059669`) for positive success states, and Amber (`#d97706`) for interactive CTA/ticket highlights.
- **Responsive Grid:** Flexbox and CSS Grid layout designed to scale gracefully from mobile viewports (375px+) to ultra-wide displays.
- **Micro-Interactions:** Hover-lift effects on event cards, pulse animations on loading states, smooth slide-in/fade-in transitions, and real-time input validations.

## Observability

- Precise console warnings and notifications on API errors.
- Visual status indicators (badges) for Orders and Payments so users and Admins always know the system state.

## Alternatives Considered

- **Tailwind CSS integration:** Installing Tailwind was considered, but we decided to stick with standard CSS custom properties in `src/index.css` and explicit styling classes to ensure zero installation risk, complete styling control, and strict compliance with the established `CLAUDE.md` frontend specifications.
