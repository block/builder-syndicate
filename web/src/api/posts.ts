import { PostListResponse, PostResponse } from '../types/api'

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
