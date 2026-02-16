import React from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';

function useListings(params) {
  return useQuery({
    queryKey: ['listings', params.toString()],
    queryFn: async () => {
      const { data } = await api.get(`/api/listings?${params.toString()}`);
      return data;
    },
  });
}

export default function ListingsPage() {
  const [params, setParams] = useSearchParams({ page: '0', size: '12' });
  const { data, isLoading } = useListings(params);

  const setFilter = (key, val) => {
    const next = new URLSearchParams(params);
    if (val === '' || val == null) next.delete(key); else next.set(key, val);
    next.set('page', '0');
    setParams(next);
  };

  const page = Number(params.get('page') || 0);
  const size = Number(params.get('size') || 12);

  const skeletons = Array.from({ length: 6 });

  return (
    <div className="sl-page-container">
      <div className="sl-page-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', gap: 16, marginBottom: 20 }}>
        <div>
          <h1 className="sl-page-title">Browse services</h1>
          <div className="sl-page-subtitle">Search verified providers and filter by price or category.</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(0,220px))', gap: 8, marginBottom: 20 }}>
        <input className="sl-input" placeholder="Search..." defaultValue={params.get('q') || ''}
               onBlur={(e) => setFilter('q', e.target.value)} />
        <input className="sl-input" type="number" placeholder="Min price" defaultValue={params.get('minPrice') || ''}
               onBlur={(e) => setFilter('minPrice', e.target.value)} />
        <input className="sl-input" type="number" placeholder="Max price" defaultValue={params.get('maxPrice') || ''}
               onBlur={(e) => setFilter('maxPrice', e.target.value)} />
        <input className="sl-input" type="number" placeholder="Category ID" defaultValue={params.get('categoryId') || ''}
               onBlur={(e) => setFilter('categoryId', e.target.value)} />
      </div>

      {isLoading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(0,240px))', gap: 12 }}>
          {skeletons.map((_, i) => (
            <div key={i} className="sl-page-card" style={{ padding: 14 }}>
              <div className="sl-skeleton" style={{ height: 14, width: '60%' }} />
              <div className="sl-skeleton" style={{ height: 10, width: '90%', marginTop: 8 }} />
              <div className="sl-skeleton" style={{ height: 10, width: '50%', marginTop: 8 }} />
            </div>
          ))}
        </div>
      )}
      {!isLoading && data && data.content.length === 0 && (
        <div className="sl-empty" style={{ marginTop: 8 }}>
          No services match your filters. Try clearing some filters or adjusting your price range.
        </div>
      )}
      {!isLoading && data && data.content.length > 0 && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(0,240px))', gap: 12 }}>
            {data.content.map((item) => (
              <div key={item.id} className="sl-page-card" style={{ padding: 14 }}>
                <div className="font-medium">{item.title}</div>
                <div className="text-sm text-gray-300" style={{ marginTop: 4 }}>
                  {item.description?.slice(0, 120) || 'No description provided.'}
                </div>
                <div style={{ marginTop: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.9rem' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>${item.price}</div>
                    <div className="sl-text-muted" style={{ fontSize: '0.75rem' }}>{item.categoryName || 'Uncategorized'}</div>
                  </div>
                  <Link className="sl-btn sl-btn-secondary" to={`/listings/${item.id}`}>
                    View
                  </Link>
                </div>
              </div>
            ))}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 22 }}>
            <button
              disabled={page <= 0}
              className="sl-btn sl-btn-secondary"
              onClick={() => setParams(p => { const n = new URLSearchParams(p); n.set('page', String(page-1)); return n; })}
            >
              Prev
            </button>
            <div>Page {page+1} of {Math.ceil(data.totalElements / size)}</div>
            <button
              disabled={(page+1) >= Math.ceil(data.totalElements / size)}
              className="sl-btn sl-btn-secondary"
              onClick={() => setParams(p => { const n = new URLSearchParams(p); n.set('page', String(page+1)); return n; })}
            >
              Next
            </button>
          </div>
        </>
      )}
      </div>
    </div>
  );
}
