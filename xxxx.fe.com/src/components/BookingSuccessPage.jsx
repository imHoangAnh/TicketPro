import React, { useState, useEffect } from 'react';
import { useLocation, Link, useSearchParams } from 'react-router-dom';
import { CheckCircle2, XCircle, Ticket, Home, Loader2, Calendar, MapPin, AlertCircle, ShoppingBag } from 'lucide-react';
import { orderService } from '../services/api';

function formatPrice(price) {
  if (price === undefined || price === null) return '0đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
}

export default function BookingSuccessPage() {
  const { state } = useLocation();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [orderDetail, setOrderDetail] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  // Extract query parameters if redirected from VNPAY
  const querySuccess = searchParams.get('success');
  const queryOrderId = searchParams.get('orderId');
  const queryError = searchParams.get('error');

  useEffect(() => {
    const fetchOrder = async () => {
      if (queryOrderId) {
        setLoading(true);
        try {
          const order = await orderService.getOrderById(queryOrderId);
          setOrderDetail(order);
        } catch (err) {
          console.error('Error fetching order details:', err);
          setErrorMsg('Không thể tải thông tin đơn hàng này từ máy chủ.');
        } finally {
          setLoading(false);
        }
      }
    };
    fetchOrder();
  }, [queryOrderId]);

  // Handle Loading state
  if (loading) {
    return (
      <div className="loading-container" style={{ height: '80vh' }}>
        <Loader2 className="spinner" size={48} />
        <p>Đang kiểm tra và cập nhật trạng thái đơn hàng...</p>
      </div>
    );
  }

  // Case 1: Payment Failed redirection from VNPAY
  if (querySuccess === 'false') {
    return (
      <div style={{ background: '#f8fafc', minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 16px' }}>
        <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 60px rgba(0,0,0,0.08)', padding: '48px 40px', maxWidth: '520px', width: '100%', textAlign: 'center' }}>
          <div style={{ width: '72px', height: '72px', borderRadius: '50%', background: '#fee2e2', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
            <XCircle size={36} style={{ color: 'var(--color-danger)' }} />
          </div>

          <h1 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '12px', color: 'var(--color-primary-dark)' }}>Thanh toán thất bại</h1>
          <p style={{ color: '#64748b', fontSize: '14px', marginBottom: '24px', lineHeight: '1.6' }}>
            Rất tiếc! Giao dịch của bạn không thể hoàn tất hoặc đã bị hủy từ phía cổng thanh toán.
          </p>

          {queryError && (
            <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '12px', padding: '16px', marginBottom: '28px', textAlign: 'left' }}>
              <div style={{ fontSize: '11px', color: '#991b1b', fontWeight: 700, letterSpacing: '0.5px', marginBottom: '4px' }}>MÃ LỖI HỆ THỐNG</div>
              <div style={{ fontFamily: 'monospace', fontSize: '14px', fontWeight: '700', color: 'var(--color-danger)' }}>{queryError}</div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '12px' }}>
            <Link
              to="/tickets"
              style={{ flex: 1, padding: '14px', borderRadius: '12px', border: '1.5px solid var(--color-border)', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '14px', fontWeight: 700, color: '#475569', textDecoration: 'none' }}
            >
              <Ticket size={16} /> Xem sự kiện khác
            </Link>
            <Link
              to="/"
              style={{ flex: 1, padding: '14px', borderRadius: '12px', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '14px', fontWeight: 700, color: 'white', textDecoration: 'none' }}
            >
              <Home size={16} /> Về trang chủ
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Determine which success info to display
  let bookingCode = '';
  let title = '';
  let venue = '';
  let date = '';
  let amountText = '';

  if (state?.bookingCode) {
    // Loaded via direct State (Mock success)
    bookingCode = state.bookingCode;
    title = state.event?.title;
    venue = state.event?.venue;
    date = state.event?.startAt;
    amountText = formatPrice(state.amount);
  } else if (orderDetail) {
    // Loaded via redirected VNPAY callback parameters
    bookingCode = orderDetail.orderNumber;
    title = orderDetail.eventTitle || `Sự kiện #${orderDetail.eventId}`;
    venue = orderDetail.venue || 'Địa điểm tổ chức';
    date = orderDetail.startAt;
    amountText = formatPrice(orderDetail.totalPrice);
  } else {
    // fallback empty state
    return (
      <div className="loading-container" style={{ height: '60vh' }}>
        <AlertCircle size={48} style={{ color: 'var(--color-text-light)' }} />
        <p style={{ color: 'var(--color-text-secondary)' }}>{errorMsg || 'Không tìm thấy thông tin đặt vé.'}</p>
        <Link to="/" className="btn btn-primary" style={{ marginTop: 'var(--space-4)' }}>Về trang chủ</Link>
      </div>
    );
  }

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 16px' }}>
      <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 60px rgba(0,0,0,0.08)', padding: '48px 40px', maxWidth: '520px', width: '100%', textAlign: 'center' }}>
        <div style={{ width: '72px', height: '72px', borderRadius: '50%', background: '#dcfce7', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
          <CheckCircle2 size={36} style={{ color: 'var(--color-success)' }} />
        </div>

        <h1 style={{ fontSize: '26px', fontWeight: 900, marginBottom: '8px', color: 'var(--color-primary-dark)' }}>Đặt vé thành công!</h1>
        <p style={{ color: '#64748b', fontSize: '14.5px', marginBottom: '28px', lineHeight: '1.6' }}>
          Yêu cầu đặt vé của bạn đã được xác nhận và thanh toán thành công. Chi tiết vé đã được chuyển về email của bạn.
        </p>

        {/* Dashboard styled ticket ticket code */}
        <div style={{ background: 'radial-gradient(circle at 10% 20%, rgba(246, 173, 43, 0.05) 0%, rgba(248, 250, 252, 1) 90.1%)', border: '2.5px dashed #f6ad2b', borderRadius: '16px', padding: '24px', marginBottom: '28px', position: 'relative' }}>
          <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', fontWeight: 800, letterSpacing: '1px', marginBottom: '8px' }}>MÃ GIAO DỊCH ĐẶT VÉ</div>
          <div style={{ fontSize: '28px', fontWeight: 900, color: 'var(--color-primary)', letterSpacing: '2px', fontFamily: 'monospace' }}>{bookingCode}</div>
          <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '8px', fontWeight: '500' }}>
            Tổng thanh toán: <strong style={{ color: 'var(--color-primary)' }}>{amountText}</strong>
          </div>
        </div>

        {/* Ticket Details summary */}
        <div style={{ background: '#f8fafc', borderRadius: '16px', padding: '20px', marginBottom: '32px', textAlign: 'left', border: '1px solid var(--color-border-light)' }}>
          <div style={{ fontWeight: 800, fontSize: '16px', color: 'var(--color-primary-dark)', marginBottom: '8px' }}>{title}</div>
          <div style={{ fontSize: '13px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
            <MapPin size={13} /> {venue}
          </div>
          {date && (
            <div style={{ fontSize: '13px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Calendar size={13} /> {new Date(date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
            </div>
          )}
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <Link
            to="/tickets"
            style={{ flex: 1, padding: '14px', borderRadius: '12px', border: '1.5px solid var(--color-border)', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '14px', fontWeight: 700, color: '#475569', textDecoration: 'none' }}
          >
            <Ticket size={16} /> Mua thêm vé
          </Link>
          <Link
            to="/"
            style={{ flex: 1, padding: '14px', borderRadius: '12px', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '14px', fontWeight: 700, color: 'white', textDecoration: 'none' }}
          >
            <Home size={16} /> Về trang chủ
          </Link>
        </div>
      </div>
    </div>
  );
}
