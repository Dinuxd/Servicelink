import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../lib/api';
import toast from 'react-hot-toast';

function useCategories(page=0, size=50) {
  return useQuery({ queryKey: ['admin-categories', page, size], queryFn: async () => (await api.get('/api/admin/categories', { params: { page, size } })).data });
}

export default function AdminCategoriesPage() {
  const { data } = useCategories();
  const qc = useQueryClient();
  const [form, setForm] = useState({ name: '', icon: '' });

  const createMut = useMutation({
    mutationFn: async () => (await api.post('/api/admin/categories', form)).data,
    onSuccess: () => { toast.success('Created'); setForm({name:'',icon:''}); qc.invalidateQueries({ queryKey: ['admin-categories'] }); },
  });
  const updateMut = useMutation({
    mutationFn: async (cat) => (await api.put(`/api/admin/categories/${cat.id}`, cat)).data,
    onSuccess: () => { toast.success('Updated'); qc.invalidateQueries({ queryKey: ['admin-categories'] }); },
  });
  const delMut = useMutation({
    mutationFn: async (id) => (await api.delete(`/api/admin/categories/${id}`)).data,
    onSuccess: () => { toast.success('Deleted'); qc.invalidateQueries({ queryKey: ['admin-categories'] }); },
  });

  return (
    <div className="sl-page-card-admin" style={{ maxWidth: 860, margin: '0 auto' }}>
      <h1 className="sl-page-title">Admin · Categories</h1>
      <div className="sl-page-subtitle">Manage how services are grouped in search.</div>

      <div className="sl-stack-h" style={{ marginTop: 16, marginBottom: 16 }}>
        <input className="sl-input" placeholder="Name" value={form.name} onChange={e=>setForm({...form, name: e.target.value})} />
        <input className="sl-input" placeholder="Icon" value={form.icon} onChange={e=>setForm({...form, icon: e.target.value})} />
        <button className="sl-hero-cta-primary" onClick={()=>createMut.mutate()} disabled={createMut.isPending}>
          {createMut.isPending ? 'Creating…' : 'Create'}
        </button>
      </div>

      {(!data?.content || data.content.length === 0) && (
        <div className="sl-empty">
          No categories yet. Create the first category above.
        </div>
      )}

      <div className="space-y-2">
        {(data?.content || []).map(c => (
          <div
            key={c.id}
            className="border rounded p-2 flex items-center justify-between"
            style={{ background: 'rgba(15,23,42,0.9)', borderColor: 'rgba(148,163,184,0.55)' }}
          >
            <div>{c.name} {c.icon && <span style={{ marginLeft: 8 }}>{c.icon}</span>}</div>
            <div className="sl-stack-h">
              <button className="sl-hero-cta-secondary" onClick={() => updateMut.mutate({ id: c.id, name: c.name + ' *', icon: c.icon })}>
                Edit sample
              </button>
              <button className="sl-hero-cta-secondary" style={{ color: '#fecaca' }} onClick={() => delMut.mutate(c.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
