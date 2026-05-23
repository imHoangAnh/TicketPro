import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { Ticket, ShieldCheck, ChevronRight, Loader2, CreditCard, Sparkles, AlertCircle, ShoppingBag, MapPin, Calendar } from 'lucide-react';
import { orderService, paymentService } from '../services/api';

function formatPrice(price) {
  if (price === undefined || price === null) return '0đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
}

function formatDate(dateStr) {
  if (!dateStr) return 'Đang cập nhật';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return dateStr;
  }
}

export default function CartPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [createdOrder, setCreatedOrder] = useState(null);

  if (!state?.ticketType || !state?.event) {
    return (
      <div className="loading-container" style={{ height: '60vh' }}>
        <ShoppingBag size={48} style={{ opacity: 0.3, marginBottom: 'var(--space-4)' }} />
        <p style={{ color: 'var(--color-text-secondary)' }}>Không có vé nào trong giỏ hàng.</p>
        <Link to="/tickets" className="btn btn-primary" style={{ marginTop: 'var(--space-4)' }}>Khám phá sự kiện</Link>
      </div>
    );
  }

  const { event, ticketType, quantity } = state;
  const priceToUse = ticketType.price || 0;
  const total = priceToUse * quantity;

  const handleReserveOrder = async () => {
    setLoading(true);
    setError(null);
    try {
      // POST /api/orders
      const order = await orderService.createOrder({ 
        ticketTypeId: ticketType.id, 
        quantity 
      });
      setCreatedOrder(order);
    } catch (err) {
      console.error('Order creation error:', err);
      const msg = err.response?.data?.message || 'Giữ chỗ vé thất bại. Có thể vé đã hết hoặc vượt quá số lượng tối đa.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleMockPayment = async () => {
    if (!createdOrder) return;
    setLoading(true);
    setError(null);
    try {
      await paymentService.payMockSuccess(createdOrder.id);
      // Success, route to booking success
      navigate('/booking-success', { 
        state: { 
          bookingCode: createdOrder.orderNumber, 
          event, 
          ticketType, 
          quantity,
          amount: total,
          paid: true
        } 
      });
    } catch (err) {
      console.error('Mock payment error:', err);
      setError('Thanh toán thử nghiệm thất bại. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const handleVNPAYPayment = async () => {
    if (!createdOrder) return;
    setLoading(true);
    setError(null);
    try {
      const result = await paymentService.getVNPAYUrl(createdOrder.id);
      if (result && result.paymentUrl) {
        // Redirect to external VNPAY sandbox
        window.location.href = result.paymentUrl;
      } else {
        setError('Không thể khởi tạo cổng thanh toán VNPAY.');
      }
    } catch (err) {
      console.error('VNPAY integration error:', err);
      setError('Gặp lỗi khi kết nối với cổng thanh toán VNPAY.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', paddingBottom: '80px' }}>
      {/* Breadcrumbs */}
      <div style={{ background: 'white', borderBottom: '1px solid #f1f5f9', padding: '12px 0' }}>
        <div className="container" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: '#94a3b8' }}>
          <Link to="/" style={{ color: '#94a3b8', textDecoration: 'none' }}>Trang chủ</Link>
          <ChevronRight size={12} />
          <Link to="/tickets" style={{ color: '#94a3b8', textDecoration: 'none' }}>Sự kiện</Link>
          <ChevronRight size={12} />
          <span style={{ color: 'var(--color-primary)', fontWeight: 600 }}>Thanh toán và Đặt chỗ</span>
        </div>
      </div>

      <div className="container" style={{ maxWidth: '720px', paddingTop: '40px' }}>
        {/* Progress Stepper bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', padding: '0 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '28px', height: '28px', borderRadius: '50%', background: 'var(--color-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', fontSize: '12px' }}>1</span>
            <span style={{ fontWeight: '700', color: 'var(--color-primary-dark)', fontSize: '14px' }}>Giữ chỗ vé</span>
          </div>
          <div style={{ flexGrow: 1, height: '2px', background: createdOrder ? 'var(--color-primary)' : '#e2e8f0', margin: '0 16px' }} />
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '28px', height: '28px', borderRadius: '50%', background: createdOrder ? 'var(--color-primary)' : '#cbd5e1', color: createdOrder ? 'white' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', fontSize: '12px' }}>2</span>
            <span style={{ fontWeight: '700', color: createdOrder ? 'var(--color-primary-dark)' : '#94a3b8', fontSize: '14px' }}>Thanh toán</span>
          </div>
        </div>

        <h1 style={{ fontSize: '28px', fontWeight: 900, marginBottom: '24px', color: 'var(--color-primary-dark)' }}>
          {createdOrder ? 'Chọn phương thức thanh toán' : 'Xác nhận giữ chỗ đơn hàng'}
        </h1>

        {/* Error notification */}
        {error && (
          <div className="toast error" style={{ position: 'relative', top: 0, right: 0, minWidth: '100%', marginBottom: '24px', pointerEvents: 'auto' }}>
            <AlertCircle size={20} />
            <span className="toast-message">{error}</span>
          </div>
        )}

        {/* Order Details Card */}
        <div style={{ background: 'white', borderRadius: '20px', border: '1px solid #f1f5f9', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', overflow: 'hidden', marginBottom: '24px' }}>
          <div style={{ display: 'flex', gap: '20px', padding: '24px', background: 'linear-gradient(135deg, rgba(26,54,93,0.02) 0%, rgba(26,54,93,0) 100%)' }}>
            <img
              src={event.image || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=200&h=120&fit=crop'}
              alt={event.title}
              style={{ width: '120px', height: '80px', objectFit: 'cover', borderRadius: '12px', flexShrink: 0, border: '1px solid var(--color-border-light)' }}
            />
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              <div style={{ fontWeight: 800, fontSize: '16px', color: 'var(--color-primary-dark)', marginBottom: '6px' }}>{event.title}</div>
              <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '4px' }}>
                <MapPin size={13} /> {event.venue}
              </div>
              <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Calendar size={13} /> {formatDate(event.startAt)}
              </div>
            </div>
          </div>

          <div style={{ borderTop: '1px solid var(--color-border-light)', padding: '24px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              <span>Hạng vé chọn</span>
              <strong style={{ color: 'var(--color-primary-dark)' }}>{ticketType.name}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              <span>Đơn giá vé</span>
              <strong>{formatPrice(priceToUse)}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              <span>Số lượng mua</span>
              <strong style={{ color: 'var(--color-primary-dark)' }}>{quantity} vé</strong>
            </div>
            
            {createdOrder && (
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
                <span>Mã giao dịch đặt chỗ</span>
                <code style={{ background: '#f1f5f9', padding: '2px 8px', borderRadius: '4px', fontWeight: '700', color: 'var(--color-primary)' }}>
                  {createdOrder.orderNumber}
                </code>
              </div>
            )}

            <div style={{ borderTop: '2px dashed var(--color-border-light)', paddingTop: '16px', display: 'flex', justifyContent: 'space-between', fontWeight: 900, fontSize: '18px' }}>
              <span>Tổng cộng thanh toán</span>
              <span style={{ color: 'var(--color-primary)', fontSize: '20px' }}>{formatPrice(total)}</span>
            </div>
          </div>
        </div>

        {/* Security / Help banner */}
        <div style={{ background: 'white', borderRadius: '16px', border: '1px solid #f1f5f9', padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
          <ShieldCheck size={20} style={{ color: 'var(--color-success)', flexShrink: 0 }} />
          <span>Hệ thống của chúng tôi được mã hóa an toàn 256-bit SSL. Vé điện tử và mã QR sẽ gửi tức thì tới email của bạn ngay sau khi thanh toán hoàn tất.</span>
        </div>

        {/* Action Buttons based on Step */}
        {!createdOrder ? (
          <button
            className="btn btn-primary"
            style={{ width: '100%', padding: '16px', borderRadius: '12px', fontSize: '16px', fontWeight: 800, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
            onClick={handleReserveOrder}
            disabled={loading}
          >
            {loading ? (
              <>
                <Loader2 size={18} className="spinner" style={{ animation: 'spin 1s linear infinite', borderTopColor: '#fff' }} />
                Đang giữ vé chỗ cho bạn...
              </>
            ) : (
              <>
                <Ticket size={18} />
                TIẾP TỤC ĐẶT VÉ & THANH TOÁN
              </>
            )}
          </button>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <button
              className="btn btn-outline"
              style={{ padding: '16px', borderRadius: '12px', fontSize: '15px', fontWeight: 800, borderColor: 'var(--color-primary)' }}
              onClick={handleMockPayment}
              disabled={loading}
            >
              {loading ? <Loader2 size={18} className="spinner" /> : <Sparkles size={18} />}
              THANH TOÁN THỬ NGHIỆM
            </button>
            <button
              className="btn btn-accent"
              style={{ padding: '16px', borderRadius: '12px', fontSize: '15px', fontWeight: 800 }}
              onClick={handleVNPAYPayment}
              disabled={loading}
            >
              {loading ? <Loader2 size={18} className="spinner" /> : <CreditCard size={18} />}
              VNPAY SANDBOX
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
