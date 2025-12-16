import React from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';

function useListing(id) {
  return useQuery({ queryKey: ['listing', id], queryFn: async () => (await api.get(`/api/listings/${id}`)).data });
}
function useReviews(id) {
  return useQuery({ queryKey: ['reviews', id], queryFn: async () => (await api.get(`/api/listings/${id}/reviews`)).data });
}

export default function ListingDetailPage() {
  const { id } = useParams();
  const { data: listing, isLoading } = useListing(id);
  const { data: reviews } = useReviews(id);

  if (isLoading) {
    return (
      <div className="sl-page-card" style={{ maxWidth: 760, margin: '0 auto' }}>
        <div className="sl-skeleton" style={{ height: 20, width: '55%', marginBottom: 10 }} />
        <div className="sl-skeleton" style={{ height: 14, width: '35%', marginBottom: 16 }} />
        <div className="sl-skeleton" style={{ height: 80, width: '100%' }} />
      </div>
    );
  }
  if (!listing) {
    return (
      <div className="sl-page-card" style={{ maxWidth: 760, margin: '0 auto' }}>
        <div className="sl-page-title">Listing not found</div>
        <div className="sl-page-subtitle">This service may have been removed.</div>
      </div>
    );
  }

  return (
    <div className="sl-page-card" style={{ maxWidth: 760, margin: '0 auto' }}>
      <h1 className="sl-page-title">{listing.title}</h1>
      <div className="sl-page-subtitle">{listing.categoryName || 'Service listing'}</div>
      <div style={{ marginTop: 14, fontSize: '0.98rem' }}>{listing.description}</div>
      <div style={{ marginTop: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>${listing.price}</div>
          <div className="text-xs text-gray-400">Per job · secure checkout</div>
        </div>
        <button className="sl-hero-cta-primary">Book this service</button>
      </div>

      <h2 className="sl-page-title" style={{ fontSize: '1rem', marginTop: 26, marginBottom: 8 }}>Reviews</h2>
      {(!reviews || reviews.length === 0) && (
        <div className="sl-empty">
          This service has no reviews yet. Be the first to book and leave feedback.
        </div>
      )}
      {reviews && reviews.length > 0 && (
        <div className="space-y-3">
          {reviews.map((r) => (
            <div
              key={r.id}
              className="border rounded p-3"
              style={{ background: 'rgba(15,23,42,0.9)', borderColor: 'rgba(148,163,184,0.55)' }}
            >
              <div style={{ fontWeight: 550 }}>Rating: {r.rating}/5</div>
              <div className="text-gray-200" style={{ marginTop: 4 }}>
                {r.content}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
