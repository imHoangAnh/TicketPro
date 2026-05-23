import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { authService } from '../services/api';
import { Lock, Mail, Eye, EyeOff, Loader2 } from 'lucide-react';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  // Redirect back to prior page or home
  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Vui lòng điền đầy đủ thông tin đăng nhập.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      await authService.login({ email, password });
      // Notify parent components via custom event to update auth state immediately
      window.dispatchEvent(new CustomEvent('auth-status-change'));
      navigate(from, { replace: true });
    } catch (err) {
      console.error('Login error:', err);
      const msg = err.response?.data?.message || 'Email hoặc mật khẩu không chính xác.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Chào mừng trở lại</h2>
          <p className="auth-subtitle">Đăng nhập tài khoản để tiếp tục đặt vé của bạn</p>
        </div>

        {error && (
          <div className="toast error" style={{ position: 'relative', top: 0, right: 0, minWidth: '100%', marginBottom: 'var(--space-4)', pointerEvents: 'auto' }}>
            <span className="toast-message">{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email</label>
            <div style={{ position: 'relative' }}>
              <input
                id="email"
                type="email"
                className="form-control"
                placeholder="ten@viethuong.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ paddingLeft: '44px' }}
                disabled={loading}
                required
              />
              <Mail size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-light)' }} />
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: 'var(--space-6)' }}>
            <label className="form-label" htmlFor="password">Mật khẩu</label>
            <div style={{ position: 'relative' }}>
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="form-control"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ paddingLeft: '44px', paddingRight: '44px' }}
                disabled={loading}
                required
              />
              <Lock size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-light)' }} />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{ position: 'absolute', right: '16px', top: '50%', transform: 'translateY(-50%)', background: 'none', color: 'var(--color-text-light)', border: 'none', padding: 0 }}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '14px' }} disabled={loading}>
            {loading ? (
              <>
                <Loader2 size={18} className="spinner" style={{ animation: 'spin 1s linear infinite', borderTopColor: '#fff' }} />
                Đang đăng nhập...
              </>
            ) : (
              'Đăng Nhập'
            )}
          </button>
        </form>

        <div style={{ marginTop: 'var(--space-6)', textAlign: 'center', fontSize: 'var(--font-size-sm)', color: 'var(--color-text-secondary)' }}>
          Chưa có tài khoản?{' '}
          <Link to="/register" className="auth-link">
            Đăng ký ngay
          </Link>
        </div>

        {/* Credentials guide for seamless testing */}
        <div style={{ marginTop: 'var(--space-8)', padding: 'var(--space-4)', background: 'var(--color-bg-section)', borderRadius: 'var(--radius-md)', fontSize: 'var(--font-size-xs)' }}>
          <p style={{ fontWeight: '700', color: 'var(--color-primary)', marginBottom: 'var(--space-2)' }}>Tài khoản thử nghiệm hệ thống:</p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-2)' }}>
            <div>
              <p style={{ fontWeight: '600' }}>Admin Account:</p>
              <code style={{ color: 'var(--color-hot)' }}>admin@example.com</code><br/>
              <code>password123</code>
            </div>
            <div>
              <p style={{ fontWeight: '600' }}>User Account:</p>
              <code style={{ color: 'var(--color-success)' }}>user@example.com</code><br/>
              <code>password123</code>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
