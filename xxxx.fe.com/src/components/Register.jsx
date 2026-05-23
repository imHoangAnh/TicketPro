import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/api';
import { User, Mail, Lock, Eye, EyeOff, Loader2 } from 'lucide-react';

function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!fullName || !email || !password || !confirmPassword) {
      setError('Vui lòng điền đầy đủ tất cả các trường.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      await authService.register({ email, password, fullName });
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      console.error('Registration error:', err);
      const msg = err.response?.data?.message || 'Đăng ký thất bại. Email có thể đã được sử dụng.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Đăng ký tài khoản</h2>
          <p className="auth-subtitle">Trở thành thành viên để nhận nhiều ưu đãi đặt vé</p>
        </div>

        {error && (
          <div className="toast error" style={{ position: 'relative', top: 0, right: 0, minWidth: '100%', marginBottom: 'var(--space-4)', pointerEvents: 'auto' }}>
            <span className="toast-message">{error}</span>
          </div>
        )}

        {success && (
          <div className="toast success" style={{ position: 'relative', top: 0, right: 0, minWidth: '100%', marginBottom: 'var(--space-4)', pointerEvents: 'auto' }}>
            <span className="toast-message">Đăng ký thành công! Đang chuyển hướng sang trang đăng nhập...</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="fullName">Họ và tên</label>
            <div style={{ position: 'relative' }}>
              <input
                id="fullName"
                type="text"
                className="form-control"
                placeholder="Nguyễn Văn A"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                style={{ paddingLeft: '44px' }}
                disabled={loading || success}
                required
              />
              <User size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-light)' }} />
            </div>
          </div>

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
                disabled={loading || success}
                required
              />
              <Mail size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-light)' }} />
            </div>
          </div>

          <div className="form-group">
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
                disabled={loading || success}
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

          <div className="form-group" style={{ marginBottom: 'var(--space-6)' }}>
            <label className="form-label" htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <div style={{ position: 'relative' }}>
              <input
                id="confirmPassword"
                type={showPassword ? 'text' : 'password'}
                className="form-control"
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                style={{ paddingLeft: '44px' }}
                disabled={loading || success}
                required
              />
              <Lock size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-light)' }} />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '14px' }} disabled={loading || success}>
            {loading ? (
              <>
                <Loader2 size={18} className="spinner" style={{ animation: 'spin 1s linear infinite', borderTopColor: '#fff' }} />
                Đang tạo tài khoản...
              </>
            ) : (
              'Đăng Ký Thành Viên'
            )}
          </button>
        </form>

        <div style={{ marginTop: 'var(--space-6)', textAlign: 'center', fontSize: 'var(--font-size-sm)', color: 'var(--color-text-secondary)' }}>
          Đã có tài khoản?{' '}
          <Link to="/login" className="auth-link">
            Đăng nhập ngay
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
