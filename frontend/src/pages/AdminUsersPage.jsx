import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../lib/api';

function useUsers(role, page = 0, size = 20) {
  return useQuery({
    queryKey: ['admin-users', role, page, size],
    queryFn: async () => (await api.get('/api/admin/users', { params: { role, page, size } })).data,
  });
}

export default function AdminUsersPage() {
  const [role, setRole] = React.useState('');
  const { data } = useUsers(role);
  const qc = useQueryClient();

  const toggle = useMutation({
    mutationFn: async (id) => (await api.patch(`/api/admin/users/${id}/toggle-active`)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] })
  });

  return (
    <div className="sl-page-container">
      <div className="sl-page-card-admin" style={{ maxWidth: 860, margin: '0 auto' }}>
        <h1 className="sl-page-title">Admin · Users</h1>
        <div className="sl-page-subtitle">Inspect accounts and toggle access.</div>

        <select
          className="sl-input"
          style={{ maxWidth: 200, marginTop: 16, marginBottom: 16 }}
          value={role}
          onChange={(e) => setRole(e.target.value)}
        >
          <option value="">All</option>
          <option value="user">User</option>
          <option value="provider">Provider</option>
          <option value="admin">Admin</option>
        </select>

        {(!data?.content || data.content.length === 0) && (
          <div className="sl-empty">
            No users found for this filter.
          </div>
        )}

        <div className="space-y-2">
          {(data?.content || []).map((u) => (
            <div
              key={u.id}
              className="border rounded p-3 flex items-center justify-between sl-surface-panel"
              style={{ background: 'rgba(15,23,42,0.9)', borderColor: 'rgba(148,163,184,0.35)' }}
            >
              <div>
                <div style={{ fontWeight: 550 }}>{u.name}</div>
                <div className="sl-stack-h" style={{ marginTop: 4, fontSize: '0.78rem' }}>
                  <span className="text-xs text-gray-300">{u.email}</span>
                  <span className={`sl-badge-admin ${u.active ? 'sl-badge-success' : 'sl-badge-danger'}`}>
                    {u.active ? 'Active' : 'Disabled'}
                  </span>
                  {u.roles?.map((r) => (
                    <span key={r} className="sl-badge sl-badge-muted">{r}</span>
                  ))}
                </div>
              </div>
              <button
                className={"sl-hero-cta-secondary"}
                onClick={() => toggle.mutate(u.id)}
              >
                {u.active ? 'Deactivate' : 'Activate'}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
