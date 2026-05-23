/* eslint-disable react-hooks/set-state-in-effect, no-unused-vars */
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, ListOrdered, Ticket, CheckCircle2, Trash2, RefreshCw, ChevronLeft, Sliders, Settings, Tag, Calendar, MapPin, X, Loader2, Edit, Check, AlertTriangle } from 'lucide-react';
import { adminService, eventService } from '../services/api';

function formatPrice(p) {
  if (p === undefined || p === null) return '—';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p);
}

function formatDT(s) {
  if (!s) return '—';
  try {
    return new Date(s).toLocaleString('vi-VN');
  } catch (e) {
    return s;
  }
}

const STATUS_MAP = {
  'PENDING': { label: 'Chờ thanh toán', color: '#f59e0b', bgClass: 'badge-warning' },
  'PAID': { label: 'Đã thanh toán', color: '#10b981', bgClass: 'badge-success' },
  'CANCELLED': { label: 'Đã hủy', color: '#94a3b8', bgClass: 'badge-danger' },
  'PAYMENT_FAILED': { label: 'Lỗi thanh toán', color: '#ef4444', bgClass: 'badge-danger' },
  'EXPIRED': { label: 'Hết hạn', color: '#64748b', bgClass: 'badge-info' }
};

export default function ManagerPage() {
  const [tab, setTab] = useState('events');
  const [events, setEvents] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Create / Edit Event Modal State
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null); // null = Create mode
  const [eventForm, setEventForm] = useState({
    title: '', description: '', venue: '', startAt: '', endAt: '', image: ''
  });

  // Ticket Types management sub-state (associated with an event)
  const [ticketModalOpen, setTicketModalOpen] = useState(false);
  const [targetEventForTickets, setTargetEventForTickets] = useState(null);
  const [ticketForm, setTicketForm] = useState({
    name: '', price: '', stockInitial: ''
  });
  const [editingTicketType, setEditingTicketType] = useState(null); // null = Create mode

  // Alert/Toast Notification state
  const [toast, setToast] = useState({ msg: '', type: '' });

  const showToast = (msg, type) => {
    setToast({ msg, type });
    setTimeout(() => setToast({ msg: '', type: '' }), 4000);
  };

  // 1. Fetch Events
  const loadEvents = useCallback(async () => {
    setLoading(true);
    try {
      // Using standard public endpoint but we can also use admin endpoints if we want
      // For now we load active events as standard list. 
      // To ensure admins can see even inactive/disabled events, we can load them or let standard load.
      const data = await eventService.getEvents();
      setEvents(data || []);
    } catch (err) {
      console.error('Error loading events:', err);
      showToast('Không thể tải danh sách sự kiện.', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  // 2. Fetch Orders
  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const data = await adminService.getOrders();
      setOrders(data || []);
    } catch (err) {
      console.error('Error loading orders:', err);
      showToast('Không thể tải danh sách đơn hàng.', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (tab === 'events') loadEvents();
    if (tab === 'orders') loadOrders();
  }, [tab, loadEvents, loadOrders]);

  // Event Mutations
  const handleOpenEventModal = (evt = null) => {
    if (evt) {
      setEditingEvent(evt);
      setEventForm({
        title: evt.title || '',
        description: evt.description || '',
        venue: evt.venue || '',
        startAt: evt.startAt ? evt.startAt.slice(0, 16) : '',
        endAt: evt.endAt ? evt.endAt.slice(0, 16) : '',
        image: evt.image || ''
      });
    } else {
      setEditingEvent(null);
      setEventForm({
        title: '', description: '', venue: '', startAt: '', endAt: '', image: ''
      });
    }
    setEventModalOpen(true);
  };

  const handleSaveEvent = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        ...eventForm,
        startAt: eventForm.startAt ? new Date(eventForm.startAt).toISOString() : null,
        endAt: eventForm.endAt ? new Date(eventForm.endAt).toISOString() : null,
      };

      if (editingEvent) {
        await adminService.updateEvent(editingEvent.id, payload);
        showToast('Cập nhật sự kiện thành công!', 'success');
      } else {
        await adminService.createEvent(payload);
        showToast('Tạo mới sự kiện thành công!', 'success');
      }
      setEventModalOpen(false);
      loadEvents();
    } catch (err) {
      console.error('Error saving event:', err);
      showToast('Lưu sự kiện thất bại.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteEvent = async (eventId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa sự kiện này? Thao tác này sẽ ẩn và soft-delete sự kiện khỏi phía khách hàng.')) {
      return;
    }
    try {
      await adminService.deleteEvent(eventId);
      showToast('Đã xóa sự kiện thành công.', 'success');
      loadEvents();
    } catch (err) {
      console.error('Error deleting event:', err);
      showToast('Xóa sự kiện thất bại.', 'error');
    }
  };

  const handleToggleEventStatus = async (event) => {
    const nextStatus = !event.active;
    try {
      await adminService.setEventStatus(event.id, nextStatus);
      showToast(`Đã ${nextStatus ? 'kích hoạt' : 'tạm dừng'} sự kiện thành công.`, 'success');
      loadEvents();
    } catch (err) {
      console.error('Error toggling status:', err);
      showToast('Thay đổi trạng thái sự kiện thất bại.', 'error');
    }
  };

  // Ticket Type Mutations
  const handleOpenTicketsModal = (evt) => {
    setTargetEventForTickets(evt);
    setTicketModalOpen(true);
    setEditingTicketType(null);
    setTicketForm({ name: '', price: '', stockInitial: '' });
  };

  const handleEditTicketTypeClick = (ticketType) => {
    setEditingTicketType(ticketType);
    setTicketForm({
      name: ticketType.name || '',
      price: ticketType.price || '',
      stockInitial: ticketType.stockInitial || ''
    });
  };

  const handleSaveTicketType = async (e) => {
    e.preventDefault();
    if (!targetEventForTickets) return;
    setLoading(true);
    try {
      const payload = {
        name: ticketForm.name,
        price: Number(ticketForm.price),
        stockInitial: Number(ticketForm.stockInitial),
        stockAvailable: Number(ticketForm.stockInitial) // initial stockAvailable maps stockInitial
      };

      if (editingTicketType) {
        await adminService.updateTicketType(editingTicketType.id, payload);
        showToast('Cập nhật hạng vé thành công!', 'success');
      } else {
        await adminService.createTicketType(targetEventForTickets.id, payload);
        showToast('Thêm mới hạng vé thành công!', 'success');
      }

      // Refresh target event details to get updated ticket types list
      const updatedEvent = await eventService.getEventById(targetEventForTickets.id);
      setTargetEventForTickets(updatedEvent);
      
      // Reset form
      setEditingTicketType(null);
      setTicketForm({ name: '', price: '', stockInitial: '' });
      loadEvents(); // Reload dashboard background lists
    } catch (err) {
      console.error('Error saving ticket type:', err);
      showToast('Lưu hạng vé thất bại.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteTicketType = async (ticketTypeId) => {
    if (!window.confirm('Bạn có chắc muốn xóa loại vé này? Thao tác này không thể hoàn tác nếu đã có người đặt.')) {
      return;
    }
    try {
      await adminService.deleteTicketType(ticketTypeId);
      showToast('Đã xóa hạng vé thành công.', 'success');
      
      // Refresh event ticket list
      const updatedEvent = await eventService.getEventById(targetEventForTickets.id);
      setTargetEventForTickets(updatedEvent);
      loadEvents();
    } catch (err) {
      console.error('Error deleting ticket type:', err);
      showToast('Xóa hạng vé thất bại.', 'error');
    }
  };

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', paddingBottom: '80px' }}>
      {/* Toast Alert floating */}
      {toast.msg && (
        <div className="toast-container">
          <div className={`toast ${toast.type === 'success' ? 'success' : 'error'}`}>
            <span className="toast-message">{toast.msg}</span>
            <X size={16} className="toast-close" onClick={() => setToast({ msg: '', type: '' })} />
          </div>
        </div>
      )}

      {/* Admin Panel Header Banner */}
      <div style={{ background: 'linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 100%)', padding: '24px 0', boxShadow: 'var(--shadow-md)' }}>
        <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.6)', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '4px' }}>HỆ THỐNG QUẢN TRỊ</div>
            <h1 style={{ color: 'white', fontSize: '24px', fontWeight: '900', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
              🎫 TicketPro Manager <span className="badge badge-danger" style={{ fontSize: '10px' }}>ADMIN CONSOLE</span>
            </h1>
          </div>
          <Link to="/" className="btn btn-outline" style={{ color: 'white', borderColor: 'rgba(255,255,255,0.3)', padding: '8px 16px', borderRadius: 'var(--radius-full)' }}>
            <ChevronLeft size={16} /> Về Trang Chủ
          </Link>
        </div>
      </div>

      {/* Management Navigation Tabs */}
      <div style={{ background: 'white', borderBottom: '1px solid var(--color-border)' }}>
        <div className="container" style={{ display: 'flex' }}>
          <button 
            onClick={() => setTab('events')}
            className={`nav-link ${tab === 'events' ? 'active' : ''}`}
            style={{ padding: '20px 24px', borderRadius: 0, fontWeight: 700, fontSize: '14px', border: 'none', background: 'none' }}
          >
            <Sliders size={16} style={{ marginRight: '8px' }} /> Quản lý Sự kiện
          </button>
          <button 
            onClick={() => setTab('orders')}
            className={`nav-link ${tab === 'orders' ? 'active' : ''}`}
            style={{ padding: '20px 24px', borderRadius: 0, fontWeight: 700, fontSize: '14px', border: 'none', background: 'none' }}
          >
            <ListOrdered size={16} style={{ marginRight: '8px' }} /> Quản lý Đơn hàng
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="container" style={{ paddingTop: '32px' }}>
        
        {/* Loading Spinner */}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'center', margin: '20px 0' }}>
            <Loader2 className="spinner" size={32} />
          </div>
        )}

        {/* TAB 1: EVENTS */}
        {tab === 'events' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <div>
                <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-primary-dark)' }}>Danh sách Sự kiện</h2>
                <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Quản lý các chương trình, kích hoạt sự kiện và hạng vé tương ứng</p>
              </div>
              <button className="btn btn-primary" onClick={() => handleOpenEventModal()}>
                <Plus size={16} /> Tạo sự kiện mới
              </button>
            </div>

            {events.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '60px', background: 'white', borderRadius: '16px', border: '1px solid var(--color-border)' }}>
                <AlertTriangle size={32} style={{ color: 'var(--color-text-light)', marginBottom: '8px' }} />
                <p style={{ color: 'var(--color-text-secondary)', fontWeight: 600 }}>Không tìm thấy sự kiện nào.</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table className="premium-table">
                  <thead>
                    <tr>
                      <th>Sự kiện</th>
                      <th>Địa điểm</th>
                      <th>Thời gian</th>
                      <th>Trạng thái</th>
                      <th>Hạng Vé (Bán/Tổng)</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {events.map((evt) => {
                      const activeTickets = evt.ticketTypes ? evt.ticketTypes.filter(t => t.active) : [];
                      const totalAvailable = activeTickets.reduce((a, b) => a + b.stockAvailable, 0);
                      const totalInitial = activeTickets.reduce((a, b) => a + b.stockInitial, 0);

                      return (
                        <tr key={evt.id}>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                              <img src={evt.image || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=100&h=100&fit=crop'} alt="" style={{ width: '40px', height: '40px', borderRadius: '6px', objectFit: 'cover' }} />
                              <div>
                                <strong style={{ color: 'var(--color-primary-dark)', fontSize: '14px' }}>{evt.title}</strong>
                                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', maxWidth: '280px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                  {evt.description || 'Không có mô tả.'}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px' }}>
                              <MapPin size={13} style={{ color: '#64748b' }} /> {evt.venue}
                            </div>
                          </td>
                          <td style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <Calendar size={13} /> {formatDT(evt.startAt || evt.startTime)}
                            </div>
                          </td>
                          <td>
                            <span 
                              onClick={() => handleToggleEventStatus(evt)}
                              className={`badge ${evt.active ? 'badge-success' : 'badge-danger'}`} 
                              style={{ cursor: 'pointer', userSelect: 'none' }}
                            >
                              {evt.active ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <div style={{ fontSize: '13px' }}>
                              <button 
                                className="btn btn-outline btn-sm" 
                                onClick={() => handleOpenTicketsModal(evt)}
                                style={{ padding: '4px 10px', fontSize: '11px', display: 'flex', alignItems: 'center', gap: '4px' }}
                              >
                                <Tag size={12} /> Hạng vé ({activeTickets.length})
                              </button>
                              <div style={{ fontSize: '10px', color: 'var(--color-text-secondary)', marginTop: '4px', textAlign: 'center' }}>
                                Còn lại: <strong>{totalAvailable}</strong> / {totalInitial}
                              </div>
                            </div>
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: '8px' }}>
                              <button className="btn btn-sm btn-outline" onClick={() => handleOpenEventModal(evt)} style={{ padding: '6px' }}>
                                <Edit size={14} />
                              </button>
                              <button className="btn btn-sm btn-danger" onClick={() => handleDeleteEvent(evt.id)} style={{ padding: '6px' }}>
                                <Trash2 size={14} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: ORDERS */}
        {tab === 'orders' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <div>
                <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-primary-dark)' }}>Quản lý Đơn hàng</h2>
                <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Giám sát trạng thái đơn hàng của tất cả người dùng trong hệ thống</p>
              </div>
              <button className="btn btn-outline btn-sm" onClick={loadOrders}>
                <RefreshCw size={14} style={{ marginRight: '4px' }} /> Làm mới
              </button>
            </div>

            {orders.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '60px', background: 'white', borderRadius: '16px', border: '1px solid var(--color-border)' }}>
                <AlertTriangle size={32} style={{ color: 'var(--color-text-light)', marginBottom: '8px' }} />
                <p style={{ color: 'var(--color-text-secondary)', fontWeight: 600 }}>Chưa phát sinh giao dịch đơn hàng nào.</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table className="premium-table">
                  <thead>
                    <tr>
                      <th>Mã đơn hàng</th>
                      <th>Khách hàng</th>
                      <th>Hạng vé mua</th>
                      <th>SL</th>
                      <th>Tổng tiền</th>
                      <th>Trạng thái</th>
                      <th>Thời gian tạo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((ord) => {
                      const st = STATUS_MAP[ord.status] || STATUS_MAP['PENDING'];
                      return (
                        <tr key={ord.id}>
                          <td>
                            <code style={{ fontSize: '13px', fontWeight: '700', color: 'var(--color-primary)', fontFamily: 'monospace' }}>
                              #{ord.orderNumber}
                            </code>
                          </td>
                          <td>
                            <div style={{ fontSize: '13px' }}>
                              <strong>User ID: {ord.userId}</strong>
                              <div style={{ fontSize: '10px', color: 'var(--color-text-secondary)' }}>Mã hóa hệ thống</div>
                            </div>
                          </td>
                          <td>
                            <span style={{ fontWeight: '600' }}>{ord.ticketTypeName || `Vé loại #${ord.ticketTypeId}`}</span>
                          </td>
                          <td style={{ fontWeight: '700' }}>{ord.quantity}</td>
                          <td style={{ fontWeight: '800', color: 'var(--color-primary-dark)' }}>{formatPrice(ord.totalPrice)}</td>
                          <td>
                            <span className={`badge ${st.bgClass}`}>
                              {st.label}
                            </span>
                          </td>
                          <td style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                            {formatDT(ord.createdAt)}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* EVENT EDIT/CREATE MODAL POPUP */}
      {eventModalOpen && (
        <div className="modal-overlay" onClick={() => setEventModalOpen(false)}>
          <div className="modal-container" style={{ maxWidth: '580px' }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">{editingEvent ? 'Cập nhật sự kiện' : 'Tạo sự kiện mới'}</h3>
              <X size={20} className="modal-close" onClick={() => setEventModalOpen(false)} />
            </div>
            <form onSubmit={handleSaveEvent}>
              <div className="modal-body">
                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label className="form-label">Tên sự kiện *</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      placeholder="VD: Show ca nhạc Anh Trai Vượt Ngàn Chông Gai 2026"
                      value={eventForm.title}
                      onChange={(e) => setEventForm({ ...eventForm, title: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Địa điểm tổ chức *</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      placeholder="VD: Sân vận động Quân Khu 7, TP. HCM"
                      value={eventForm.venue}
                      onChange={(e) => setEventForm({ ...eventForm, venue: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Mô tả chi tiết</label>
                    <textarea 
                      className="form-control" 
                      style={{ minHeight: '80px', resize: 'vertical' }}
                      placeholder="Giới thiệu chi tiết chương trình, nghệ sĩ khách mời..."
                      value={eventForm.description}
                      onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                    <div className="form-group">
                      <label className="form-label">Thời gian bắt đầu *</label>
                      <input 
                        type="datetime-local" 
                        className="form-control" 
                        value={eventForm.startAt}
                        onChange={(e) => setEventForm({ ...eventForm, startAt: e.target.value })}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Thời gian kết thúc *</label>
                      <input 
                        type="datetime-local" 
                        className="form-control" 
                        value={eventForm.endAt}
                        onChange={(e) => setEventForm({ ...eventForm, endAt: e.target.value })}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Đường dẫn hình ảnh banner (URL)</label>
                    <input 
                      type="url" 
                      className="form-control" 
                      placeholder="https://example.com/banner.png (để trống nếu dùng ảnh mẫu)"
                      value={eventForm.image}
                      onChange={(e) => setEventForm({ ...eventForm, image: e.target.value })}
                    />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline btn-sm" onClick={() => setEventModalOpen(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary btn-sm">Lưu lại</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* TICKET TYPES MODAL POPUP */}
      {ticketModalOpen && targetEventForTickets && (
        <div className="modal-overlay" onClick={() => setTicketModalOpen(false)}>
          <div className="modal-container" style={{ maxWidth: '680px' }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 className="modal-title">Hạng vé sự kiện</h3>
                <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>{targetEventForTickets.title}</span>
              </div>
              <X size={20} className="modal-close" onClick={() => setTicketModalOpen(false)} />
            </div>

            <div className="modal-body" style={{ background: '#f8fafc' }}>
              {/* Form to add/edit ticket types */}
              <form onSubmit={handleSaveTicketType} style={{ background: 'white', padding: '16px', borderRadius: '12px', border: '1px solid var(--color-border)', marginBottom: '20px' }}>
                <h4 style={{ fontWeight: '800', fontSize: '13px', color: 'var(--color-primary-dark)', marginBottom: '12px', textTransform: 'uppercase' }}>
                  {editingTicketType ? 'Cập nhật hạng vé' : 'Thêm hạng vé mới'}
                </h4>
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr auto', gap: '10px', alignItems: 'end' }}>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label" style={{ fontSize: '10px' }}>TÊN HẠNG VÉ *</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      placeholder="VD: VIP, Standard, GA"
                      value={ticketForm.name}
                      onChange={(e) => setTicketForm({ ...ticketForm, name: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label" style={{ fontSize: '10px' }}>ĐƠN GIÁ (VND) *</label>
                    <input 
                      type="number" 
                      min="1"
                      className="form-control" 
                      placeholder="VD: 500000"
                      value={ticketForm.price}
                      onChange={(e) => setTicketForm({ ...ticketForm, price: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label" style={{ fontSize: '10px' }}>SỐ LƯỢNG MỞ BÁN *</label>
                    <input 
                      type="number" 
                      min="1"
                      className="form-control" 
                      placeholder="VD: 100"
                      value={ticketForm.stockInitial}
                      onChange={(e) => setTicketForm({ ...ticketForm, stockInitial: e.target.value })}
                      required
                    />
                  </div>
                  <div style={{ display: 'flex', gap: '4px' }}>
                    <button type="submit" className="btn btn-primary" style={{ padding: '12px' }}>
                      <Check size={16} />
                    </button>
                    {editingTicketType && (
                      <button type="button" className="btn btn-outline" style={{ padding: '12px' }} onClick={() => {
                        setEditingTicketType(null);
                        setTicketForm({ name: '', price: '', stockInitial: '' });
                      }}>
                        <X size={16} />
                      </button>
                    )}
                  </div>
                </div>
              </form>

              {/* List of current ticket types for event */}
              <h4 style={{ fontWeight: '800', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '10px' }}>HẠNG VÉ ĐÃ CÓ</h4>
              {targetEventForTickets.ticketTypes && targetEventForTickets.ticketTypes.length === 0 ? (
                <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', textAlign: 'center', padding: '20px' }}>Sự kiện này chưa có hạng vé nào được tạo.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {targetEventForTickets.ticketTypes && targetEventForTickets.ticketTypes.map(t => (
                    <div key={t.id} style={{ background: 'white', padding: '14px 16px', borderRadius: '12px', border: '1px solid var(--color-border-light)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong style={{ fontSize: '14px', color: 'var(--color-primary-dark)' }}>{t.name}</strong>
                        <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                          Kho vé: <strong>{t.stockAvailable}</strong> / {t.stockInitial} | Đã bán: <strong>{t.stockInitial - t.stockAvailable}</strong>
                        </div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <span style={{ fontWeight: '800', color: 'var(--color-primary)', fontSize: '14px' }}>
                          {formatPrice(t.price)}
                        </span>
                        <div style={{ display: 'flex', gap: '4px' }}>
                          <button className="btn btn-sm btn-outline" style={{ padding: '6px' }} onClick={() => handleEditTicketTypeClick(t)}>
                            <Edit size={12} />
                          </button>
                          <button className="btn btn-sm btn-danger" style={{ padding: '6px' }} onClick={() => handleDeleteTicketType(t.id)}>
                            <Trash2 size={12} />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-primary btn-sm" onClick={() => setTicketModalOpen(false)}>
                Hoàn tất
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
