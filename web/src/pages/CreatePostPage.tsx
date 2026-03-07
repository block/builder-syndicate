import { useState, useEffect, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { createPost } from '../api/posts'
import './CreatePostPage.css'

function CreatePostPage() {
  const { isLoggedIn, isLoading } = useAuth()
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!isLoading && !isLoggedIn) {
      navigate('/login', { replace: true })
    }
  }, [isLoggedIn, isLoading, navigate])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !body.trim()) {
      setError('Title and body are required.')
      return
    }
    setIsSubmitting(true)
    setError(null)
    try {
      const post = await createPost({ title: title.trim(), body: body.trim() })
      navigate(`/posts/${post.id}`, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create post')
      setIsSubmitting(false)
    }
  }

  if (isLoading) return <div className="create-status">Loading...</div>
  if (!isLoggedIn) return null

  return (
    <div className="create-post">
      <h1 className="create-title">New Post</h1>
      <form className="create-form" onSubmit={handleSubmit}>
        {error && <div className="create-error">{error}</div>}
        <div className="form-group">
          <label htmlFor="post-title" className="form-label">Title</label>
          <input
            id="post-title"
            type="text"
            className="form-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="What are you sharing?"
            maxLength={300}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="post-body" className="form-label">Body (Markdown)</label>
          <textarea
            id="post-body"
            className="form-textarea"
            value={body}
            onChange={(e) => setBody(e.target.value)}
            placeholder="Write your post in Markdown..."
            rows={12}
            required
          />
        </div>
        <div className="create-actions">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate('/')}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Publishing...' : 'Publish Post'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default CreatePostPage
