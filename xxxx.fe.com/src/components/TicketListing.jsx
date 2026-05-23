import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, MapPin, Tag, ArrowRight, Loader2, Sparkles } from 'lucide-react';
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
  } catch {
    return dateStr;
  }
}

function EventCard({ event }) {
  // Find minimum price among ticket types to show "Giá từ ..."
  const activeTickets = event.ticketTypes ? event.ticketTypes.filter(t => t.active) : [];
  const minPrice = activeTickets.length > 0 
    ? Math.min(...activeTickets.map(t => t.price)) 
    : null;

  return (
    <div className="ticket-card" id={`event-${event.id}`} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <span className="ticket-badge new">Sự Kiện Hot</span>
      <div className="ticket-image-wrapper" style={{ height: '220px', flexShrink: 0 }}>
        <img 
          src={event.image || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=400&h=300&fit=crop'} 
          alt={event.title} 
          className="ticket-image" 
          loading="lazy" 
          style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
        />
      </div>
      <div className="ticket-info" style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '24px' }}>
        <h3 className="ticket-name" style={{ height: '48px', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', marginBottom: '12px', fontSize: '16px', fontWeight: 800 }}>
          {event.title}
        </h3>
        
        <div className="ticket-meta" style={{ marginBottom: '16px', minHeight: '18px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span className="ticket-meta-item" style={{ fontSize: '13px' }}>
            <Calendar size={13} style={{ color: 'var(--color-primary-light)' }} /> {formatDate(event.startAt || event.startTime)}
          </span>
          <span className="ticket-meta-item" style={{ fontSize: '13px' }}>
            <MapPin size={13} style={{ color: 'var(--color-primary-light)' }} /> {event.venue || 'Địa điểm đang cập nhật'}
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
          
          <Link to={`/ticket/${event.id}`} className="btn btn-primary" style={{ padding: '10px 16px', borderRadius: 'var(--radius-md)' }}>
            Đặt vé <ArrowRight size={14} style={{ marginLeft: '4px' }} />
          </Link>
        </div>
      </div>
    </div>
  );
}

export default function TicketListing() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await eventService.getEvents();
        // Backend returns active events list
        setEvents(data || []);
      } catch (error) {
        console.error('Failed to fetch events:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  if (loading) {
    return (
      <div className="loading-container" style={{ padding: '100px 0' }}>
        <Loader2 className="spinner" size={40} />
        <p>Đang tải danh sách sự kiện hấp dẫn...</p>
      </div>
    );
  }

  return (
    <section className="ticket-listing" id="tickets">
      <div className="container">
        <div className="ticket-listing-header">
          <h2 className="section-title">
            <Sparkles size={20} style={{ color: 'var(--color-accent)', strokeWidth: 3 }} /> SỰ KIỆN NỔI BẬT
          </h2>
          <Link to="/tickets" className="view-all-link">
            Xem tất cả <ArrowRight size={14} />
          </Link>
        </div>
        
        {events.length > 0 ? (
          <div className="ticket-grid">
            {events.slice(0, 8).map(event => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '80px', background: 'white', borderRadius: '20px', border: '1px solid var(--color-border)' }}>
            <p style={{ color: 'var(--color-text-secondary)', fontWeight: 500 }}>Hiện chưa có sự kiện nào đang mở bán.</p>
          </div>
        )}
      </div>
    </section>
  );
}
