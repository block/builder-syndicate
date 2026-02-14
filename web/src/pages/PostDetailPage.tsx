import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { fetchPost } from '../api/posts'
import { PostResponse } from '../types/api'
import { formatTimeAgo } from '../utils/format'
import './PostDetailPage.css'

function PostDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [post, setPost] = useState<PostResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    fetchPost(id)
      .then(setPost)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load'))
      .finally(() => setIsLoading(false))
  }, [id])

  if (isLoading) return <div className="detail-status">Loading...</div>
  if (error) return <div className="detail-status detail-error">{error}</div>
  if (!post) return <div className="detail-status">Post not found.</div>

  return (
    <div className="post-detail">
      <Link to="/" className="back-link">&larr; Back to Feed</Link>
      <article className="post-detail-card">
        <h1 className="post-detail-title">{post.title}</h1>
        <div className="post-detail-meta">
          <span className="post-detail-author">{post.authorUsername}</span>
          <span className="post-detail-sep">&middot;</span>
          <time className="post-detail-time">{formatTimeAgo(post.createdAt)}</time>
        </div>
        <div
          className="post-detail-body"
          dangerouslySetInnerHTML={{ __html: post.bodyHtml }}
        />
      </article>
    </div>
  )
}

export default PostDetailPage
