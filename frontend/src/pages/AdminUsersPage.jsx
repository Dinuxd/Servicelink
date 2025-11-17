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
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-4">Admin · Users</h1>
      <select className="border rounded p-2 mb-3" value={role} onChange={e=>setRole(e.target.value)}>
        <option value="">All</option>
        <option value="user">User</option>
        <option value="provider">Provider</option>
        <option value="admin">Admin</option>
      </select>
      <div className="space-y-2">
        {(data?.content || []).map(u => (
          <div key={u.id} className="border rounded p-2 flex items-center justify-between">
            <div>
              <div className="font-medium">{u.name}</div>
              <div className="text-xs">{u.email} · {u.roles?.join(', ')}</div>
            </div>
            <button className="text-blue-600" onClick={() => toggle.mutate(u.id)}>
              {u.active ? 'Deactivate' : 'Activate'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
