/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Calendar, MapPin, Share2, Heart, ShieldCheck, Ticket, Info, Map as MapIcon, CheckCircle2, ChevronRight, Loader2, Sparkles } from 'lucide-react';
import { eventService } from '../services/api';

function formatPrice(price) {
  if (price === undefined || price === null) return 'Liên hệ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
}

function formatDate(dateStr) {
  if (!dateStr) return 'Đang cập nhật';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return dateStr;
  }
}

export default function TicketDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedTicketType, setSelectedTicketType] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    const fetchEventDetail = async () => {
      try {
        const data = await eventService.getEventById(id);
        setEvent(data);
        // Automatically select first active ticket type as default
        const activeTypes = data?.ticketTypes ? data.ticketTypes.filter(t => t.active) : [];
        if (activeTypes.length > 0) {
          setSelectedTicketType(activeTypes[0]);
        }
      } catch (error) {
        console.error('Failed to fetch event detail:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchEventDetail();
    window.scrollTo(0, 0);
  }, [id]);

  if (loading) {
    return (
      <div className="loading-container" style={{ height: '80vh' }}>
        <Loader2 className="spinner" size={48} />
        <p>Đang tải thông tin chi tiết sự kiện...</p>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="loading-container" style={{ height: '80vh' }}>
        <h2>Không tìm thấy sự kiện</h2>
        <p>Sự kiện này không tồn tại hoặc đã bị gỡ bỏ.</p>
        <Link to="/tickets" className="btn btn-primary" style={{ marginTop: 'var(--space-4)' }}>Quay lại danh sách</Link>
      </div>
    );
  }

  const activeTicketTypes = event.ticketTypes ? event.ticketTypes.filter(t => t.active) : [];

  const handleBookingClick = () => {
    if (!selectedTicketType) {
      alert('Vui lòng chọn hạng vé trước khi đặt.');
      return;
    }

    if (quantity > selectedTicketType.stockAvailable) {
      alert(`Rất tiếc, chỉ còn ${selectedTicketType.stockAvailable} vé cho hạng vé này.`);
      return;
    }

    // Navigate to Cart passing selected event, ticket type and quantity
    navigate('/cart', {
      state: {
        event: {
          id: event.id,
          title: event.title,
          venue: event.venue,
          startAt: event.startAt || event.startTime,
          image: event.image
        },
        ticketType: selectedTicketType,
        quantity: quantity
      }
    });
  };

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    alert('Đã sao chép liên kết sự kiện vào khay nhớ tạm!');
  };

  return (
    <div className="ticket-detail-page" style={{ background: '#f8fafc', minHeight: '100vh' }}>
      {/* Breadcrumbs */}
      <div style={{ background: 'white', borderBottom: '1px solid #f1f5f9', padding: '12px 0' }}>
        <div className="container" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: '#94a3b8' }}>
          <Link to="/" style={{ color: '#94a3b8', textDecoration: 'none' }}>Trang chủ</Link>
          <ChevronRight size={12} />
          <Link to="/tickets" style={{ color: '#94a3b8', textDecoration: 'none' }}>Sự kiện</Link>
          <ChevronRight size={12} />
          <span style={{ color: 'var(--color-primary)', fontWeight: 600 }}>{event.title}</span>
        </div>
      </div>

      {/* Banner/Hero Section */}
      <div style={{ position: 'relative', height: '320px', background: '#0f172a' }}>
        <img 
          src={event.image || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=1200&h=600&fit=crop'} 
          alt={event.title} 
          style={{ width: '100%', height: '100%', objectFit: 'cover', opacity: 0.4 }} 
        />
        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(to top, #f8fafc 0%, transparent 100%)' }} />
        
        <div className="container" style={{ position: 'relative', height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <div style={{ maxWidth: '800px', transform: 'translateY(-10px)' }}>
            <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
              <span className="ticket-badge new" style={{ padding: '4px 12px' }}>HOT EVENTS</span>
              <span style={{ background: 'rgba(255,255,255,0.2)', padding: '4px 12px', borderRadius: '4px', color: 'white', fontSize: '11px', fontWeight: 700, backdropFilter: 'blur(4px)', textTransform: 'uppercase' }}>CHÍNH HÃNG</span>
            </div>
            <h1 style={{ fontSize: '36px', fontWeight: 900, color: 'white', marginBottom: '16px', lineHeight: '1.2', textShadow: '0 2px 10px rgba(0,0,0,0.5)' }}>{event.title}</h1>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', color: 'white' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px' }}>
                <Calendar size={16} style={{ color: 'var(--color-accent)' }} /> {formatDate(event.startAt || event.startTime)}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px' }}>
                <MapPin size={16} style={{ color: 'var(--color-accent)' }} /> {event.venue || 'Địa điểm đang cập nhật'}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{ marginTop: '-40px', position: 'relative', zIndex: 10, paddingBottom: '60px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '24px', alignItems: 'start' }}>
          
          {/* Left Content Column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Event Description */}
            <section style={{ background: 'white', padding: '32px', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
                <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: 'var(--color-accent-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-accent-hover)' }}>
                  <Info size={18} />
                </div>
                <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Giới thiệu sự kiện</h2>
              </div>
              <p style={{ color: '#475569', lineHeight: '1.7', fontSize: '15px', whiteSpace: 'pre-line' }}>
                {event.description || 'Chưa có mô tả chi tiết cho sự kiện này.'}
              </p>
            </section>

            {/* Event Seat Map Mockup */}
            <section style={{ background: 'white', padding: '32px', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
                <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: 'var(--color-accent-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-accent-hover)' }}>
                  <MapIcon size={18} />
                </div>
                <h2 style={{ fontSize: '20px', fontWeight: 800 }}>Sơ đồ chỗ ngồi</h2>
              </div>
              <div style={{ width: '100%', height: '300px', background: '#f8fafc', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed #cbd5e1' }}>
                <div style={{ textAlign: 'center' }}>
                  <MapIcon size={40} style={{ marginBottom: '12px', color: '#94a3b8' }} />
                  <p style={{ color: '#64748b', fontSize: '14px', fontWeight: 600 }}>Sơ đồ khán đài đang được cập nhật</p>
                  <p style={{ color: '#94a3b8', fontSize: '12px', marginTop: '4px' }}>Hạng vé chọn tương ứng với khu vực ghế ngồi tương ứng</p>
                </div>
              </div>
            </section>

            {/* Buying Benefits */}
            <section style={{ background: 'white', padding: '32px', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Sparkles size={18} style={{ color: 'var(--color-accent)' }} /> An tâm mua vé cùng TicketPro
              </h2>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px' }}>
                {[
                  { title: 'Chính hãng 100%', desc: 'Hợp tác trực tiếp BTC', icon: <CheckCircle2 size={20} style={{ color: 'var(--color-success)' }} /> },
                  { title: 'Bảo mật tuyệt đối', desc: 'Thanh toán an toàn', icon: <ShieldCheck size={20} style={{ color: 'var(--color-primary)' }} /> },
                  { title: 'Hỗ trợ khách hàng', desc: 'CSKH 24/7 nhiệt tình', icon: <Info size={20} style={{ color: 'var(--color-accent)' }} /> },
                  { title: 'Nhận vé tức thì', desc: 'Vé điện tử gửi qua Email', icon: <Ticket size={20} style={{ color: 'var(--color-hot)' }} /> }
                ].map((item, idx) => (
                  <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', padding: '16px 12px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center' }}>
                    <div>{item.icon}</div>
                    <h4 style={{ fontWeight: 800, fontSize: '12px', color: 'var(--color-primary)' }}>{item.title}</h4>
                    <p style={{ fontSize: '10px', color: '#94a3b8' }}>{item.desc}</p>
                  </div>
                ))}
              </div>
            </section>
          </div>

          {/* Sticky Booking Sidebar */}
          <aside style={{ position: 'sticky', top: '96px' }}>
            <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 40px rgba(15, 23, 42, 0.08)', overflow: 'hidden', border: '1px solid #f1f5f9' }}>
              <div style={{ background: 'var(--color-primary)', padding: '24px', color: 'white' }}>
                <div style={{ fontSize: '12px', opacity: 0.8, marginBottom: '4px', fontWeight: 600, textTransform: 'uppercase' }}>Giá vé đã chọn</div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
                  <span style={{ fontSize: '28px', fontWeight: 900 }}>
                    {selectedTicketType ? formatPrice(selectedTicketType.price) : 'Liên hệ'}
                  </span>
                </div>
              </div>

              <div style={{ padding: '24px' }}>
                {/* Dynamic Ticket Types Selector */}
                <div style={{ marginBottom: '20px' }}>
                  <label style={{ fontSize: '11px', fontWeight: 800, color: '#94a3b8', letterSpacing: '0.5px', marginBottom: '12px', display: 'block' }}>CHỌN HẠNG VÉ</label>
                  
                  {activeTicketTypes.length === 0 ? (
                    <p style={{ fontSize: '13px', color: 'var(--color-danger)', fontWeight: 600 }}>Tất cả các hạng vé đã hết hoặc tạm khóa.</p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                      {activeTicketTypes.map(t => (
                        <button 
                          key={t.id}
                          onClick={() => {
                            setSelectedTicketType(t);
                            setQuantity(1); // reset qty
                          }}
                          style={{ 
                            padding: '14px 18px', 
                            borderRadius: '12px', 
                            border: '1.5px solid',
                            borderColor: selectedTicketType?.id === t.id ? 'var(--color-primary)' : 'var(--color-border)',
                            background: selectedTicketType?.id === t.id ? 'var(--color-accent-light)' : 'white',
                            textAlign: 'left',
                            transition: 'all 0.2s',
                            cursor: 'pointer',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            width: '100%'
                          }}
                        >
                          <div>
                            <div style={{ fontWeight: 800, fontSize: '14px', color: selectedTicketType?.id === t.id ? 'var(--color-primary-dark)' : 'var(--color-text)' }}>
                              {t.name}
                            </div>
                            <div style={{ fontSize: '11px', color: t.stockAvailable < 10 ? 'var(--color-danger)' : 'var(--color-text-secondary)', marginTop: '2px', fontWeight: 500 }}>
                              {t.stockAvailable <= 0 ? 'Hết vé' : `Còn lại: ${t.stockAvailable.toLocaleString()} vé`}
                            </div>
                          </div>
                          <div style={{ textAlign: 'right' }}>
                            <div style={{ fontWeight: 800, fontSize: '14px', color: 'var(--color-primary)' }}>
                              {formatPrice(t.price)}
                            </div>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {/* Quantity adjustment */}
                {selectedTicketType && selectedTicketType.stockAvailable > 0 && (
                  <div style={{ marginBottom: '24px' }}>
                    <label style={{ fontSize: '11px', fontWeight: 800, color: '#94a3b8', marginBottom: '12px', display: 'block' }}>SỐ LƯỢNG</label>
                    <div style={{ display: 'flex', alignItems: 'center', background: '#f8fafc', padding: '10px 16px', borderRadius: '12px', justifyContent: 'space-between', border: '1px solid var(--color-border-light)' }}>
                      <button 
                        onClick={() => setQuantity(Math.max(1, quantity - 1))} 
                        style={{ width: '32px', height: '32px', borderRadius: '8px', border: '1px solid #cbd5e1', background: 'white', fontWeight: 800, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      >
                        -
                      </button>
                      <span style={{ fontSize: '18px', fontWeight: 800 }}>{quantity}</span>
                      <button 
                        onClick={() => setQuantity(Math.min(selectedTicketType.stockAvailable, quantity + 1))} 
                        style={{ width: '32px', height: '32px', borderRadius: '8px', border: '1px solid #cbd5e1', background: 'white', fontWeight: 800, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      >
                        +
                      </button>
                    </div>
                  </div>
                )}

                {/* Total price section */}
                {selectedTicketType && (
                  <div style={{ borderTop: '2px dashed var(--color-border-light)', paddingTop: '20px', marginBottom: '24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '13px', color: '#64748b' }}>
                      <span>Tạm tính ({quantity} vé)</span>
                      <span>{formatPrice(selectedTicketType.price * quantity)}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '18px', fontWeight: 900 }}>
                      <span>Tổng cộng</span>
                      <span style={{ color: 'var(--color-primary)' }}>{formatPrice(selectedTicketType.price * quantity)}</span>
                    </div>
                  </div>
                )}

                <button
                  className="btn btn-accent"
                  style={{ width: '100%', padding: '16px', borderRadius: '12px', fontSize: '16px', fontWeight: 800, marginBottom: '16px' }}
                  onClick={handleBookingClick}
                  disabled={!selectedTicketType || selectedTicketType.stockAvailable <= 0}
                >
                  {selectedTicketType && selectedTicketType.stockAvailable <= 0 ? 'ĐÃ HẾT VÉ' : 'ĐẶT VÉ NGAY'}
                </button>
                
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button onClick={handleShare} style={{ flex: 1, padding: '10px', borderRadius: '10px', border: '1px solid var(--color-border)', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '12px', fontWeight: 600, color: 'var(--color-text-secondary)', cursor: 'pointer' }}>
                    <Share2 size={14} /> Chia sẻ
                  </button>
                  <button onClick={() => setIsFavorite(!isFavorite)} style={{ flex: 1, padding: '10px', borderRadius: '10px', border: '1px solid var(--color-border)', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', fontSize: '12px', fontWeight: 600, color: isFavorite ? 'var(--color-danger)' : 'var(--color-text-secondary)', cursor: 'pointer' }}>
                    <Heart size={14} fill={isFavorite ? 'var(--color-danger)' : 'none'} style={{ transition: 'all 0.2s' }} /> {isFavorite ? 'Đã lưu' : 'Lưu lại'}
                  </button>
                </div>
              </div>
            </div>
          </aside>

        </div>
      </div>
    </div>
  );
}
