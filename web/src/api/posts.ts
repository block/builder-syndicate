import { PostListResponse, PostResponse, CreatePostRequest } from '../types/api'

export async function fetchPosts(): Promise<PostListResponse> {
  const res = await fetch('/api/v1/posts')
  if (!res.ok) throw new Error('Failed to fetch posts')
  const data = await res.json()
  return { posts: data.posts ?? [] }
}

export async function fetchPost(id: string): Promise<PostResponse> {
  const res = await fetch(`/api/v1/posts/${id}`)
  if (!res.ok) throw new Error('Failed to fetch post')
  return res.json()
}

export async function createPost(data: CreatePostRequest): Promise<PostResponse> {
  const res = await fetch('/api/v1/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Failed to create post')
  return res.json()
}
