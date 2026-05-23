/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, SlidersHorizontal, ArrowLeft, Calendar, MapPin, ArrowRight, ChevronDown, Loader2 } from 'lucide-react';
import { eventService } from '../services/api';

function formatPrice(price) {
  if (price === undefined || price === null) return 'Liên hệ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
}

function formatDate(dateStr) {
  if (!dateStr) return 'Đang cập nhật';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
}

export default function TicketsPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedVenue, setSelectedCategory] = useState('All');
  const [sortBy, setSortBy] = useState('Mới nhất');
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await eventService.getEvents();
        setEvents(data || []);
      } catch (error) {
        console.error('Failed to fetch events:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  // Filter distinct venues to use in filter sidebar
  const venues = ['All', ...new Set(events.map(e => e.venue).filter(Boolean))];

  const filteredEvents = events.filter(e => {
    const matchesSearch = e.title?.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          e.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesVenue = selectedVenue === 'All' || e.venue === selectedVenue;
    return matchesSearch && matchesVenue;
  });

  // Sort logic
  const sortedEvents = [...filteredEvents].sort((a, b) => {
    const getMinPrice = (evt) => {
      const activeTypes = evt.ticketTypes ? evt.ticketTypes.filter(t => t.active) : [];
      return activeTypes.length > 0 ? Math.min(...activeTypes.map(t => t.price)) : Infinity;
    };

    if (sortBy === 'Giá tăng dần') {
      return getMinPrice(a) - getMinPrice(b);
    } else if (sortBy === 'Giá giảm dần') {
      return getMinPrice(b) - getMinPrice(a);
    } else {
      // Default: newest first (newest ID)
      return b.id - a.id;
    }
  });

  return (
    <div className="tickets-page" style={{ background: '#f8fafc', paddingBottom: '80px' }}>
      {/* Premium Header Banner */}
      <div style={{ background: 'var(--color-primary)', padding: '40px 0 100px', color: 'white' }}>
        <div className="container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '40px' }}>
            <div>
              <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'rgba(255,255,255,0.7)', fontSize: '14px', marginBottom: '16px', textDecoration: 'none' }} className="hover-white">
                <ArrowLeft size={16} /> Quay lại trang chủ
              </Link>
              <h1 style={{ fontSize: '40px', fontWeight: 900, letterSpacing: '-1px' }}>Khám phá sự kiện</h1>
            </div>
            <div style={{ background: 'rgba(255,255,255,0.1)', padding: '12px 20px', borderRadius: '12px', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.1)' }}>
              <div style={{ fontSize: '12px', opacity: 0.7, marginBottom: '4px' }}>Tổng sự kiện hoạt động</div>
              <div style={{ fontSize: '24px', fontWeight: 800 }}>{events.length}</div>
            </div>
          </div>
          
          <div style={{ position: 'relative', maxWidth: '700px' }}>
            <input 
              type="text" 
              placeholder="Tìm kiếm concert, sự kiện âm nhạc, thể thao..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ 
                width: '100%', 
                padding: '18px 24px 18px 56px', 
                borderRadius: '16px', 
                border: 'none',
                fontSize: '16px',
                boxShadow: '0 10px 25px -5px rgba(0,0,0,0.3)',
                outline: 'none'
              }}
            />
            <Search size={22} style={{ position: 'absolute', left: '20px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
          </div>
        </div>
      </div>

      <div className="container" style={{ marginTop: '-40px' }}>
        <div className="tickets-page-layout" style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '32px' }}>
          
          {/* Sidebar Filters */}
          <aside>
            <div style={{ 
              background: 'white', 
              padding: '32px', 
              borderRadius: '20px', 
              boxShadow: '0 4px 20px -2px rgba(0,0,0,0.05)',
              border: '1px solid #f1f5f9',
              position: 'sticky',
              top: '100px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '24px', color: 'var(--color-primary)', fontWeight: 800, fontSize: '18px' }}>
                <Filter size={20} /> BỘ LỌC
              </div>

              <div style={{ marginBottom: '24px' }}>
                <label style={{ fontSize: '11px', fontWeight: 800, color: '#94a3b8', letterSpacing: '1.5px', marginBottom: '16px', display: 'block', textTransform: 'uppercase' }}>ĐỊA ĐIỂM</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {venues.map(v => (
                    <button 
                      key={v}
                      onClick={() => setSelectedCategory(v)}
                      style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '12px 16px', 
                        borderRadius: '12px',
                        fontSize: '14px',
                        fontWeight: 700,
                        textAlign: 'left',
                        background: selectedVenue === v ? 'var(--color-accent-light)' : 'transparent',
                        color: selectedVenue === v ? 'var(--color-accent-hover)' : '#475569',
                        transition: 'all 0.2s',
                        border: 'none',
                        cursor: 'pointer',
                        width: '100%'
                      }}
                    >
                      {v === 'All' ? 'Tất cả địa điểm' : v}
                      {selectedVenue === v && <ChevronDown size={14} style={{ transform: 'rotate(-90deg)' }} />}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          {/* Main Content Area */}
          <main>
            {/* Sorting & Stats Bar */}
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center', 
              background: 'white', 
              padding: '16px 24px', 
              borderRadius: '16px', 
              boxShadow: '0 2px 10px rgba(0,0,0,0.02)',
              marginBottom: '24px',
              border: '1px solid #f1f5f9'
            }}>
              <div style={{ fontSize: '15px', color: '#64748b' }}>
                Hiển thị <strong>{sortedEvents.length}</strong> sự kiện phù hợp
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '14px', color: '#475569' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', opacity: 0.8 }}>
                  <SlidersHorizontal size={16} /> Sắp xếp:
                </div>
                <div style={{ position: 'relative' }}>
                  <select 
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    style={{ 
                      appearance: 'none',
                      background: '#f8fafc',
                      border: '1px solid #e2e8f0',
                      padding: '8px 36px 8px 16px',
                      borderRadius: '8px',
                      fontWeight: 700,
                      color: 'var(--color-primary)',
                      cursor: 'pointer',
                      fontSize: '14px',
                      outline: 'none'
                    }}
                  >
                    <option>Mới nhất</option>
                    <option>Giá tăng dần</option>
                    <option>Giá giảm dần</option>
                  </select>
                  <ChevronDown size={14} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none', color: 'var(--color-primary)' }} />
                </div>
              </div>
            </div>

            {loading ? (
              <div className="loading-container">
                <Loader2 className="spinner" size={40} />
                <p>Đang tải sự kiện...</p>
              </div>
            ) : sortedEvents.length > 0 ? (
              <div className="ticket-grid" style={{ gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px' }}>
                {sortedEvents.map(event => {
                  const activeTypes = event.ticketTypes ? event.ticketTypes.filter(t => t.active) : [];
                  const minPrice = activeTypes.length > 0 ? Math.min(...activeTypes.map(t => t.price)) : null;
                  
                  return (
                    <div className="ticket-card" key={event.id} style={{ border: 'none', boxShadow: '0 4px 15px rgba(0,0,0,0.05)', borderRadius: '16px', overflow: 'hidden', display: 'flex', flexDirection: 'column', height: '100%' }}>
                      <span className="ticket-badge new" style={{ padding: '4px 10px', fontSize: '11px' }}>HOT</span>
                      
                      <div className="ticket-image-wrapper" style={{ height: '220px', flexShrink: 0 }}>
                        <img 
                          src={event.image || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=400&h=300&fit=crop'} 
                          alt={event.title} 
                          className="ticket-image" 
                          style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
                        />
                      </div>

                      <div className="ticket-info" style={{ padding: '24px', flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <h3 className="ticket-name" style={{ fontSize: '16px', fontWeight: 800, height: '48px', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', marginBottom: '16px', lineHeight: '1.5' }}>
                          {event.title}
                        </h3>
                        <div className="ticket-meta" style={{ marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '6px', minHeight: '20px' }}>
                          <span className="ticket-meta-item" style={{ fontSize: '13px' }}>
                            <Calendar size={14} style={{ color: 'var(--color-primary-light)' }} /> {formatDate(event.startAt || event.startTime)}
                          </span>
                          <span className="ticket-meta-item" style={{ fontSize: '13px' }}>
                            <MapPin size={14} style={{ color: 'var(--color-primary-light)' }} /> {event.venue || 'Địa điểm đang cập nhật'}
                          </span>
                        </div>

                        <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '20px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', height: '36px', lineHeight: '1.4' }}>
                          {event.description || 'Chưa có mô tả chi tiết cho sự kiện này.'}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: 'auto', borderTop: '1px solid var(--color-border-light)', paddingTop: '16px' }}>
                          <div>
                            <div style={{ fontSize: '11px', color: 'var(--color-text-light)', fontWeight: 600, textTransform: 'uppercase' }}>Giá chỉ từ</div>
                            <div className="ticket-price-only" style={{ fontSize: '20px', fontWeight: 900, color: 'var(--color-primary)' }}>
                              {minPrice !== null ? formatPrice(minPrice) : 'Liên hệ'}
                            </div>
                          </div>
                          
                          <Link to={`/ticket/${event.id}`} className="ticket-cta" style={{ padding: '10px 16px', borderRadius: 'var(--radius-md)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            MUA VÉ <ArrowRight size={14} />
                          </Link>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '100px 0', background: 'white', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
                <div style={{ width: '80px', height: '80px', background: '#f1f5f9', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
                  <Search size={32} style={{ color: '#94a3b8' }} />
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-primary)', marginBottom: '8px' }}>Không tìm thấy sự kiện nào</h3>
                <p style={{ color: '#64748b', fontSize: '14px' }}>Anh vui lòng điều chỉnh bộ lọc hoặc từ khóa tìm kiếm nhé!</p>
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
