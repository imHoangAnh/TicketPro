/* eslint-disable react-hooks/set-state-in-effect, no-unused-vars */
import React, { useState, useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { authService, orderService, paymentService } from '../services/api';
import { Phone, LogOut, Shield, ShoppingBag, User as UserIcon, ChevronDown, Calendar, CreditCard, X, Loader2 } from 'lucide-react';

export default function Header() {
  const [user, setUser] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [ordersModalOpen, setOrdersModalOpen] = useState(false);
  const [myOrders, setMyOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [cancellingOrderId, setCancellingOrderId] = useState(null);

  const dropdownRef = useRef(null);
  const location = useLocation();
  const navigate = useNavigate();

  const updateAuth = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (err) {
        setUser(null);
      }
    } else {
      setUser(null);
    }
  };

  useEffect(() => {
    updateAuth();
    window.addEventListener('auth-status-change', updateAuth);
    window.addEventListener('auth-logout', updateAuth);

    // Close dropdown on click outside
    const handleOutsideClick = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);

    return () => {
      window.removeEventListener('auth-status-change', updateAuth);
      window.removeEventListener('auth-logout', updateAuth);
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, []);

  const handleLogout = async () => {
    try {
      await authService.logout();
      setDropdownOpen(false);
      navigate('/');
    } catch (err) {
      console.error('Logout error:', err);
    }
  };

  const loadMyOrders = async () => {
    setLoadingOrders(true);
    try {
      const orders = await orderService.getMyOrders();
      // Sort orders descending by ID or date so newest are on top
      const sorted = orders.sort((a, b) => b.id - a.id);
      setMyOrders(sorted);
    } catch (err) {
      console.error('Error fetching user orders:', err);
    } finally {
      setLoadingOrders(false);
    }
  };

  const openOrdersModal = () => {
    setDropdownOpen(false);
    setOrdersModalOpen(true);
    loadMyOrders();
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này? Thao tác này không thể hoàn tác.')) {
      return;
    }
    setCancellingOrderId(orderId);
    try {
      await orderService.cancelOrder(orderId);
      // Reload orders
      await loadMyOrders();
    } catch (err) {
      console.error('Error cancelling order:', err);
      alert('Không thể hủy đơn hàng. Vui lòng thử lại sau.');
    } finally {
      setCancellingOrderId(null);
    }
  };

  const handlePayPending = async (order) => {
    try {
      // Show loader or options
      const confirmPay = window.confirm(`Bạn muốn thanh toán đơn hàng #${order.orderNumber} bằng VNPAY Sandbox? Click Cancel để dùng thanh toán Thử Nghiệm Nhanh.`);
      if (confirmPay) {
        const result = await paymentService.getVNPAYUrl(order.id);
        if (result && result.paymentUrl) {
          window.location.href = result.paymentUrl;
        }
      } else {
        await paymentService.payMockSuccess(order.id);
        alert('Thanh toán thử nghiệm thành công!');
        loadMyOrders();
      }
    } catch (err) {
      console.error('Payment error:', err);
      alert('Thanh toán thất bại. Vui lòng thử lại sau.');
    }
  };

  const formatPrice = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PAID':
        return <span className="badge badge-success">Đã thanh toán</span>;
      case 'CANCELLED':
        return <span className="badge badge-danger">Đã hủy</span>;
      case 'PENDING':
        return <span className="badge badge-warning">Chờ thanh toán</span>;
      default:
        return <span className="badge badge-info">{status}</span>;
    }
  };

  return (
    <>
      <header className="header" id="header">
        <div className="container header-inner">
          <Link to="/" className="logo">
            <div className="logo-icon">🎫</div>
            <div className="logo-text">
              <span className="logo-name">TICKET PRO</span>
              <span className="logo-tagline">Đặt Vé · Giá Trị Thực</span>
            </div>
          </Link>

          <nav>
            <ul className="nav-list">
              <li>
                <Link to="/" className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}>
                  Trang Chủ
                </Link>
              </li>
              <li>
                <Link to="/tickets" className={`nav-link ${location.pathname === '/tickets' ? 'active' : ''}`}>
                  Sự Kiện & Vé
                </Link>
              </li>
            </ul>
          </nav>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
            <a href="tel:0901234567" className="header-hotline" id="hotline-btn">
              <Phone size={14} />
              <span className="phone-text">0901 234 567</span>
            </a>

            {user ? (
              <div className="profile-menu" ref={dropdownRef}>
                <div className="profile-trigger" onClick={() => setDropdownOpen(!dropdownOpen)}>
                  <div className="profile-avatar">
                    {user.fullName ? user.fullName.charAt(0).toUpperCase() : <UserIcon size={14} />}
                  </div>
                  <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--color-text)' }}>
                    {user.fullName || user.email}
                  </span>
                  <ChevronDown size={14} style={{ color: 'var(--color-text-secondary)' }} />
                </div>

                {dropdownOpen && (
                  <div className="profile-dropdown">
                    <div className="profile-dropdown-header">
                      <span>Đăng nhập với tư cách:</span>
                      <div className="profile-dropdown-name">{user.fullName || user.email}</div>
                      <div style={{ display: 'flex', gap: '4px', marginTop: '4px' }}>
                        {user.roles && user.roles.map((role, idx) => (
                          <span key={idx} className={`badge ${role === 'ADMIN' ? 'badge-danger' : 'badge-info'}`} style={{ fontSize: '9px', padding: '2px 6px' }}>
                            {role}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div className="profile-dropdown-item" onClick={openOrdersModal}>
                      <ShoppingBag size={16} />
                      Đơn hàng của tôi
                    </div>

                    {user.roles && user.roles.includes('ADMIN') && (
                      <Link to="/system/manager" className="profile-dropdown-item" onClick={() => setDropdownOpen(false)}>
                        <Shield size={16} style={{ color: 'var(--color-danger)' }} />
                        Trang quản trị (Admin)
                      </Link>
                    )}

                    <div className="profile-dropdown-item danger" onClick={handleLogout}>
                      <LogOut size={16} />
                      Đăng xuất
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <Link to="/login" className="btn btn-outline" style={{ padding: '8px 20px', borderRadius: 'var(--radius-full)' }}>
                Đăng Nhập
              </Link>
            )}
          </div>
        </div>
      </header>

      {/* Orders History Modal Overlay */}
      {ordersModalOpen && (
        <div className="modal-overlay" onClick={() => setOrdersModalOpen(false)}>
          <div className="modal-container" style={{ maxWidth: '750px' }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Đơn hàng của tôi</h3>
              <X size={20} className="modal-close" onClick={() => setOrdersModalOpen(false)} />
            </div>

            <div className="modal-body" style={{ background: 'var(--color-bg)' }}>
              {loadingOrders ? (
                <div className="loading-container">
                  <Loader2 size={36} className="spinner" />
                  <p>Đang tải lịch sử mua vé...</p>
                </div>
              ) : myOrders.length === 0 ? (
                <div style={{ textAlign: 'center', padding: 'var(--space-12) 0', color: 'var(--color-text-secondary)' }}>
                  <ShoppingBag size={48} style={{ margin: '0 auto var(--space-4)', opacity: 0.3 }} />
                  <p>Bạn chưa đặt vé nào cả.</p>
                  <Link to="/tickets" onClick={() => setOrdersModalOpen(false)} className="btn btn-primary" style={{ marginTop: 'var(--space-4)' }}>
                    Mua vé ngay
                  </Link>
                </div>
              ) : (
                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '4px' }}>
                  {myOrders.map((order) => (
                    <div key={order.id} className="order-card" style={{ background: 'var(--color-bg-white)' }}>
                      <div className="order-card-header">
                        <div>
                          <span style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-xs)' }}>Mã đơn hàng:</span>
                          <div className="order-id">#{order.orderNumber}</div>
                        </div>
                        <div>
                          {getStatusBadge(order.status)}
                        </div>
                      </div>

                      <div className="order-card-body">
                        <div className="order-item-info">
                          <h4>{order.ticketTypeName || `Vé loại #${order.ticketTypeId}`}</h4>
                          <div className="order-qty-price">
                            Đơn giá: {formatPrice(order.unitPrice || (order.totalPrice / order.quantity))} | Số lượng: <strong>{order.quantity}</strong>
                          </div>
                        </div>

                        <div className="order-total-block">
                          <div className="order-total-amount">
                            {formatPrice(order.totalPrice)}
                          </div>
                          
                          {order.status === 'PENDING' && (
                            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleCancelOrder(order.id)}
                                disabled={cancellingOrderId === order.id}
                              >
                                {cancellingOrderId === order.id ? 'Đang hủy...' : 'Hủy đơn'}
                              </button>
                              <button
                                className="btn btn-sm btn-accent"
                                onClick={() => handlePayPending(order)}
                              >
                                Thanh toán
                              </button>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-outline btn-sm" onClick={() => setOrdersModalOpen(false)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
