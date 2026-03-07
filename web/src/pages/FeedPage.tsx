import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { fetchPosts } from '../api/posts'
import { PostResponse } from '../types/api'
import { formatTimeAgo } from '../utils/format'
import './FeedPage.css'

function PostCard({ post }: { post: PostResponse }) {
  return (
    <article className="post-card">
      <div className="post-card-content">
        <h2 className="post-card-title">
          <Link to={`/posts/${post.id}`}>{post.title}</Link>
        </h2>
        <div className="post-card-meta">
          <span className="post-card-author">{post.authorUsername}</span>
          <span className="post-card-sep">&middot;</span>
          <time className="post-card-time">{formatTimeAgo(post.createdAt)}</time>
        </div>
      </div>
    </article>
  )
}

function FeedPage() {
  const [posts, setPosts] = useState<PostResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchPosts()
      .then((data) => setPosts(data.posts))
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load'))
      .finally(() => setIsLoading(false))
  }, [])

  if (isLoading) return <div className="feed-status">Loading posts...</div>
  if (error) return <div className="feed-status feed-error">{error}</div>

  if (posts.length === 0) {
    return (
      <div className="feed">
        <h1 className="feed-title">Feed</h1>
        <div className="feed-empty">
          <p>No posts yet. Be the first to share something.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="feed">
      <h1 className="feed-title">Feed</h1>
      <div className="feed-list">
        {posts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  )
}

export default FeedPage
